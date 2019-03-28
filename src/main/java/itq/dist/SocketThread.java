package itq.dist;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import itq.dist.ConversationException.ERROR;

public class SocketThread extends Thread
{
    private static final Logger LOG = LogManager.getLogger(SocketThread.class);
    private static final int MAX_MSG_LENGTH = 1024;
    private static final int PERMIT_TICKETS = 4;
    private static final int PAYMENT_TIMEOUT = 6000;

    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private Db db;
    private SessionControl sc;
    private int sessionId;
    private StringBuilder msgOut;

    // TODO: declarar esto dentro de los metodos correspondientes
    // la informacion del login debe estar el menor tiempo posible en memoria
    private int nRequestedTickets;
    private int idEvent;
    private int[] reqTicketIds;
    private boolean alive = true;
    // private Ticketinfo[]; // i needed one of these!!!!!!
    private String usr;
    private String pass;
    private String email;
    private String stateResidence;
    private Event[] searchResult;
    private EventInfo selectedEvent;
    private Ticket[] reservedTickets;
    private TimerThread ticketTimer;

    /**
     * create an association between variable and its value
     * 
     * @param C_START_SESSION
     *            equal to 0
     * @param S_START_SESSION
     *            equal to 1
     * @param GET_EVENT_LIST
     *            equal to 2
     * @param POST_EVENT_LIST
     *            equal to 3
     * @param GET_EVENT_INFO
     *            equal to 4
     * @param POST_EVENT_INFO
     *            equal to 5
     * @param GET_AVAILABLE_SEATS
     *            equal to 6
     * @param POST_AVAILABLE_SEATS
     *            equal to 7
     * @param REQUEST_RESERVE_TICKETS
     *            equal to 8
     * @param CONFIRM_RESERVE_TICKETS
     *            equal to 9
     * @param SINGUP
     *            equal to 10
     * @param SINGUP_STATUS
     *            equal to 11
     * @param LOGIN_CHECK
     *            equal to 12
     * @param LOGIN_STATUS
     *            equal to 13
     * @param POST_PAYMENT_INFO
     *            equal to 14
     * @param PUCHARASE_COMPLETED
     *            equal to 15
     */
    private static enum STATE {
        C_START_SESSION,
        S_START_SESSION,
        GET_EVENT_LIST,
        POST_EVENT_LIST,
        GET_EVENT_INFO,
        POST_EVENT_INFO,
        GET_AVAILABLE_SEATS,
        POST_AVAILABLE_SEATS,
        REQUEST_RESERVE_TICKETS,
        CONFIRM_RESERVE_TICKETS,
        SINGUP,
        SINGUP_STATUS,
        LOGIN_CHECK,
        LOGIN_STATUS,
        POST_PAYMENT_INFO,
        PUCHARASE_COMPLETED
    }

    private STATE currentState;
    private STATE targetClientState;

    /**
     * create an association between variable and its value
     * 
     * @param NULL
     *            equal to 0
     * @param INT
     *            equal to 1
     * @param FLOAT
     *            equal to 2
     * @param ARRAY
     *            equal to 3
     */
    private enum TYPES {
        NULL,
        INT,
        FLOAT,
        STRING,
        ARRAY
    }

    /**
     * 
     * @param socket
     * @param db
     * @param sc
     *            this method set each one of all these three parameters where the
     *            Socket is the socket, db is the data base already loaded and sc is
     *            the control of the session client
     */
    SocketThread(Socket socket, Db db, SessionControl sc)
    {
        this.socket = socket;
        this.db = db;
        this.sc = sc;
        this.msgOut = new StringBuilder(MAX_MSG_LENGTH);
        this.currentState = STATE.C_START_SESSION;
        try
        {
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException e)
        {
            LOG.error(e.getMessage());
        }
    }

    /**
     * This method create the sequence of the conversation between the server-client
     * 
     * @throws IOException
     */
    public STATE targetState() throws IOException
    {
        String rawTargetState = dataIn.readUTF().split(",")[0]; // get the Operation Number
        targetClientState = StrToState(rawTargetState);
        return targetClientState;
    }

