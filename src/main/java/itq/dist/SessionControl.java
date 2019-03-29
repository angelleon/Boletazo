package itq.dist;

import java.util.Dictionary;
import java.util.Enumeration;

public class SessionControl
{
    private int availableCount;
    private boolean[] avalilableSessionIDs;
    private int startId; // First id, session ids will be in closed set [startId, startID +
                         // MAX_SESSIONS - 1]
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
                    sessionTimers[i] = new SessionTimer(sessionTimeout, sessionId, this);
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
                    break;
                }
            }
        }
        return sessionId;
    }

    public synchronized void releaseSessionId(int sessionId) throws SessionException
    {
        if (!isValid(sessionId))
            throw new SessionException();
        avalilableSessionIDs[sessionId] = true;
        sessionTimers[sessionId] = null;
        availableCount++;
    }

    public synchronized boolean isValid(int sessionId)
    {
        return isInRange(sessionId) && isActive(sessionId);
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
        return !avalilableSessionIDs[sessionId];
    }

    public int getMaxSessions()
    {
        return maxSessions;
    }

    public void updateSessionTimer(int sessionId) // throws NullPointerException
    {
        sessionTimers[sessionIdToIndex(sessionId)].reset();
    }

    private int sessionIdToIndex(int sessionId)
    {
        return sessionId - startId;
    }
}
