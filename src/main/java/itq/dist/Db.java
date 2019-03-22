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
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Db
{
    private static final Logger log = LogManager.getLogger(Db.class);

    // Informacion necesaria para conectarse a mysql
    private static final String USR = "boletazodev";
    private static final String PASSWD = "contrapass";
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/Boletazo?useLegacyDatetimeCode=false&serverTimezone=UTC";

    // Lista de querys

    /**
     * En un PreparedStatement se pueden reemplazar valores en el query usando el
     * metodo setTIPO(int position, TIPO valor) donde position es la posicion del
     * (?) dentro del query iniciando con 1, TIPO depende de lo que se va a
     * reemplazar, y valor es..... supongo que el valor que se va a poner ahi
     */
    private static final String SELECT_EVENT_AT_DATE = "SELECT *"
            + "FROM Event"
            + "WHERE date BETWEEN ?" // aqui se reemplaza el (?) con una fecha usando setDate(1, date)
            + "AND DATE_ADD(?, INTERVAL 1 DAY)"; // lo mismo de arriba setDate(2, date)

    private static final String SELECT_AVAILABLE_TICKETS = "SELECT T.* "
            + "FROM Ticket T, Status S "
            + "WHERE T.idStatus = (SELECT idStatus "
            + "FROM Status "
            + "WHERE status = 'DISPONIBLE'); ";

    private static final String SELECT_EVENT_IN_AVENUE = "SELECT * "
			+ "FROM Venue V "
			+ "WHERE V.name = (SELECT name "
			+ "FROM Venue "
			+ "WHERE name = ?);";    
	
    
    private static char mander = 'c';

    private Connection conn;
    private boolean connected;

    // Estructuras para almacenamiento temporal de información
    protected HashMap<Integer, Boleto> availableTickets;
    
    Db()
    {
        connected = false;
        availableTickets = new HashMap<Integer, Boleto>();
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

    public boolean getConnected()
    {
        return connected;
    }

    /**
     * Metodo que precarga a memoria los eventos y boletos disponibles
     */
    public void preload()
    {
        try
        {
            PreparedStatement ps = conn
                    .prepareStatement(SELECT_AVAILABLE_TICKETS);
            ResultSet result = ps.executeQuery();

            Integer idTicket;
            String seatNumber;
            int idStatus;
            int idSection;
            int idEvent;

            while (result.next())
            {
                idTicket = result.getInt("idTicket");
                idStatus = result.getInt("idStatus");
                idSection = result.getInt("idSection");
                idEvent = result.getInt("idEvent");
                if (!availableTickets.containsKey(idTicket))
                {
                    seatNumber = result.getString("seatNumber");
                    availableTickets.put(idTicket,
                            new Boleto(idTicket.intValue(), seatNumber,
                                    idStatus, idSection, idEvent));
                }
            }
            ps.close();
        }
        catch (SQLException e)
        {
            log.error("");
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

            // Iterate over rows returned by the query
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
     * 
     * @param Avenue
     * @return An array with all events that ocurr on an Avenue
     */
    public Event[] getEventsWhere(String Avenue)
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
            PreparedStatement ps = conn.prepareStatement(SELECT_EVENT_IN_AVENUE);
            ps.setString(1, Avenue);
            
            result = ps.executeQuery();
            // getting number of selected rows
            nEvents = result.last() ? result.getRow() : 0;
            int i = 0;
            events = new Event[nEvents];
            result.beforeFirst();

            Event ev;
            int idEvent = 0;
            int idVenue = 0;
            String name = "";
            String Address = "";
            String city = "";
            LocalDate evDate = LocalDate.now();

            // Iterate over rows returned by the query
            // Populating array
            while (result.next())
            {
            	idEvent = result.getInt("idEvent");
                idVenue = result.getInt("idAvenue");
                name = result.getString("name");
                Address= result.getString("address");
                city = result.getString("city");
                idVenue = result.getInt("idVenue");
                ev = new Event(idEvent, idVenue, name, Address, city);
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
}