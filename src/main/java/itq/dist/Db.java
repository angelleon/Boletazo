package itq.dist;

import java.sql.*;
//import java.sql.Date;
import java.util.Calendar;
import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.zone.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.cj.result.LocalDateTimeValueFactory;

public class Db
{
    private static final Logger log = LogManager.getLogger(Db.class);

    // Informacion necesaria para conectarse a mysql
    private static final String USR = "boletazodev";
    private static final String PASSWD = "contrapass";
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/Boletazo";

    // Lista de querys
    private static final String SELECT_EVENT_BETWEEN_DATES = "SELECT DATE_ADD(?, INTERVAL 1 YEAR)"; // "SELECT
                                                                                                    // SYSDATE()"; //
                                                                                                    // "SELECT * FROM
                                                                                                    // Event WHERE date
    // BETWEEN ? AND DATE_ADD(?, INTERVAL 1
    // YEAR)";

    private static char mander = 'c';

    private Connection conn;
    private boolean connected;

    Db()
    {
        connected = false;
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(URL, USR, PASSWD);
            connected = true;
        }
        catch (SQLException e)
        {
            log.error("An error occurred when trying to connect to DB");
            log.error(e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            log.error(e.getMessage());
        }
        catch (IllegalAccessException e)
        {
            log.error(e.getMessage());
        }
        catch (InstantiationException e)
        {
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
        if (!connected)
        {
            events = new Event[1];
            events[0] = new Event();
            return events;
        }
        int nEvents = 0;
        ResultSet result = null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        LocalDate ld = LocalDate.parse("2017/01/18", formatter);

        Calendar cal = Calendar.getInstance();
        cal.setTime(java.util.Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR, 0);
        log.info(cal.getTime());

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
            // ps.setDate(2, d);
            // ps.setDate(2, new java.sql.Date(t1));
            result = ps.executeQuery();
            while (result.next())
            {
                log.debug(result.getDate(1));
                nEvents++;
            }

        }
        catch (SQLException e)
        {
            log.error(e.getMessage());
        }
        events = new Event[nEvents];
        log.debug("Retrived [" + nEvents + "] events");
        for (int i = 0; i < nEvents; i++)
        {
            events[i] = new Event();
        }
        return events;
    }
}