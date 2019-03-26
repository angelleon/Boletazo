package itq.dist;

public class DbException extends BoletazoException
{
    /**
     * 
     */
    private static final long SERIAL_VERSION_UID = 1L;

    public static enum ERROR {
        GENERIC_ERROR,
        INCONSCISTENT_INFO,
        OUT_OF_TICKETS,
        OVERCHARGE_INFO,
        REGISTER_ERROR,
        BAD_FORMAT_REQUEST,
        BAD_VENUE,
        BAD_TICKET,
        BAD_SEATNUMBER,
        BAD_EVENT,
    }

    private ERROR error;

    DbException()
    {

    }

    DbException(ERROR error)
    {
        this.error = error;
    }

    /**
     * Get the error message from the database
     * 
     * @return error
     */
    @Override
    public String getMessage()
    {
        return super.getMessage() + "\n" + errorToStr(error);
    }

    /**
     * Translate the error code into a string
     * 
     * @param error
     * @return String error
     */
    private static String errorToStr(ERROR error)
    {
        switch (error)
        {
        case GENERIC_ERROR:
            return "DataBase exception for Boletazo software";
        case INCONSCISTENT_INFO:
            return "Provided information is inconsisten with database";
        case OUT_OF_TICKETS:
            return "The request event has no more tickets to sell";
        case OVERCHARGE_INFO:
            return "Several request has overcharge the tables";
        case REGISTER_ERROR:
            return "Failed to complete the register process";
        case BAD_FORMAT_REQUEST:
            return "The request secuence from the user had a bad format";
        case BAD_VENUE:
            return "The request has not been found by filter Venue";
        case BAD_TICKET:
            return "The request has not been found by filter Ticket";
        case BAD_SEATNUMBER:
            return "The request has not been found by filter Seat number";
        case BAD_EVENT:
            return "The request has not been found by filter Event";
        default:
            return "";
        }
    }
}
