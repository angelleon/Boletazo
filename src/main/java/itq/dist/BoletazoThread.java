package itq.dist;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
//import java.io.InputStream;
//import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import itq.dist.ConversationException.ERROR;

public class BoletazoThread extends Thread
{

    // TODO liberar boletos si el cliente manda una solicitud de reserva con 0 ids
    private static final Logger LOG = LogManager.getLogger(BoletazoThread.class);
    private static final int MAX_MSG_LENGTH = BoletazoConf.MAX_MSG_LENGTH;
    public static final int PERMITED_TICKETS = BoletazoConf.PERMITED_TICKETS;
    private static final int EMAIL_PORT = BoletazoConf.EMAIL_PORT;
    private static final String EMAIL_IP = BoletazoConf.EMAIL_IP;

    private Socket socket;
    private Db db;
    private SessionControl sessionControl;
    private SessionControl anonymousSc;

    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private StringBuilder msgOut;

    String rawMsg;
    String[] rawTokensIn;
    private int sessionId;

    private int nRequestedTickets;
    private int reqIdEvent;
    private int[] reqTicketIds;

    private String usr;
    private String pass;
    private String email;
    private String numberCard;
    private int idusr;

    private String stateResidence;

    private EventInfo[] searchResult;
    private EventInfo selectedEvent;
    private Ticket[] reservedTickets;
    private int[] soldTickets;

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
    public static enum STATE {
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

    private STATE targetConversationState;
    private STATE currentConversationState;

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
    BoletazoThread(Socket socket, Db db, SessionControl sessionControl, SessionControl anonymousSc)
    {
        this.socket = socket;
        this.db = db;
        this.sessionControl = sessionControl;
        this.anonymousSc = anonymousSc;

        this.msgOut = new StringBuilder(MAX_MSG_LENGTH);
        this.targetConversationState = STATE.C_START_SESSION;
        this.currentConversationState = STATE.C_START_SESSION;
        try
        {
            dataIn = new DataInputStream(socket.getInputStream());
            // dataOut = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException e)
        {
            LOG.error("IOException " + e.getMessage());
        }
    }

    /**
     * This method create the sequence of the conversation between the server-client
     * 
     * @throws IOException
     */
    public STATE targetState() throws ConversationException, IOException
    {
        targetConversationState = parseRawState(rawTokensIn[0]);
        LOG.debug("Getting target state [" + targetConversationState + "] from raw message [" + rawMsg + "]");
        return targetConversationState;
    }

    @Override
    public void run()
    {
        try
        {
            try
            {
                readRawMsg();
                dataOut = new DataOutputStream(socket.getOutputStream());
                targetState();
                checkMsgIntegrity();
                switch (targetConversationState)
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
                    throw new ConversationException();
                }
            }
            catch (ConversationException e)
            {
                LOG.debug("ConversationException " + e.getMessage());
                e.printStackTrace();
            }
            catch (SessionException e)
            {
                LOG.debug("SessionException " + e.getMessage());
                e.printStackTrace();
            }
            catch (DbException e)
            {
                LOG.debug("DbException " + e.getMessage());
                e.printStackTrace();
            }
            finally
            {
                socket.close();
            }
        }
        catch (IOException e)
        {
            LOG.debug("IOException " + e.getMessage());
            e.printStackTrace();
        }
    }

    // TODO: implementar un metodo por cada constante del enum STATE
    // TODO: considerar lanzar excepciones para manejar todos los errores de la
    // conversacion dentro de un bloque catch

