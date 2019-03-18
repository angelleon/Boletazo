package itq.dist;

public class BoletazoException extends Exception
{
    /**
     * no se que hace este atributo, eclipse lo declaro como quickfix
     */
    private static final long serialVersionUID = 1L;

    public static enum ERROR
    {
        GENERIC_ERROR
    }

    private int errorCode;

    BoletazoException()
    {
        super();
        errorCode = ERROR.GENERIC_ERROR.ordinal();
    }

    BoletazoException(int errorCode)
    {
        this.errorCode = errorCode;
    }

}
