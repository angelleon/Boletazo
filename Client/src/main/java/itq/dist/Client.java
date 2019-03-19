package itq.dist;

public class Client {	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//numero de peticiones al banco 
		//for(int i = 0 ;i<10;i++) {
		Thread t_client = new HiloCliente();
		t_client.start();
		//}
		
	}
}




