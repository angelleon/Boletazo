package itq.dist;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import itq.dist.DbException.ERROR;

public class Db
{
    private static final Logger LOG = LogManager.getLogger(Db.class);

    // Informacion necesaria para conectarse a mysql
    private static final String USR = "boletazodev";
    private static final String PASSWD = "contrapass";
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/Boletazo?useLegacyDatetimeCode=false&serverTimezone=UTC";

    // TODO comentar la condicion date >= SYSDATE()
    // Lista de querys

    /**
     * En un PreparedStatement se pueden reemplazar valores en el query usando el
     * metodo setTIPO(int position, TIPO valor) donde position es la posicion del
     * (?) dentro del query iniciando con 1, TIPO depende de lo que se va a
     * reemplazar, y valor es..... supongo que el valor que se va a poner ahi
     */
    // ResultSet result = null;
    private static final String UPDATE_USR_INFO = "insert into UserInfo "
            + "(email,estado) values (?,?) ";
    private static final String UPDATE_LOG_INFO = "INSERT INTO LoginInfo "
            + "(username,password) values (?,?) ";
    private static final String SEARCH_EVENT_BY_DATE = "SELECT * "
            + "FROM Event "
            + "WHERE date >= ? " // aqui se reemplaza el (?) con una fecha usando setDate(1, date)
            // + "AND ? >= SYSDATE() "
            + "ORDER BY date ASC"; // lo mismo de arriba setDate(2, date)

    private static final String SEARCH_EVENTS_BY_HOUR = "SELECT * "
            + "FROM Event "
            + "WHERE HOUR(date) >= ? "
            // + "AND date >= SYADATE() "
            + "ORDER BY date ASC";

    private static final String SELECT_AVAILABLE_TICKETS = "SELECT T.* "
            + "FROM Ticket T, Event E "
            + "WHERE T.idEvent = E.idEvent "
            + "AND T.idStatus = (SELECT idStatus "
            + "                  FROM Status "
            + "                  WHERE status = 'DISPONIBLE') "
            // + "AND E.date >= SYSDATE() "
            + "ORDER BY T.idTicket";

    // TODO Verificar columnas seleccionadas
    private static final String SELECT_BY_IDTICKET = "SELECT S.cost "
            + "FROM Section S, Ticket T "
            + "WHERE T.idTicket = ? "
            + "AND T.idSection = S.idSection "
            + "ORDER BY T.idTicket";

    private static final String SEARCH_EVENT_BY_NAME = "SELECT * "
            + "FROM Event "
            + "WHERE LOWER(name) LIKE LOWER(?) "
            // + "AND date >= SYSDATE() "
            + "ORDER BY name ASC";

    private static final String SEARCH_EVENT_BY_VENUE_NAME = "SELECT E.* "
            + "FROM Event E, Venue V "
            + "WHERE E.idVenue = V.idVenue "
            + "AND LOWER(V.name) LIKE LOWER(?) "
            // + "AND date >= SYSDATE() "
            + "ORDER BY V.name ASC";

    private static final String SEARCH_EVENT_BY_COST = "SELECT E.*, S.name, S.cost "
            + "FROM Event E, Section S, Section_has_Event ShE "
            + "WHERE E.idEvent = ShE.idEvent "
            + "AND ShE.idSection = S.idSection "
            + "AND S.cost <= ? "
            // + "AND E.date >= SYSDATE() "
            + "ORDER BY S.cost DESC";

    private static final String SELECT_EVENT_BY_SECTION_NAME = "SELECT E.* "
            + "FROM Event E, Section S, Section_has_Event ShE "
            + "WHERE E.idEvent = ShE.idEvent "
            + "AND S.idSection = ShE.idSection "
            + "AND S.name = ? "
            // + "AND E.date >= SYSDATE() "
            + "ORDER BY S.name ASC";

