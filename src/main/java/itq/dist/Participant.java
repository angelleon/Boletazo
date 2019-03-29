package itq.dist;

public class Participant
{
    int idParticipant;
    String name;
    String description;

    /**
     * @param idParticipant
     *            the id to set
     * @param name
     *            the name to set
     * @param description
     *            the description to set
     */
    Participant(int idParticipant, String name, String description)
    {
        this.idParticipant = idParticipant;
        this.name = name;
        this.description = description;
    }

    public int getIdParticipant()
    {
        return idParticipant;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return description
     */
    public String getDescription()
    {
        return description;
    }

}
