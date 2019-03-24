package itq.dist;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.lang.StringBuilder;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SocketThread extends Thread
{
    private static final Logger log = LogManager.getLogger(SocketThread.class);
    private static final int MAX_MSG_LENGTH = 1024;

    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private Db db;
    private SessionControl sc;
    private int sessionId;
    private StringBuilder msgOut;

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
            log.error(e.getMessage());
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
        String[] valuesIn = spliceMsgTokens(rawMsg, getTokenNumber(), getArrayLengthPositions());
        return false;
    }

    private boolean sStartSession() throws ConversationException, IOException
    {
        currentState = STATE.S_START_SESSION;
        msgOut.setLength(0);
        int sessionId = sc.getNewSessionId();
        if (sessionId > 0)
        {
            msgOut.append(STATE.S_START_SESSION.ordinal());
            msgOut.append(sessionId);
            dataOut.writeUTF(msgOut.toString());
            return true;
        }
        return false;
    }

    private boolean getEventList() throws ConversationException, IOException
    {
        currentState = STATE.GET_EVENT_LIST;
        return false;
    }

    private boolean postEventList() throws ConversationException, IOException
    {
        currentState = STATE.POST_EVENT_LIST;
        return false;
    }

    private boolean getEventInfo() throws ConversationException, IOException
    {
        currentState = STATE.GET_EVENT_INFO;
        return false;
    }

    private boolean postEventInfo() throws ConversationException, IOException
    {
        currentState = STATE.POST_EVENT_INFO;
        return false;
    }

    private boolean getAvailableSeats()
            throws ConversationException, IOException
    {
        currentState = STATE.GET_AVAILABLE_SEATS;
        return false;
    }

    private boolean postAvailableSeats() throws ConversationException, IOException
    {
        currentState = STATE.POST_AVAILABLE_SEATS;
        return false;
    }

    //
    private boolean requestReserveTickets()
            throws ConversationException, IOException
    {
        return false;
    }

    private boolean confirmReserveTickets()
            throws ConversationException, IOException
    {
        return false;
    }

    private boolean singup() throws ConversationException, IOException
    {
        return false;
    }

    private boolean singupStatus() throws ConversationException, IOException
    {
        return false;
    }

    private boolean loginCheck() throws ConversationException, IOException
    {
        return false;
    }

    private boolean loginStatus() throws ConversationException, IOException
    {
        return false;
    }

    private boolean postPaymentInfo() throws ConversationException, IOException
    {
        currentState = STATE.POST_PAYMENT_INFO;

        String rawMsg = dataIn.readUTF();
        String[] valuesIn = rawMsg.split(",");
        int sessionId = Integer.parseInt(valuesIn[1]);
        return false;
    }

    private boolean pucharaseCompleted()
            throws ConversationException, IOException
    {
        return false;
    }

    private String[] spliceMsgTokens(String rawMsg, int nTokens, int[] arrayLengthPositions)
    {

        return new String[0];
    }

    private boolean checkMsgIntegrity(String rawMsg) throws ConversationException
    {
        String[] valuesIn = rawMsg.split(",");
        int nTokens = getTokenNumber();
        int[] arrayLength = getArrayLengthPositions();
        int n = valuesIn.length;
        if (n < nTokens)
        {
            throw new ConversationException(ConversationException.ERROR.NOT_ENOUGH_ARGUMENTS);
        }
        else
        {
            switch (currentState)
            {
            case C_START_SESSION:
                break;
            case GET_AVAILABLE_SEATS:
                break;
            case GET_EVENT_INFO:
                break;
            case GET_EVENT_LIST:
                break;
            case LOGIN_CHECK:
                break;
            case LOGIN_STATUS:
                break;
            case POST_EVENT_INFO:
                break;
            case POST_EVENT_LIST:
                break;
            case POST_PAYMENT_INFO:
                break;
            case PUCHARASE_COMPLETED:
                break;
            case REQUEST_RESERVE_TICKETS:
                break;
            case SINGUP:
                break;
            case S_START_SESSION:
                break;
            default:
                break;

            }
        }
        return false;
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

    private int[] getArrayLengthPositions() throws ConversationException
    {
        switch (currentState)
        {
        case C_START_SESSION:
            return new int[0];
        case S_START_SESSION:
            return new int[0];
        case GET_EVENT_LIST:
            return new int[0];
        case POST_EVENT_LIST:
            return new int[] {3};
        case GET_EVENT_INFO:
            return new int[0];
        case POST_EVENT_INFO:
            return new int[] {7};
        case GET_AVAILABLE_SEATS:
            return new int[0];
        case POST_AVAILABLE_SEATS:
            return new int[] {3};
        case REQUEST_RESERVE_TICKETS:
            return new int[0];
        case CONFIRM_RESERVE_TICKETS:
            return new int[] {2};
        case SINGUP:
            return new int[0];
        case SINGUP_STATUS:
            return new int[0];
        case LOGIN_CHECK:
            return new int[0];
        case LOGIN_STATUS:
            return new int[0];
        case POST_PAYMENT_INFO:
            return new int[0];
        case PUCHARASE_COMPLETED:
            return new int[0];
        default:
            throw new ConversationException();
        }
    }

    @SuppressWarnings("incomplete-switch")
    private TYPES[] getArgumentTypes()
    {
        TYPES[] t = new TYPES[getTokenNumber()];
        switch (currentState)
        {
        case C_START_SESSION:
            t[0] = TYPES.INT;
            t[1] = TYPES.NULL;
            break;
        case GET_EVENT_LIST:
            t[0] = TYPES.INT;
            t[1] = TYPES.INT;
            t[2] = TYPES.STRING;
            t[3] = TYPES.STRING;
            t[4] = TYPES.STRING;
            t[5] = TYPES.INT;
            t[6] = TYPES.FLOAT;
            t[7] = TYPES.STRING;
            break;
        case GET_EVENT_INFO:
        case GET_AVAILABLE_SEATS:
            t[0] = TYPES.INT;
            t[1] = TYPES.INT;
            t[2] = TYPES.INT;
            break;
        case REQUEST_RESERVE_TICKETS:
            t[0] = TYPES.INT;
            t[1] = TYPES.INT;
            t[2] = TYPES.INT;
            t[3] = TYPES.INT;
            t[4] = TYPES.ARRAY;
            break;
        case SINGUP:
            t[0] = TYPES.INT;
            t[1] = TYPES.STRING;
            t[2] = TYPES.STRING;
            t[3] = TYPES.STRING;
            t[4] = TYPES.STRING;
            break;
        case LOGIN_CHECK:
            t[0] = TYPES.INT;
            t[1] = TYPES.INT;
            t[2] = TYPES.STRING;
            t[3] = TYPES.STRING;
            break;
        case POST_PAYMENT_INFO:
            t[0] = TYPES.INT;
            t[1] = TYPES.INT;
            t[2] = TYPES.INT;
            t[3] = TYPES.STRING;
            t[4] = TYPES.STRING;
            t[5] = TYPES.STRING;
            t[6] = TYPES.STRING;
            break;
        }
        return t;
    }
}
