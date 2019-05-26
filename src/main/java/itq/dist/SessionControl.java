package itq.dist;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SessionControl
{
    private static final Logger LOG = LogManager.getLogger(SessionControl.class);

    private int availableCount;
    private boolean[] avalilableSessionIDs;
    private int startId; // First id, session ids will be in closed set [startId, startID +
                         // maxSessions - 1]
    private int sessionTimeout;

    private int lastId;
    private int maxSessions;
    private int lastAssignedIndex;

    private Ticket[][] reservedTickets;
    private String[] email;
    private String[] user;

    private SessionTimer[] sessionTimers;

    /**
     * 
     * @param availableCount
     * @param avalilableSessionIDs
     * @param startId
     * @param lastId
     * @param maxSessions
     * @param lastAssignedIndex
     */

    SessionControl(int startId, int maxSessions, int sessionTimeout)
    {
        this.startId = startId;
        this.maxSessions = maxSessions;
        lastId = startId + maxSessions - 1;
        avalilableSessionIDs = new boolean[maxSessions];
        sessionTimers = new SessionTimer[maxSessions];
        availableCount = maxSessions;
        lastAssignedIndex = 0;
        reservedTickets = new Ticket[maxSessions][];
        email = new String[maxSessions];
        user = new String[maxSessions];
        for (int i = 0; i < avalilableSessionIDs.length; i++)
        {
            avalilableSessionIDs[i] = true;
            reservedTickets[i] = new Ticket[SocketThread.PERMITED_TICKETS];
        }

    }

    SessionControl()
    {
        this(0, 1024, 3000);
    }

    public synchronized int getNewSessionId()
    {
        int sessionId = -1;
        if (availableCount > 0)
        {
            // Algoritmo que asigna los ids de forma circular
            for (int i = lastAssignedIndex + 1; i <= lastId; i++)
            {
                if (avalilableSessionIDs[i])
                {
                    avalilableSessionIDs[i] = false;
                    availableCount--;
                    lastAssignedIndex = i;
                    sessionId = startId + i;
                    SessionTimer timer = new SessionTimer(sessionTimeout, sessionId, this);
                    sessionTimers[i] = timer;
                    timer.start();
                    LOG.info("Retrived new sessionId [" + sessionId + "]");
                    return sessionId;
                }
            }
            for (int i = 0; i < lastAssignedIndex; i++)
            {
                if (avalilableSessionIDs[i])
                {
                    avalilableSessionIDs[i] = false;
                    availableCount--;
                    lastAssignedIndex = i;
                    sessionId = startId + i;
                    SessionTimer timer = new SessionTimer(sessionTimeout, sessionId, this);
                    sessionTimers[i] = timer;
                    timer.start();
                    LOG.info("Retrived new sessionId [" + sessionId + "]");
                    return sessionId;
                }
            }
        }
        return sessionId;
    }

    public synchronized void releaseSessionId(int sessionId) throws SessionException
    {
        if (!isValid(sessionId))
            throw new SessionException();
        int index = sessionIdToIndex(sessionId);
        avalilableSessionIDs[index] = true;
        sessionTimers[index] = null;
        availableCount++;
        LOG.info("Released sessionId [" + sessionId + "]");
    }

    public synchronized boolean isValid(int sessionId)
    {
        LOG.debug(isInRange(sessionId));
        boolean b = isInRange(sessionId);
        if (!b)
            return false;
        return isActive(sessionId);
    }

    /**
     * Check if the session ID is in the range.
     * 
     * @param sessionId
     * @return true if session on the range(sessionIs is occuped), false otherwise
     */

    private boolean isInRange(int sessionId)
    {
        return sessionId >= startId && sessionId <= lastId;
    }

    // ToDo: decidir si esto es synchronized o no
    private synchronized boolean isActive(int sessionId)
    {
        int index = sessionIdToIndex(sessionId);
        LOG.debug("index:" + index);
        LOG.debug("timer: " + sessionTimers[index]);
        LOG.debug("available sessionId [" + sessionId + "]: [" + avalilableSessionIDs[index] + "]");
        if (sessionTimers[index] != null)
            LOG.debug("alive timer [" + sessionTimers[index].isAlive() + "]");
        boolean b = !avalilableSessionIDs[index];
        if (!b)
            return false;
        return sessionTimers[index].isAlive();
    }

    public int getMaxSessions()
    {
        return maxSessions;
    }

    public synchronized void resetSessionTimer(int sessionId) // throws NullPointerException
    {
        sessionTimers[sessionIdToIndex(sessionId)].reset();
    }

    /**
     * Convert from sessionId to respective index inside availableSessionIds and
     * sessionTimers arrays
     * 
     * @param sessionId
     * @return index of sessionId and sessionTimer inside arrays
     */
    private int sessionIdToIndex(int sessionId)
    {
        LOG.debug("sessionId [" + sessionId + "]");
        LOG.debug("startID [ " + startId + "]");
        return sessionId - startId;
    }

    public synchronized void setReservedTickets(Ticket[] t, int sessionId)
    {
        LOG.debug("Setting reserved tickets for session: [" + sessionId + "] [" + t.length + "] tickets");
        reservedTickets[sessionIdToIndex(sessionId)] = t;
    }

    public synchronized Ticket[] getReservedTickets(int sessionId)
    {
        return reservedTickets[sessionIdToIndex(sessionId)];
    }

    public synchronized void setEmail(String email, int sessionId)
    {
        this.email[sessionIdToIndex(sessionId)] = email;
    }

    public synchronized String getEmail(int sessionId)
    {
        return email[sessionIdToIndex(sessionId)];
    }

    public synchronized void setUser(String user, int sessionId)
    {
        this.user[sessionIdToIndex(sessionId)] = user;
    }

    public synchronized String getUser(int sessionId)
    {
        return user[sessionIdToIndex(sessionId)];
    }
}