    @Override
    public void run()
    {
        try
        {
            targetState();
            switch (targetClientState)
            {
            case C_START_SESSION:
                cStartSession();
                sStartSession();
                break;
            case GET_EVENT_LIST:
                getEventList();
                postEventList();
                break;
            case GET_EVENT_INFO:
                getEventInfo();
                postEventInfo();
                break;
            case GET_AVAILABLE_SEATS:
                getAvailableSeats();
                postAvailableSeats();
                break;
            case REQUEST_RESERVE_TICKETS:
                requestReserveTickets();
                confirmReserveTickets();
                break;
            case SINGUP:
                singup();
                singupStatus();
                break;
            case LOGIN_CHECK:
                loginCheck();
                loginStatus();
                break;
            case POST_PAYMENT_INFO:
                postPaymentInfo();
                pucharaseCompleted();
                break;
            default:
                socket.close(); // Maybe here??
            }
        }
        catch (ConversationException e)
        {
            LOG.debug(e.getMessage());
        }
        catch (SessionException e)
        {
            LOG.debug(e.getMessage());
        }
        catch (DbException e)
        {
            LOG.debug(e.getMessage());
        }
        catch (IOException e)
        {
            LOG.debug(e.getMessage());
        }
    }

    // TODO: implementar un metodo por cada constante del enum STATE
    // TODO: considerar lanzar excepciones para manejar todos los errores de la
    // conversacion dentro de un bloque catch

    // La tablita .....si hay sesion tabla.. ok no..

    /**
     * The first action to send a request to the server
     * 
     * @return true if the conversation is successful(The client already has a
     *         petition for tickets), false if the message has an error in its
     *         syntax and content
     * @throws ConversationException
     * @throws IOException
     * 
     *             read the message and split its content with a ,
     */
    private boolean cStartSession() throws ConversationException, IOException
    {
        String rawMsg = dataIn.readUTF();
        if (checkMsgIntegrity(rawMsg))
        {
            String[] valuesIn = rawMsg.split(",");
            checkConversationState(valuesIn[0]);
            return true;
        }
        return false;
    }

    /**
     * the server has already received a request for tickets
     * 
     * @return true if the server received a successful petition by the client
     *         software, false if syntax or content is different
     * @throws ConversationException
     * @throws IOException
     */
    private boolean sStartSession() throws ConversationException, IOException
    {
        currentState = STATE.S_START_SESSION;
        msgOut.setLength(0);
        sessionId = sc.getNewSessionId();
        sc.sessionTimer(sessionId);
        if (sessionId > 0)
        {
            msgOut.append(STATE.S_START_SESSION.ordinal());
            msgOut.append(",");
            msgOut.append(sessionId);
            dataOut.writeUTF(msgOut.toString());
            return true;
        }
        return false;
    }

    /**
     * is a solicitation for the events available
     * 
     * @return true if the process got all then requirements for an specific event
     * @throws ConversationException
     * @throws SessionException
     * @throws IOException
     */
    private boolean getEventList() throws ConversationException, SessionException, DbException, IOException
    {
        currentState = STATE.GET_EVENT_LIST;
        String rawMsg = dataIn.readUTF();
        sc.updateSessionTimer();
        if (checkMsgIntegrity(rawMsg))
        {
            String[] valuesIn = rawMsg.split(",");
            checkConversationState(valuesIn[0]);
            checkSessionId(valuesIn[1]);
            String eventName = valuesIn[2];
            String venueName = valuesIn[3];
            String rawDate = valuesIn[4];
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-YYYY");
            LocalDate eventDate;
            try
            {
                eventDate = LocalDate.parse(rawDate, format);
            }
            catch (DateTimeParseException e)
            {
                throw new ConversationException(ERROR.INCORRECT_DATE_FORMAT);
            }
            int hour = Integer.parseInt(valuesIn[5]);
            if (hour < 0 || hour > 23)
                throw new ConversationException(ERROR.VALUE_OUT_OF_RANGE);
            float cost = Float.parseFloat(valuesIn[6]);
            String sectionName = valuesIn[7];
            searchResult = db.searchEvents(eventName, venueName, eventDate, hour, cost, sectionName);
            return true;
        }
        return false;
    }

