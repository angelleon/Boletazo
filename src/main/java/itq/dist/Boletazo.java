package itq.dist;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

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
        boolean alive = true;
        ServerSocket serverSocket;
        SessionControl sc = new SessionControl(0, 10);
        // ArrayList<SocketThread> threads = new ArrayList<SocketThread>();
        Db db = new Db();
        if (db.getConnected())
        {
            db.preload();
            // initialConnection();
            try
            {
                serverSocket = new ServerSocket(PORT);
                while (alive)
                {
                    Socket socket = serverSocket.accept();
                    SocketThread t = new SocketThread(socket, db, sc);
                    t.start();
                    // threads.add(t);
                }
                serverSocket.close();
            }
            catch (IOException e)
            {
                log.error("An I/O error occurred");
                log.error(e.getMessage());
            }
        }
        else
        {
            log.error("Can not connect to DataBase");
        }
        log.info("Exiting...");
    }

    /**
     * Connects to professor server to send IP and port of this service
     * 
     * @return true if succesful connection, else otherwise
     */
    private static boolean initialConnection()
    {
        try
        {
            Socket socketProf = new Socket(PROF_HOST, PROF_PORT);
            DataOutputStream dataOut = new DataOutputStream(
                    socketProf.getOutputStream());
            dataOut.writeUTF("" + TEAM_NUM + "," + getIP());
            socketProf.close();
            return true;
        }
        catch (UnknownHostException e)
        {
            log.error(e.getMessage());
        }
        catch (IOException e)
        {
            log.error("An I/O error ocurred");
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * Get the ip from the assigned host
     * 
     * @return ethernet's assigned ip
     */
    private static String getIP() throws UnknownHostException
    {
        // ToDo: modificar para obtener la ip en windows
        String system = System.getProperty("os.name");
        if (system.equals("Linux"))
        {
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
                            && !address.isLoopbackAddress()) { return address
                                    .getHostAddress(); }
                }
            }
            catch (SocketException e)
            {
            }
            return "127.0.0.1";
        }
        // For windows...
        return InetAddress.getLocalHost().getHostAddress().toString();
        // if you have active other ethernet interface .... unable!
    }
}