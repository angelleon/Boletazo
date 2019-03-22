package itq.dist;

public class ConversationException extends BoletazoException
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static enum ERROR {
        GENERIC_ERROR,
        INCORRECT_NUMBER_FORMAT
    }

    private ERROR error;

    ConversationException()
    {
        super();
    }

    ConversationException(int errorCode)
    {
        super(errorCode);
    }

}
