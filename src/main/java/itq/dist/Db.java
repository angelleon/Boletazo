package itq.dist;

import java.sql.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Db
{
    private static final Logger log = LogManager.getLogger(Db.class);
    private static final String USR = "boletazodev";
    private static final String PASSWD = "contrapass";
    private static final String URL = "jdbc:mysql://localhost:3306/Boletazo";

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
    public Event[] getEventsAt(Date date)
    {

    }
}