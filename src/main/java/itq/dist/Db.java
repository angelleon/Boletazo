package itq.dist;

//import java.sql.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.sql.SQLException;
import java.lang.ClassNotFoundException;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Db
{
    private static final Logger log = LogManager.getLogger(Db.class);

    // Informacion necesaria para conectarse a mysql
    private static final String USR = "boletazodev";
    private static final String PASSWD = "contrapass";
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/Boletazo";

    // Lista de querys
    private static final String SELECT_EVENT_AT_DATE = "SELECT * FROM Event WHERE date BETWEEN ? AND DATE_ADD(?, INTERVAL 1 DAY)";

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
     * @return An array with all events that ocurr at certain date (day)
     */
    public Event[] getEventsAt(LocalDate date)
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

        try
        {
            PreparedStatement ps = conn.prepareStatement(SELECT_EVENT_AT_DATE);
            Date searchDate = Date.valueOf(date);
            ps.setDate(1, searchDate);
            ps.setDate(2, searchDate);
            result = ps.executeQuery();

            // getting number of selected rows
            nEvents = result.last() ? result.getRow() : 0;
            int i = 0;
            events = new Event[nEvents];
            result.beforeFirst();

            Event ev;
            int idEvent = 0;
            String name = "";
            String description = "";
            LocalDate evDate = LocalDate.now();
            int idVenue = 0;

            // Populating array
            while (result.next())
            {
                idEvent = result.getInt("idEvent");
                name = result.getString("name");
                 description = result.getString("description");
                evDate = result.getDate("date").toLocalDate();
                idVenue = result.getInt("idVenue");
                ev = new Event(idEvent, name, description, evDate, idVenue);
                events[i] = ev;
                i++;
            }

        }
        catch (SQLException e)
        {
            log.error(e.getMessage());
        }
        log.debug("Retrived [" + nEvents + "] events");
        return events;
    }
    /**
     *Consulta los eventos por hora  
     *
     *
     */
    
    
}