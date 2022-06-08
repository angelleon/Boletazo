package itq.dist;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Date;
import java.text.SimpleDateFormat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

public class Report extends TimerThread
{
    private static final Logger LOG = LogManager.getLogger(Report.class);
    private static final String PATH_REPORT_DIR = "/opt/Boletazo/Reports/";

    private static FileWriter newfile;
    private static BufferedWriter bw;
    private static Date date;
    private static SimpleDateFormat dateMask;

    private static final int MSECONDS_IN_A_DAY = 60 * 60 * 24 * 1000;

    private Flag alive;
    private int initialSleep;
    private Db db;

    private static final String ftp = BoletazoConf.FTP;
    private static final String user = BoletazoConf.USR_FTP;
    private static final String password = BoletazoConf.PASSWD_FTP;
    private static final int FTP_PORT = BoletazoConf.FTP_PORT;

    private LocalDateTime now;
    private LocalDateTime next;

    public Report() {
        super();
    }

    public Report(Flag alive, Db db) throws IOException
    {
        super();

        this.alive = alive;
        this.db = db;

        now = LocalDateTime.now();
        next = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(),
                BoletazoConf.REPORT_HOUR, BoletazoConf.REPORT_MINUTE);
        if (now.isAfter(next))
        {
            next = next.plusDays(1);
        }
        initialSleep = 1000 * (int) (next.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC));

        // LOG.debug(alive.equals(super.alive));
        LOG.debug("Server hour: [" + now + "]");
        LOG.debug("Next report hour: [" + next + "]");
        LOG.debug("Waiting [" + initialSleep + "] mseconds until next report");
    }

    @Override
    public void run()
    {
        // timeout = initialSleep;
        timeout = 4000;
        updateTime = 5000;
        try
        {
            super.run();
            reportDay();

            timeout = calcNextSleepInterval();
            while (alive.isSet())
            {
                super.run();
                LOG.debug("exec");
                reportDay();
                timeout = calcNextSleepInterval();
            }
        }
        catch (SQLException ex)
        {
            LOG.error(ex.getMessage());
        }
        catch (IOException ex)
        {
            LOG.error(ex.getMessage());
        }
    }

    /***
     * Generate the report of last day
     * 
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public String reportDay() throws SQLException, IOException
    {
        date = new Date();
        dateMask = new SimpleDateFormat("dd-MM-yyyy");
        String report_path = PATH_REPORT_DIR + dateMask.format(date) + ".txt";
        LOG.debug(report_path);
        File file = new File(report_path);
        file.createNewFile();
        newfile = new FileWriter(file);
        bw = new BufferedWriter(newfile);
        bw.newLine();
        bw.append(
                "|    event          |     venue      |     cost     |     statusticket      |     card number      |");
        bw.newLine();
        bw.append(
                "***************************************************************************************************");

        String reportRow = "";
        try
        {
            for (ReportInfo repInf : db.getReportInfo())
            {
                bw.newLine();
                reportRow = "|    " + repInf.getEventName() + "    |    " + repInf.getSite() + "    |    "
                        + repInf.getCost()
                        + "    |    " + repInf.getStatus() + "    |    " + repInf.getCard();
                LOG.debug("Writing to report \n" + reportRow);
                bw.append(reportRow);
            }
        }
        catch (DbException ex)
        {
            LOG.error(ex.getMessage());
            bw.newLine();
            bw.append("Can not retrive information from DB !!!!!");
        }

        bw.newLine();

        bw.append(
                "***************************************************************************************************");
        LOG.info("Check report on : " + report_path);
        bw.close();
        FTPClient client = new FTPClient();

        // info to conect service FTP

        try
        {
            // connecting to service
            client.connect(InetAddress.getByName(ftp), FTP_PORT);
            LOG.info("start conexion with FTP ip:" + ftp);
            // Logueado un usuario (true = pudo conectarse, false = no pudo
            // conectarse)
            boolean estado = client.login(user, password);
            if (estado)
            {
                if (FTPReply.isPositiveCompletion(client.getReplyCode()))
                {
                    // Report newReport = new Report();
                    // newReport.reportDay();
                    // File file = new File(report_path); // direc del documento en el dispositi
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
        }
        return report_path;
    }

    private int calcNextSleepInterval()
    {
        now = LocalDateTime.now();
        next = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(),
                BoletazoConf.REPORT_HOUR, BoletazoConf.REPORT_MINUTE).plusDays(1);
        return 1000 * (int) (next.toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC));
    }
}
