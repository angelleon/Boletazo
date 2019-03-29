package itq.dist;

import java.sql.Connection;
import java.sql.Date;
//import java.sql.*;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

import itq.dist.DbException.ERROR;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Db
{
    private static final Logger LOG = LogManager.getLogger(Db.class);

    // Informacion necesaria para conectarse a mysql
    private static final String USR = "boletazodev";
    private static final String PASSWD = "contrapass";
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/Boletazo?useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static final LocalDate TODAY = LocalDate.now();

    // TODO comentar la condicion date >= SYSDATE()
    // Lista de querys

    /**
     * En un PreparedStatement se pueden reemplazar valores en el query usando el
     * metodo setTIPO(int position, TIPO valor) donde position es la posicion del
     * (?) dentro del query iniciando con 1, TIPO depende de lo que se va a
     * reemplazar, y valor es..... supongo que el valor que se va a poner ahi
     */
    private static final String SEARCH_EVENT_BY_DATE = "SELECT * "
            + "FROM Event "
            + "WHERE date BETWEEN ? " // aqui se reemplaza el (?) con una fecha usando setDate(1, date)
            + "AND ? >= SYSDATE() "
            + "ORDER BY date ASC"; // lo mismo de arriba setDate(2, date)

    private static final String SEARCH_EVENTS_BY_HOUR = "SELECT * "
            + "FROM Event"
            + "WHERE HOUR(date) >= ? "
            + "AND date >= SYADATE() "
            + "ORDER BY date ASC";

    private static final String SELECT_AVAILABLE_TICKETS = "SELECT T.* "
            + "FROM Ticket T, Status S, Event E "
            + "WHERE T.idEvent = E.idEvent "
            + "WHERE T.idStatus = (SELECT idStatus "
            + "                    FROM Status "
            + "                    WHERE status = 'DISPONIBLE') "
            + "ORDER BY T.idTicket";

    // TODO Verificar columnas seleccionadas
    private static final String SELECT_BY_IDTICKET = "SELECT S.cost "
            + "FROM Section S, Ticket t "
            + "WHERE T.idTicket = ? "
            + "AND T.idSection = S.idSection "
            + "ORDER BY T.idTicket";

    private static final String SEARCH_EVENT_BY_NAME = "SELECT * "
            + "FROM Event "
            + "WHERE LOWER(name) LIKE LOWER('%?%') "
            + "AND date >= SYSDATE() "
            + "ORDER BY name ASC";

    private static final String SEARCH_EVENT_BY_VENUE_NAME = "SELECT E.* "
            + "FROM Event E, Venue V "
            + "WHERE E.idVenue = V.idVenue "
            + "AND LOWER(V.name) LIKE LOWER('%?%') "
            + "AND date >= SYSDATE() "
            + "ORDER BY V.name ASC";

    private static final String SEARCH_EVENT_BY_COST = "SELECT E.*, S.name, S.cost "
            + "FROM Event E, Section S, Section_has_Event ShE "
            + "WHERE E.idEvent = ShE.idEvent "
            + "AND ShE.idSection = S.idSection "
            + "AND S.cost <= ? "
            + "AND E.date >= SYSDATE() "
            + "ORDER BY S.cost DESC";

    private static final String SELECT_EVENT_BY_SECTION_NAME = "SELECT E.* "
            + "FROM Event E, Section S, Section_has_Event ShE "
            + "WHERE E.idEvent = ShE.idEvent "
            + "AND S.idSection = ShE.idSection "
            + "AND S.name = '?' "
            + "AND E.date >= SYSDATE() "
            + "ORDER BY S.name ASC";

    private static final String SELECT_AVAILABLE_SECTIONS_BY_IDEVENT = "SELECT S.* "
            + "FROM Section S, Event E, Section_has_Event ShE "
            + "WHERE ShE.idSection = S.idSection "
            + "AND E.idEvent = ShE.idEvent "
            + "AND E.idEvent = ? "
            + "AND E.date >= SYSDATE() "
            + "ORDER BY S.idSection";

    private static final String UPDATE_TICKET_STATUS = "UPDATE  Ticket "
            + "SET idStatus = (SELECT idStatus "
            + "                FROM Status "
            + "                WHERE LOWER(name) = LOWER(?)) "
            + "WHERE idTicket = ?";

    @SuppressWarnings("unused")
    private static char mander = 'c';

    private Connection conn;
    private boolean connected;

    // Estructuras para almacenamiento temporal de información
    protected HashMap<Integer, Ticket> availableTickets;

    Db()
    {
        connected = false;
        availableTickets = new HashMap<Integer, Ticket>();
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(URL, USR, PASSWD);
            connected = true;
        }
        catch (SQLException e)
        {
            LOG.error("An error occurred while tried to connect to DB");
            LOG.error(e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            LOG.error(e.getMessage());
        }
        catch (IllegalAccessException e)
        {
            LOG.error(e.getMessage());
        }
        catch (InstantiationException e)
        {
            LOG.error(e.getMessage());
        }
    }

    /**
     * Get the connected status of the database
     * 
     * @return connected
     */
    public boolean getConnected()
    {
        return connected;
    }

    /**
     * Previous loading the events and tickets on the database
     */
    public void preLoad()
    {
        try
        {
            Statement st = conn.createStatement();
            ResultSet result = st.executeQuery(SELECT_AVAILABLE_TICKETS);

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
                            new Ticket(idTicket.intValue(), seatNumber,
                                    idStatus, idSection, idEvent));
                }
            }
            st.close();
        }
        catch (SQLException e)
        {
            LOG.error("An error has ocurr while tried to obtein a loanding of each tickets information ");
            LOG.error(e.getMessage());<>
        }
    }

    /**
     * Search in the database all the events that ocurr at certain date (day).
     * 
     * @param date:
     * @return Event[] events that ocurr at certain date (day)
     */
    public Event[] searchEventsByDate(LocalDate date)
    {
        Event[] events = null;
        int nEvents = 0;
        ResultSet result = null;

        try
        {
            PreparedStatement ps = conn.prepareStatement(SEARCH_EVENT_BY_DATE);
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
            ps.close();
        }
        catch (SQLException e)
        {
            LOG.error(e.getMessage());
        }
        LOG.debug("Retrived [" + nEvents + "] events");
        return events;
    }

    /**
     * Search in the database all the events that ocurr at certain hour.
     * 
     * @return Event[] array with events on the same time
     */
    public Event[] getEventsAtHour(int hour)
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
            PreparedStatement ps = conn.prepareStatement(SEARCH_EVENTS_BY_HOUR);
            ps.setInt(1, hour);

            result = ps.executeQuery();

            // getting number of selected rows
            nEvents = result.last() ? result.getRow() : 0;
            events = new Event[nEvents];
            int i = 0;
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
                ev = new Event(idEvent, name, description, evDate, idVenue);
                events[i] = ev;
                i++;
            }
            ps.close();
        }
        catch (SQLException e)
        {
            LOG.error(e.getMessage());
        }
        LOG.debug("Retrived [" + nEvents + "] events");
        return events;
    }

    /**
     * Look ticket by idticket :V, know the cost of the ticket
     * 
     * @param idticket
     * @return float cost......
     */
    public float getTicketById(int idticket)
    {
        float cost = 0f;
        ResultSet result = null;
        try
        {
            PreparedStatement ps = conn.prepareStatement(SELECT_BY_IDTICKET);
            ps.setInt(1, idticket);
            result = ps.executeQuery();
            cost = result.first() ? result.getFloat("cost") : 0f;
            ps.close();
        }
        catch (SQLException e)
        {
            LOG.error(e.getMessage());
        }
        LOG.debug("ticket :" + idticket + " $" + cost);
        return cost;
    }

    /**
     * Search in the database all the events that ocurr at certain Avenue.
     * 
     * @param venueName
     * @return Event[] events that ocurr on an venue
     */
    public Event[] searchEventsByVenueName(String venueName)
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
            PreparedStatement ps = conn.prepareStatement(SEARCH_EVENT_BY_VENUE_NAME);
            ps.setString(1, venueName);

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
            String address = "";
            String city = "";
            LocalDate evDate = LocalDate.now();

            // Iterate over rows returned by the query
            // Populating array
            while (result.next())
            {
                idEvent = result.getInt("idEvent");
                idVenue = result.getInt("idAvenue");
                name = result.getString("name");
                address = result.getString("address");
                city = result.getString("city");
                idVenue = result.getInt("idVenue");
                ev = new Event(idEvent, idVenue, name, address, city);
                events[i] = ev;
                i++;
            }
        }
        catch (SQLException e)
        {
            LOG.error(e.getMessage());
        }
        LOG.debug("Retrived [" + nEvents + "] events");
        return events;
    }

    public Event[] searchEvents(String eventName, String venueName, LocalDate eventDate, int hour, float cost,
            String sectionName) throws DbException
    {
        HashMap<Integer, Event> events = new HashMap<Integer, Event>();
        Event[] results = searchEventsByName(eventName);
        for (Event ev : results)
        {
            if (!events.containsKey(ev.getIdEvent()))
            {
                events.put(new Integer(ev.getIdEvent()), ev);
            }
        }
        results = searchEventsByVenueName(venueName);
        for (Event ev : results)
        {
            if (!events.containsKey(ev.getIdEvent()))
            {
                events.put(new Integer(ev.getIdEvent()), ev);
            }
        }
        results = getEventsAtHour(hour);
        for (Event ev : results)
        {
            if (!events.containsKey(ev.getIdEvent()))
            {
                events.put(new Integer(ev.getIdEvent()), ev);
            }
        }
        results = searchEventsByCost(cost);
        for (Event ev : results)
        {
            if (!events.containsKey(ev.getIdEvent()))
            {
                events.put(new Integer(ev.getIdEvent()), ev);
            }
        }
        results = searchEventsBySectionName(sectionName);

        results = new Event[events.values().size()];
        int i = 0;
        for (Event ev : events.values())
        {
            results[i] = ev;
        }
        return results;
    }

    /**
     * Status that confirm if the user has been register on the database
     * 
     * @return false: user has been registered before
     */
    public boolean singup(String email, String usr, String passwd)
    {
        ResultSet result = null;
        String userRegistered = "select count(iduser) "
                + "from UserInfo"
                + "where email = ? ";
        try
        {
            PreparedStatement ps = conn.prepareStatement(userRegistered);
            ps.setString(1, email);
            result = ps.executeQuery();
            result.next();
            if (result.getInt("count(iduser)") == 0)
            {
                // then allows registration
                return true;
            }
            else
            {
                return false;
            }
            // ps.close();
        }
        catch (SQLException e)
        {
            LOG.error(e.getMessage());
        }

        return false;
    }

    /**
     * Register an user into the database to keep is login information stored
     * 
     * @param user
     *            User to register
     * @param passwd
     *            user password...
     * @return true: correct update on UserInfo,LoginInfo
     */
    public boolean toRegister(String user, String passwd, String email, String residence)
    {
        // ResultSet result = null;
        String updateUsrInfo = "insert into UserInfo "
                + "(email,estado) values (?,?) ";
        String updateLoginInfo = "INSERT INTO LoginInfo " +
                "(username,password) values (?.?) ";

        try
        {
            PreparedStatement ps = conn.prepareStatement(updateUsrInfo);
            ps.setString(1, email);
            ps.setString(2, residence);
            ps.executeUpdate();

            ps = conn.prepareStatement(updateLoginInfo);
            ps.setString(1, user);
            ps.setString(2, passwd);
            ps.close();
            return true;
        }
        catch (SQLException e)
        {
            LOG.error(e.getMessage());
        }

        return false;
    }

    /**
     * Login Check for a new account to see if had an account created or need one.
     * 
     * @param user
     * @param password
     * @return true: the user exist false : the user require registration
     */
    public boolean login(String usr, String pass)
    {
        String userExist = "select idlogin "
                + "from LoginInfo "
                + "where username = ? "
                + "and password = ? ";
        try
        {
            ResultSet result = null;
            PreparedStatement ps = conn.prepareStatement(userExist);
            ps.setString(1, usr);
            ps.setString(2, pass);
            ps.executeUpdate();
            result = ps.executeQuery();

            int users = result.last() ? result.getRow() : 0;
            if (users > 0)
            {
                // login
                return true;
            }
            ps.close();
        }
        catch (SQLException e)
        {
            LOG.error(e.getMessage());
        }
        return false;

    }

    /**
     * Search in the database all the events that had a certain cost.
     * 
     * @param date
     * @return Event[] events array with all the events
     */
    public Event[] searchEventsByCost(float cost)
    {
        Event[] events = null;
        if (!connected)
        {
            events = new Event[1];
            events[0] = new Event();
            return events;
        }
        try
        {
            PreparedStatement ps = conn.prepareStatement(SEARCH_EVENT_BY_COST);
            ps.setFloat(1, cost);
            ResultSet result = ps.executeQuery();

            int nEvents = result.last() ? result.getRow() : 0;
            result.beforeFirst();
            events = new Event[nEvents];

            Event ev;
            int idEvent = 0;
            String name = "";
            String address = "";
            String city = "";
            int idVenue = 0;
            int i = 0;

            while (result.next())
            {
                idEvent = result.getInt("idEvent");
                idVenue = result.getInt("idAvenue");
                name = result.getString("name");
                address = result.getString("address");
                city = result.getString("city");
                idVenue = result.getInt("idVenue");
                cost = result.getFloat("cost");
                ev = new Event(idEvent, idVenue, name, address, city, cost);
                events[i] = ev;
                i++;
            }
        }
        catch (SQLException e)
        {
            LOG.error(e.getMessage());
        }
        return events;
    }

    // TODO terminar implementacion
    private Event[] searchEventsByName(String name) throws DbException
    {
        Event[] events = new Event[0];
        try
        {
            PreparedStatement ps = conn.prepareStatement(SEARCH_EVENT_BY_NAME);
            ps.setString(1, name);
            ResultSet result = ps.executeQuery();

            int nEvents = result.last() ? result.getRow() : 0;
            result.beforeFirst();
            events = new Event[nEvents];

            int idEvent;
            String evName;
            String description;
            LocalDate date;
            int idVenue;

            int i = 0;
            while (result.next())
            {
                idEvent = result.getInt("idEvent");
                evName = result.getString("name");
                description = result.getString("description");
                date = result.getDate("date").toLocalDate();
                idVenue = result.getInt("idVenue");
                events[i] = new Event(idEvent, evName, description, date, idVenue);
                i++;
            }
            ps.close();
        }
        catch (SQLException e)
        {
            throw new DbException(ERROR.GENERIC_ERROR);
        }
        return events;
    }

    /**
     * Get the general info of a certain event searched by the eventID
     * 
     * @param eventId
     * @return Event
     */

    public EventInfo getEventInfo(int eventId)
    {
        return new EventInfo();
    }

    public Section[] getAvailabeSections()
    {
        Section[] sections = new Section[0];
        try
        {
            PreparedStatement ps = conn.prepareStatement(SELECT_AVAILABLE_SECTIONS_BY_IDEVENT);
            ResultSet result = ps.executeQuery();

            int nSections = result.last() ? result.getRow() : 0;
            result.beforeFirst();
            sections = new Section[nSections];

            int idSection;
            String name;
            float cost;

            int i = 0;
            while (result.next())
            {
                idSection = result.getInt("idSection");
                name = result.getString("name");
                cost = result.getFloat("cost");
                sections[i] = new Section(idSection, name, cost);
                i++;
            }

        }
        catch (SQLException e)
        {
        }
        return sections;
    }

    /**
     * Get the boleto that correspond to a certain IDTicket
     * 
     * @param idTicket
     * @return Boleto
     */

    public Ticket getBoletoById(int idTicket)
    {
        return availableTickets.get(idTicket);
    }

    public int consultTicketStatus(int idTicket)
    {
        if (availableTickets.containsKey(idTicket)) { return availableTickets.get(idTicket).getIdStatus(); }
        return -1;
    }

    public boolean updateTicketStatus(int idTicket, int status)
    {
        if (availableTickets.containsKey(idTicket))
        {
            try
            {
                PreparedStatement ps = conn.prepareStatement(UPDATE_TICKET_STATUS);
                ps.setInt(1, status);
                ps.setInt(2, idTicket);
                ps.executeUpdate();
                // tenemos q volver a la guia houston...
                LOG.info("ticket : " + idTicket + " VENDIDO ");
                ps.close();
                return true;
            }
            catch (SQLException e)
            {
                LOG.error(e.getMessage());
            }
        }
        return false;
    }

    private Event[] searchEventsBySectionName(String name)
    {
        Event[] events = new Event[0];
        try
        {
            PreparedStatement ps = conn.prepareStatement(SELECT_EVENT_BY_SECTION_NAME);
            ps.setString(1, name);
            ResultSet results = ps.executeQuery();

            int nEvents = results.last() ? results.getRow() : 0;
            results.beforeFirst();
            events = new Event[nEvents];

            int idEvent;
            String evName;
            String description;
            LocalDate date;
            int idVenue;

            int i = 0;
            while (results.next())
            {
                idEvent = results.getInt("idEvent");
                evName = results.getString("name");
                description = results.getString("description");
                date = results.getDate("date").toLocalDate();
                idVenue = results.getInt("idVenue");
                events[i] = new Event(idEvent, evName, description, date, idVenue);
                i++;
            }
        }
        catch (SQLException e)
        {

        }
        return events;
    }
}
