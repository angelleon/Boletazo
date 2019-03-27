/**
 * 
 */
package itq.dist;

public class TimerThread extends Thread
{
    private int time;
    private int sessionId;
    private int operationType;
    private boolean update = false;
    private SessionControl sc;

    /**
     * Default timer with a timer defined in 10 seconds and used by the reserved
     * tickets control
     * 
     * @param time
     */

    public TimerThread()
    {
        this.time = 10000;
        this.sessionId = 0;
        this.operationType = 0;
    }

    /**
     * Timer used for reserved tickets control whit a time defined by the user
     * 
     * @param time
     * @param sessionId
     * @param operationType
     */
    public TimerThread(int time)
    {
        this.time = time;
        this.sessionId = 0;
        this.operationType = 0;
    }

    /**
     * General Used for Session Timer Control avoiding away from keyboard users
     * 
     * @param time
     * @param sessionId
     * @param type
     */
    public TimerThread(int time, int sessionId, int operationType)
    {
        this.time = time;
        this.sessionId = sessionId;
        this.operationType = operationType;
    }

    /**
     * Set a time to wait for this thread
     * 
     * @param time
     * @return the time to set
     */
    public void setTime(int time)
    {
        this.time = time;
    }

    /**
     * set update control
     * 
     * @param update
     */
    public void setUpdate(boolean update)
    {
        this.update = update;
    }

    /**
     * set sessionId
     * 
     * @param sessionId
     */
    public void setSessionID(int sessionId)
    {
        this.sessionId = sessionId;
    }

    /**
     * get the sessionId
     * 
     * @return sessionId
     */
    public int getSessionId()
    {
        return sessionId;
    }

    @Override
    public void run()
    {
        switch (operationType)
        {
        case 0:
            timerTicketPurchase();
            break;
        case 1:
            timerSessionControl();
            break;
        }
    }

    /**
     * this method tries to create a counter to control the process of choose and
     * buy a ticket by a client
     */
    public void timerTicketPurchase()
    {
        try
        {
            Thread.sleep(time); // Tiempo de espera para la compra
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * this method tries to create a counter to control the process of session
     * control to avoid away from keyboard users
     */
    public void timerSessionControl()
    {
        try
        {
            while (update)
            {
                update = false;
                Thread.sleep(time);
            }
            sc.releaseSessionId(sessionId);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SessionException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}