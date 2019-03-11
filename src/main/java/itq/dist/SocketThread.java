package itq.dist;

import java.net.Socket;

public class SocketThread extends Thread
{
    private Socket socket;

    SocketThread(Socket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {

    }
}
