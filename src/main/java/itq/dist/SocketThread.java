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
    private int[] tickets_Array;
    // private Ticketinfo[]; // i needed one of these!!!!!!
    private String usr;
    private String pass;
    private String email;
    private String stateResidence;
    private Event[] searchResult;
    private EventInfo selectedEvent;
    private TimerThread timer;

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

    private enum TYPES {
        NULL,
        INT,
        FLOAT,
        STRING,
        ARRAY
    }

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

    @Override
    public void run()
    {
        try
        {
            cStartSession();
            sStartSession();
            getEventList();
            postEventList();
            getEventInfo();
            postEventInfo();
            getAvailableSeats();
            postAvailableSeats();
            requestReserveTickets();
            confirmReserveTickets();
            if (currentState == STATE.SINGUP)
            {
                singup();
                singupStatus();
            }
            loginCheck();
            loginStatus();
            postPaymentInfo();
            pucharaseCompleted();
            socket.close();
        }
        catch (ConversationException e)
        {

        }
        catch (SessionException e)
        {

        }
        catch (IOException e)
        {

        }
    }

    // ToDo: implementar un metodo por cada constante del enum STATE
    // ToDo: considerar lanzar excepciones para manejar todos los errores de la
    // conversacion dentro de un bloque catch

    // La tablita .....si hay sesion tabla.. ok no..
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

    private boolean sStartSession() throws ConversationException, IOException
    {
        currentState = STATE.S_START_SESSION;
        msgOut.setLength(0);
        sessionId = sc.getNewSessionId();
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

    private boolean getEventList() throws ConversationException, SessionException, IOException
    {
        currentState = STATE.GET_EVENT_LIST;
        String rawMsg = dataIn.readUTF();
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
            searchResult = db.search(eventName, venueName, eventDate, hour, cost, sectionName);
            return true;
        }
        return false;
    }

    private boolean postEventList() throws ConversationException, IOException
    {
        currentState = STATE.POST_EVENT_LIST;
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

    private boolean getEventInfo() throws ConversationException, SessionException, IOException
    {
        currentState = STATE.GET_EVENT_INFO;
        String rawMsg = dataIn.readUTF();
        if (checkMsgIntegrity(rawMsg))
        {
            String[] valuesIn = rawMsg.split(",");
            checkConversationState(valuesIn[0]);
            checkSessionId(valuesIn[1]);
            int idEvent = Integer.parseInt(valuesIn[2]);
            selectedEvent = db.getEventInfo(idEvent);
        }
        return false;
    }

    private boolean postEventInfo() throws ConversationException, SessionException, IOException
    {
        currentState = STATE.POST_EVENT_INFO;
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

    private boolean getAvailableSeats()
            throws ConversationException, SessionException, IOException
    {
        currentState = STATE.GET_AVAILABLE_SEATS;
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

    private boolean postAvailableSeats() throws ConversationException, IOException
    {
        currentState = STATE.POST_AVAILABLE_SEATS;
        msgOut.setLength(0);
        msgOut.append(currentState.ordinal());
        return false;
    }

    // opCode = 8
    private boolean requestReserveTickets()
            throws ConversationException, SessionException, IOException
    {
        currentState = STATE.REQUEST_RESERVE_TICKETS;
        String rawMsg = dataIn.readUTF();

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
                int[] ticketsArray = new int[nRequestedTickets];
                // desde la posicion de nRequestedTicked + nRequestTicked
                int numPart = 4;

                // array with the request idtickets
                TimerThread wait = null;
                for (int i = 0; i < nRequestedTickets; i++)
                {
                    wait = new TimerThread();
                    LOG.debug(" posicion en mensaje " + numPart + " posicion-numero de ticket " + i);
                    tickets_Array[i] = Integer.parseInt(parts[numPart]);
                    reserv[i] = db.getBoletoById(tickets_Array[i]);
                    reserv[i].setTimer(wait);// comenzar el tiempo de apartado
                    numPart++;
                }
            }
            LOG.error("Los tickets solicitados exceden el limite permitido ");
            return true;
        }
        return false;
    }

    /**
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! requiere el tiempo de espera y no
     * entendi como hacer ese pedo (olvidalo)
     * 
     * @return true; all tickets were reserved
     * @throws ConversationException
     * @throws IOException
     */
    private boolean confirmReserveTickets()
            throws ConversationException, IOException
    {
        currentState = STATE.CONFIRM_RESERVE_TICKETS;
        msgOut.setLength(0);
        float cost = 0;

        if (sessionId > 0) // #ArmaTuMensaje !!(revisar esta wean en todos)
        {
            msgOut.append(currentState);
            msgOut.append(",");
            msgOut.append(sessionId);
            // todo los tickets fueron rerservados
            msgOut.append(",");
            for (int i = 0; i < tickets_Array.length; i++)
            {
                if (db.consultStatusTicket(tickets_Array[i]) == 1)
                { // ver si puede ser reservado ....y reservarlo :V
                    db.update_Ticket_Status(tickets_Array[i], 2);
                    cost = cost + db.getTicketById(tickets_Array[i]);
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
            for (int i = 0; i < tickets_Array.length; i++)
            {
                msgOut.append(",");
                msgOut.append(tickets_Array[i]);
                // sacar por aca un arreglo de cada ticket con su detalle?, #nunca se usa
                // despues
            }
            dataOut.writeUTF(msgOut.toString());
            return true;
        }
        return false;
    }

    private boolean singup() throws ConversationException, SessionException, IOException
    {
        currentState = STATE.SINGUP;
        String rawMsg = dataIn.readUTF();
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
     */
    private boolean singupStatus() throws ConversationException, IOException
    {
        currentState = STATE.SINGUP_STATUS;
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

    private boolean loginCheck() throws ConversationException, SessionException, IOException
    {
        currentState = STATE.LOGIN_CHECK;
        // msgOut.setLength(0);
        String rawMsg = dataIn.readUTF();

        if (checkMsgIntegrity(rawMsg) && sessionId > 0)
        {
            String[] parts = rawMsg.split(",");
            int opCode = Integer.parseInt(parts[0]);
            sessionId = Integer.parseInt(parts[1]);
            usr = parts[2];
            pass = parts[3];
            if (db.login(usr, pass)) { return true; }
        }
        LOG.debug(" algo esta mal escrito U_U " + rawMsg);
        return false;
    }

    private boolean loginStatus() throws ConversationException, IOException
    {
        currentState = STATE.LOGIN_STATUS;
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

        if (checkMsgIntegrity(rawMsg) && sessionId > 0)
        {
            String[] parts = rawMsg.split(",");
            int opcode = Integer.parseInt(parts[0]);
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

    private boolean pucharaseCompleted()
            throws ConversationException, IOException
    {
        currentState = STATE.PUCHARASE_COMPLETED;
        msgOut.setLength(0);

        msgOut.append(currentState);

        msgOut.append(",");
        msgOut.append(sessionId);
        msgOut.append(",");
        msgOut.append(nRequestedTickets);
        msgOut.append(",");

        msgOut.append("el arreglo de los tickets....");
        if (db.update_Ticket_Status(ticketsArray[i], 3) && reserv[i])
        {
            msgOut.append("0");
            return true;
        }
        msgOut.append("1");
        dataOut.writeUTF(msgOut.toString());
        return false;
    }

    private String[] spliceMsgTokens(String rawMsg, int nTokens, int[] arrayLengthPositions)
    {

        return new String[0];
    }

    private boolean checkMsgIntegrity(String rawMsg) throws ConversationException
    {
        boolean correct = false;
        String[] valuesIn = rawMsg.split(",");
        int nTokens = getTokenNumber();
        int[] arrayLengths = getArrayLengthPositions();
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

    // ToDo: terminar la definicion del metodo
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

    // ToDo: evaluar la utilidad real de este metodo
    // tal vez se pueden usar constantes en su lugar
    private int[] getArrayLengthPositions() throws ConversationException
    {
        switch (currentState)
        {
        case C_START_SESSION:
            return new int[0];
        case GET_EVENT_LIST:
            return new int[0];
        case GET_EVENT_INFO:
            return new int[0];
        case GET_AVAILABLE_SEATS:
            return new int[0];
        case REQUEST_RESERVE_TICKETS:
            return new int[] {3};
        case SINGUP:
            return new int[0];
        case LOGIN_CHECK:
            return new int[0];
        case POST_PAYMENT_INFO:
            return new int[0];
        default:
            throw new ConversationException(ERROR.INCORRECT_CONVERSATION_STATE);
        }
    }

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

    private void checkConversationState(String rawConversationState) throws ConversationException
    {
        if (Integer.parseInt(rawConversationState) != currentState.ordinal())
            throw new ConversationException(ERROR.INCORRECT_CONVERSATION_STATE);
    }

    private void checkSessionId(String rawSessionId) throws SessionException
    {
        if (Integer.parseInt(rawSessionId) != sessionId)
            throw new SessionException(SessionException.ERROR.INVALID_SESSION_ID);
    }
}