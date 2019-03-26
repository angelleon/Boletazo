package itq.dist;

public class Participant
{
    String name;
    String description;

    /**
     * @param name
     * @param description
     */

    /**
     * @param name
     *            the capacity to set
     * @param description
     *            the description to set
     */
    Participant(String name, String description)
    {
        this.name = name;
        this.description = description;
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
