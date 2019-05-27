package itq.dist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SessionTimer extends TimerThread
{
    private static final Logger LOG = LogManager.getLogger(SessionTimer.class);

    private int sessionId;
    private SessionControl sessionControl;
    private Flag stopFlag;

    SessionTimer(int timeout, int sessionId, SessionControl sessionControl)
    {
        super(timeout);
        this.sessionId = sessionId;
        this.sessionControl = sessionControl;
        stopFlag = new Flag(false);

    }

    @Override
    public void run()
    {
        try
        {
            while (alive.isSet())
            {
                Thread.sleep(updateTime);
                addElapsed(updateTime);
                alive.setState(elapsedTime >= timeout);
            }
            sessionControl.releaseSessionId(sessionId);
            LOG.debug("Session timer finished [" + sessionId + "]");
        }
        catch (InterruptedException e)
        {
            LOG.error(e.getMessage());
        }
        catch (SessionException e)
        {
            LOG.error(e.getMessage());
        }
    }

    public void stopTimer()
    {
        stopFlag.unset();
    }
}
