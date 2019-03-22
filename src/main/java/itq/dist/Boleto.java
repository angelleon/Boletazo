package itq.dist;

public class Boleto
{
    private int idTicket;
    private String seatNumber;
    private int idStatus;
    private int idSection;
    private int idEvent;

    public static enum STATUS {
        NULL, // DB does not define an id with value 0
        AVAILABLE,
        RESERVED,
        SOLD
    }

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
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
    public void ConfirmationTicketPurchase()
    {
        timer.interrupt();
    }
}
