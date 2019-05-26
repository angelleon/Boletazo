package itq.dist.ftp;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import itq.dist.Report;

public class ClientFTP
{
    private static final Logger LOG = LogManager.getLogger(ClientFTP.class);

    public static void main(String[] args) 
    {

        FTPClient client = new FTPClient();

        // info to conect service FTP
        String ftp = "192.168.1.107";
        String user = "userboletazo";
        String password = "1614";

        try
        {
            // connecting to service
            client.connect(InetAddress.getByName("192.168.1.107"), 21);
            LOG.info("start conexion with FTP ip:" + ftp);
            // Logueado un usuario (true = pudo conectarse, false = no pudo
            // conectarse)
            boolean estado = client.login(user, password);
            if (estado)
            {
                if (FTPReply.isPositiveCompletion(client.getReplyCode()))
                {
                    Report newReport = new Report();
                    //newReport.reportDay();
                    File file = new File(newReport.reportDay());                                // direc del documento en el dispositi
                                                                                                // local que se desea subir al ftp
                    FileInputStream input = new FileInputStream(file);
                    client.setFileType(FTP.BINARY_FILE_TYPE);
                    client.enterLocalActiveMode();
                  
                    LOG.info("Succesfull upload ");
                    if (!client.storeFile(file.getName(), input))
                    {
                        
                        LOG.info("Failed upload!");
                    }
                    input.close();
                }
                else
                {
                    
                    LOG.error("Error,you can't connect whit this session or user");
                }

                // Close sesion
                client.logout();

                // Disconect 
                client.disconnect();
            }
            else
            {
                LOG.error("Error, you can't connect whit this session");
            }
        }
        catch (IOException ioe)
        {
            LOG.error(ioe.getMessage());
            
            LOG.error("Error conexion con el server,cant upload the file");
        }catch(SQLException e) {
            LOG.error("Error on BD ,you can't finish report");
            e.printStackTrace();
        }
    }
}