    /**
     * create a message to send to the client with all events available
     * 
     * @return true if all the information needed by the client is founded in the
     *         data base after that is sent to the client separated by , return
     *         false if some part of the information is not founded
     * @throws ConversationException
     * @throws IOException
     */
    private boolean postEventList() throws ConversationException, IOException
    {
        currentState = STATE.POST_EVENT_LIST;
        sc.updateSessionTimer();
        msgOut.setLength(0);
        msgOut.append(STATE.POST_EVENT_LIST.ordinal());
        msgOut.append(",");
        msgOut.append(sessionId);
        msgOut.append(",");
        msgOut.append(searchResult.length);
        msgOut.append(",");
        for (Event ev : searchResult)
        {
            msgOut.append(ev.getIdEvent());
            msgOut.append(",");
            msgOut.append(ev.getName());
        }
        dataOut.writeUTF(msgOut.toString());
        return true;
    }

    /**
     * this method load the information associated to an specific event
     * 
     * @return return true if the events information was founded in other case
     *         return false
     * @throws ConversationException
     * @throws SessionException
     * @throws IOException
     */

    private boolean getEventInfo() throws ConversationException, SessionException, DbException, IOException
    {
        currentState = STATE.GET_EVENT_INFO;
        String rawMsg = dataIn.readUTF();
        sc.updateSessionTimer();
        if (checkMsgIntegrity(rawMsg))
        {
            String[] valuesIn = rawMsg.split(",");
            checkConversationState(valuesIn[0]);
            checkSessionId(valuesIn[1]);
            idEvent = Integer.parseInt(valuesIn[2]);
            selectedEvent = db.getEventInfo(idEvent);
        }
        return false;
    }

    /**
     * Create a message with information about an specific event separated by , then
     * is sent to the client
     * 
     * @return true if the information about the particular event is founded in
     *         other case return false
     * @throws ConversationException
     * @throws SessionException
     * @throws IOException
     */
    private boolean postEventInfo() throws ConversationException, SessionException, IOException
    {
        currentState = STATE.POST_EVENT_INFO;
        sc.updateSessionTimer();
        msgOut.setLength(0);
        msgOut.append(currentState.ordinal());
        msgOut.append(",");
        msgOut.append(sessionId);
        msgOut.append(",");
        msgOut.append(selectedEvent.getIdEvent());
        msgOut.append(",");
        msgOut.append(selectedEvent.getName());
        msgOut.append(",");
        LocalDate[] dates = selectedEvent.getDates();
        msgOut.append(dates.length);
        for (LocalDate d : dates)
        {
            msgOut.append(",");
            msgOut.append(d.getDayOfMonth());
            msgOut.append("-");
            msgOut.append(d.getMonthValue());
            msgOut.append("-");
            msgOut.append(d.getYear());
        }
        Participant[] participants = selectedEvent.getParticipants();
        msgOut.append(participants.length);
        for (Participant p : participants)
        {
            msgOut.append(",");
            msgOut.append(p.getName());
            msgOut.append(",");
            msgOut.append(p.getDescription());
        }
        dataOut.writeUTF(msgOut.toString());
        return true;
    }

    /**
     * create a request for the server to show the available seats
     * 
     * @return
     * @throws ConversationException
     * @throws SessionException
     * @throws IOException
     */
    private boolean getAvailableSeats()
            throws ConversationException, SessionException, IOException
    {
        currentState = STATE.GET_AVAILABLE_SEATS;
        sc.updateSessionTimer();
        String rawMsg = dataIn.readUTF();
        if (checkMsgIntegrity(rawMsg))
        {
            String[] valuesIn = rawMsg.split(",");
            checkConversationState(valuesIn[0]);
            checkSessionId(valuesIn[1]);
            idEvent = Integer.parseInt(valuesIn[2]);
        }
        return false;
    }

    /**
     * 
     * @return true if the information is founded in other case return false
     * @throws ConversationException
     * @throws IOException
     */
    private boolean postAvailableSeats() throws ConversationException, IOException
    {
        currentState = STATE.POST_AVAILABLE_SEATS;
        sc.updateSessionTimer();
        msgOut.setLength(0);
        msgOut.append(currentState.ordinal());
        return false;
    }

