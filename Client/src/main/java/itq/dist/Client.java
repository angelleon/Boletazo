package itq.dist;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Client extends Thread{	
	
	private static final Logger logger = LogManager.getLogger(Client.class);
	
	static final String HOST = "localhost";
	
	static final int PORT =2000;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//numero de peticiones al banco 
		for(int i = 0 ;i<10;i++) {
			MyThread t_client = new MyThread(HOST, PORT, i);
			t_client.start();
		}
		
	}
}