    // Table .....if there is active session, table.. ok no..

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
        currentConversationState = STATE.C_START_SESSION;
        return true;
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
        currentConversationState = STATE.S_START_SESSION;
        msgOut.setLength(0);
        sessionId = anonymousSc.getNewSessionId();
        msgOut.append(STATE.S_START_SESSION.ordinal());
        msgOut.append(",");
        msgOut.append(sessionId);
        LOG.debug(msgOut.toString());
        dataOut.writeUTF(msgOut.toString());
        return true;
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
        currentConversationState = STATE.GET_EVENT_LIST;
        // String rawMsg = dataIn.readUTF();
        // String[] rawTokensIn = rawMsg.split(",");
        checkSessionId(rawTokensIn[1]);
        sessionId = Integer.parseInt(rawTokensIn[1]);
        resetSession(sessionId);
        String eventName = rawTokensIn[2];
        String venueName = rawTokensIn[3];
        String rawDate = rawTokensIn[4];
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate eventDate;
        try
        {
            eventDate = LocalDate.parse(rawDate, format);
        }
        catch (DateTimeParseException e)
        {
            LOG.debug(rawDate);
            LOG.debug(e.getMessage());
            throw new ConversationException(ERROR.INCORRECT_DATE_FORMAT, currentConversationState);
        }
        int hour = Integer.parseInt(rawTokensIn[5]);
        if (hour < 0 || hour > 23)
            throw new ConversationException(ERROR.VALUE_OUT_OF_RANGE, currentConversationState);
        float cost = Float.parseFloat(rawTokensIn[6]);
        String sectionName = rawTokensIn[7];
        searchResult = db.searchEvents(eventName, venueName, eventDate, hour, cost, sectionName);
        return true;
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
        currentConversationState = STATE.POST_EVENT_LIST;
        msgOut.setLength(0);
        msgOut.append(STATE.POST_EVENT_LIST.ordinal());
        msgOut.append(",");
        msgOut.append(sessionId);
        msgOut.append(",");
        msgOut.append(searchResult.length);
        msgOut.append(",");
        LOG.debug(searchResult);
        for (EventInfo ev : searchResult)
        {
            LOG.debug(ev.toString());
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
        currentConversationState = STATE.GET_EVENT_INFO;
        checkSessionId(rawTokensIn[1]);
        sessionId = Integer.parseInt(rawTokensIn[1]);
        resetSession(sessionId);
        String[] rawTokensIn = rawMsg.split(",");
        reqIdEvent = Integer.parseInt(rawTokensIn[2]);
        selectedEvent = db.getEventInfoByIdEvent(reqIdEvent);
        return true;
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
        currentConversationState = STATE.POST_EVENT_INFO;
        msgOut.setLength(0);
        msgOut.append(targetConversationState.ordinal());
        msgOut.append(",");
        msgOut.append(sessionId);
        msgOut.append(",");
        msgOut.append(selectedEvent.getIdEvent());
        msgOut.append(",");
        msgOut.append(selectedEvent.getName());
        msgOut.append(",");
        // LocalDate[] dates = selectedEvent.getDates();
        msgOut.append("1,");
        // for (LocalDate d : dates)
        // {
        // msgOut.append(",");
        // msgOut.append(d.getDayOfMonth());
        // msgOut.append("-");
        // msgOut.append(d.getMonthValue());
        // msgOut.append("-");
        // msgOut.append(d.getYear());
        // }
        LocalDate date = selectedEvent.getDate();
        msgOut.append(date.getDayOfMonth());
        msgOut.append("-");
        msgOut.append(date.getMonthValue());
        msgOut.append("-");
        msgOut.append(date.getYear());
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
        currentConversationState = STATE.GET_AVAILABLE_SEATS;
        checkSessionId(rawTokensIn[1]);
        sessionId = Integer.parseInt(rawTokensIn[1]);
        resetSession(sessionId);
        reqIdEvent = Integer.parseInt(rawTokensIn[2]);
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
        currentConversationState = STATE.POST_AVAILABLE_SEATS;
        msgOut.setLength(0);
        msgOut.append(targetConversationState.ordinal());
        dataOut.writeUTF(msgOut.toString());
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
        currentConversationState = STATE.REQUEST_RESERVE_TICKETS;
        checkSessionId(rawTokensIn[1]);
        sessionId = Integer.parseInt(rawTokensIn[1]);
        resetSession(sessionId);
        // String rawMsg = dataIn.readUTF();
        // String[] parts = rawMsg.split(",");
        // int opcode = Integer.parseInt(parts[0]);
        sessionId = Integer.parseInt(rawTokensIn[1]);
        reqIdEvent = Integer.parseInt(rawTokensIn[2]);
        nRequestedTickets = Integer.parseInt(rawTokensIn[3]);
        // see if the nRequestedTickets is equal or less than permit_Ticket
        if (nRequestedTickets <= PERMITED_TICKETS)
        {

            reqTicketIds = new int[nRequestedTickets];
            // desde la posicion de nRequestedTicked + nRequestTicked
            int numPart = 4;
            // array with the request idtickets
            for (int i = 0; i < nRequestedTickets; i++)
            {
                LOG.debug(" posicion en mensaje " + numPart + " posicion-numero de ticket " + i);
                reqTicketIds[i] = Integer.parseInt(rawTokensIn[numPart]);
                numPart++;
            }
            if (db.consultTicketStatus(reqTicketIds))
            {
                reservedTickets = new Ticket[nRequestedTickets];
                for (int i = 0; i < reqTicketIds.length; i++)
                {
                    reservedTickets[i] = db.getAvailableTicketById(reqTicketIds[i]);
                    LOG.debug(reservedTickets[i]);
                }
                anonymousSc.setReservedTickets(reservedTickets, sessionId);
            }
        }
        // Starting time for pay reserved tickets
        return true;
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
        currentConversationState = STATE.CONFIRM_RESERVE_TICKETS;
        msgOut.setLength(0);
        float cost = 0;

        msgOut.append(targetConversationState);
        msgOut.append(",");
        msgOut.append(sessionId);
        // todo los tickets fueron rerservados
        msgOut.append(",");
        // TODO: Arreglar esto

        if (db.consultTicketStatus(reqTicketIds))
        {
            for (int i = 0; i < reqTicketIds.length; i++)
            {
                db.updateTicketStatus(reqTicketIds[i], "reservado");
                cost += db.getTicketCostById(reqTicketIds[i]);
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
            anonymousSc.setReservedTickets(reservedTickets, sessionId);
            // sessionControl.setReservedTickets(reservedTickets, sessionId);
            dataOut.writeUTF(msgOut.toString());
            return true;
        }
        else
        {
            LOG.info("Requested tickets are not available");
            msgOut.append(0);
            msgOut.append("," + 0 + ",null");
            // details of each ticket
            /*
             * for (int i = 0; i < reqTicketIds.length; i++) { msgOut.append(",");
             * msgOut.append(reqTicketIds[i]); // sacar por aca un arreglo de cada ticket
             * con su detalle?, #nunca se usa // despues }
             */
            // anonymousSc.setReservedTickets(reservedTickets, sessionId);
            // sessionControl.setReservedTickets(reservedTickets, sessionId);
            dataOut.writeUTF(msgOut.toString());
            return false;
        }

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
        currentConversationState = STATE.SINGUP;
        checkSessionId(rawTokensIn[1]);
        sessionId = Integer.parseInt(rawTokensIn[1]);
        resetSession(sessionId);
        String rawMsg = dataIn.readUTF();
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
            idusr = db.getIdUser(email);
            return true;
        }
        LOG.error("no se puede registrar " + rawMsg);
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
        currentConversationState = STATE.SINGUP_STATUS;
        msgOut.setLength(0);
        msgOut.append(targetConversationState.ordinal());
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
        currentConversationState = STATE.LOGIN_CHECK;
        checkSessionId(rawTokensIn[1]);
        sessionId = Integer.parseInt(rawTokensIn[1]);
        resetSession(sessionId);
        String[] parts = rawMsg.split(",");
        sessionId = Integer.parseInt(parts[1]);
        usr = parts[2];
        pass = parts[3];
        if (db.login(usr, pass))
        {
            email = db.getEmailByUsername(usr);
            return true;
        }
        LOG.debug(" something is written wrong U_U " + rawMsg);
        return false;
    }

