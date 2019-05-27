/**
 * 
 */
package itq.dist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimerThread extends Thread
{
    private static final Logger LOG = LogManager.getLogger(TimerThread.class);
    protected int updateTime = 250;
    protected int elapsedTime;

    protected int timeout;
    protected Flag alive;

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

    public TimerThread(int timeout)
    {
        this(timeout, 250);
    }

    /**
     * Timer used for reserved tickets control whit a time defined by the user
     * 
     * @param time
     * @param sessionId
     * @param operationType
     */
    public TimerThread(int timeout, int updateTime)
    {
        this(timeout, updateTime, new Flag(true));
    }

    public TimerThread(int timeout, int updateTime, Flag alive)
    {
        super();
        this.timeout = timeout;
        elapsedTime = 0;
        this.updateTime = updateTime;
        this.alive = alive;
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
        this.alive.setState(update);
    }

    @Override
    public void run()
    {
        try
        {
            while (alive.isSet())
            {
                Thread.sleep(updateTime);
                addElapsed(updateTime);
                alive.setState(elapsedTime >= timeout);
            }
        }
        catch (InterruptedException e)
        {
            LOG.error(e.getMessage());
        }
    }

    protected synchronized void addElapsed(int msecs)
    {
        elapsedTime += msecs;
    }

    public synchronized void reset()
    {
        elapsedTime = 0;
    }
}
