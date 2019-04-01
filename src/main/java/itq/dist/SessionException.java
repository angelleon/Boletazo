package itq.dist;

public class SessionException extends BoletazoException
{
    private static final long SERIAL_VERSION_UID = 1L;

    /**
     * create 2 variables with value of 0 and 1 respectively
     * 
     * @param GENERIC_ERROR
     *            equal to 0
     * @param INVALID_SESSION_ID
     *            equal to 1
     */

    public static enum ERROR {
        GENERIC_ERROR,
        INVALID_SESSION_ID,
        OUT_OF_RANGE_ID,
        SUPPLY_ID_REQUEST,
    }

    private ERROR error;

    /**
     * @param error,
     *            the error to set
     */
    SessionException(ERROR error)
    {
        super(BoletazoException.ERROR.SPECIFIC_ERROR);
        this.error = error;
    }

    /**
     * create a particular control exception for sessions controls
     */
    SessionException()
    {
        super();
        this.error = ERROR.GENERIC_ERROR;
    }

    /**
     * Return a specific error message, when an session error happened
     */
    @Override
    public String getMessage()
    {
        return super.getMessage() + "\n" + errorToStr(error);
    }

    /**
     * 
     * @param error
     * @return if true return a ,message for session error, if false return nothing
     */
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
