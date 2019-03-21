package itq.dist;

public class Boleto
{
    private int idTicket;
    private String seatNumber;
    private int idStatus;
    private int idSection;
    private int idEvent;
  
    private int PurchaseTime = 6000; //tiempo configurable para la espera de compra
	  private Thread Timer = new TimerThread(PurchaseTime);

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
	  public synchronized boolean TicketPurchase()
    {
		    Timer.start();
		    try
        {
			      Timer.join();
			      if(Timer.isInterrupted())
            {
				        setStatus(true);	//Se concreto la compra en el tiempo de apartado
				        return true;
			      }
            else
            {
				        setStatus(false);	//no se concreto la compra en el tiempo de apartado
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
	public void ConfirmationTicketPurchase() {
		Timer.interrupt();
	}
}