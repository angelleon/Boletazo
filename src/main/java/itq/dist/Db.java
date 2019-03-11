package itq.dist;

import java.sql.*;
//import java.sql.Date;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Db
{
    private static final Logger log = LogManager.getLogger(Db.class);

    // Informacion necesaria para conectarse a mysql
    private static final String USR = "boletazodev";
    private static final String PASSWD = "contrapass";
    private static final String URL = "jdbc:mysql://localhost:3306/Boletazo";

    // Lista de querys
    private static final String SELECT_EVENT_BETWEEN_DATES = "SELECT * FROM Events WHERE date BETWEEN ? AND DATE_ADD(?, INTERVAL 1 DAY)";

    private static char mander = 'c';

    private Connection conn;

    Db()
    {
        try
        {
            conn = DriverManager.getConnection(URL, USR, PASSWD);
        }
        catch (SQLException e)
        {
            log.error("An error occurred when trying to connect to DB");
            log.error(e.getMessage());
        }
    }

    /**
     * 
     * @param date:
     * @return An array with all events that ocurr at certain date
     */
    public Event[] getEventsAt(java.util.Date date)
    {
        Event[] events = null;
        int nEvents = 0;
        ResultSet result = null;

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR, 0);

        long t0 = cal.getTimeInMillis();

        cal.add(Calendar.HOUR, 23);
        cal.add(Calendar.MINUTE, 59);
        cal.add(Calendar.SECOND, 59);
        cal.add(Calendar.MILLISECOND, 999);

        long t1 = cal.getTimeInMillis();

        try
        {
            PreparedStatement ps = conn.prepareStatement(SELECT_EVENT_BETWEEN_DATES);
            java.sql.Date d = new java.sql.Date(t0);
            ps.setDate(1, d);
            ps.setDate(2, d);
            // ps.setDate(2, new java.sql.Date(t1));
            result = ps.executeQuery();

            if (result.last())
            {
                nEvents = result.getRow();
            }
        }
        catch (SQLException e)
        {
        }
        events = new Event[nEvents];
        for (int i = 0; i < nEvents; i++)
        {
            events[i] = new Event();
        }
        return events;
    }
}