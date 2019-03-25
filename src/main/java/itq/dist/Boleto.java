package itq.dist;

public class Boleto
{
    public static enum STATUS {
        NULL, // DB does not define an id with value 0
        AVAILABLE,
        RESERVED,
        SOLD
    }

    private int idTicket;
    private String seatNumber;
    private int idStatus;
    private int idSection;
    private int idEvent;

    private STATUS ticketStatus;

    private int purchaseTime = 6000; // tiempo configurable para la espera de compra
    private TimerThread timer;

    Boleto(int idTicket, String seatNumber, int idStatus, int idSection,
            int idEvent)
    {
        this.idTicket = idTicket;
        this.seatNumber = seatNumber;
        this.idStatus = idStatus;
        this.idSection = idSection;
        this.idEvent = idEvent;
        setStatus(idStatus);
    }

    Boleto(int idTicket, String seatNumber, int idStatus, int idSection,
            int idEvent, TimerThread timer)
    {
        this(idTicket, seatNumber, idStatus, idSection, idEvent);
        this.timer = timer;
    }

    Boleto()
    {
        this(0, "a0", 0, 0, 0);
    }

    public TimerThread getTimer()
    {
        return timer;
    }

    public void setTimer(TimerThread timer)
    {
        this.timer = timer;
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

    public synchronized boolean TicketPurchase()
    {
        timer.setTime(purchaseTime);
        timer.start();
        try
        {
            timer.join();
            if (timer.isInterrupted())
            {
                idStatus = STATUS.SOLD.ordinal(); // Se concreto la compra en el
                                                  // tiempo de apartado
                return true;
            }
            else
            {
                idStatus = STATUS.AVAILABLE.ordinal(); // no se concreto la
                                                       // compra en el tiempo de
                                                       // apartado
                return false;
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            return false;
        }
    }
    public void ConfirmationTicketPurchase()
    {
        timer.interrupt();
    }

    private void setStatus(int idStatus)
    {
        if (idStatus == STATUS.NULL.ordinal())
        {
            ticketStatus = STATUS.NULL;
        }
        else if (idStatus == STATUS.AVAILABLE.ordinal())
        {
            ticketStatus = STATUS.AVAILABLE;
        }
        else if (idStatus == STATUS.RESERVED.ordinal())
        {
            ticketStatus = STATUS.RESERVED;
        }
        else if (idStatus == STATUS.SOLD.ordinal())
        {
            ticketStatus = STATUS.SOLD;
        }
        else
        {

        }
    }
}
