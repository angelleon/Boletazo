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
        /*
         * SessionControl sc = new SessionControl(); int[] sessionId = new int[200]; int
         * releasedCount = 0; int obtainedConunt = 0;
         */
        Db data = new Db();//
        data.preLoad();
        int[] tickets = {3, 7, 9, 12};
        data.updateTicketSold(tickets, "1234567890", 1);
    }
}
