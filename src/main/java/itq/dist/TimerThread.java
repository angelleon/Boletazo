/**
 * 
 */
package itq.dist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimerThread extends Thread
{
    private static final Logger LOG = LogManager.getLogger(TimerThread.class);
    protected static final int updateTime = 500;
    protected int elapsedTime;

    protected int timeout;
    protected Flag update;

    /**
     * Default timer with a timer defined in 10 seconds and used by the reserved
     * tickets control
     * 
     * @param timeout
     */

    public TimerThread()
    {
        this(5000);
    }

    /**
     * Timer used for reserved tickets control whit a time defined by the user
     * 
     * @param time
     * @param sessionId
     * @param operationType
     */
    public TimerThread(int timeout)
    {
        super();
        this.timeout = timeout;
        update = new Flag(true);
        elapsedTime = 0;
    }

    /**
     * Set a time to wait for this thread
     * 
     * @param time
     * @return the time to set
     */
    public void setTime(int time)
    {
        this.timeout = time;
    }

    /**
     * set update control
     * 
     * @param update
     */
    public void setUpdate(boolean update)
    {
        this.update.setState(update);
    }

    @Override
    public void run()
    {
        try
        {
            while (update.isSet())
            {
                Thread.sleep(updateTime);
                elapsedTime += updateTime;
                update.setState(elapsedTime >= timeout);
            }
        }
        catch (InterruptedException e)
        {
            LOG.error(e.getMessage());
        }
    }

    public void reset()
    {
        elapsedTime = 0;
    }
}