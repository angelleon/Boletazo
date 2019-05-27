package itq.dist;

public class ReportInfo
{
    String eventName;
    String site;
    double cost;
    int status;
    int card;

    public ReportInfo(String eventName, String site, double cost, int status, int card)
    {
        this.eventName = eventName;
        this.site = site;
        this.cost = cost;
        this.status = status;
        this.card = card;
    }

    public String getEventName()
    {
        return eventName;
    }

    public void setEventName(String eventName)
    {
        this.eventName = eventName;
    }

    public String getSite()
    {
        return site;
    }

    public void setSite(String site)
    {
        this.site = site;
    }

    public double getCost()
    {
        return cost;
    }

    public void setCost(double cost)
    {
        this.cost = cost;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public int getCard()
    {
        return card;
    }

    public void setCard(int card)
    {
        this.card = card;
    }
}