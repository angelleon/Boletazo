package itq.dist;

import java.net.ServerSocket;
import Java.net.Socket;
import java.lang.DataOutputStream;
import Java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Boletazo
{
	private static final Logger log = LogManager.getLogger();
	private static final String PROF_HOST = "localhost";
	private static final int PROF_PORT = 5000;
	
	public static void main(String[] args)
	{
		boolean alive = false;
		ServerSocket serverSocket;
		ArrayList<SocketThread> threads = new ArrayList<SocketThread>();
		try
		{
			while (alive)
			{
				serverSocket = new ServerSocket(PORT);
				Socket socket = serverSocket.accept();
				SocketThread t = new SocketThread(socket);
				t.start();
				threads.add(t);
			}
			serverSocket.close();
		}
		catch (IOException e)
		{
			log.error("An I/O error occurred");
			log.error(e.getMessage());
		}
	}
	
	private static boolean initialConnection()
	{
		try
		{
			Socket socket = new Socket(PROF_HOST, PROF_PORT);
			DataOutpuStream dataOut = new DataOutputStream(socket.getOutputStream());
			dataOut.writeUTF("" + teamNum + "," + getIP());
		}
		catch (IOException e)
		{
			log.error("An I/O error ocurred");
			log.error(e.getMessage());
		}
		return false;
	}
	
	private String getIP()
	{
		return "127.0.0.1";
	}
}