    private static final String SELECT_AVAILABLE_SECTIONS_BY_IDEVENT = "SELECT S.* "
            + "FROM Section S, Event E, Section_has_Event ShE "
            + "WHERE ShE.idSection = S.idSection "
            + "AND E.idEvent = ShE.idEvent "
            + "AND E.idEvent = ? "
            // + "AND E.date >= SYSDATE() "
            + "ORDER BY S.idSection";

    private static final String UPDATE_TICKET_STATUS = "UPDATE  Ticket "
            + "SET idStatus = (SELECT idStatus "
            + "                FROM Status "
            + "                WHERE LOWER(status) = LOWER(?)) "
            + "WHERE idTicket = ?";

    // TODO: terminar statement
    private static final String SELECT_EVENT_BY_EVENTID = "SELECT E.* "
            + "FROM Event E "
            + "WHERE E.idEvent = ?";

    private static final String SELECT_PARTICIPANT_BY_IDEVENT = "SELECT P.* "
            + "FROM Participant P, Event E, Event_has_Participant EhP "
            + "WHERE EhP.idEvent = E.idEvent "
            + "AND EhP.idParticipant = P.idParticipant "
            + "AND E.idEvent = ? "
            // + "AND E.date >= SYSDATE() "
            + "";

    @SuppressWarnings("unused")
    private static char mander = 'c';

    private Connection conn;
    private boolean connected;

