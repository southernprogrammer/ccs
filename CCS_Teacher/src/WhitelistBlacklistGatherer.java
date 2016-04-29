

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Bryan
 */
import javax.swing.*;
import java.io.*;

public class WhitelistBlacklistGatherer extends Thread{
    private JTextField programsList;
    public WhitelistBlacklistGatherer(JTextField programsList)
    {
        this.programsList = programsList;
    }
    public void run()
    {
        BufferedInputStream bis = Executor.execute("c:\\program files\\ccs\\CCS_Teacher\\GatherWhitelistBlacklist.exe");
        BufferedReader br = new BufferedReader(new InputStreamReader(bis));
        String line = "";
        programsList.setText("");
        try{
            while((line = br.readLine()) != null)
            {
                programsList.setText(programsList.getText() + line + ";");
            }
        }
        catch(Exception e){}

        if(programsList.getText().contains("whitelist;"))
        {
            programsList.setText(programsList.getText().replaceAll("whitelist;", " whitelist"));
        }
        else
        {
            programsList.setText(programsList.getText().replaceAll("blacklist;", " blacklist"));
        }
    }
}
