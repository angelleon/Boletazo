package itq.dist;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Report
{
    private Connection conn;
    private static final Logger LOG = LogManager.getLogger();
    private static final String RUTE_REPORT = "C:\\opt\\eclipse\\Espacio\\PROYECTO\\Reports";
    private static final String SOLD_TODAY = " select event.name Event,venue.name Place,section.cost,ticket.idstatus "
            + " from ticket,event,venue,section "
            + " where ticket.idstatus > 1 "
            + " and event.idevent = ticket.idevent "
            + " and event.idvenue = venue.idvenue "
            + " and ticket.idsection = section.idsection "
            + " order by cost ";

    private static FileWriter newfile;
    private static BufferedWriter content;
    private static Date date;

    public Report() throws IOException
    {
        date = new Date();
        newfile = new FileWriter(RUTE_REPORT + date);
    }

    public String reportDay() throws SQLException, IOException
    {
        String report = date.toString() + "\n";

        try
        {
            PreparedStatement ps = conn.prepareStatement(SOLD_TODAY);
            ResultSet resp = ps.executeQuery();
            while (resp.next())
            {
                String eventName = resp.getString("Event");
                String site = resp.getString("Place");
                double cost = resp.getDouble("cost");
                int status = resp.getInt("idStatus");
                report = report + "\n";
                report = report + "*********************************************************************";
                report = report + "\n" + "\t" + eventName + "\t" + site + "\t" + cost + "\t" + status;
            }
            content = new BufferedWriter(newfile);
            content.write(report);
            LOG.debug("Writing report: " + report);

        }
        catch (SQLException e)
        {
            LOG.error("Error on BD ,you can't finish report");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            LOG.error("You can�t write report document");
            e.printStackTrace();
        }
        return RUTE_REPORT + date;
    }
}
