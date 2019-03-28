package itq;

public class Client
{

    public static void main(String[] args) {    

        //client type 1 (without sigup) but... with all steps
        Thread t_client = new ThreadClient(1);
        t_client.start();
        //client type 2 ...with sigup!, with all steps
        Thread t_client2 = new ThreadClient(2);
        t_client2.start();

        /* para seleccionar en que paso comenzar #unaIdea
        int step = 0;
        Thread tClientStep = new ThreadClient(1,step);
        tClientStep.start();
        
        Thread tClientStep = new ThreadClient(2,step);
        tClientStep.start();
        */
       
    }

}
