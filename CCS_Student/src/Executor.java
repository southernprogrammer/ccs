

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Bryan
 */

import java.io.*;

public class Executor {
    public Executor()
    {

    }
    public static BufferedInputStream execute(String command)
    {
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
        //now set command back to its original state
        command = originalCommand;
        try
        {
            Process p;
            if(workingDir.equals(""))
                p = Runtime.getRuntime().exec(cmd);
            else
                p = Runtime.getRuntime().exec(cmd, null, new File(workingDir));
            bis = new BufferedInputStream(p.getInputStream());
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        return bis;
    }

    public static Process execute(String command, boolean returnProcess)
    {
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
        //now we can set the command back to its original state
        command = originalCommand;
        Process p = null;
        try{
            if(workingDir.equals(""))
                p = Runtime.getRuntime().exec(cmd);
            else
                p = Runtime.getRuntime().exec(cmd, null, new File(workingDir));
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        return p;
    }
}
