/**
 * 
 */
package itq.dist;

/**
 * @author Laptop
 *
 */
public class TimerThread extends Thread
{
    private int time;

    public TimerThread(int time)
    {
        this.time = time;
    }

    public void setTime(int time)
    {
        this.time = time;
    }

    @Override
    public void run()
    {
        time();
    }

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
