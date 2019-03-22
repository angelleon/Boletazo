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

    private static enum STATE
    {
        C_START_SESSION,	// 0
        S_START_SESSION, 	// 1
        GET_EVENT_LIST,		// 2
        POST_EVENT_LIST,	// 3
        GET_EVENT_INFO,		// 4
        POST_EVENT_INFO,	// 5
        GET_AVAILABLE_SEATS,// 6
        POST_AVAILABLE_SEATS,	// 7
        REQUEST_RESERVE_TICKETS,// 8
        CONFIRM_RESERVE_TICKETS,// 9
        SINGUP,					// 10
        SINGUP_STATUS,			// 11
        LOGIN_CHECK,			// 12
        LOGIN_STATUS,			// 13
        POST_PAYMENT_INFO,		// 14
        PUCHARASE_COMPLETED		// 15
    }

    private int currentState;

    SocketThread(Socket socket)
    {
        this.socket = socket;
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

    // La tablita .....si hay sesion tabla.. ok no..
    private boolean cStartSession() throws ConversationException, IOException
    {
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

    private boolean getAvailableSeats() throws ConversationException, IOException
    {
        return false;
    }

    private boolean postAvailableSeats() throws ConversationException, IOException
    {
        return false;
    }

    private boolean requestReserveTickets() throws ConversationException, IOException
    {
        return false;
    }

    private boolean confirmReserveTickets() throws ConversationException, IOException
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

    private boolean pucharaseCompleted() throws ConversationException, IOException
    {
        return false;
    }
}