    /**
     * 
     * @return
     * @throws ConversationException
     * @throws IOException
     */
    private boolean loginStatus() throws ConversationException, DbException, SessionException, IOException
    {
        currentConversationState = STATE.LOGIN_STATUS;
        msgOut.setLength(0);
        msgOut.append(targetConversationState);
        msgOut.append(",");
        if (db.login(usr, pass))
        {
            int oldSessionId = sessionId;
            anonymousSc.releaseSessionId(oldSessionId);
            sessionId = sessionControl.getNewSessionId();
            msgOut.append(sessionId);
            msgOut.append(",");
            msgOut.append("0");
            dataOut.writeUTF(msgOut.toString());
            LOG.debug(msgOut.toString());
            sessionControl.setReservedTickets(anonymousSc.getReservedTickets(oldSessionId), sessionId);
            sessionControl.setEmail(email, sessionId);
            sessionControl.setUser(usr, sessionId);
            return true;
        }
        msgOut.append("1");
        LOG.debug(msgOut.toString());
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
        currentConversationState = STATE.POST_PAYMENT_INFO;
        checkSessionId(rawTokensIn[1]);
        sessionId = Integer.parseInt(rawTokensIn[1]);
        resetSession(sessionId);
        /*
         * String rawMsg = dataIn.readUTF(); String[] parts = rawMsg.split(",");
         * sessionId = Integer.parseInt(parts[1]);
         */
        numberCard = rawTokensIn[2];

        LOG.debug(numberCard + " " + numberCard.length());
        if (numberCard.length() == 19)
        {
            String date = rawTokensIn[3];
            String cvv = rawTokensIn[4];
            String type = rawTokensIn[5];
            if (type.equals("VISA") || type.equals("MASTERCARD"))
            {
                LOG.info("Compra por :" + numberCard + "," + date + "," + cvv + "," + type);
                return true;
            }
            LOG.error("tipo tarjeta incorrecta ");
        }
        LOG.error("longuitud tarjeta incorrecta ");
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
            throws ConversationException, DbException, IOException
    {
        currentConversationState = STATE.PUCHARASE_COMPLETED;
        msgOut.setLength(0);
        msgOut.append(targetConversationState);
        msgOut.append(",");
        msgOut.append(sessionId);
        msgOut.append(",");
        msgOut.append(nRequestedTickets);
        msgOut.append(",");
        msgOut.append("el arreglo de los tickets....");
        // TODO completar condicion
        boolean success = true;
        emailSender(); // It is responsible for sending the request to the mail server
        if (reservedTickets != null
                && sessionControl.confirmTickets(sessionId))
        {
            reservedTickets = sessionControl.getReservedTickets(sessionId);
            LOG.debug(reservedTickets);
            soldTickets = new int[reservedTickets.length];
            LOG.debug("" + reservedTickets + " " + reservedTickets.length + " " + sessionId);
            for (int i = 0; i < reservedTickets.length; i++)
            {
                LOG.debug(i);
                LOG.debug(reservedTickets[i]);
                if (db.updateTicketStatus(reservedTickets[i].getIdTicket(), "vendido")) // && reservedTickets[i].)
                {
                    soldTickets[i] = reservedTickets[i].getIdTicket();
                    msgOut.append("" + soldTickets[i]);
                }
                else
                {
                    success = false;
                }
            }
            success = true;
            msgOut.append("1");
            dataOut.writeUTF(msgOut.toString());
            db.updateTicketSold(soldTickets, numberCard, idusr);
            emailSender(); // It is responsible for sending the request to the mail server
        }
        else
        {
            success = false;
            msgOut.append("null");
            dataOut.writeUTF(msgOut.toString());
        }
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
    private boolean checkMsgIntegrity() throws ConversationException
    {
        boolean correct = false;
        // String[] rawTokensIn = rawMsg.split(",");
        int nTokens = getTokenNumber();
        // int[] arrayLengths = getArrayLengthPositions();
        int valuesLen = rawTokensIn.length;
        TYPES[] types = getArgumentTypes();
        if (valuesLen < nTokens)
        {
            throw new ConversationException(ERROR.NOT_ENOUGH_ARGUMENTS, currentConversationState);
        }
        else
        {
            try
            {
                switch (currentConversationState)
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
                        correct = correct && checkArgument(rawTokensIn[i], types[i]);
                    }
                    break;
                case REQUEST_RESERVE_TICKETS:
                    int contFirst = 0;
                    for (; contFirst < nTokens; contFirst++)
                    {
                        if (types[contFirst] == TYPES.ARRAY)
                        {
                            LOG.debug("breaking loop");
                            break;
                        }
                        correct = correct && checkArgument(rawTokensIn[contFirst], types[contFirst]);
                    }
                    int arrayLength = Integer.parseInt(rawTokensIn[contFirst]);
                    for (int contSecond = 0; contSecond < arrayLength; contSecond++)
                    {
                        correct = correct && checkArgument(rawTokensIn[contFirst], TYPES.INT);
                        contFirst++;
                        correct = correct && checkArgument(rawTokensIn[contFirst], TYPES.INT);
                        contFirst++;
                    }
                    if (contFirst < valuesLen)
                    { throw new ConversationException(ERROR.NOT_ENOUGH_ARGUMENTS,
                            currentConversationState); }
                    break;
                default:
                    throw new ConversationException(ERROR.INCORRECT_CONVERSATION_STATE, currentConversationState);
                }
            }
            catch (NumberFormatException e)
            {
                throw new ConversationException(ERROR.INCORRECT_NUMBER_FORMAT, currentConversationState);
            }
        }
        return correct;
    }

