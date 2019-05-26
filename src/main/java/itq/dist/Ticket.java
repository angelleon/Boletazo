package itq.dist;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Ticket
{
    private static final Logger LOG = LogManager.getLogger(Ticket.class);

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

    Ticket(int idTicket, String seatNumber, int idStatus, int idSection,
            int idEvent)
    {
        this.idTicket = idTicket;
        this.seatNumber = seatNumber;
        this.idStatus = idStatus;
        this.idSection = idSection;
        this.idEvent = idEvent;
    }

    Ticket(int idTicket, String seatNumber, int idStatus, int idSection,
            int idEvent, TimerThread timer)
    {
        this(idTicket, seatNumber, idStatus, idSection, idEvent);
    }

    Ticket()
    {
        this(0, "a0", 0, 0, 0);
    }

    /**
     * Get the ID Ticket from the class boleto
     * 
     * @return
     */
    public int getIdTicket()
    {
        LOG.debug(idTicket);
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
    public synchronized int getIdStatus()
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
    // public synchronized boolean ticketPurchase()
    // {
    // timer.setTime(purchaseTime);// whaaaat?
    // timer.start();
    // try
    // {
    // timer.join();
    // if (timer.isInterrupted())
    // {
    // idStatus = STATUS.SOLD.ordinal(); // Se concreto la compra en el
    // // tiempo de apartado
    // return true;
    // }
    // else
    // {
    // idStatus = STATUS.AVAILABLE.ordinal(); // no se concreto la
    // // compra en el tiempo de
    // // apartado
    // setStatus(idStatus);
    // return false;
    // }
    // }
    // catch (InterruptedException e)
    // {
    // e.printStackTrace();
    // return false;
    // }
    // }

    /**
     * Set status that means if the ticket is available for purchase or is in
     * another state. Available - Ready to buy. Reserved - Wait for purchase. Sold
     * No available for buy.
     * 
     * @param idStatus
     */
    private void setStatusId(int idStatus)
    {
        this.idStatus = idStatus;
    }

    public void setTicketStatus(STATUS status)
    {
        this.ticketStatus = status;
        setStatusId(ticketStatus.ordinal());
    }
}
