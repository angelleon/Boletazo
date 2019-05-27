package itq.dist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SessionTimer extends TimerThread
{
    private static final Logger LOG = LogManager.getLogger(SessionTimer.class);

    private int sessionId;
    private SessionControl sessionControl;

    SessionTimer(int timeout, int sessionId, SessionControl sessionControl)
    {
        super(timeout);
        this.sessionId = sessionId;
        this.sessionControl = sessionControl;
    }

    @Override
    public void run()
    {
        try
        {
            super.run();
            sessionControl.releaseSessionId(sessionId);
            LOG.debug("Session timer finished [" + sessionId + "]");
        }
        catch (SessionException e)
        {
            LOG.error(e.getMessage());
        }
    }
}