    // opCode = 8
    /**
     * This method read a request by the client to buy an specific number of tickets
     * 
     * @return true if the information and the number of tickets can be bought in
     *         other case return false
     * @throws ConversationException
     * @throws SessionException
     * @throws IOException
     */
    private boolean requestReserveTickets()
            throws ConversationException, SessionException, DbException, IOException
    {
        currentState = STATE.REQUEST_RESERVE_TICKETS;
        String rawMsg = dataIn.readUTF();
        sc.updateSessionTimer();
        if (checkMsgIntegrity(rawMsg) && sessionId > 0)
        {
            String[] parts = rawMsg.split(",");
            // int opcode = Integer.parseInt(parts[0]);
            sessionId = Integer.parseInt(parts[1]);
            idEvent = Integer.parseInt(parts[2]);
            nRequestedTickets = Integer.parseInt(parts[3]);
            // see if the nRequestedTickets is equal or less than permit_Ticket
            if (nRequestedTickets <= PERMIT_TICKETS)
            {
                reqTicketIds = new int[nRequestedTickets];
                // desde la posicion de nRequestedTicked + nRequestTicked
                int numPart = 4;
                // array with the request idtickets
                for (int i = 0; i < nRequestedTickets; i++)
                {
                    ticketTimer = new TimerThread();
                    LOG.debug(" posicion en mensaje " + numPart + " posicion-numero de ticket " + i);
                    reqTicketIds[i] = Integer.parseInt(parts[numPart]);
                    reservedTickets[i] = db.getBoletoById(reqTicketIds[i]);
                    numPart++;
                }
            }
            // Starting time for pay reserved tickets
            ticketTimer = new TimerThread(PAYMENT_TIMEOUT);
            ticketTimer.run();
            return true;
        }
        return false;

    }

    /**
     * 
     * @return true; all tickets were reserved
     * @throws ConversationException
     * @throws IOException
     */
    private boolean confirmReserveTickets()
            throws ConversationException, DbException, IOException
    {
        currentState = STATE.CONFIRM_RESERVE_TICKETS;
        msgOut.setLength(0);
        float cost = 0;
        sc.updateSessionTimer();

        if (sessionId > 0) // #ArmaTuMensaje !!(revisar esta wean en todos)
        {
            msgOut.append(currentState);
            msgOut.append(",");
            msgOut.append(sessionId);
            // todo los tickets fueron rerservados
            msgOut.append(",");
            for (int i = 0; i < reqTicketIds.length; i++)
            {
                if (db.consultTicketStatus(reqTicketIds[i]) == 1)
                { // ver si puede ser reservado ....y reservarlo :V
                    db.updateTicketStatus(reqTicketIds[i], 2);
                    cost = cost + db.getTicketById(reqTicketIds[i]);
                }
                else
                {
                    LOG.info(" No se puede obtener el estado del ticket");
                }
            }
            msgOut.append(cost);
            msgOut.append(",");
            msgOut.append(nRequestedTickets);
            // details of each ticket
            for (int i = 0; i < reqTicketIds.length; i++)
            {
                msgOut.append(",");
                msgOut.append(reqTicketIds[i]);
                // sacar por aca un arreglo de cada ticket con su detalle?, #nunca se usa
                // despues
            }
            dataOut.writeUTF(msgOut.toString());
            return true;
        }
        return false;
    }

    /**
     * this method tries to sing up a new client that does not has an account yet
     * 
     * @return true if the account was created i other case return false
     * @throws ConversationException
     * @throws SessionException
     * @throws IOException
     */
    private boolean singup() throws ConversationException, SessionException, DbException, IOException
    {
        currentState = STATE.SINGUP;
        String rawMsg = dataIn.readUTF();
        sc.updateSessionTimer();
        if (checkMsgIntegrity(rawMsg))
        {
            String[] parts = rawMsg.split(",");
            // int opcode = Integer.parseInt(parts[0]);
            sessionId = Integer.parseInt(parts[1]);
            usr = parts[2];
            pass = parts[3];
            email = parts[4];
            stateResidence = parts[5];
            if (db.singup(email, usr, pass))
            {
                LOG.info("usuario registrado: " + usr + " - " + email);
                return true;
            }
            LOG.error("no se puede registrar " + rawMsg);
        }
        return false;
    }

