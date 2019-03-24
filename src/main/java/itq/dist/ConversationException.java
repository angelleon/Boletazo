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
        VALUE_OUT_OF_RANGE
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

    @Override
    public String getMessage()
    {
        return super.getMessage() + errorToStr(error);
    }

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
        default:
            return "";
        }
    }
}
