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

    /**
     * Get the TimerThread from the class boleto
     * 
     * @return TimerThread
     */
    public TimerThread getTimer()
    {
        return timer;
    }

    /**
     * Set a TimerThread on the class boleto
     * 
     * @param TimerThread
     */
    public void setTimer(TimerThread timer)
    {
        this.timer = timer;
    }

    /**
     * Get the ID Ticket from the class boleto
     * 
     * @return
     */
    public int getIdTicket()
    {
        return idTicket;
    }

    /**
     * Get the seat number from the class boleto
     * 
     * @return SeatNumber
     */
    public String getSeatNumber()
    {
        return seatNumber;
    }

    /**
     * Get the ID Status from the class boleto
     * 
     * @return IdStatus
     */
    public int getIdStatus()
    {
        return idStatus;
    }

    /**
     * Get the ID Section from the class boleto
     * 
     * @return idSection
     */
    public int getIdSection()
    {
        return idSection;
    }

    /**
     * Get the ID Event from the class boleto
     * 
     * @return idEvent
     */
    public int getIdEvent()
    {
        return idEvent;
    }

    /**
     * Set the boleto status on reserved until the client response with a
     * confirmation purchase
     * 
     * @return true if purchase complete | false if purchase failed
     */
    public synchronized boolean ticketPurchase()
    {
        timer.setTime(purchaseTime);// whaaaat?
        timer.start();
        try
        {
            timer.join();
            if (timer.isInterrupted())
            {
                idStatus = STATUS.SOLD.ordinal(); // Se concreto la compra en el
                                                  // tiempo de apartado
                setStatus(idStatus);
                return true;
            }
            else
            {
                idStatus = STATUS.AVAILABLE.ordinal(); // no se concreto la
                                                       // compra en el tiempo de
                                                       // apartado
                setStatus(idStatus);
                return false;
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Interrupt the TimerThread to confirm a purchase from the ticket
     */
    public void confirmationTicketPurchase()
    {
        timer.interrupt();
    }

    /**
     * Set status that means if the ticket is available for purchase or is in
     * another state. Available - Ready to buy. Reserved - Wait for purchase. Sold -
     * No available for buy.
     * 
     * @param idStatus
     */
    private void setStatus(int idStatus)
    {
        setStatus(idStatus);
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
