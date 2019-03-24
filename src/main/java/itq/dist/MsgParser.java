package itq.dist;

public class MsgParser
{
    public static enum GRAMATICA {
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

    public void parse(String rawMsg, GRAMATICA state)
    {
        switch (state)
        {
        case CONFIRM_RESERVE_TICKETS:
            break;
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
        case POST_AVAILABLE_SEATS:
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
        case SINGUP_STATUS:
            break;
        case S_START_SESSION:
            break;
        default:
            break;

        }
    }

}
