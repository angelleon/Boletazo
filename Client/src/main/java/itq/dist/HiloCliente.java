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

public class HiloCliente extends Thread{
	private static final Logger logger = LogManager.getLogger(Client.class);
	static final String HOST ="172.16.8.93";
	static final int PORT = 5000;
	
	public void run() {
		nuevoClient();
	}
	public void nuevoClient(){
		
		//tank1-500-usr11
		String ip="172.16.8.63"; 
		String peticion = "16140244,"+ip+","+2001+",FINAL";
		
		//Formato : Cuenta#,monto,op| 
/*		
String peticion="Cuenta1,"+20+","+0+
	"|Cuenta1,"+10000+","+0+
	"|Cuenta1,"+1000+","+0+
	"|Cuenta1,"+100+","+0+
	"|Cuenta1,"+100+","+0+
	"|Cuenta1,"+100+","+0+
	"|Cuenta1,"+100+","+0+
	"|Cuenta1,"+100+","+0+
	"|Cuenta1,"+100+","+0+
	"|Cuenta1,"+100+","+0+
	"|Cuenta1,"+(-20)+","+0;
*/
		//String peticion ="tank3"+","+al.nextInt(2000)+","+"Usr1";
		try{
			
			logger.info("Intentando conexion");  
				
				Socket clientSocket = new Socket(HOST,PORT);
				OutputStream outStream = clientSocket.getOutputStream();
				DataOutputStream flowOut= new DataOutputStream(outStream);
				
				//logger.debug(peticion);
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
	}
}
