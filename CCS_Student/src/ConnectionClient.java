

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

public class ConnectionClient extends Thread{
    //this class deals with the teacher after a connection is made
    private Socket clientSocket;
    private Socket suSocket;
    private PrintWriter suOut;
    public ConnectionClient(Socket clientSocket, int suSocketNum)
    {
        this.clientSocket = clientSocket;
        this.suSocket = null;
        this.suOut = null;
        try{
            this.suSocket = new Socket("127.0.0.1", suSocketNum);
        }
        catch(Exception e){System.out.println("Could not create client socket for CCS Student SU");}
    }
    public @Override void run()
    {
        try {
            
            //Get Output Streams
            if(suSocket != null)
                suOut = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(suSocket.getOutputStream())));
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));

            // initiate conversation with teacher
            out.writeObject(new PayloadObject("Hello"));
            out.flush();

            //we needed to send something before creating this input stream
            //else we end up in a deadlock
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
            
            PayloadObject currObj = null;
            String command = "";
            String saltedCommand = "";
            String saltString = "";
            Encryption enc = new Encryption();
            boolean policy = false;
            boolean kmblock = false;
            boolean kmunblock = false;
            while ((currObj = ((PayloadObject)(in.readObject()))) != null) {
                //this is the meat of it all
                String currMessage = currObj.getMessage();
                if(currMessage.startsWith("Execute") || currMessage.startsWith("Policy") || currMessage.startsWith("KeyboardAndMouseBlocker") || currMessage.startsWith("KeyboardAndMouseUnblocker"))
                {
                    if(currMessage.startsWith("Execute"))
                        command = currMessage.substring(8).trim();
                    else if(currMessage.startsWith("KeyboardAndMouseBlocker"))
                    {
                        //the command and what it is doing is the same
                        command = "KeyboardAndMouseBlocker";
                        kmblock = true;
                    }
                    else if(currMessage.startsWith("KeyboardAndMouseUnblocker"))
                    {
                        command = "KeyboardAndMouseUnblocker";
                        kmunblock = true;
                    }
                    else if(currMessage.startsWith("Policy"))
                    {
                        policy = true;
                        command = currMessage.substring(7).trim();
                    }
                    
                    //get the command or policy string that we will likely enact

                    saltString = enc.getSalt();
                    //uses a salt of 10 bytes converted to a string
                    System.out.println("Encrypting: " + command);
                    saltedCommand = saltString + command;

                    out.writeObject(new PayloadObject("Verify", enc.encrypt(saltedCommand.getBytes())));
                    out.flush();
                    //encrypt with public key and send back to teacher
                }
                else if(currMessage.startsWith("Verified"))
                {
                    System.out.println("Checking Verification");
                    //make sure it's valid and run it
                    //the teacher should have decrypted with the private key
                    //and then encrypted with the private key
                    byte[] decrypt = enc.decrypt(currObj.getEncrypted());
                    String decryptedCommand = new String(decrypt);
                    if(decryptedCommand.equals(saltedCommand))
                    {
                        System.out.println("Running Command");
                        if(policy)
                            suWrite("ApplyPolicy~~" + command);
                        else if(kmblock || kmunblock)
                        {
                            suWrite(command);
                        }
                        else
                            Executor.execute(command);
                    }
                    out.writeObject(new PayloadObject("Bye"));
                    out.flush();
                    break; //we are sending bye so we can exit the loop
                }
                else
                {
                    break; //else "Bye" was probably sent
                }
            }
            out.close();
            in.close();
            if(suOut != null)
                suOut.close();
            //the clientSocket will get overwritten when the serverSocket
            //accepts a new connection, so no reason to overwrite
        }
        catch(Exception e)
        {
            System.out.println("There was a problem with the input or output stream " + e);
        }

    }

    public void suWrite(String command)
    {
        //send this to be run as a super user
        try {
            if(suOut != null)
            {
                suOut.write(command);
                suOut.flush();
            }
            else
                System.out.println("The Super User Process is Not Running");
        }
        catch(Exception e){System.out.println("Could not connect to super user process");return;}
    }
}
