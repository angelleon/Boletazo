package itq.dist;

public class Flag
{
    private boolean state;

    Flag()
    {
        this(false);
    }

    Flag(boolean state)
    {
        this.state = state;
    }

    public void set()
    {
        state = true;
    }

    public void unset()
    {
        state = false;
    }

    public void toggle()
    {
        state = !state;
    }

    public void setState(boolean state)
    {
        this.state = state;
    }

    public boolean value()
    {
        return getState();
    }

    public boolean isSet()
    {
        return getState();
    }

    public boolean getState()
    {
        return state;
    }
}
