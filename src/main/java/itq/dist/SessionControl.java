package itq.dist;

public class SessionControl
{
    private int availableCount;
    private boolean[] avalilableSessionIDs;
    private int startId; // First id, session ids will be in closed range [startId, startID +
                         // MAX_SESSIONS - 1]
    private int lastId;
    private int maxSessions;
    private int lastAssignedIndex;

    private TimerThread sessionTimer;
    // private TimerThread[] timerList = new TimerThread[2];
    // timerList[0] = operationType 0 - timer for reserved tickets
    // timerList[1] = operationType 1 - timer for session control

    /**
     * 
     * @param availableCount
     * @param avalilableSessionIDs
     * @param startId
     * @param lastId
     * @param maxSessions
     * @param lastAssignedIndex
     */

    SessionControl(int startId, int maxSessions)
    {
        this.startId = startId;
        this.maxSessions = maxSessions;
        lastId = startId + maxSessions - 1;
        avalilableSessionIDs = new boolean[maxSessions];
        availableCount = maxSessions;
        lastAssignedIndex = 0;
        for (int i = 0; i < avalilableSessionIDs.length; i++)
        {
            avalilableSessionIDs[i] = true;
        }
    }

    SessionControl()
    {
        this(0, 1024);
    }

    public synchronized int getNewSessionId()
    {
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
                    return startId + i;
                }
            }
            for (int i = 0; i < lastAssignedIndex; i++)
            {
                if (avalilableSessionIDs[i])
                {
                    avalilableSessionIDs[i] = false;
                    availableCount--;
                    lastAssignedIndex = i;
                    return startId + i;
                }
            }
        }
        return -1;
    }

    public synchronized void releaseSessionId(int sessionId) throws SessionException
    {
        if (!isValid(sessionId))
            throw new SessionException();
        avalilableSessionIDs[sessionId] = true;
        availableCount++;
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

    public void sessionTimer(int sessionId)
    {
        if (!sessionTimer.isAlive())
        {
            sessionTimer = new TimerThread(10000, sessionId, 1);
            sessionTimer.start();
            sessionTimer.run();
        }
        else
        {
            // ToDo: Exception? for duplicated sessionId?
        }
    }

    public void updateSessionTimer()
    {
        if (sessionTimer.isAlive())
        {
            sessionTimer.setUpdate(true);
        }
        else
        {
            // ToDo: Exception? for updated out of time?
        }
    }
}
