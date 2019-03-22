package itq.dist;

import java.time.LocalDate;

public class Event
{
    private int idEvent;
    private int idVenue;
    private String name;
    private String description;
    private String city;
    private String Address;
    private LocalDate date;
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
    Event(int idEvent, int idVenue, String name, String Address, String city)
    {
        this.idEvent = idEvent;
        this.idVenue = idVenue;
        this.name = name;
        this.Address = Address;
        this.city = city;
    }
    /**
     * @return the name
     */
    public String getName() {
    	return name;
    }
    /**
     * @return the idEvent
     */
    public int getIdEvent() {
    	return idEvent;
    }
    @Override
    public String toString()
    {
        return "Event object {idEvent: " + idEvent + ", name: " + name + ", description: " + description + ", date: "
                + date.toString() + ", idVenue: " + idVenue + "}";
    }



}
