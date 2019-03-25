package itq.dist;

import java.time.LocalDate;

public class EventInfo
{
    private int idEvent;
    String name;
    LocalDate[] dates;
    int idVenue;
    Participant[] participants;

    EventInfo()
    {

    }

    EventInfo(int idEvent, String name, LocalDate[] dates, int idVenue, Participant[] participants)
    {
        this.idEvent = idEvent;
        this.name = name;
        this.dates = dates;
        this.idVenue = idVenue;
        this.participants = participants;
    }

    public int getIdEvent()
    {
        return idEvent;
    }

    public String getName()
    {
        return name;
    }

    public LocalDate[] getDates()
    {
        return dates;
    }

    public int getIdVenue()
    {
        return idVenue;
    }

    public Participant[] getParticipants()
    {
        return participants;
    }
}
