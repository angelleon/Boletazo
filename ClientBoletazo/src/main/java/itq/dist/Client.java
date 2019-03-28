package itq.dist;

import java.net.UnknownHostException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * this client send two request, on the "happy path", 
 * for modify or test other things, rewrite -ThreadClient-
 */
public class Client {	
	
	private static final Logger LOG = LogManager.getLogger(Client.class);	
	static final String HOST = "localhost";
	static final int PORT =2000;
	
	public static void main(String[] args) {	

        //client type 1 (without sigup)
        Thread t_client = new ThreadClient(1);
        t_client.start();
        //client type 2 ...with sigup!
        Thread t_client2 = new ThreadClient(2);
        t_client2.start();
	   
	}
}




