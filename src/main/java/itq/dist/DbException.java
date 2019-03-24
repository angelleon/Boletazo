package itq.dist;

public class DbException extends BoletazoException
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static enum ERROR {
        GENERIC_ERROR,
        INCONSCISTENT_INFO
    }

    private ERROR error;

    DbException()
    {

    }

    DbException(ERROR error)
    {
        this.error = error;
    }

    @Override
    public String getMessage()
    {
        return super.getMessage() + "\n" + errorToStr(error);
    }

    private static String errorToStr(ERROR error)
    {
        switch (error)
        {
        case GENERIC_ERROR:
            return "DataBase exception for Boletazo software";
        case INCONSCISTENT_INFO:
            return "Provided information is inconsisten with database";
        default:
            return "";
        }
    }
}