    // TODO: terminar la definicion del metodo

    /**
     * this method check and ensure depending to the type of request
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
     * number of tokens
     * 
     * @return the next number that corresponds to the variable name
     */
    private int getTokenNumber()
    {
        switch (currentConversationState)
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
     * retorna la posicion donde se encuentra la cantidad de tokens
     * 
     * @return an Array
     * @throws ConversationException
     */

    /*
     * private int[] getArrayLengthPositions() throws ConversationException { switch
     * (targetClientState) { case C_START_SESSION: return new int[0]; case
     * GET_EVENT_LIST: return new int[0]; case GET_EVENT_INFO: return new int[0];
     * case GET_AVAILABLE_SEATS: return new int[0]; case REQUEST_RESERVE_TICKETS:
     * return new int[] {3}; case SINGUP: return new int[0]; case LOGIN_CHECK:
     * return new int[0]; case POST_PAYMENT_INFO: return new int[0]; default: throw
     * new ConversationException(ERROR.INCORRECT_CONVERSATION_STATE); } }
     */

    /**
     * indica que tipo de dato se espera en cada posicion del mensaje
     * 
     * @return
     * @throws ConversationException
     */
    @SuppressWarnings("incomplete-switch")
    private TYPES[] getArgumentTypes() throws ConversationException
    {
        TYPES[] type = new TYPES[getTokenNumber()];
        switch (currentConversationState)
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
            throw new ConversationException(ERROR.INCORRECT_CONVERSATION_STATE, currentConversationState);
        }
        return type;
    }

