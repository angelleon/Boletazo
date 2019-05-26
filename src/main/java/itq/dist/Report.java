package itq.dist;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Report
{
    private Connection conn;
    private static final Logger LOG = LogManager.getLogger(); 
    private static final String RUTE_REPORT = "C:/opt/eclipse/Espacio/PROYECTO/Reports/"; 
    private static final String SOLD_TODAY = 
             " select event.name Event,venue.name Place,section.cost,ticket.idstatus, sold_tickets.idcard, sold_tickets.iduser "
           + " from ticket,event,venue,section,sold_tickets "
           + " where ticket.idstatus > 1 "
           + " and event.idevent = ticket.idevent "
           + " and event.idvenue = venue.idvenue "
           + " and ticket.idsection = section.idsection "
           + " and ticket.idticket = sold_tickets.idticket "
           + " and date_format(sold_tickets.datesale, '%H:%i:%s') > '00:00:00' "  
           + " and date_format(sold_tickets.datesale, '%H:%i:%s') < '23:59:59' "
           + " and date_format(sold_tickets.datesale, '%d-%m-%Y') == date_format(curdate()-1,'%d-%m-%Y') "
           + " order by section.cost,sold_tickets.idcard ";

    private static FileWriter newfile;
    private static BufferedWriter bw;
    private static Date date;
    private static SimpleDateFormat dateMask;
    public Report() throws IOException{       
        date = new Date();
        dateMask = new SimpleDateFormat("dd-MM-yyyy");
        File file = new File(RUTE_REPORT+dateMask.format(date)+".txt");
        file.createNewFile();
        newfile = new FileWriter(file);
    }
    
    /**
     * check the hour to make a report
     * @throws SQLException
     * @throws IOException
     */
    public void sendReport() throws SQLException, IOException {
        Calendar calendar = new GregorianCalendar();
        Date today = new Date();
        calendar.setTime(today);
        
        int hr = calendar.get(Calendar.HOUR_OF_DAY);//format 24
        int minute = calendar.get(Calendar.MINUTE); // 0 - 59 
        
        if(hr == 3 && minute == 0) {
            LOG.info("Time to report!");
            reportDay();
        }
    }
    /***
     * Generate de report of last day
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public String reportDay() throws SQLException, IOException{
        bw = new BufferedWriter(newfile);
        bw.newLine();
        bw.append("Event          |\t Venue      |\t Cost     |\t statusticket      |\t numero tarjeta      |");
        bw.newLine();
        bw.append("*******************************************************************************");
        
       try {
           PreparedStatement ps = conn.prepareStatement(SOLD_TODAY);
           ResultSet resp = ps.executeQuery();
           while(resp.next()) {
               String eventName = resp.getString("Event");
               String site = resp.getString("Place");
               double cost = resp.getDouble("cost");
               int status = resp.getInt("idStatus");
               int card = resp.getInt("Card");
               bw.newLine();
               bw.append(eventName+"|\t"+site+"|\t"+cost+"|\t"+status+"|\t"+card);
           }           
           bw.newLine();
           bw.append("*******************************************************************************");
           LOG.debug("Check report on : "+RUTE_REPORT+dateMask.format(date)+".txt");
           bw.close();
           
       }catch(SQLException e) {
           LOG.error("Error on BD ,you can't finish the report");
           e.printStackTrace();
       }catch(IOException e) {
           LOG.error("You can´t write report document");
           e.printStackTrace();
       }
       return RUTE_REPORT+dateMask.format(date)+".txt";
    }   
}
