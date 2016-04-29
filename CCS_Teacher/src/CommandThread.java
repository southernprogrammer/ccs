

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
import java.util.*;

public class CommandThread extends Thread{
    //command is run on student computer
    private String command;
    private String executeOnTeacherAfter;
    //executeOnTeacher executes on the teacher after or before communication
    //with the student
    private String commandType;
    private Vector<Student> stus;

    public CommandThread(String command, Vector<Student> stus)
    {
        this.command = command;
        this.stus = stus;
        this.executeOnTeacherAfter = "";
        this.commandType = "Execute";
    }

    public CommandThread(String command, Vector<Student> stus, String commandType)
    {
        this.command = command;
        this.stus = stus;
        this.commandType = commandType;
        this.executeOnTeacherAfter = "";
    }
   
    public CommandThread(String command, Vector<Student> stus, String commandType, String executeOnTeacher)
    {
        this.command = command;
        this.stus = stus;
        this.commandType = commandType;
        this.executeOnTeacherAfter = executeOnTeacher;
    }
    public @Override void run()
    {
        Iterator itr = stus.iterator();
        while(itr.hasNext())
        {
            Student stu = (Student)itr.next();
            try{

                Socket theSocket = new Socket(stu.getHostName(), stu.getSocketNum());

                ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(theSocket.getOutputStream()));

                ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(theSocket.getInputStream()));
                

                PayloadObject fromStudent = null;
                Encryption enc = new Encryption();

                while((fromStudent = ((PayloadObject)(in.readObject()))) != null) //TODO: Maybe add a timeout feature
                {
                    String fromMessage = fromStudent.getMessage();
                    if(fromMessage.equals("Hello"))
                    {
                        //The student machine confirms it is alive
                        //so send the command
                        System.out.println("Sending Command");
                        if(commandType.toLowerCase().equals("execute"))
                            out.writeObject(new PayloadObject("Execute " + command));
                        else if(commandType.toLowerCase().equals("keyboardandmouseblocker"))
                            out.writeObject(new PayloadObject("KeyboardAndMouseBlocker"));
                        else if(commandType.toLowerCase().equals("keyboardandmouseunblocker"))
                            out.writeObject(new PayloadObject("KeyboardAndMouseUnblocker"));
                        else if(commandType.toLowerCase().equals("policy"))
                            out.writeObject(new PayloadObject("Policy " + command));
                        out.flush();
                    }
                    else if(fromMessage.startsWith("Verify"))
                    {
                        //the student responeded with the command we sent with a salt
                        //in front, and it encrypted the command with the public key
                        System.out.println("Verifying");
                        //decrypt the data we got back
                        byte[] decrypted = enc.decrypt(fromStudent.getEncrypted());
                        String decryptedString = new String(decrypted);
                        //check if we actually sent the command
                        if(decryptedString.contains(command))
                        {
                            byte[] sendBack = enc.encrypt(decrypted);
                            out.writeObject(new PayloadObject("Verified", sendBack));
                            out.flush();
                        }
                        else
                        {
                            //else we do not want them to run the command
                            out.writeObject(new PayloadObject("Bye"));
                            out.flush();
                        }
                    }
                    else //else Bye was probably sent
                    {
                        in.close();
                        out.close();
                        theSocket.close();
                        break;
                    }
                }
            }
            catch(Exception e)
            {
                javax.swing.JOptionPane.showMessageDialog(stus.firstElement().getComp(), "Could not Connect to Client: " + e);
            }
        }
        //if we have a command to execute on the teacher's computer
        //after the command is sent to the
        //student or students, run it
        if(!this.executeOnTeacherAfter.equals(""))
            Executor.execute(stus, executeOnTeacherAfter);
    }
}