    /**
     * Create a new user for Boletazo
     * 
     * @return
     * @throws ConversationException
     * @throws IOException
     */
    private boolean singupStatus() throws ConversationException, DbException, IOException
    {
        currentState = STATE.SINGUP_STATUS;
        sc.updateSessionTimer();
        msgOut.setLength(0);
        msgOut.append(currentState.ordinal());
        msgOut.append(",");
        msgOut.append(sessionId);
        msgOut.append(",");
        if (db.toRegister(usr, pass, email, stateResidence))
        {
            msgOut.append(0);
            dataOut.writeUTF(msgOut.toString());

            return true;
        }
        msgOut.append(1);
        dataOut.writeUTF(msgOut.toString());
        LOG.info("usuario equivocado o falta registrar " + usr + " - " + email);
        return false;
    }

    /**
     * 
     * @return
     * @throws ConversationException
     * @throws SessionException
     * @throws IOException
     */
    private boolean loginCheck() throws ConversationException, SessionException, DbException,
            IOException
    {
        currentState = STATE.LOGIN_CHECK;
        // msgOut.setLength(0);
        String rawMsg = dataIn.readUTF();
        sc.updateSessionTimer();
        if (checkMsgIntegrity(rawMsg) && sessionId > 0)
        {
            String[] parts = rawMsg.split(",");
            checkConversationState(parts[0]);
            sessionId = Integer.parseInt(parts[1]);
            usr = parts[2];
            pass = parts[3];
            if (db.login(usr, pass)) { return true; }
        }
        LOG.debug(" algo esta mal escrito U_U " + rawMsg);
        return false;
    }

    /**
     * 
     * @return
     * @throws ConversationException
     * @throws IOException
     */
    private boolean loginStatus() throws ConversationException, IOException
    {
        currentState = STATE.LOGIN_STATUS;
        sc.updateSessionTimer();
        msgOut.setLength(0);
        msgOut.append(currentState);
        msgOut.append(",");
        msgOut.append(sessionId);
        msgOut.append(",");
        if (db.login(usr, pass))
        {
            msgOut.append("0");
            dataOut.writeUTF(msgOut.toString());
            return true;
        }
        msgOut.append("1");
        dataOut.writeUTF(msgOut.toString());
        return false;
    }

    /**
     * Client confirm that he wants the tickets
     * 
     * @return
     * @throws ConversationException
     * @throws IOException
     */
    private boolean postPaymentInfo() throws ConversationException, SessionException, IOException
    {
        currentState = STATE.POST_PAYMENT_INFO;
        String rawMsg = dataIn.readUTF();
        sc.updateSessionTimer();
        if (checkMsgIntegrity(rawMsg) && sessionId > 0)
        {
            String[] parts = rawMsg.split(",");
            checkConversationState(parts[0]);
            sessionId = Integer.parseInt(parts[1]);
            String numberCard = parts[2];
            if (numberCard.length() == 20)
            {
                String date = parts[3];
                String cvv = parts[4];
                String type = parts[5];
                if (type.equals("VISA") || type.equals("MASTERCARD"))
                {
                    LOG.info("Compra por :" + numberCard + "," + date + "," + cvv + "," + type);
                    return true;
                }
                LOG.error("tipo tarjeta incorrecta ");
            }
            LOG.error("longuitud tarjeta incorrecta ");
        }
        return false;
    }

    /**
     * The methos send a message with the sell successful of the number of the
     * tickets bought
     * 
     * @return return true if the tickets have already bought on the data base in
     *         other case return false
     * @throws ConversationException
     * @throws IOException
     */
    private boolean pucharaseCompleted()
            throws ConversationException, IOException
    {
        currentState = STATE.PUCHARASE_COMPLETED;
        sc.updateSessionTimer();
        msgOut.setLength(0);
        msgOut.append(currentState);
        msgOut.append(",");
        msgOut.append(sessionId);
        msgOut.append(",");
        msgOut.append(nRequestedTickets);
        msgOut.append(",");
        msgOut.append("el arreglo de los tickets....");
        // TODO completar condicion
        boolean success = true;
        for (int i = 0; i < reqTicketIds.length; i++)
        {
            if (db.updateTicketStatus(reqTicketIds[i], 3)) // && reservedTickets[i].)
            {
                msgOut.append("0");
            }
            else
            {
                success = false;
            }
        }
        msgOut.append("1");
        dataOut.writeUTF(msgOut.toString());
        return success;
    }

