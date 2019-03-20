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
            // Algoritmo que asigna los ids de forma circular
            for (int i = (lastAssignedIndex == maxSessions - 1 ? 0
                    : lastAssignedIndex); i < avalilableSessionIDs.length; i++)
            {
                if (avalilableSessionIDs[i])
                {
                    avalilableSessionIDs[i] = false;
                    availableCount--;
                    lastAssignedIndex = i;
                    return startId + i;
                }
            }
        return -1;
    }

    public synchronized void releaseSessionId(int sessionId) throws SessionException
    {
        if (isValid(sessionId) || !isActive(sessionId))
            throw new SessionException();
        avalilableSessionIDs[sessionId] = true;
        availableCount++;
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
        return !avalilableSessionIDs[sessionId];
    }
}