    // Estructuras para almacenamiento temporal de informaci�n
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
            int count = 0;
            while (result.next())
            {
                idTicket = Integer.valueOf(result.getInt("idTicket"));
                idStatus = result.getInt("idStatus");
                idSection = result.getInt("idSection");
                idEvent = result.getInt("idEvent");
               // LOG.debug(idTicket+"-"+idEvent+"-"+idStatus+"-"+idSection);
                if (!availableTickets.containsKey(idTicket))
                {
                    seatNumber = result.getString("seatNumber");
                    availableTickets.put(idTicket,
                            new Ticket(Integer.valueOf(idTicket), seatNumber,
                                    idStatus, idSection, idEvent));
                    count++;
                }
            }
            LOG.info("Preloading [" + count + "] tickets");
            st.close();
        }
        catch (SQLException e)
        {
            LOG.error("An error has ocurr while tried to obtein a loanding of each tickets information ");
            LOG.error(e.getMessage());// <>
        }
    }

    /**
     * Search in the database all the events that ocurr at certain date (day).
     * 
     * @param date:
     * @return Event[] events that ocurr at certain date (day)
     */
    public EventInfo[] searchEventsByDate(LocalDate date) throws DbException
    {
        EventInfo[] events = new EventInfo[0];

        try
        {
            PreparedStatement ps = conn.prepareStatement(SEARCH_EVENT_BY_DATE);
            Date searchDate = Date.valueOf(date);
            ps.setDate(1, searchDate);
            ps.setDate(2, searchDate);
            events = buildEventInfoArray(ps.executeQuery());
            ps.close();
        }
        catch (SQLException e)
        {
            LOG.error(e.getMessage());
            throw new DbException();
        }
        return events;
    }

    /**
     * Search in the database all the events that ocurr at certain hour.
     * 
     * @return Event[] array with events on the same time
     */
    public EventInfo[] getEventsAtHour(int hour) throws DbException
    {
        EventInfo[] events = new EventInfo[0];
        try
        {
            PreparedStatement ps = conn.prepareStatement(SEARCH_EVENTS_BY_HOUR);
            ps.setInt(1, hour);
            events = buildEventInfoArray(ps.executeQuery());
            ps.close();
        }
        catch (SQLException e)
        {
            LOG.error(e.getMessage());
        }
        LOG.debug("Retrived [" + events.length + "] events");
        return events;
    }

    /**
     * Look ticket by idticket :V, know the cost of the ticket
     * 
     * @param idticket
     * @return float cost......
     */
    public float getTicketCostById(int idticket)
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
    public EventInfo[] searchEventsByVenueName(String venueName) throws DbException
    {
        EventInfo[] events = new EventInfo[0];
        try
        {
            PreparedStatement ps = conn.prepareStatement(SEARCH_EVENT_BY_VENUE_NAME);
            ps.setString(1, "%" + venueName + "%");
            events = buildEventInfoArray(ps.executeQuery());
            ps.close();
        }
        catch (SQLException e)
        {
            LOG.error(e.getMessage());
        }
        LOG.debug("Retrived [" + events.length + "] events");
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
    public boolean singup(String email, String usr, String passwd) throws DbException
    {
        ResultSet result = null;
        String userRegistered = "select count(iduser) "
                + "from UserInfo "
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
            throw new DbException();
        }
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
    public boolean toRegister(String user, String passwd, String email, String residence) throws DbException
    {
        try
        {
            PreparedStatement ps = conn.prepareStatement(UPDATE_USR_INFO);
            ps.setString(1, email);
            ps.setString(2, residence);
            ps.executeUpdate();
            ps.close();

            ps = conn.prepareStatement(UPDATE_LOG_INFO);
            ps.setString(1, user);
            ps.setString(2, passwd);
            ps.executeUpdate(updateLoginInfo);
            ps.close();
            return true;
        }
        catch (SQLException e)
        {
            LOG.error(e.getMessage());
            throw new DbException();
        }
    }

    /**
     * Login Check for a new account to see if had an account created or need one.
     * 
     * @param user
     * @param password
     * @return true: the user exist false : the user require registration
     */
    public boolean login(String usr, String pass) throws DbException
    {
        String userExist = "SELECT idlogin "
                + "FROM LoginInfo "
                + "WHERE username = ? "
                + "AND password = ? ";
        try
        {
            ResultSet result = null;
            PreparedStatement ps = conn.prepareStatement(userExist);
            ps.setString(1, usr);
            ps.setString(2, pass);
            result = ps.executeQuery();

            int users = result.last() ? result.getRow() : 0;
            if (users > 0)
            {
                // login
                ps.close();
                return true;
            }
            ps.close();
        }
        catch (SQLException e)
        {
            LOG.debug(e.getMessage());
            throw new DbException();
        }
        return false;

    }

    /**
     * Search in the database all the events that had a certain cost.
     * 
     * @param date
     * @return Event[] events array with all the events
     */
    public EventInfo[] searchEventsByCost(float cost) throws DbException
    {
        EventInfo[] events = new EventInfo[0];
        try
        {
            PreparedStatement ps = conn.prepareStatement(SEARCH_EVENT_BY_COST);
            ps.setFloat(1, cost);
            events = buildEventInfoArray(ps.executeQuery());
            ps.close();
        }
        catch (SQLException e)
        {
            LOG.debug(e.getMessage());
            throw new DbException();
        }
        return events;
    }

    // TODO terminar implementacion
    private EventInfo[] searchEventsByName(String name) throws DbException
    {
        EventInfo[] events = new EventInfo[0];
        try
        {
            PreparedStatement ps = conn.prepareStatement(SEARCH_EVENT_BY_NAME);
            ps.setString(1, "%" + name + "%");
            events = buildEventInfoArray(ps.executeQuery());
            ps.close();
        }
        catch (SQLException e)
        {
            LOG.debug(e.getMessage());
            throw new DbException();
        }
        return events;
    }

    /**
     * Get the general info of a certain event searched by the eventID
     * 
     * @param eventId
     * @return Event
     */

    public EventInfo getEventInfoByIdEvent(int idEvent) throws DbException
    {
        EventInfo evInfo = new EventInfo();
        try
        {
            PreparedStatement ps = conn.prepareStatement(SELECT_EVENT_BY_EVENTID);
            ps.setInt(1, idEvent);
            EventInfo[] ev = buildEventInfoArray(ps.executeQuery());
            evInfo = ev[0];
        }
        catch (SQLException e)
        {
            LOG.debug(e.getMessage());
            throw new DbException();
        }
        return evInfo;
    }

    public Section[] getAvailabeSections() throws DbException
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
            LOG.debug(e.getMessage());
            throw new DbException();
        }
        return sections;
    }

    /**
     * Get the boleto that correspond to a certain IDTicket
     * 
     * @param idTicket
     * @return Boleto
     */

    public Ticket getAvailableTicketById(int idTicket)
    {
        return availableTickets.get(idTicket);
    }

    // TODO: esto actualmente trabaja solo con los tickets que se obtuvieron en el
    // metodo preload()
    // para obtener cualquier status es necesario un query, de lo contrario eliminar
    // este ToDo

    public int consultTicketStatus(int idTicket)
    {
        Integer idT = Integer.valueOf(idTicket);
        if (availableTickets
                .containsKey(idT)) { return availableTickets.get(idT).getIdStatus(); }
        return 3;
    }

    public boolean updateTicketStatus(int idTicket, String status) throws DbException
    {
        if (availableTickets.containsKey(idTicket))
        {
            try
            {
                PreparedStatement ps = conn.prepareStatement(UPDATE_TICKET_STATUS);
                ps.setString(1, status);
                ps.setInt(2, idTicket);
                ps.executeUpdate();
                // tenemos q volver a la guia houston...
                LOG.info("ticket : " + idTicket + " VENDIDO ");
                ps.close();
                return true;
            }
            catch (SQLException e)
            {
                LOG.debug(e.getMessage());
                throw new DbException();
            }
        }
        return false;
    }

    private EventInfo[] searchEventsBySectionName(String name) throws DbException
    {
        EventInfo[] events = new EventInfo[0];
        try
        {
            PreparedStatement ps = conn.prepareStatement(SELECT_EVENT_BY_SECTION_NAME);
            ps.setString(1, name);
            events = buildEventInfoArray(ps.executeQuery());
            ps.close();
        }
        catch (SQLException e)
        {
            LOG.debug(e.getMessage());
            throw new DbException();
        }
        return events;
    }

    private Participant[] getParticipantByIdEvent(int idEvent) throws DbException
    {
        Participant[] participants = new Participant[0];
        try
        {
            PreparedStatement ps = conn.prepareStatement(SELECT_PARTICIPANT_BY_IDEVENT);
            ps.setInt(1, idEvent);
            ResultSet results = ps.executeQuery();

            int nParticipants = results.next() ? results.getRow() : 0;
            participants = new Participant[nParticipants];
            results.beforeFirst();

            int idParticipant;
            String name;
            String description;

            int i = 0;
            while (results.next())
            {
                idParticipant = results.getInt("idParticipant");
                name = results.getString("name");
                description = results.getString("description");
                participants[i] = new Participant(idParticipant, name, description);
                i++;
            }

            ps.close();
        }
        catch (SQLException e)
        {
            LOG.debug(e.getMessage());
            throw new DbException();
        }
        return participants;
    }

    private EventInfo[] buildEventInfoArray(ResultSet results) throws DbException
    {
        EventInfo[] events = new EventInfo[0];
        try
        {
            int nEvents = results.last() ? results.getRow() : 0;
            results.beforeFirst();
            events = new EventInfo[nEvents];

            int idEvent;
            String evName;
            String description;
            LocalDate date;
            int idVenue;
            Participant[] participants;

            int i = 0;
            while (results.next())
            {
                idEvent = results.getInt("idEvent");
                evName = results.getString("name");
                description = results.getString("description");
                date = results.getDate("date").toLocalDate();
                idVenue = results.getInt("idVenue");
                participants = getParticipantByIdEvent(idEvent);
                events[i] = new EventInfo(idEvent, evName, description, date, idVenue, participants);
                LOG.debug(events[i]);
                i++;
            }
        }
        catch (SQLException e)
        {
            LOG.error(e.getMessage());
            throw new DbException();
        }
        return events;
    }
}