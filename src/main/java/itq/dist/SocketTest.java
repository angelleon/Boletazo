package itq.dist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SocketTest extends Thread
{

    private static final Logger log = LogManager.getLogger(Test.class);
    int sessionId;
    SessionControl sc;

    public SocketTest(int sessionId, SessionControl sc)
    {
        this.sessionId = sessionId;
        this.sc = sc;
    }

    @Override
    public void run()
    {
        // sc.sessionTimer(sessionId);
        connectionTimed();
    }

    public void connectionTimed()
    {
        if (sessionId > 0)
        {
            try
            {
                Thread.sleep(5000);
                sc.resetSessionTimer(sessionId);
                log.info("Out of Time, expired sessionId, GETTING NEWS ID�S");
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
