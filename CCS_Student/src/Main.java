

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Bryan
 */

public class Main {
    public static void main(String[] args)
    {
        Configuration conf = new Configuration("c:\\program files\\ccs\\CCS_Student\\Configuration.dat");
        Student stu = new Student("ccs_" + conf.get("classroomname"), Integer.parseInt(conf.get("socketnum")), Integer.parseInt(conf.get("ccssuport")));
    }
}
