package itq.dist;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//separate class from the proyect for tests 
public class Test
{
    private static final Logger log = LogManager.getLogger(Test.class);

    public static void main(String[] args) throws UnknownHostException
    {
        Random r = new Random();
        System.out.println(InetAddress.getLocalHost().getHostAddress().toString());

        SessionControl sc = new SessionControl(0, 200);
        int[] sessionId = new int[200];
        int releasedCount = 0;
        int obtainedConunt = 0;
        for (int i = 0; i < 100; i++)
        {
            sessionId[i] = sc.getNewSessionId();
            log.info("sessionId: [" + sessionId[i] + "]");
            // sc.sessionTimer(sessionId[i]);
            SocketTest myTestThread = new SocketTest(sessionId[i], sc);
            myTestThread.start();
            try
            {
                if (i == 99)
                {
                    // sc.releaseSessionId(sessionId[i]);
                    i = 0;
                    Thread.sleep(1000);
                }
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        log.info("Obtained [" + obtainedConunt + "] sessionIds");
        log.info("Released [" + releasedCount + "] sessionIds");
    }
}
