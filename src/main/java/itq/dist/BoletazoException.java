package itq.dist;

public class BoletazoException extends Exception
{
    /**
     * no se que hace este atributo, eclipse lo declaro como quickfix
     */
    private static final long serialVersionUID = 1L;

    private ERROR error;

    public static enum ERROR {
        GENERIC_ERROR
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
        default:
            return "";
        }
    }
}
