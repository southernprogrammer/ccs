

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Bryan
 */

import java.util.*;
import java.io.*;

public class Executor {
    public static BufferedInputStream execute(String command)
    {
        //sometimes we want to wait for the process to
        //terminate to execute a different command
        //this is the case with vncviewer.exe
        //because we need to stop the vncserver from running
        //on the client's computer
        Process p = null;
        BufferedInputStream bis = null;
        //interpret clsaspath='something' as setting
        //the working directory
        String originalCommand = command;
        String workingDir = "";
        if(command.contains("classpath='"))
        {
            //get the working directory
            workingDir = command.substring(command.indexOf("classpath='")+11, command.indexOf("'", command.indexOf("classpath='")+11));
            //now remove the information from the command
            command = command.replace(" classpath='" + workingDir + "'", "");
        }
        String[] cmd = command.split(" ");
        //now we can reset the command back to its original state
        command = originalCommand;

        try {
            if(workingDir.equals(""))
                p = Runtime.getRuntime().exec(cmd);
            else
                p = Runtime.getRuntime().exec(cmd, null, new File(workingDir));
            bis = new BufferedInputStream(p.getInputStream());
        }

        catch (Exception e)
        {
            System.out.println(e);
        }
        return bis;
    }
    public static BufferedInputStream execute(Vector<Student> stus, String command)
    {
        //sometimes we want to wait for the process to
        //terminate to execute a different command
        //this is the case with vncviewer.exe
        //because we need to stop the vncserver from running
        //on the client's computer
        Process p = null;
        BufferedInputStream bis = null;
        String originalCommand = command;
        String workingDir = "";
        if(command.contains("classpath='"))
        {
            //get the working directory
            workingDir = command.substring(command.indexOf("classpath='")+11, command.indexOf("'", command.indexOf("classpath='")+11));
            //now remove the information from the command
            command = command.replace(" classpath='" + workingDir + "'", "");
        }
        String[] cmd = command.split(" ");
        //and now command can be the originalCommand
        command = originalCommand;
        try {
            if(workingDir.equals(""))
                p = Runtime.getRuntime().exec(cmd);
            else
                p = Runtime.getRuntime().exec(cmd, null, new File(workingDir));

            bis = new BufferedInputStream(p.getInputStream());
        }

        catch (Exception e)
        {
            System.out.println(e);
        }

        if(p != null && stus != null)
        {
            //if it's a command that needs a command waiter
            
            if(command.contains("vncviewer.exe"))
            {
                CommandWaiter cw = new CommandWaiter(stus, "vncviewer.exe", p);
                cw.start();
            }
            else if(command.contains("MulticastVNC"))
            {
                //when MulticastVNC is done
                //we can shut down the VNCServer on the teacher's computer
                CommandWaiter cw = new CommandWaiter(stus, "MulticastVNC", p);
                cw.start();
            }
        }
        return bis;
    }
}
