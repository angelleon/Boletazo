package itq.dist;

public class SessionException extends BoletazoException
{
    /**
     * 
     */
    private static final long SERIAL_VERSION_UID = 1L;

    public static enum ERROR {
        GENERIC_ERROR,
        INVALID_SESSION_ID
    }

    private ERROR error;

    SessionException(ERROR error)
    {
        super();
        this.error = error;
    }

    SessionException()
    {
        this(ERROR.GENERIC_ERROR);
    }

    @Override
    public String getMessage()
    {
        return super.getMessage() + "\n" + errorToStr(error);
    }

    private String errorToStr(ERROR error)
    {
        switch (error)
        {
        case GENERIC_ERROR:
            return "Session exception for Boletazo software";
        default:
            return "";
        }
    }
}
