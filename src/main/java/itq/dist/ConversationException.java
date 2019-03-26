package itq.dist;

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

    ConversationException(ERROR error)
    {
        super();
        this.error = error;
    }

    ConversationException()
    {
        this(ERROR.GENERIC_ERROR);
    }

    /**
     * Get the error message generated on the conversation between the server and
     * the client
     */
    @Override
    public String getMessage()
    {
        return super.getMessage() + errorToStr(error);
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
