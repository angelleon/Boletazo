package itq.dist;

public class BoletazoException extends Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * no se que hace este atributo, eclipse lo declaro como quickfix
     */
    @SuppressWarnings("unused")
    private static final long SERIAL_VERSION_UID = 1L;

    private ERROR error;

    public static enum ERROR {
        GENERIC_ERROR,
        IP_PROBLEM,
        CONNECTION_ERROR,
        INCONSCISTENT_MESSAGE,
        ONLOAD_DATABASE,
    };

    BoletazoException()
    {
        this(ERROR.GENERIC_ERROR);
    }

    BoletazoException(ERROR error)
    {
        super();
        this.error = error;
    }

    /**
     * Get the error message from Boletazo
     */
    @Override
    public String getMessage()
    {
        return super.getMessage() + "\n" + errorToStr(error);
    }

    /**
     * Translate the error code enumerator into text
     * 
     * @param error
     * @return String error
     */
    private static String errorToStr(ERROR error)
    {
        switch (error)
        {
        case GENERIC_ERROR:
            return "Top-level exception for Boletazo software";
        case IP_PROBLEM:
            return "Failed to get a correct format IP";
        case CONNECTION_ERROR:
            return "Conection refused by inconsistent conection";
        case INCONSCISTENT_MESSAGE:
            return "Bad format message for incoming connection";
        case ONLOAD_DATABASE:
            return "Database on load charge, request incomplete";
        default:
            return "";
        }
    }
}
