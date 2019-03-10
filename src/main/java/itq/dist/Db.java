import java.lang.jdbc;

public class Db
{
	private static final String usr = "boletazodev";
	private static final String passwd = "contrapass";
	private conn;
	
	Db()
	{
		try
		{
			
		}
		catch (SqlException e)
		{
			log.error("An error occurred when trying to connect to DB");
			log.error(e.getMessage());
		}
	}
}