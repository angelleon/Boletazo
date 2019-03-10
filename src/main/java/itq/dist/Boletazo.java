package itq.dist;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Boletazo
{
    private static final Logger log = LogManager.getLogger();
    private static final int PORT = 2000;
    private static final String PROF_HOST = "localhost";
    private static final int PROF_PORT = 5000;
    private static final int TEAM_NUM = 3;

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
            DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
            dataOut.writeUTF("" + TEAM_NUM + "," + getIP());
        }
        catch (IOException e)
        {
            log.error("An I/O error ocurred");
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * 
     * @return ethernet's assigned ip
     */
    private static String getIP()
    {
        // ToDo: modificar para obtener la ip en windows
        String interfaceName = "eno1";
        try
        {
            NetworkInterface netInt = NetworkInterface.getByName(interfaceName);
            Enumeration<InetAddress> addresses = netInt.getInetAddresses();
            InetAddress address;
            while (addresses.hasMoreElements())
            {
                address = addresses.nextElement();
                if (address instanceof Inet4Address
                        && !address.isLoopbackAddress()) { return address.getHostAddress(); }
            }
        }
        catch (SocketException e)
        {
        }
        return "127.0.0.1";
    }
}