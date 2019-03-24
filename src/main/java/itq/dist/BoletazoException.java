package itq.dist;

public class BoletazoException extends Exception
{
    /**
     * no se que hace este atributo, eclipse lo declaro como quickfix
     */
    private static final long serialVersionUID = 1L;

    public static enum ERROR {
        GENERIC_ERROR
    }

    private ERROR error;

    BoletazoException()
    {
        this(ERROR.GENERIC_ERROR);
    }

    BoletazoException(ERROR error)
    {
        super();
        this.error = error;
    }

}
