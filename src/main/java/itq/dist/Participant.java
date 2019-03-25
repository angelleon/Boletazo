package itq.dist;

public class Participant
{
    String name;
    String description;

    Participant(String name, String description)
    {
        this.name = name;
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

}