    /**
     * Translate the String operation number into a STATE Enumeric
     * 
     * @param rawState
     * @return enum STATE
     */
    public STATE parseRawState(String rawState) throws ConversationException
    {
        int state = Integer.parseInt(rawState);
        switch (state)
        {
        case 0:
            return STATE.C_START_SESSION;
        case 1:
            return STATE.S_START_SESSION;
        case 2:
            return STATE.GET_EVENT_LIST;
        case 3:
            return STATE.POST_EVENT_LIST;
        case 4:
            return STATE.GET_EVENT_INFO;
        case 5:
            return STATE.POST_EVENT_INFO;
        case 6:
            return STATE.GET_AVAILABLE_SEATS;
        case 7:
            return STATE.POST_AVAILABLE_SEATS;
        case 8:
            return STATE.REQUEST_RESERVE_TICKETS;
        case 9:
            return STATE.CONFIRM_RESERVE_TICKETS;
        case 10:
            return STATE.SINGUP;
        case 11:
            return STATE.SINGUP_STATUS;
        case 12:
            return STATE.LOGIN_CHECK;
        case 13:
            return STATE.LOGIN_STATUS;
        case 14:
            return STATE.POST_PAYMENT_INFO;
        case 15:
            return STATE.PUCHARASE_COMPLETED;
        default:
            throw new ConversationException(ConversationException.ERROR.INCORRECT_CONVERSATION_STATE,
                    currentConversationState);
        }
    }

