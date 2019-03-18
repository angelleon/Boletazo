package itq.dist;

import java.net.Socket;

public class SocketThread extends Thread
{
    private Socket socket;

    private static enum STATE
    {
        C_START_SESSION, S_START_SESSION, GET_EVENT_LIST, POST_EVENT_LIST, GET_EVENT_INFO, POST_EVENT_INFO, GET_AVAILABLE_SEATS, POST_AVAILABLE_SEATS, REQUEST_RESERVE_TICKETS, CONFIRM_RESERVE_TICKETS, SINGUP, SINGUP_STATUS, LOGIN_CHECK, LOGIN_STATUS, POST_PAYMENT_INFO, PUCHARASE_COMPLETED
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

    }

    // ToDo: implementar un metodo por cada constante del enum STATE
    // ToDo: considerar lanzar excepciones para manejar todos los errores de la
    // conversacion dentro de un bloque catch

    private boolean cStartSession()
    {
        return false;
    }

    private boolean sStartSession()
    {
        return false;
    }

    private boolean getEventList()
    {
        return false;
    }

    private boolean postEventList()
    {
        return false;
    }

    private boolean getEventInfo()
    {
        return false;
    }

    private boolean postEventInfo()
    {
        return false;
    }

    private boolean getAvailableSeats()
    {
        return false;
    }

    private boolean postAvailableSeats()
    {
        return false;
    }

    private boolean requestReserveTickets()
    {
        return false;
    }

    private boolean confirmReserveTickets()
    {
        return false;
    }

    private boolean singup()
    {
        return false;
    }

    private boolean singupStatus()
    {
        return false;
    }

    private boolean loginCheck()
    {
        return false;
    }

    private boolean loginStatus()
    {
        return false;
    }

    private boolean postPaymentInfo()
    {
        return false;
    }

    private boolean pucharaseCompleted()
    {
        return false;
    }
}
