package itq.dist;

public class SessionException extends BoletazoException
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    SessionException()
    {

    }

    SessionException(int erroCode)
    {
        super(erroCode);
    }
}