    /**
     * This method check the syntax of the message sent between the client and the
     * server
     * 
     * @param rawMsg
     * @return true if the message corresponds to the syntax of the process that is
     *         happening in other case return false
     * @throws ConversationException
     */
    private boolean checkMsgIntegrity(String rawMsg) throws ConversationException
    {
        boolean correct = false;
        String[] valuesIn = rawMsg.split(",");
        int nTokens = getTokenNumber();
        // int[] arrayLengths = getArrayLengthPositions();
        int valuesLen = valuesIn.length;
        TYPES[] types = getArgumentTypes();
        if (valuesLen < nTokens)
        {
            throw new ConversationException(ERROR.NOT_ENOUGH_ARGUMENTS);
        }
        else
        {
            try
            {
                switch (currentState)
                {
                case C_START_SESSION:
                case GET_EVENT_LIST:
                case GET_EVENT_INFO:
                case GET_AVAILABLE_SEATS:
                case SINGUP:
                case LOGIN_CHECK:
                case POST_PAYMENT_INFO:
                    for (int i = 0; i < nTokens; i++)
                    {
                        correct = correct && checkArgument(valuesIn[i], types[i]);
                    }
                    break;
                case REQUEST_RESERVE_TICKETS:
                    int contFirst = 0;
                    for (; contFirst < nTokens; contFirst++)
                    {
                        if (types[contFirst] == TYPES.ARRAY)
                            break;
                        correct = correct && checkArgument(valuesIn[contFirst], types[contFirst]);
                    }
                    int arrayLength = Integer.parseInt(valuesIn[contFirst]);
                    for (int contSecond = 0; contSecond < arrayLength; contSecond++)
                    {
                        correct = correct && checkArgument(valuesIn[contFirst], TYPES.INT);
                        contFirst++;
                        correct = correct && checkArgument(valuesIn[contFirst], TYPES.INT);
                        contFirst++;
                    }
                    if (contFirst < valuesLen) { throw new ConversationException(ERROR.TO_MANY_ARGUMENTS); }
                    break;
                default:
                    throw new ConversationException(ERROR.INCORRECT_CONVERSATION_STATE);
                }
            }
            catch (NumberFormatException e)
            {
                throw new ConversationException(ERROR.INCORRECT_NUMBER_FORMAT);
            }
        }
        return correct;
    }

    // TODO: terminar la definicion del metodo

    /**
     * this method makes a parsing or an equal actions depending to the type of
     * request
     * 
     * @param token
     * @param types
     * @return return true if the action has completed in other case return false
     * @throws NumberFormatException
     */
    private boolean checkArgument(String token, TYPES types) throws NumberFormatException
    {
        switch (types)
        {
        case ARRAY:
            return false;
        case FLOAT:
            Float.parseFloat(token);
            return true;
        case INT:
            Integer.parseInt(token);
            return true;
        case NULL:
            return token.equals("null");
        case STRING:
            return token.length() > 0;
        default:
            return false;
        }
    }

    /**
     * 
     * @return the next number that corresponds to the variable name
     */
    private int getTokenNumber()
    {
        switch (currentState)
        {
        case C_START_SESSION:
            return 2;
        case GET_EVENT_LIST:
            return 8;
        case GET_EVENT_INFO:
            return 3;
        case GET_AVAILABLE_SEATS:
            return 3;
        case REQUEST_RESERVE_TICKETS:
            return 5;
        case SINGUP:
            return 5;
        case LOGIN_CHECK:
            return 4;
        case POST_PAYMENT_INFO:
            return 7;
        default:
            return -1;
        }
    }

    // TODO: evaluar la utilidad real de este metodo
    // tal vez se pueden usar constantes en su lugar
    /**
     * 
     * @return
     * @throws ConversationException
     */

    /*
     * private int[] getArrayLengthPositions() throws ConversationException { switch
     * (currentState) { case C_START_SESSION: return new int[0]; case
     * GET_EVENT_LIST: return new int[0]; case GET_EVENT_INFO: return new int[0];
     * case GET_AVAILABLE_SEATS: return new int[0]; case REQUEST_RESERVE_TICKETS:
     * return new int[] {3}; case SINGUP: return new int[0]; case LOGIN_CHECK:
     * return new int[0]; case POST_PAYMENT_INFO: return new int[0]; default: throw
     * new ConversationException(ERROR.INCORRECT_CONVERSATION_STATE); } }
     */

