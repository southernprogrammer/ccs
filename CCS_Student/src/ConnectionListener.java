

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Bryan
 */

import java.net.*;
import java.io.*;

public class ConnectionListener extends Thread{
    private int socketnum;
    private int suSocketNum;
    public ConnectionListener(int socketnum, int suSocketNum)
    {
        this.socketnum = socketnum;
        this.suSocketNum = suSocketNum;
    }
    public @Override void run()
    {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        try {
            serverSocket = new ServerSocket(socketnum);
        }
        catch(IOException e)
        {
            System.out.println("The Student cannot listen to port: " + socketnum);
            System.exit(-1);
        }
        while(true)
        {
            try{
                clientSocket = serverSocket.accept();
                ConnectionClient c = new ConnectionClient(clientSocket, this.suSocketNum);
                c.start();
            }
            catch(IOException e)
            {
                System.out.println("Accept of teacher failed");
            }
        }
    }
}
