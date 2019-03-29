package itq.dist;

import java.util.Dictionary;
import java.util.Enumeration;

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
    private Dictionary<String, TimerThread> dictionaryTimer = new Dictionary<String, TimerThread>() {

        @Override
        public int size()
        {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public TimerThread remove(Object key)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public TimerThread put(String key, TimerThread value)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Enumeration<String> keys()
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isEmpty()
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public TimerThread get(Object key)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Enumeration<TimerThread> elements()
        {
            // TODO Auto-generated method stub
            return null;
        }
    };
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
        if (!isValid(sessionId) || !isActive(sessionId))
            throw new SessionException();
        avalilableSessionIDs[sessionId] = true;
        availableCount++;
        dictionaryTimer.remove(sessionId);
    }

    /**
     * Check if the session ID is in the range.
     * 
     * @param sessionId
     * @return true if session on the range(sessionIs is occuped), false otherwise
     */
    public boolean isValid(int sessionId)
    {
        return sessionId >= startId && sessionId <= lastId;
    }

    // ToDo: decidir si esto es synchronized o no
    public boolean isActive(int sessionId)
    {
        return !avalilableSessionIDs[sessionId];
    }

    public int getMaxSessions()
    {
        return maxSessions;
    }

    public synchronized void sessionTimer(int sessionId)
    {
        sessionTimer = new TimerThread(10000000, sessionId, 1, this); // 10,000 seconds of timer per session, WIP (work
                                                                      // in progress)
        sessionTimer.setDaemon(true);
        dictionaryTimer.put("" + sessionId, sessionTimer);
        sessionTimer.start();
    }

    public void updateSessionTimer(int sessionId) // throws NullPointerException
    {
        // OnProcessToResolve - WIP (Work in progress)
        sessionTimer = dictionaryTimer.get("" + sessionId);
        sessionTimer.setUpdate(true);
    }
}
