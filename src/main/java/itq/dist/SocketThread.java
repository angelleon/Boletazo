package itq.dist;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.events.EventException;
import org.apache.logging.log4j.LogManager;

public class SocketThread extends Thread
{
    private static final Logger log = LogManager.getLogger(SocketThread.class);

    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private Db db;
    private SessionControl sc;
    

    
    private static enum STATE {
        C_START_SESSION,
        S_START_SESSION,
        GET_EVENT_LIST,
        POST_EVENT_LIST,
        GET_EVENT_INFO,
        POST_EVENT_INFO,
        GET_AVAILABLE_SEATS,
        POST_AVAILABLE_SEATS,
        REQUEST_RESERVE_TICKETS,
        CONFIRM_RESERVE_TICKETS,
        SINGUP,
        SINGUP_STATUS,
        LOGIN_CHECK,
        LOGIN_STATUS,
        POST_PAYMENT_INFO,
        PUCHARASE_COMPLETED
    }

    private int currentState;

    SocketThread(Socket socket, Db db, SessionControl sc)
    {
        this.socket = socket;
        this.db = db;
        this.sc = sc;
        this.currentState = STATE.C_START_SESSION.ordinal();
    }

    @Override
    public void run()
    {
        try
        {
            cStartSession();
            sStartSession();
            getEventList();
            postEventList();
            getEventInfo();
            postEventInfo();
            getAvailableSeats();
            postAvailableSeats();
            requestReserveTickets();
            confirmReserveTickets();
            if (currentState == STATE.SINGUP.ordinal())
            {
                singup();
                singupStatus();
            }
            loginCheck();
            loginStatus();
            postPaymentInfo();
            pucharaseCompleted();

        }
        catch (ConversationException e)
        {

        }
        catch (IOException e)
        {

        }
    }

    // ToDo: implementar un metodo por cada constante del enum STATE
    // ToDo: considerar lanzar excepciones para manejar todos los errores de la
    // conversacion dentro de un bloque catch

    private boolean cStartSession() throws ConversationException, IOException
    {
    
    	String rawMsg = dataIn.readUTF();
        String[] valuesIn;
        return false;
    }

    private boolean sStartSession() throws ConversationException, IOException
    {
        return false;
    }

    private boolean getEventList() throws ConversationException, IOException
    {
        return false;
    }

    private boolean postEventList() throws ConversationException, IOException
    {
    	String msg = dataIn.readUTF();
    	String[] dataMSG = msg.split(",");
    	Event[] event;
    	//msj := “2,1020(,EN,VN,ED,H,C,SN)” 3 = VENUE NAME 
    	event = db.getEventsWhere(dataMSG[3]);
    	msg = STATE.POST_EVENT_LIST + "," + dataMSG[1];
    	for(int i = 0; i < event.length; i++) {
    		if(i == event.length - 1)
    			msg += event[i].getIdEvent() + "|";
    		else
    			msg += event[i].getIdEvent() + ",";
    	}
    	for(int i = 0; i < event.length; i++) {    		
    		if(i == event.length - 1)
    			msg += event[i].getName() + "|";
    		else
    			msg += event[i].getName();
    	}
    	dataOut.writeUTF(msg);
        return false;
    }

    private boolean getEventInfo() throws ConversationException, IOException
    {
        return false;
    }

    private boolean postEventInfo() throws ConversationException, IOException
    {
        return false;
    }

    private boolean getAvailableSeats()
            throws ConversationException, IOException
    {
        return false;
    }

    private boolean postAvailableSeats()
            throws ConversationException, IOException
    {
        return false;
    }

    private boolean requestReserveTickets()
            throws ConversationException, IOException
    {
    	String msg = dataIn.readUTF();
    	String[] dataMSG = msg.split(",");
    	msg = STATE.CONFIRM_RESERVE_TICKETS + ",";
    	for(int i = 0; i < dataMSG.length - 1; i++) {
    		if(i == dataMSG.length - 2)
    			msg += dataMSG[i+1];		
    		else
    			msg += dataMSG[i+1] + ",";
    	}
    	//msj := “8,1020,1,4,31,32,33,34”
    	int[] idTicket = new int[dataMSG.length - 4];
    	for(int i = 0; i < idTicket.length; i++) {
    		idTicket[i] = Integer.parseInt(dataMSG[4+i]);
    	}
    	
    	for(Boleto ticket : db.availableTickets.values()) {
    		for(int i = 0; i < idTicket.length; i++) {
    			if(ticket.getIdTicket() == idTicket[i] && ticket.getIdStatus() == 1) {
    				dataOut.writeUTF(msg);	//envio de respuesta al cliente
    				ticket.TicketPurchase();//espera por confirmacion.
    			}
    		}
    	}	
    	return false;
    }

    private boolean confirmReserveTickets()
            throws ConversationException, IOException
    {
    	String msg = dataIn.readUTF();
    	String[] dataMSG = msg.split(",");
    	//msj := “12,2020,123-123-123-123,04/22,333,VISA|MASTERCARD”
    	
    	int[] idTicket = new int[dataMSG.length - 4];
    	for(int i = 0; i < idTicket.length; i++) {
    		idTicket[i] = Integer.parseInt(dataMSG[4+i]);
    	}
    	
    	for(Boleto ticket : db.availableTickets.values()) {
    		for(int i = 0; i < idTicket.length; i++) {
    			if(ticket.getIdTicket() == idTicket[i] && ticket.getIdStatus() == 1) {
    				dataOut.writeUTF(msg);
    				ticket.TicketPurchase();
    			}
    		}
    	}
    	return false;
    }

    private boolean singup() throws ConversationException, IOException
    {
        return false;
    }

    private boolean singupStatus() throws ConversationException, IOException
    {
        return false;
    }

    private boolean loginCheck() throws ConversationException, IOException
    {
        return false;
    }

    private boolean loginStatus() throws ConversationException, IOException
    {
        return false;
    }

    private boolean postPaymentInfo() throws ConversationException, IOException
    {
        return false;
    }

    private boolean pucharaseCompleted()
            throws ConversationException, IOException
    {
        return false;
    }

    private String[] spliceMsgTokens(String rawMsg, int nTokens, int[] arrayLengthPositions)
    {

        return new String[0];
    }

    private void checkMsgIntegrity(String rawMsg)
    {

    }
}
