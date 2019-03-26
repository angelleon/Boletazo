package itq.dist;

public class SessionException extends BoletazoException
{
    /**
     * 
     */
    private static final long SERIAL_VERSION_UID = 1L;

    public static enum ERROR {
        GENERIC_ERROR,
        INVALID_SESSION_ID,
        OUT_OF_RANGE_ID,
        SUPPLY_ID_REQUEST,
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
        case INVALID_SESSION_ID:
            return "Session ID is not a numeric ID";
        case OUT_OF_RANGE_ID:
            return "The ID sending by the user is not in the software limits";
        case SUPPLY_ID_REQUEST:
            return "No more IDs for the incoming users connections";
        default:
            return "";
        }
    }
}
