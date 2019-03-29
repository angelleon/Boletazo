package itq.dist;

import java.time.LocalDate;

public class EventInfo extends Event
{
    private Participant[] participants;

    EventInfo()
    {
        this(0, "", "", LocalDate.now(), 0, new Participant[0]);
    }

    EventInfo(int idEvent, String name, String description, LocalDate date, int idVenue, Participant[] participants)
    {
        super(idEvent, name, description, date, idVenue);
        this.participants = participants;
    }

    @Override
    public int getIdEvent()
    {
        return idEvent;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public LocalDate getDates()
    {
        return date;
    }

    @Override
    public int getIdVenue()
    {
        return idVenue;
    }

    public Participant[] getParticipants()
    {
        return participants;
    }
}
