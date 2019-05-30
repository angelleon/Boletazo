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
    private static final int PORT = BoletazoConf.PORT;
    private static final String PROF_HOST = BoletazoConf.PROF_HOST;
    private static final int PROF_PORT = BoletazoConf.PROF_PORT;
    private static final int TEAM_NUM = BoletazoConf.TEAM_NUM;
    private static final int ANONYMOUS_SESSION_TIMEOUT = BoletazoConf.ANONYMOUS_SESSION_TIMEOUT;
    private static final int SESSION_TIMEOUT = BoletazoConf.SESSION_TIMEOUT;

    public static void main(String[] args) throws SQLException
    {
        /*
         * Db db = new Db(); if (db.getConnected()) { db.preLoad(); //
         * initialConnection();
         * 
         * } else { LOG.error("Can not connect to DataBase"); } LOG.info("Exiting...");
         */

        LOG.info("Boletazo server started at " + LocalDateTime.now().toString());
        Flag alive = new Flag(true);
        ServerSocket serverSocket;
        SessionControl anonymousSc = new SessionControl(0, 10, ANONYMOUS_SESSION_TIMEOUT);
        SessionControl sessionControl = new SessionControl(10, 10, SESSION_TIMEOUT);
        Db db = new Db();
        if (db.getConnected())
        {
            db.preLoad();
            // initialConnection();
            try
            {
                serverSocket = new ServerSocket(PORT);
                // Report rep = new Report(alive, db);
                // rep.start();
                while (alive.isSet())
                {
                    Socket socket = serverSocket.accept();
                    BoletazoThread thread = new BoletazoThread(socket, db, sessionControl, anonymousSc);
                    thread.start();
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
