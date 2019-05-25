package itq.dist;

import java.sql.Connection;
import java.sql.Date;
//import java.sql.*;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
    // Lista de querys
    /**
     * En un PreparedStatement se pueden reemplazar valores en el query usando el
     * metodo setTIPO(int position, TIPO valor) donde position es la posicion del
     * (?) dentro del query iniciando con 1, TIPO depende de lo que se va a
     * reemplazar, y valor es..... supongo que el valor que se va a poner ahi
     */
    private static final 
    private static final String SELECT_EVENT_AT_DATE = "SELECT *"
            + "FROM Event"
            + "WHERE date BETWEEN ?" // aqui se reemplaza el (?) con una fecha usando setDate(1, date)
            + "AND DATE_ADD(?, INTERVAL 1 DAY)"; // lo mismo de arriba setDate(2, date)

    private static final String SELECT_EVENT_AT_HOUR = "SELECT * "
            + "from Event "
            + "WHERE date_format(date,'%H:%i') = ? ";

    private static final String SELECT_AVAILABLE_TICKETS = "SELECT T.* "
            + "FROM Ticket T, Status S "
            + "WHERE T.idStatus = (SELECT idStatus "
            + "FROM Status "
            + "WHERE status = 'DISPONIBLE')";
    private static final String SELECT_BY_TICKET_ID = "SELECT cost "
            + "FROM section,ticket "
            + "where idticket = ? "
            + "and ticket.idsection = section.idsection ";
    private static final String SEARCH_EVENT_BY_NAME = "SELECT * "
            + "FROM Event "
            + "WHERE LOWER(name) LIKE LOWER('%?%')";

    private static final String SELECT_EVENT_IN_VENUE = "SELECT E.* "
            + "FROM Event E, Venue V "
            + "WHERE E.idVenue = V.idVenue "
            + "AND LOWER(V.name) LIKE LOWER('%?%')";

    private static final String SEARCH_EVENT_BY_COST = "SELECT E.*, S.name, S.cost "
            + "FROM Event E, Section S, Section_has_Event ShE"
            + "WHERE E.idEvent = ShE.idEvent "
            + "AND ShE.idSection = S.idSection"
            + "AND S.cost <= ?";

    private static final String SEARCH_EVENT_BY_SECTION_NAME = "SELECT E.* "
            + "FROM Event E, Section S, Section_has_Event ShE "
            + "WHERE E.idEvent = ShE.idEvent "
            + "AND S.idSection = ShE.idSection "
            + "AND S.name = '?'";

    private static final String SELECT_AVAILABLE_SECTIONS_BY_IDEVENT = "SELECT S.* "
            + "FROM Section S, Event E, Section_has_Event ShE "
            + "WHERE ShE.idSection = S.idSection "
            + "AND E.idEvent = ShE.idEvent "
            + "AND E.idEvent = ?";

    private static final String UPDATE_TICKET_STATUS = "UPDATE  Ticket "
            + "SET idStatus= ? "
            + "WHERE idTicket = ? ";

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
            LOG.error("An error occurred when trying to connect to DB");
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
                            new Ticket(idTicket.intValue(), seatNumber,
                                    idStatus, idSection, idEvent));
                }
            }
            ps.close();
        }
        catch (SQLException e)
        {
            LOG.error("");
            LOG.error(e.getMessage());
        }
    }

    /**
     * Search in the database all the events that ocurr at certain date (day).
     * 
     * @param date:
     * @return Event[] events that ocurr at certain date (day)
     */
    public Event[] getEventsAtDay(LocalDate date)
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
    public Event[] getEventsAtHour(LocalDate date)
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
            PreparedStatement ps = conn.prepareStatement(SELECT_EVENT_AT_HOUR);
            Date searchDate = Date.valueOf(date);
            ps.setDate(1, searchDate);

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
        String ticket = "";
        if (!connected) { return cost; }
        LOG.debug("Retrived [" + idticket + "] ");
        try
        {
            PreparedStatement ps = conn.prepareStatement(SELECT_BY_TICKET_ID);
            ps.setInt(1, idticket);
            result = ps.executeQuery();
            cost = result.getFloat("cost");
            ps.close();
        }
        catch (SQLException e)
        {
            LOG.error(e.getMessage());
        }
        LOG.debug("ticket :" + idticket + " $" + cost);
        return cost;

        /*
         * 
         * while (result.next()) {
         * 
         * String seat = result.getString("seatNumber"); int status =
         * result.getInt("idStatus"); int section = result.getInt("idSection"); int
         * event = result.getInt("idEvent"); ticket = idticket + "," + seat + "," +
         * status + "," + section + "," + event; } ps.close(); return ticket; } catch
         * (SQLException e) {
         * 
         * } return "";
         */
    }

    /**
     * Search in the database all the events that ocurr at certain Avenue.
     * 
     * @param Avenue
     * @return Event[] events that ocurr on an venue
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
            PreparedStatement ps = conn.prepareStatement(SELECT_EVENT_IN_VENUE);
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

    public EventInfo[] searchEvents(String eventName, String venueName, LocalDate eventDate, int hour, float cost,
            String sectionName) throws DbException
    {
        HashMap<Integer, EventInfo> events = new HashMap<Integer, EventInfo>();
        EventInfo[] results = new EventInfo[0];
        results = searchEventsByName(eventName);
        for (EventInfo ev : results)
        {
            if (!events.containsKey(ev.getIdEvent()))
            {
                events.put(new Integer(ev.getIdEvent()), ev);
            }
        }
        results = searchEventsByVenueName(venueName);
        for (EventInfo ev : results)
        {
            if (!events.containsKey(ev.getIdEvent()))
            {
                events.put(new Integer(ev.getIdEvent()), ev);
            }
        }
        results = getEventsAtHour(hour);
        for (EventInfo ev : results)
        {
            if (!events.containsKey(ev.getIdEvent()))
            {
                events.put(new Integer(ev.getIdEvent()), ev);
            }
        }
        results = searchEventsByCost(cost);
        for (EventInfo ev : results)
        {
            if (!events.containsKey(ev.getIdEvent()))
            {
                events.put(new Integer(ev.getIdEvent()), ev);
            }
        }
        results = searchEventsBySectionName(sectionName);

        results = new EventInfo[events.values().size()];
        int i = 0;
        for (EventInfo ev : events.values())
        {
            results[i] = ev;
            i++;
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
    public Event[] getEventsPerCost(LocalDate date)
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
            Event ev;
            int idEvent = 0;
            String name = "";
            String address = "";
            String city = "";
            float cost = 0;
            LocalDate evDate = LocalDate.now();
            int idVenue = 0;
            int numerator = 0;
            PreparedStatement ps = conn.prepareStatement(SEARCH_EVENT_BY_COST);
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
                events[numerator] = ev;
                numerator++;
            }
        }
        catch (SQLException e)
        {
            LOG.error(e.getMessage());
        }
        return events;
    }

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
                return true;
            }
            catch (SQLException e)
            {
                LOG.error(e.getMessage());
            }
        }
        return false;
    }
}
