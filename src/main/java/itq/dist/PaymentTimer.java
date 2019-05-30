package itq.dist;

public class PaymentTimer extends TimerThread
{
    SessionControl sc;
    Flag confirmed;
    int sessionId;

    public PaymentTimer(int timeout, int sessionId, SessionControl sc)
    {
        super(timeout);
        this.sessionId = sessionId;
        this.sc = sc;
        confirmed = new Flag(false);
    }

    @Override
    public void run()
    {
        super.run();
        if (confirmed.isSet())
        { return; }
        sc.setReservedTickets(null, sessionId);
    }

    public synchronized void confirm()
    {
        confirmed.set();
    }
}
