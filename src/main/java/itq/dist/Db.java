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
    private static final String USR = "boletazodev";
    private static final String PASSWD = "contrapass";
    private static final String URL = "jdbc:mysql://localhost:3306/Boletazo";
    private static final String SELECT_EVENT_BETWEEN_DATES = "SELECT * FROM Events WHERE date BETWEEN ? AND ?";
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
            ps.setDate(1, new java.sql.Date(t0));
            ps.setDate(2, new java.sql.Date(t1));
            result = ps.executeQuery();
        }
        catch (SQLException e)
        {
        }
        
        if (result != null)
        {
            result.
        }

        return events;
    }
}