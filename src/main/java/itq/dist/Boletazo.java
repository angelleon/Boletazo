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
import java.sql.SQLException;
import java.util.Enumeration;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Boletazo
{
    private static final Logger LOG = LogManager.getLogger();
    private static final int PORT = 2000;
    private static final String PROF_HOST = "localhost";
    private static final int PROF_PORT = 5000;
    private static final int TEAM_NUM = 3;

    public static void main(String[] args) throws SQLException
    {
        LOG.info("Boletazo server started at " + LocalDateTime.now().toString());
        boolean alive = true;
        ServerSocket serverSocket;
        SessionControl sc = new SessionControl(0, 10);
        Db db = new Db();
        if (db.getConnected())
        {
            db.preLoad();
            initialConnection();
            try
            {
                serverSocket = new ServerSocket(PORT);
                while (alive)
                {
                    Socket socket = serverSocket.accept();
                    SocketThread thread = new SocketThread(socket, db, sc);
                    thread.start();
                    //I'm not sure if this goes here...
                    Report rep = new Report();
                    rep.sendReport();
                }
                serverSocket.close();
            }
            catch (IOException e)
            {
                LOG.error("An I/O error occurred");
                LOG.error(e.getMessage());
            }
        }
        else
        {
            LOG.error("Can not connect to DataBase");
        }
        LOG.info("Exiting...");
    }
    /**
     * Connects to professor server to send IP and port of this service
     * 
     * @return true if successful connection, else otherwise
     */
    private static boolean initialConnection()
    {
        try
        {
            Socket socketProf = new Socket(PROF_HOST, PROF_PORT);
            DataOutputStream dataOut = new DataOutputStream(socketProf.getOutputStream());
            dataOut.writeUTF(" " + TEAM_NUM + "," + getIP());
            socketProf.close();
            return true;
        }
        catch (UnknownHostException e)
        {
            LOG.error(e.getMessage());
        }
        catch (IOException e)
        {
            LOG.error("An I/O error ocurred");
            LOG.error(e.getMessage());
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
        // TODO: modificar para obtener la ip en windows
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
                    if (address instanceof Inet4Address && !address.isLoopbackAddress())
                    {
                        LOG.info(
                                "ip assigned by InetAddres: " + InetAddress.getLocalHost().getHostAddress().toString());
                        return address.getHostAddress();
                    }
                }
            }
            catch (SocketException e)
            {
                LOG.error("Can't get ip for Linux (1)");
            }
            return "127.0.0.1";
        }
        // For windows...
        LOG.info("ip assigned by InetAddres: " + InetAddress.getLocalHost().getHostAddress().toString());
        return InetAddress.getLocalHost().getHostAddress().toString();
        // if you have active other ethernet interface .... unable!
    }
}