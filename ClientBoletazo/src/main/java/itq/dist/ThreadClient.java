package itq.dist;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
//import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadClient extends Thread{
	private static final Logger lOG = LogManager.getLogger(ThreadClient.class);
	static final String HOST = "<ipServer>";
	static final int PORT= "<portShared>";
	private static DataOutputStream flowOut;
	private static int client;
	// variables to test
	private static int idSession = 1;
	private static int step = 0;
	
	//KAZO NO KANOJO
	
	public ThreadClient(int client) {
	    this.client=client;
	}
	
	public DataOutputStream newMessage() {
	    Socket clientSocket = new Socket(HOST,PORT);
        OutputStream outStream = clientSocket.getOutputStream();
        return flowOut= new DataOutputStream(outStream);     
	}
	public void run() {
	    cStartSession();
	    getEventList();
	    getEventInfo();
	    getAvailableSeats();
	    requestReserveTickets();
	    
	    if(Client==2) {
	        sigup();
	    }
	    
	    loginCheck();
	    postPaymentInfo();
	}
	
	private void complete() {
	    cStartSession();
        getEventList();
        getEventInfo();
        getAvailableSeats();
        requestReserveTickets();
        
        if(Client==2) {
            sigup();
        }
        
        loginCheck();
        postPaymentInfo();
	}
	private boolean cStartSession() {
	    try {
	        
	        DataOutputStream messageOut = newMessage();
            messageOUt="";
            logger.info(" conectado con solicitud\n :"+peticion);
            InputStream inStream= clientSocket.getInputStream();
            
            DataInputStream dataIn = new DataInputStream(inStream);
            String input = dataIn.readUTF().toString();
            //respuesta
            logger.info("Respuesta serv: "+input);              
            clientSocket.close();
	    }catch(UnknownHostException e) {
            logger.error("without conection to host "+HOST+" on port "+PORT);
            e.printStackTrace();
            
        }catch(IOException e){
            logger.error(" IOException "+e.printStackTrace().toString());
        }
	    return false;
	}
	private boolean getEventList() {
        
    }
	private boolean getEventList() {
   
    }
    private boolean getEventInfo() {
            
    }
    private boolean getAvailableSeats() {
        
    }
    private boolean requestReserveTickets() {
        
    }
    private boolean sigup() {
        
    }
    private boolean loginCheck() {
        
    }
    private boolean posPaymentInfo() {
        
    }
	
	/*
	public void nuevoClient(){

		try{
			
			LOG.info("Intentando conexion");  
				
				Socket clientSocket = new Socket(HOST,PORT);
				OutputStream outStream = clientSocket.getOutputStream();
				DataOutputStream flowOut= new DataOutputStream(outStream);
				
				LOG.debug(peticion);
				flowOut.writeUTF(peticion);
				logger.info(" conectado con solicitud\n :"+peticion);
				InputStream inStream= clientSocket.getInputStream();
				DataInputStream dataIn = new DataInputStream(inStream);
				String input = dataIn.readUTF().toString();
				//respuesta
				logger.info("Respuesta serv: "+input);				
				clientSocket.close();
				
			}catch(UnknownHostException e) {
				logger.error("Error de conexion del host "+HOST+" con puerto "+PORT);
				e.printStackTrace();
				
			}catch(IOException e){
				logger.error(" Error en el canal de datos "+HOST+" con puerto "+PORT);
				e.printStackTrace();
			}
	}*/
	
}
