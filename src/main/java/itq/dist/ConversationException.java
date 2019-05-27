package itq.dist;

import itq.dist.BoletazoThread.STATE;

public class ConversationException extends BoletazoException
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static enum ERROR {
        GENERIC_ERROR,
        INCORRECT_NUMBER_FORMAT,
        TO_MANY_ARGUMENTS,
        NOT_ENOUGH_ARGUMENTS,
        INCORRECT_CONVERSATION_STATE,
        VALUE_OUT_OF_RANGE,
        INCORRECT_DATE_FORMAT
    }

    private ERROR error;
    private STATE state;

    ConversationException(ERROR error, STATE conversationState)
    {
        super(BoletazoException.ERROR.SPECIFIC_ERROR);
        this.error = error;
        this.state = conversationState;
    }

    ConversationException()
    {
        super();
        this.error = ERROR.GENERIC_ERROR;
        this.state = STATE.C_START_SESSION;
    }

    /**
     * Get the error message generated on the conversation between the server and
     * the client
     */
    @Override
    public String getMessage()
    {
        String msg = "";
        msg += super.getMessage();
        msg += msg.length() == 0 ? "" : "\n";
        StackTraceElement[] st = this.getStackTrace();
        for (StackTraceElement ste : st)
        {
            msg += ste.toString() + "\n";
        }
        msg += errorToStr(error) + " in step " + state;
        return msg;
    }

    /**
     * Translate the error code to an string
     * 
     * @param error
     * @return String error
     */
    private static String errorToStr(ERROR error)
    {
        switch (error)
        {
        case GENERIC_ERROR:
            return "an error has ocurred";
        case INCORRECT_NUMBER_FORMAT:
            return "number has not the correct format";
        case NOT_ENOUGH_ARGUMENTS:
            return "there are not enough arguments to complete the petition";
        case TO_MANY_ARGUMENTS:
            return "there are more argumets than expected";
        case INCORRECT_CONVERSATION_STATE:
            return "actual conversation state is incorrect";
        case VALUE_OUT_OF_RANGE:
            return "provided value is out of range";
        case INCORRECT_DATE_FORMAT:
            return "provided date have incorrect format";
        default:
            return "";
        }
    }
}
