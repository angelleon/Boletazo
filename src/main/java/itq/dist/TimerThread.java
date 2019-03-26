/**
 * 
 */
package itq.dist;

public class TimerThread extends Thread
{
    private int time;

    /**
     * @param time
     */

    public TimerThread()
    {
        this(6000);
    }

    public TimerThread(int time)
    {
        this.time = time;
    }

    /**
     * 
     * @param time
     *            the time to set
     */

    public void setTime(int time)
    {
        this.time = time;
    }

    @Override
    public void run()
    {
        time();
    }

    /**
     * this method tries to create a counter to control the process of choose and
     * buy a ticket by a client
     */
    public void time()
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
}
