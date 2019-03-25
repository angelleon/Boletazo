package itq.dist;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.lang.StringBuilder;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SocketThread extends Thread
{
    private static final Logger log = LogManager.getLogger(SocketThread.class);
    private static final int MAX_MSG_LENGTH = 1024;

    private Socket socket;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private Db db;
    private SessionControl sc;
    private int sessionId;
    private StringBuilder msgOut;
    
    private int nRequestedTickets;
    private int idEvent;
    private int[] tickets_array;
    private String usr;
	private String pass;
	private String email;
	private String stateResidence;
	
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

    private STATE currentState;

    private enum TYPES {
        NULL,
        INT,
        FLOAT,
        STRING,
        ARRAY
    }

    SocketThread(Socket socket, Db db, SessionControl sc)
    {
        this.socket = socket;
        this.db = db;
        this.sc = sc;
        this.msgOut = new StringBuilder(MAX_MSG_LENGTH);
        this.currentState = STATE.C_START_SESSION;
        try
        {
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException e)
        {
            log.error(e.getMessage());
        }
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
            if (currentState == STATE.SINGUP)
            {
                singup();
                singupStatus();
            }
            loginCheck();
            loginStatus();
            postPaymentInfo();
            pucharaseCompleted();
            socket.close();
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

    // La tablita .....si hay sesion tabla.. ok no..
    private boolean cStartSession() throws ConversationException, IOException
    {
        String rawMsg = dataIn.readUTF();
        String[] valuesIn = spliceMsgTokens(rawMsg, getTokenNumber(), getArrayLengthPositions());
        return false;
    }

    private boolean sStartSession() throws ConversationException, IOException
    {
        currentState = STATE.S_START_SESSION;
        msgOut.setLength(0);
        int sessionId = sc.getNewSessionId();
        if (sessionId > 0) // #ArmaTuMensaje
        {
            msgOut.append(STATE.S_START_SESSION.ordinal());
            msgOut.append(sessionId);
            dataOut.writeUTF(msgOut.toString());
            return true;
        }
        return false;
    }

    private boolean getEventList() throws ConversationException, IOException
    {
        currentState = STATE.GET_EVENT_LIST;
        return false;
    }

    private boolean postEventList() throws ConversationException, IOException
    {
        currentState = STATE.POST_EVENT_LIST;
        return false;
    }

    private boolean getEventInfo() throws ConversationException, IOException
    {
        currentState = STATE.GET_EVENT_INFO;
        return false;
    }

    private boolean postEventInfo() throws ConversationException, IOException
    {
        currentState = STATE.POST_EVENT_INFO;
        return false;
    }

    private boolean getAvailableSeats()
            throws ConversationException, IOException
    {
        currentState = STATE.GET_AVAILABLE_SEATS;
        return false;
    }

    private boolean postAvailableSeats() throws ConversationException, IOException
    {
        currentState = STATE.POST_AVAILABLE_SEATS;
        return false;
    }

    //opCode = 8 
    private boolean requestReserveTickets()
            throws ConversationException, IOException
    {
    	currentState =STATE.REQUEST_RESERVE_TICKETS;
    	//msgOut.setLength(0);   
    	String rawMsg = dataIn.readUTF();
    	
    	if(checkMsgIntegrity(rawMsg) && sessionId >0){
    		String[] parts = rawMsg.split(",");
    		int opcode = Integer.parseInt(parts[0]);
    		sessionId = Integer.parseInt(parts[1]);
    		idEvent = Integer.parseInt(parts[2]);
    		// ver que el nRequestedTickets sea menor o igual que el numero de tickets permitidos a comprar
    		nRequestedTickets = Integer.parseInt(parts[3]);
    		
    		tickets_array = new int[nRequestedTickets];
    		//desde la posicion de nRequestedTicked + nRequestTicked
    		
    		int numPart = 2;
    		//array con los tickets solicitados
    		for(int i = 0;i<nRequestedTickets;i++) {
    			numPart++;
    			tickets_array[i] = Integer.parseInt(parts[numPart]);
    			
    		}
    		//debe comenzar aca el contador del tiempo?
    		//consultar cada ticket 
    		
    		return true;
    	}
        return false;
    }

    private boolean confirmReserveTickets()
            throws ConversationException, IOException
    {
    	currentState =STATE.CONFIRM_RESERVE_TICKETS;
    	msgOut.setLength(0);
    	 if (sessionId > 0) // #ArmaTuMensaje
         {
             msgOut.append(currentState);
             msgOut.append(sessionId);
             float cost = 0;
             ///.............Calcular el  costo...e_é
             
             
             msgOut.append(cost);
             msgOut.append(nRequestedTickets);
             
             
             dataOut.writeUTF(msgOut.toString());
             return true;
         }
    	
        return false;
    }
   
    private boolean singup() throws ConversationException, IOException
    {
    	currentState =STATE.SINGUP;
    	String rawMsg = dataIn.readUTF();
    	if(checkMsgIntegrity(rawMsg)){
    		String[] parts = rawMsg.split(",");
    		//int opcode = Integer.parseInt(parts[0]);
    		sessionId = Integer.parseInt(parts[1]);
    		 usr = parts[2];
    		 pass = parts[3];
    		 email = parts[4];
    		 stateResidence = parts[5];
    		 if(db.singup(email)){
    			 log.info("usuario registrado: "+usr+" - "+email);
    			return true;
    		 }
    		 log.error("no se puede registrar "+rawMsg);
    	}
        return false;
    }

    /**
     * Create a new user for Boletazo	 
     */
    private boolean singupStatus() throws ConversationException, IOException
    {
    	currentState = STATE.SINGUP_STATUS;
    	msgOut.setLength(0);
    	
    	msgOut.append(currentState);
		msgOut.append(sessionId);
    	if(db.toRegister(usr, pass,email,stateResidence)) {
    		msgOut.append(0);
    		dataOut.writeUTF(msgOut.toString());
    		
    		return true;
    	}
    	msgOut.append(1);
    	dataOut.writeUTF(msgOut.toString());
    	log.info("usuario equivocado o falta registrar "+usr+" - " +email);	
        return false;
    }

    private boolean loginCheck() throws ConversationException, IOException
    {
    	currentState =STATE.LOGIN_CHECK;
    	//msgOut.setLength(0);
    	String rawMsg = dataIn.readUTF();
    	
    	if(checkMsgIntegrity(rawMsg) && sessionId >0){
    		String[] parts = rawMsg.split(",");
    		int opCode = Integer.parseInt(parts[0]);
    		sessionId = Integer.parseInt(parts[1]);
    		usr = parts[2];
    		pass = parts[3];
    		if(db.login(usr,pass)) {
    			return true;
    		}
    	}
    	log.debug(" algo esta mal escrito U_U "+rawMsg);
        return false;
    }

    private boolean loginStatus() throws ConversationException, IOException
    {
    	currentState =STATE.LOGIN_STATUS;
    	msgOut.setLength(0);
    	msgOut.append(currentState);
		msgOut.append(sessionId);
		if(db.login(usr, pass)) {
			msgOut.append("0");
			dataOut.writeUTF(msgOut.toString());
			return true;
		}
		msgOut.append("1");
		dataOut.writeUTF(msgOut.toString());
        return false;
    }
    /** 14)
     * Client confirm that he wants the tickets
     * @return 
     * @throws ConversationException
     * @throws IOException
     */

    private boolean postPaymentInfo() throws ConversationException, IOException
    {
        currentState = STATE.POST_PAYMENT_INFO;
        String rawMsg = dataIn.readUTF();
    	
    	if(checkMsgIntegrity(rawMsg) && sessionId >0){
    		String[] parts = rawMsg.split(",");
    		int opcode = Integer.parseInt(parts[0]);
    		sessionId = Integer.parseInt(parts[1]);
    		String numberCard = parts[2];
    		if(numberCard.length()==20) {
    			String date = parts[3];
    			String cvv = parts[4];
    			String type = parts[5];
    			if(type.equals("VISA")||type.equals("MASTERCARD")) {
    				log.info("Compra por :"+numberCard+","+date+","+cvv+","+type);
    				return true;
    			}
    			log.error("tipo tarjeta incorrecta ");
    		}
    		log.error("longuitud tarjeta incorrecta ");
    	}
        return false;
    }

    private boolean pucharaseCompleted()
            throws ConversationException, IOException
    {
    	currentState =STATE.PUCHARASE_COMPLETED;
    	msgOut.setLength(0);
    	
    	msgOut.append(currentState);
		msgOut.append(sessionId);
		msgOut.append(nRequestedTickets);
		msgOut.append("el arreglo de los tickets....");
		if(db.update_ticket_status(tickets_array)) {
			msgOut.append("0");
			return true;
		}
		msgOut.append("1");
		dataOut.writeUTF(msgOut.toString());
        return false;
    }

    private String[] spliceMsgTokens(String rawMsg, int nTokens, int[] arrayLengthPositions)
    {

        return new String[0];
    }

    private boolean checkMsgIntegrity(String rawMsg) throws ConversationException
    {
        String[] valuesIn = rawMsg.split(",");
        int nTokens = getTokenNumber();
        int[] arrayLength = getArrayLengthPositions();
        int n = valuesIn.length;
        if (n < nTokens)
        {
            throw new ConversationException(ConversationException.ERROR.NOT_ENOUGH_ARGUMENTS);
        }
        else
        {
            switch (currentState)
            {
            case C_START_SESSION:
                break;
            case GET_AVAILABLE_SEATS:
                break;
            case GET_EVENT_INFO:
                break;
            case GET_EVENT_LIST:
                break;
            case LOGIN_CHECK:
                break;
            case LOGIN_STATUS:
                break;
            case POST_EVENT_INFO:
                break;
            case POST_EVENT_LIST:
                break;
            case POST_PAYMENT_INFO:
                break;
            case PUCHARASE_COMPLETED:
                break;
            case REQUEST_RESERVE_TICKETS:
                break;
            case SINGUP:
                break;
            case S_START_SESSION:
                break;
            default:
                break;

            }
        }
        return false;
    }

    private int getTokenNumber()
    {
        switch (currentState)
        {
        case C_START_SESSION:
            return 2;
        case GET_EVENT_LIST:
            return 8;
        case GET_EVENT_INFO:
            return 3;
        case GET_AVAILABLE_SEATS:
            return 3;
        case REQUEST_RESERVE_TICKETS:
            return 5;
        case SINGUP:
            return 5;
        case LOGIN_CHECK:
            return 4;
        case POST_PAYMENT_INFO:
            return 7;
        default:
            return -1;
        }
    }

    private int[] getArrayLengthPositions() throws ConversationException
    {
        switch (currentState)
        {
        case C_START_SESSION:
            return new int[0];
        case S_START_SESSION:
            return new int[0];
        case GET_EVENT_LIST:
            return new int[0];
        case POST_EVENT_LIST:
            return new int[] {3};
        case GET_EVENT_INFO:
            return new int[0];
        case POST_EVENT_INFO:
            return new int[] {7};
        case GET_AVAILABLE_SEATS:
            return new int[0];
        case POST_AVAILABLE_SEATS:
            return new int[] {3};
        case REQUEST_RESERVE_TICKETS:
            return new int[0];
        case CONFIRM_RESERVE_TICKETS:
            return new int[] {2};
        case SINGUP:
            return new int[0];
        case SINGUP_STATUS:
            return new int[0];
        case LOGIN_CHECK:
            return new int[0];
        case LOGIN_STATUS:
            return new int[0];
        case POST_PAYMENT_INFO:
            return new int[0];
        case PUCHARASE_COMPLETED:
            return new int[0];
        default:
            throw new ConversationException();
        }
    }

    @SuppressWarnings("incomplete-switch")
    private TYPES[] getArgumentTypes()
    {
        TYPES[] t = new TYPES[getTokenNumber()];
        switch (currentState)
        {
        case C_START_SESSION:
            t[0] = TYPES.INT;
            t[1] = TYPES.NULL;
            break;
        case GET_EVENT_LIST:
            t[0] = TYPES.INT;
            t[1] = TYPES.INT;
            t[2] = TYPES.STRING;
            t[3] = TYPES.STRING;
            t[4] = TYPES.STRING;
            t[5] = TYPES.INT;
            t[6] = TYPES.FLOAT;
            t[7] = TYPES.STRING;
            break;
        case GET_EVENT_INFO:
        case GET_AVAILABLE_SEATS:
            t[0] = TYPES.INT;
            t[1] = TYPES.INT;
            t[2] = TYPES.INT;
            break;
        case REQUEST_RESERVE_TICKETS:
            t[0] = TYPES.INT;
            t[1] = TYPES.INT;
            t[2] = TYPES.INT;
            t[3] = TYPES.INT;
            t[4] = TYPES.ARRAY;
            break;
        case SINGUP:
            t[0] = TYPES.INT;
            t[1] = TYPES.STRING;
            t[2] = TYPES.STRING;
            t[3] = TYPES.STRING;
            t[4] = TYPES.STRING;
            break;
        case LOGIN_CHECK:
            t[0] = TYPES.INT;
            t[1] = TYPES.INT;
            t[2] = TYPES.STRING;
            t[3] = TYPES.STRING;
            break;
        case POST_PAYMENT_INFO:
            t[0] = TYPES.INT;
            t[1] = TYPES.INT;
            t[2] = TYPES.INT;
            t[3] = TYPES.STRING;
            t[4] = TYPES.STRING;
            t[5] = TYPES.STRING;
            t[6] = TYPES.STRING;
            break;
        }
        return t;
    }
}
