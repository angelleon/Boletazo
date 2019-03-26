package itq.dist;

/**
 * 
 * @author rodri
 * 
 *         this class has associations with Strings and values of the different
 *         states of a conversation
 */
public class MsgParser
{
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

    /**
     * 
     * @param rawMsg
     * @param state
     *            recognize an string with its int value preassigned and breaks a
     *            determinate sequence of commands
     */
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
