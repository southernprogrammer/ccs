

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Bryan
 */
import java.sql.*;
import java.util.*;
import java.awt.Component;
import java.io.*;

public class DBHandler {
    private Connection con;
    private Component comp;
    public DBHandler(Component c, String commonappdataloc)
    {
        comp = c;
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            String appdata = System.getenv("appdata");
            File ccsAppData = new File(appdata + "\\ccs");
            //if the local appdata folder does not exist for ccs create it
            //and copy the blank db to it
            if(!ccsAppData.exists())
            {
                //first create the ccsAppData folder
                Executor.execute("cmd /C mkdir %appdata%\\ccs");
                BufferedInputStream bis = Executor.execute("cmd /C xcopy " + commonappdataloc + "\\ccs\\* %appdata%\\ccs\\* /E");
                BufferedReader br = new BufferedReader(new InputStreamReader(bis));
                //wait until it's done
                while(!br.readLine().equals("75 File(s) copied"))
                    System.out.print("");
            }
            this.con = DriverManager.getConnection("jdbc:derby:" + appdata + "\\ccs\\ccsDB");
        }
        catch(Exception e){
            javax.swing.JOptionPane.showMessageDialog(comp, "The Database Could Not Connect");
            System.exit(1);
        }

    }
    public boolean addStudents(Vector<Object> node)
    {
        if (node.size() == 0)
        {
            System.out.println("No students to add");
            return true;
        }
        //add students to the students table
        try{
            Statement sta = this.con.createStatement();
            //we only insert 1 at a time, we update in bulk
            String query = "insert into students values('" + node.get(0).toString() + "', 0, 0, 130, 100)";
            System.out.println(query);
            sta.execute(query);
        }
        catch(Exception e)
        {
            System.out.println("Could not run the addStudents query");
            return false;
        }
        return true;
    }
    public boolean removeStudents(Vector<Object> node)
    {

        try{
            Statement sta = this.con.createStatement();
            //if only 1 student is being removed
            if(node.size() == 1)
            {
                String query = "delete from students where hostname='" + node.get(0).toString()  + "'";
                System.out.println(query);
                sta.execute(query);
            }
            else
            {
                String query = "delete from students where hostname =";
                Iterator itr = node.iterator();
                int count = 0;
                while(itr.hasNext())
                {
                    //if it is the last thing to delete
                    if(count == (node.size() - 1))
                        query = query + "'" + itr.next().toString() + "'";
                    else
                        query = query + "'" + itr.next().toString() + "' or hostname=";

                    count++;
                }
                //run the built query
                System.out.println(query);
                sta.execute(query);
            }
        }
        catch(Exception e) {
            System.out.println("There was a problem removing the slected students");
        }


        //Also test multiple computers being added into db at same time
        //remove students from the student table
        return true;
    }

    public boolean addTeacher(String teacher)
    {
        boolean success = false;
        String query = "INSERT INTO TEACHER VALUES ('" + teacher.trim() + "')";
        try{
            Statement sta = con.createStatement();
            success = sta.execute(query);
        }
        catch(Exception e){}
        return success;
    }

    public boolean removeTeacher(String teacher)
    {
        boolean success = false;
        String query = "DELETE FROM TEACHER WHERE NAME = '" + teacher.trim() + "'";
        try{
            Statement sta = con.createStatement();
            success = sta.execute(query);
        }
        catch(Exception e){}
        return success;
    }

    public ResultSet getAddedComputers()
    {
        String query = "select * from students";
        ResultSet rs;
        try{
            Statement sta = con.createStatement();
            rs = sta.executeQuery(query);
        }
        catch(Exception e)
        {
            rs = null;
            System.out.println("Had a problem loading previously added comptuers from db");
        }
        return rs;
    }
    public boolean saveLayout(Vector<Student> students)
    {
        for(int i = 0; i<students.size(); i++)
        {
            Student currStudent = ((Student)(students.get(i)));
            String theHost = currStudent.toString();
            int x = currStudent.getLocation().x;
            int y = currStudent.getLocation().y;
            int width = currStudent.getSize().width;
            int height = currStudent.getSize().height;
            
            String query = "Update students set x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + " where hostName='" + theHost + "'";
            System.out.println(query);
            try{
                Statement sta = con.createStatement();
                sta.execute(query);
            }
            catch(Exception e)
            {
                System.out.println("Could not update the x,y coords for " + theHost);
            }
        }
        return true;
    }
    public ResultSet getTeachers()
    {
        ResultSet rs = null;
        String query = "Select name from teacher";
        try{
            Statement sta = con.createStatement();
            rs = sta.executeQuery(query);
        }
        catch(Exception e)
        {
        }
        return rs;
    }
    public ResultSet getPolicies(String teacher)
    {
        ResultSet rs = null;
        String query = "Select * from policy where teachername='" + teacher + "'";
        try{
            Statement sta = con.createStatement();
            rs = sta.executeQuery(query);
        }
        catch(Exception e)
        {
        }
        return rs;
    }
    public boolean addPolicy(String policyName, String teacherName, String programsList, String sitesList, boolean siteWhitelist)
    {
        boolean success = false;

        char charbit = '0';
        if(siteWhitelist)
            charbit = '1';

        String query = "INSERT INTO POLICY VALUES ('" + policyName + "', '" + teacherName + "', '" + sitesList + "', '" + programsList + "', " + charbit + ")";
        System.out.println(query);
        try{
            Statement sta = con.createStatement();
            success = sta.execute(query);
        }
        catch(Exception e){}
        return success;
    }
    public boolean updatePolicy(String policyName, String teacherName, String programsList, String sitesList, boolean siteWhitelist)
    {
        boolean success = false;

        char charbit = '0';
        if(siteWhitelist)
            charbit = '1';

        String query = "UPDATE POLICY SET SITEBLOCKSTRING = '" + sitesList + "', PROGRAMBLOCKSTRING = '" + programsList + "', SITEWHITELIST = " + charbit + " WHERE POLICYNAME = '" + policyName + "' AND TEACHERNAME = '" + teacherName + "'";
        System.out.println(query);
        try{
            Statement sta = con.createStatement();
            success = sta.execute(query);
        }
        catch(Exception e){}
        return success;
    }
    public boolean removePolicy(String policyName, String teacherName)
    {
        boolean success = false;

        String query = "DELETE FROM POLICY WHERE POLICYNAME = '" + policyName + "' AND TEACHERNAME = '" + teacherName + "'";
        System.out.println(query);
        try{
            Statement sta = con.createStatement();
            success = sta.execute(query);
        }
        catch(Exception e){}
        return success;
    }
}
