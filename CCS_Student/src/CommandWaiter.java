

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Bryan
 */

public class CommandWaiter extends Thread{
    private String command;
    private Process p;
    private boolean recursive;

    public CommandWaiter(String command, Process p, boolean recursive)
    {
        this.command = command;
        this.p = p;
        this.recursive = recursive;
    }
    public void run()
    {
            try{
                p.waitFor();
                //run the command after the process has exited
                Process newProcess = Executor.execute(command, true);
                if(recursive)
                {
                    CommandWaiter cw = new CommandWaiter(command, newProcess, true);
                    cw.start();
                }
            }
            catch (java.lang.InterruptedException e){
                System.err.println(e);
            }
    }
}