    /**
     * 
     * @return
     * @throws ConversationException
     */
    @SuppressWarnings("incomplete-switch")

    private TYPES[] getArgumentTypes() throws ConversationException
    {
        TYPES[] type = new TYPES[getTokenNumber()];
        switch (currentState)
        {
        case C_START_SESSION:
            type[0] = TYPES.INT;
            type[1] = TYPES.NULL;
            break;
        case GET_EVENT_LIST:
            type[0] = TYPES.INT;
            type[1] = TYPES.INT;
            type[2] = TYPES.STRING;
            type[3] = TYPES.STRING;
            type[4] = TYPES.STRING;
            type[5] = TYPES.INT;
            type[6] = TYPES.FLOAT;
            type[7] = TYPES.STRING;
            break;
        case GET_EVENT_INFO:
        case GET_AVAILABLE_SEATS:
            type[0] = TYPES.INT;
            type[1] = TYPES.INT;
            type[2] = TYPES.INT;
            break;
        case REQUEST_RESERVE_TICKETS:
            type[0] = TYPES.INT;
            type[1] = TYPES.INT;
            type[2] = TYPES.INT;
            type[3] = TYPES.INT;
            type[4] = TYPES.ARRAY;
            break;
        case SINGUP:
            type[0] = TYPES.INT;
            type[1] = TYPES.STRING;
            type[2] = TYPES.STRING;
            type[3] = TYPES.STRING;
            type[4] = TYPES.STRING;
            break;
        case LOGIN_CHECK:
            type[0] = TYPES.INT;
            type[1] = TYPES.INT;
            type[2] = TYPES.STRING;
            type[3] = TYPES.STRING;
            break;
        case POST_PAYMENT_INFO:
            type[0] = TYPES.INT;
            type[1] = TYPES.INT;
            type[2] = TYPES.INT;
            type[3] = TYPES.STRING;
            type[4] = TYPES.STRING;
            type[5] = TYPES.STRING;
            type[6] = TYPES.STRING;
            break;
        default:
            throw new ConversationException(ERROR.INCORRECT_CONVERSATION_STATE);
        }
        return type;
    }

    /**
     * Translate the String operation number into a STATE Enumeric
     * 
     * @param state
     * @return enum STATE
     */
    public STATE StrToState(String state)
    {
        switch (state)
        {
        case "0":
            return STATE.C_START_SESSION;
        case "1":
            return STATE.S_START_SESSION;
        case "2":
            return STATE.GET_EVENT_LIST;
        case "3":
            return STATE.POST_EVENT_LIST;
        case "4":
            return STATE.GET_EVENT_INFO;
        case "5":
            return STATE.POST_EVENT_INFO;
        case "6":
            return STATE.GET_AVAILABLE_SEATS;
        case "7":
            return STATE.POST_AVAILABLE_SEATS;
        case "8":
            return STATE.REQUEST_RESERVE_TICKETS;
        case "9":
            return STATE.CONFIRM_RESERVE_TICKETS;
        case "10":
            return STATE.SINGUP;
        case "11":
            return STATE.SINGUP_STATUS;
        case "12":
            return STATE.LOGIN_CHECK;
        case "13":
            return STATE.LOGIN_STATUS;
        case "14":
            return STATE.POST_PAYMENT_INFO;
        case "15":
            return STATE.PUCHARASE_COMPLETED;
        default:
            return currentState;
        }
    }

    /**
     * 
     * @param rawConversationState
     * @throws ConversationException
     */
    private void checkConversationState(String rawConversationState) throws ConversationException
    {
        if (Integer.parseInt(rawConversationState) != currentState.ordinal())
            throw new ConversationException(ERROR.INCORRECT_CONVERSATION_STATE);
    }

    /**
     * 
     * @param rawSessionId
     * @throws SessionException
     */
    private void checkSessionId(String rawSessionId) throws SessionException
    {
        if (Integer.parseInt(rawSessionId) != sessionId)
            throw new SessionException(SessionException.ERROR.INVALID_SESSION_ID);
    }
}