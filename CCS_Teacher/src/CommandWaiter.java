

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Bryan
 */

import java.util.*;

public class CommandWaiter extends Thread{
    private Vector<Student> students;
    private String command;
    private Process p;

    public CommandWaiter(Vector<Student> students, String command, Process p)
    {
        this.students = students;
        this.command = command;
        this.p = p;
    }
    public void run()
    {
        if(command.equals("vncviewer.exe"))
        {
            //we will send a command to single student computer
            //after the VNCViewer process has exited on the Teacher

            //executed in Student.java
            try{
                p.waitFor();
            }
            catch (java.lang.InterruptedException e){
                System.err.println(e);
            }
            students.firstElement().commandSender("c:\\program files\\ccs\\Nircmd\\nircmd killprocess WinVNC4.exe");
        }
        if(command.equals("MulticastVNC"))
        {
            //we will send commands to multiple student computers
            //after the MulticastVNC process has exited on the Teacher

            //executed in Interface.java
            try{
                p.waitFor();
            }
            catch (java.lang.InterruptedException e){
                System.err.println(e);
            }
            //stop the VNC Server from running on the Teacher
            Executor.execute("c:\\program files\\ccs\\Nircmd\\nircmd killprocess WinVNC4.exe");

            //and stop the multicastvnc viewer from running on all clients
            //that had launched the multicastvnc viewer
            CommandThread ct = new CommandThread("c:\\program files\\ccs\\Nircmd\\nircmd win close ititle \"MulticastVNC**~**\"", students);
            ct.start();
        }
    }
}
