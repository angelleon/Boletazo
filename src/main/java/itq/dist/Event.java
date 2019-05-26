package itq.dist;

import java.time.LocalDate;

public class Event
{
    protected int idEvent;
    protected String name;
    protected String description;
    protected LocalDate date;
    protected int idVenue;

    Event()
    {
        this(0, "", "", LocalDate.now(), 0);
    }

    Event(int idEvent, String name, String description, LocalDate date, int idVenue)
    {
        this.idEvent = idEvent;
        this.name = name;
        this.description = description;
        this.date = date;
        this.idVenue = idVenue;
    }

    /**
     * Get the concat string with all the parameters of the class
     */
    @Override
    public String toString()
    {
        return "Event object {idEvent: " + idEvent + ", name: " + name + ", description: " + description + ", date: "
                + date.toString() + ", idVenue: " + idVenue + "}";
    }

    public int getIdEvent()
    {
        return idEvent;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public LocalDate getDate()
    {
        return date;
    }

    public int getIdVenue()
    {
        return idVenue;
    }
}