    /**
     * @param rawConversationState
     * @throws ConversationException
     */
    // private void checkConversationState(String rawConversationState) throws
    // ConversationException
    // {
    // if (Integer.parseInt(rawConversationState) != targetClientState.ordinal())
    // throw new ConversationException(ERROR.INCORRECT_CONVERSATION_STATE);
    // }

    /**
     * check if the client had sent the correct idSession
     * 
     * @param rawSessionId
     * @throws SessionException
     */
    private boolean checkSessionId(String rawSessionId) throws SessionException
    {
        int sessionId = Integer.parseInt(rawSessionId);
        switch (currentConversationState)
        {
        case GET_EVENT_LIST:
        case GET_EVENT_INFO:
        case GET_AVAILABLE_SEATS:
        case CONFIRM_RESERVE_TICKETS:
        case REQUEST_RESERVE_TICKETS:
        case SINGUP:
        case LOGIN_CHECK:
            if (!anonymousSc.isValid(sessionId))
                throw new SessionException(SessionException.ERROR.INVALID_SESSION_ID);
            // return true;
            return true;
        case POST_PAYMENT_INFO:
            if (!sessionControl.isValid(sessionId))
                throw new SessionException(SessionException.ERROR.INVALID_SESSION_ID);
            return true;
        case C_START_SESSION:
        case LOGIN_STATUS:
        case POST_AVAILABLE_SEATS:
        case POST_EVENT_INFO:
        case POST_EVENT_LIST:
        case PUCHARASE_COMPLETED:
        case SINGUP_STATUS:
        case S_START_SESSION:
            return false;
        default:
            return false;
        }
    }

    private void resetSession(int sessionId)
    {
        switch (currentConversationState)
        {
        case GET_EVENT_LIST:
        case GET_EVENT_INFO:
        case GET_AVAILABLE_SEATS:
        case CONFIRM_RESERVE_TICKETS:
        case REQUEST_RESERVE_TICKETS:
        case SINGUP:
        case LOGIN_CHECK:
            anonymousSc.resetSessionTimer(sessionId);
            break;
        case POST_PAYMENT_INFO:
            sessionControl.resetSessionTimer(sessionId);
            break;
        case C_START_SESSION:
        case LOGIN_STATUS:
        case POST_AVAILABLE_SEATS:
        case POST_EVENT_INFO:
        case POST_EVENT_LIST:
        case PUCHARASE_COMPLETED:
        case SINGUP_STATUS:
        case S_START_SESSION:
            break;
        default:
            break;
        }
    }

    private void readRawMsg() throws IOException
    {
        rawMsg = dataIn.readUTF();
        LOG.debug("Read [" + rawMsg + "] from client [" + socket.getInetAddress() + "]");
        rawTokensIn = rawMsg.split(",");
        // dataOut = new DataOutputStream(socket.getOutputStream());
    }

    private void emailSender() throws UnknownHostException, IOException, DbException
    {
        LOG.debug("enviando email");
        Socket emailSocket = null;
        reservedTickets = sessionControl.getReservedTickets(sessionId);
        EventInfo ev = db.getEventInfoByIdEvent(reservedTickets[0].getIdEvent());
        LOG.debug("tickets " + reservedTickets);
        for (int i = 0; i < reservedTickets.length; i++)
        {
            emailSocket = new Socket(EMAIL_IP, EMAIL_PORT);
            OutputStream outStream = emailSocket.getOutputStream();
            DataOutputStream flowOut = new DataOutputStream(outStream);
            email = sessionControl.getEmail(sessionId);
            LOG.debug("Enviando correeo a " + email);
            usr = sessionControl.getUser(sessionId);
            String msg = email + ","
                    + usr + ","
                    // + selectedEvent.getName() + ","
                    + ev.getName() + ","
                    + reservedTickets[i].getIdTicket() + ","
                    // + selectedEvent.getDate() + ","
                    + ev.getDate() + ","
                    + "0";
            LOG.debug(msg);
            flowOut.writeUTF(msg);
        }
        emailSocket.close();
    }
}
