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
        for (int i = 0; i < avalilableSessionIDs.length; i++)
        {
            avalilableSessionIDs[i] = true;
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
        if (!isValid(sessionId) || !isActive(sessionId))
            throw new SessionException();
        int index = sessionIdToIndex(sessionId);
        avalilableSessionIDs[index] = true;
        sessionTimers[index] = null;
        availableCount++;
        LOG.info("Released sessionId [" + sessionId + "]");
    }

    public synchronized boolean isValid(int sessionId)
    {
        return isInRange(sessionId) && isActive(sessionId);
    }

    /**
     * 
     * @param sessionId
     * @return true if session is active (sessionIs is occuped), false otherwise
     */
    private boolean isValid(int sessionId)
    {
        return sessionId >= startId && sessionId <= lastId;
    }

    // ToDo: decidir si esto es synchronized o no
    private boolean isActive(int sessionId)
    {
        int index = sessionIdToIndex(sessionId);
        LOG.debug("available sessionId [" + sessionId + "]: [l" + avalilableSessionIDs[index] + "]\nalive timer ["
                + sessionTimers[index].isAlive() + "]");
        return !avalilableSessionIDs[index] && sessionTimers[index].isAlive();
    }

    public int getMaxSessions()
    {
        return maxSessions;
    }

    public void updateSessionTimer(int sessionId) // throws NullPointerException
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
        return sessionId - startId;
    }
}
