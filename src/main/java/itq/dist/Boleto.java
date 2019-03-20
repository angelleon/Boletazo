package itq.dist;

public class Boleto
{
	private int IDTicket;
	private boolean status;
	private int PurchaseTime = 6000; //tiempo configurable para la espera de compra
	private Thread Timer = new TimerThread(PurchaseTime);	
	
	public Boleto(int IDTicket) {
		this.IDTicket = IDTicket;
	}
	/**
	 * @return the status
	 */
	public boolean isStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(boolean status) {
		
		this.status = status;
	}
	
	public synchronized boolean TicketPurchase() {
		Timer.start();
		try {
			Timer.join();
			if(Timer.isInterrupted()) {
				setStatus(true);	//Se concreto la compra en el tiempo de apartado
				return true;
			}else {
				setStatus(false);	//no se concreto la compra en el tiempo de apartado
				return false;
			}	
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	public void ConfirmationTicketPurchase() {
		Timer.interrupt();
	}
}
