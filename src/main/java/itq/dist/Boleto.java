package itq.dist;

public class Boleto
{
    private int idTicket;
    private String seatNumber;
    private int idStatus;
    private int idSection;
    private int idEvent;

    Boleto(int idTicket, String seatNumber, int idStatus, int idSection, int idEvent)
    {
        this.idTicket = idTicket;
        this.seatNumber = seatNumber;
        this.idStatus = idStatus;
        this.idSection = idSection;
        this.idEvent = idEvent;
    }

    Boleto()
    {
        this(0, "a0", 0, 0, 0);
    }

    public int getIdTicket()
    {
        return idTicket;
    }

    public String getSeatNumber()
    {
        return seatNumber;
    }

    public int getIdStatus()
    {
        return idStatus;
    }

    public int getIdSection()
    {
        return idSection;
    }

    public int getIdEvent()
    {
        return idEvent;
    }
}
