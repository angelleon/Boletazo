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
<<<<<<< HEAD
        sc.sessionTimer(sessionId);
=======
        // sc.sessionTimer(sessionId);
>>>>>>> 1746fadcdc3757da043af9c7055c64c9d64a2a31
        connectionTimed();
    }

    public void connectionTimed()
    {
        if (sessionId > 0)
        {
            try
            {
                Thread.sleep(5000);
<<<<<<< HEAD
                sc.updateSessionTimer(sessionId);
                log.info("Out of Time, expired sessionId, GETTING NEWS ID´S");
=======
                sc.resetSessionTimer(sessionId);
                log.info("Out of Time, expired sessionId, GETTING NEWS IDï¿½S");
>>>>>>> 1746fadcdc3757da043af9c7055c64c9d64a2a31
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
