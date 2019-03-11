package itq.dist;

import java.time.LocalDate;

public class Event
{
    private int idEvent;
    private String name;
    private String description;
    private LocalDate date;
    private int idVenue;

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

    @Override
    public String toString()
    {
        return "Event object {idEvent: " + idEvent + ", name: " + name + ", description: " + description + ", date: "
                + date.toString() + ", idVenue: " + idVenue + "}";
    }
}
