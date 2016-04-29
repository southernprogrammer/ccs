

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Bryan
 */

import edu.columbia.bonaha.*;

public class Student implements BListener
{
    private BService service;
    private String classroom;
    public Student(String classroom, int socketnum, int suSocketNum)
    {
        this.classroom = classroom;
        this.listen();
        ConnectionListener conList = new ConnectionListener(socketnum, suSocketNum);
        conList.start();
    }
    public void listen()
    {
        //each student machine should be registered to a classroom
        //all computers in a room should be configured the same
        service = new BService (this.classroom, "tcp");
        service.register();
        service.setListener(this);
    }
        
    public void serviceUpdated(BNode n) {
        //we actually don't have to keep up with anything
    }
        
    public void serviceExited(BNode n) {
        //there is nothing to do
    }
}
