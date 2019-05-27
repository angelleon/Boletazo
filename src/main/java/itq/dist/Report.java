package itq.dist;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.text.SimpleDateFormat;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Report extends TimerThread
{
    private Connection conn;
    private static final Logger LOG = LogManager.getLogger(Report.class);
    private static final String RUTE_REPORT = "/opt/Boletazo/Reports/";
    private static final String SOLD_TODAY = "SELECT E.name event, V.name place, "
            + "SE.cost, Ticket.idStatus, ST.idCard, ST.idUser "
            + "FROM Ticket T, Event E, Venue V, Section SE, Sold_Tickets ST"
            + "WHERE ticket.idstatus > 1 "
            + "AND E.idEvent = Ticket.idEvent "
            + "AND E.idVenue = V.idVenue "
            + "AND T.idSection = SE.idSection "
            + "AND T.idTicket = ST.idTicket "
            + "AND DATE_FORMAT(ST.dateSale, '%H:%i:%s') > '00:00:00' "
            + "AND DATE_FORMAT(ST.datesale, '%H:%i:%s') < '23:59:59' "
            + "AND DATE_FORMAT(ST.datesale, '%d-%m-%Y') = DATE_FORMAT(CURDATE()-1,'%d-%m-%Y') "
            + "ORDER BY SE.cost, ST.idCard ";

    private static FileWriter newfile;
    private static BufferedWriter bw;
    private static Date date;
    private static SimpleDateFormat dateMask;

    private static final int MSECONDS_IN_A_DAY = 60 * 60 * 24 * 1000;

    private Flag alive;
    private int initialSleep;
    private Db db;

    public Report(Flag alive, Db db) throws IOException
    {
        super();

        this.alive = alive;
        this.db = db;

        date = new Date();
        dateMask = new SimpleDateFormat("dd-MM-yyyy");
        LOG.debug(RUTE_REPORT + dateMask.format(date) + ".txt");
        File file = new File(RUTE_REPORT + dateMask.format(date) + ".txt");
        file.createNewFile();
        newfile = new FileWriter(file);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(),
                BoletazoConstants.REPORT_HOUR, BoletazoConstants.REPORT_MINUTE);
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
        timeout = initialSleep;
        updateTime = 5000;
        try
        {
            super.run();
            reportDay();
            timeout = MSECONDS_IN_A_DAY;
            while (alive.isSet())
            {
                super.run();
                reportDay();
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
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public String reportDay() throws SQLException, IOException
    {
        bw = new BufferedWriter(newfile);
        bw.newLine();
        bw.append("event          |\t venue      |\t cost     |\t statusticket      |\t card number      |");
        bw.newLine();
        bw.append("*******************************************************************************");

        PreparedStatement ps = conn.prepareStatement(SOLD_TODAY);
        ResultSet resp = ps.executeQuery();
        while (resp.next())
        {
            String eventName = resp.getString("event");
            String site = resp.getString("place");
            double cost = resp.getDouble("cost");
            int status = resp.getInt("idStatus");
            int card = resp.getInt("card");
            bw.newLine();
            bw.append(eventName + "|\t" + site + "|\t" + cost + "|\t" + status + "|\t" + card);
        }
        bw.newLine();

        bw.append("*******************************************************************************");
        LOG.debug("Check report on : " + RUTE_REPORT + dateMask.format(date) + ".txt");
        bw.close();
        return RUTE_REPORT + dateMask.format(date) + ".txt";
    }
}
