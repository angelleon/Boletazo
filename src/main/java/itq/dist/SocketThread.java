package itq.dist;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SocketThread extends Thread
{
    private static final Logger log = LogManager.getLogger(SocketThread.class);

    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private Db db;
    private SessionControl sc;

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

    private int currentState;

    SocketThread(Socket socket, Db db, SessionControl sc)
    {
        this.socket = socket;
        this.db = db;
        this.sc = sc;
        this.currentState = STATE.C_START_SESSION.ordinal();
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
            if (currentState == STATE.SINGUP.ordinal())
            {
                singup();
                singupStatus();
            }
            loginCheck();
            loginStatus();
            postPaymentInfo();
            pucharaseCompleted();

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

    private boolean cStartSession() throws ConversationException, IOException
    {
        String rawMsg = dataIn.readUTF();
        String[] valuesIn = 
        return false;
    }

    private boolean sStartSession() throws ConversationException, IOException
    {
        return false;
    }

    private boolean getEventList() throws ConversationException, IOException
    {
        return false;
    }

    private boolean postEventList() throws ConversationException, IOException
    {
        return false;
    }

    private boolean getEventInfo() throws ConversationException, IOException
    {
        return false;
    }

    private boolean postEventInfo() throws ConversationException, IOException
    {
        return false;
    }

    private boolean getAvailableSeats()
            throws ConversationException, IOException
    {
        return false;
    }

    private boolean postAvailableSeats()
            throws ConversationException, IOException
    {
        return false;
    }

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

    private void checkMsgIntegrity(String rawMsg)
    {

    }
}
