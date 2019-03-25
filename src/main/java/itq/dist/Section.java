package itq.dist;

public class Section
{
    int idSection;
    String name;
    float cost;

    Section(int idSection, String name, float cost)
    {
        this.idSection = idSection;
        this.name = name;
        this.cost = cost;
    }

    public int getIdSection()
    {
        return idSection;
    }

    public String getName()
    {
        return name;
    }

    public float getCost()
    {
        return cost;
    }
}
