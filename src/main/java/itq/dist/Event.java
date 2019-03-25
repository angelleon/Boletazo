package itq.dist;

import java.time.LocalDate;

public class Event
{
    private int idEvent;
    private int idVenue;
    private String name;
    private String description;
    private String city;
    private String address;
    private LocalDate date;
    private float cost;

    Event()
    {

    }

    Event(int idEvent, String name, String description, LocalDate date, int idVenue)
    {
        this.idEvent = idEvent;
        this.name = name;
        this.description = description;
        this.date = date;
        this.idVenue = idVenue;
    }

    Event(int idEvent, int idVenue, String name, String address, String city)
    {
        this.idEvent = idEvent;
        this.idVenue = idVenue;
        this.name = name;
        this.address = address;
        this.city = city;
    }

    Event(int idEvent, int idVenue, String name, String address, String city, float cost)
    {
        this.idEvent = idEvent;
        this.idVenue = idVenue;
        this.name = name;
        this.address = address;
        this.city = city;
        this.cost = cost;
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

    /**
     * Get the ID from the event
     * 
     * @return IdEvent
     */
    public int getIdEvent()
    {
        return idEvent;
    }

    /**
     * Get the name from the event
     * 
     * @return name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the description from the event
     * 
     * @return description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Get the local date from the event
     * 
     * @return date
     */
    public LocalDate getDate()
    {
        return date;
    }

    /**
     * get the ID Venue from the event
     * 
     * @return IDVenue
     */
    public int getIdVenue()
    {
        return idVenue;
    }
}
