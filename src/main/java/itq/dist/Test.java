package itq.dist;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//separate class from the project for tests 
public class Test
{
    private static final Logger log = LogManager.getLogger(Test.class);

    public static void main(String[] args) throws UnknownHostException
    {
        Random r = new Random();
        System.out.println(InetAddress.getLocalHost().getHostAddress().toString());
        SessionControl sc = new SessionControl(0, 40);
        int sessionId;
        int releasedCount = 0;
        int obtainedConunt = 0;
        for (int i = 0; i < 100; i++)
        {
            sessionId = sc.getNewSessionId();
            log.info("sessionId: [" + sessionId + "]");
            if (sessionId > 0)
            {
                obtainedConunt++;
                if (r.nextBoolean())
                {
                    try
                    {
                        sc.releaseSessionId(sessionId);
                        log.info("Released sessionId: [" + sessionId + "]");
                        releasedCount++;
                    }
                    catch (SessionException e)
                    {
                        log.error(e.getMessage());
                    }
                }
            }
        }
        log.info("Obtained [" + obtainedConunt + "] sessionIds");
        log.info("Released [" + releasedCount + "] sessionIds");
    }
}
