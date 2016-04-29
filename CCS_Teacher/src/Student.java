

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Bryan
 */
import javax.swing.*;
import java.awt.Component;
import java.util.*;

public class Student extends JInternalFrame{
    private JMenuBar stuBar;
    private JMenu optionsMenu;
    private JLabel classroomNodeName;
    private JMenuItem classroomNodeRemoteDesktop;
    private JCheckBox classroomNodeSelected;
    private JLabel classroomNodeStatus;
    private String hostAddress;
    private String hostName;
    private int socketnum;
    private int vncPort;
    //the socket port to listen to
    private String vncViewerPath;
    private String vncServerPath;
    private Component comp;

    public Student(int Width, int Height, int X, int Y, String hostName, boolean online, Component comp, int vncPort, int socketnum, String vncViewerPath, String vncServerPath)
    {
        super();

        hostAddress = "";
        stuBar = new JMenuBar();
        optionsMenu = new JMenu();
        classroomNodeName = new JLabel();
        classroomNodeRemoteDesktop = new JMenuItem();
        classroomNodeSelected = new JCheckBox();
        classroomNodeStatus = new JLabel();


        this.hostName = hostName;
        this.socketnum = socketnum;
        this.comp = comp;
        this.vncPort = vncPort;
        this.vncViewerPath = vncViewerPath;
        this.vncServerPath = vncServerPath;

        this.setJMenuBar(stuBar);
        stuBar.add(optionsMenu);

        optionsMenu.setText("Options");

        classroomNodeSelected.setText("Select");
        classroomNodeName.setText(hostName);

        if(online)
        {
            classroomNodeStatus.setText("Online");
            //when online set the following controls as available
            classroomNodeSelected.setEnabled(true);
            optionsMenu.setEnabled(true);
            classroomNodeName.setEnabled(true);
            classroomNodeStatus.setEnabled(true);
        }
        else
        {
            //when offline set the following controls as unavailable
            classroomNodeStatus.setText("Offline");
            classroomNodeSelected.setEnabled(false);
            optionsMenu.setEnabled(false);
            classroomNodeName.setEnabled(false);
            classroomNodeStatus.setEnabled(false);
        }

        classroomNodeRemoteDesktop.setText("Remote Desktop");

        classroomNodeRemoteDesktop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                classroomNodeRemoteDesktopActionPerformed(evt);
            }
        });

        optionsMenu.add(classroomNodeRemoteDesktop);


        GroupLayout Layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(Layout);
        Layout.setHorizontalGroup(
            Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(Layout.createSequentialGroup()
                .addGroup(Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(Layout.createSequentialGroup()
                        .addComponent(classroomNodeSelected)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(classroomNodeStatus))
                    .addGroup(Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(classroomNodeName)))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        Layout.setVerticalGroup(
            Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, Layout.createSequentialGroup()
                .addComponent(classroomNodeName)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                .addGroup(Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(classroomNodeSelected)
                    .addComponent(classroomNodeStatus)))
        );

        //let the internal node be resized
        this.setResizable(true);

        //X, Y are set from the db
        this.setBounds(X, Y, Width, Height);
        this.setVisible(true);
    }
    public void setHostAddress(String hostAddress)
    {
        this.hostAddress = hostAddress;
        //also set this as online in the classroom interface
        this.classroomNodeStatus.setText("Online");
        //when online make sure the following controls are enabled
        classroomNodeSelected.setEnabled(true);
        optionsMenu.setEnabled(true);
        classroomNodeName.setEnabled(true);
        classroomNodeStatus.setEnabled(true);
    }
    public void unsetHostAddress()
    {
        this.hostAddress = "";
        //also set this as offline in the classroom interface
        this.classroomNodeStatus.setText("Offline");
        //when offline make sure the following controls are disnabled
        classroomNodeSelected.setSelected(false);
        classroomNodeSelected.setEnabled(false);
        optionsMenu.setEnabled(false);
        classroomNodeName.setEnabled(false);
        classroomNodeStatus.setEnabled(false);
    }
    public boolean Online()
    {
        return (this.classroomNodeStatus.getText().equals("Online"));
    }
    public String getHostAddress()
    {
        return this.hostAddress;
    }
    public String toString(){
        return hostName;
    }

    private void classroomNodeRemoteDesktopActionPerformed(java.awt.event.ActionEvent evt) {
        //first tell the student machine to turn on the vncserver
        String studentCommand = vncServerPath + " -noconsole";
        //then the teacher needs to launch its vncviewer
        String teacherCommand = vncViewerPath + " " + this.getHostAddress() + ":" + vncPort;
        //this can be accomplished in this 1 command
        this.commandSender(studentCommand, teacherCommand);
    }

    public void vncBroadcast(String teacherName, int multicastProxyPort)
    {       
        commandSender("java classpath='c:\\program files\\ccs\\MulticastVNC' vncviewer HOST " + teacherName + " PORT " + multicastProxyPort);
    }

    public void commandSender(String command)
    {
        //overloaded function

        //we will make this a thread to keep things
        //moving along smoothly in the interface
        Vector<Student> stus = new Vector<Student>();
        stus.add(this);

        CommandThread ct = new CommandThread(command, stus);
        ct.start();
    }
    public void commandSender(String command, String executeOnTeacherAfter)
    {
        //overloaded function

        //this version of commandSender should only be used on commands
        //that are sent to the student one at a time
        //no multiselection commands should use this

        //we will make this a thread to keep things
        //moving along smoothly in the interface
        Vector<Student> stus = new Vector<Student>();
        stus.add(this);
        
        CommandThread ct = new CommandThread(command, stus, "Execute", executeOnTeacherAfter);
        ct.start();
    }

    public void keyboardAndMouseBlocker()
    {
        //overloaded function

        //this version of commandSender should only be used on commands
        //that are sent to the student one at a time
        //no multiselection commands should use this

        //we will make this a thread to keep things
        //moving along smoothly in the interface
        Vector<Student> stus = new Vector<Student>();
        stus.add(this);

        CommandThread ct = new CommandThread("KeyboardAndMouseBlocker", stus, "KeyboardAndMouseBlocker");
        ct.start();
    }

    public void keyboardAndMouseUnblocker()
    {
        //overloaded function

        //this version of commandSender should only be used on commands
        //that are sent to the student one at a time
        //no multiselection commands should use this

        //we will make this a thread to keep things
        //moving along smoothly in the interface
        Vector<Student> stus = new Vector<Student>();
        stus.add(this);

        CommandThread ct = new CommandThread("KeyboardAndMouseUnblocker", stus, "KeyboardAndMouseUnblocker");
        ct.start();
    }

    public void applyPolicy(String programsList, String sitesList, boolean sitesWhiteslist)
    {

        String listType = "";
        if(sitesWhiteslist)
            listType = "whitelist";
        else
            listType = "blacklist";

        //if the programs list is empty, or if we have an empty blacklist
        if(programsList.trim().equals("") || programsList.trim().equals("blacklist"))
            programsList = "none\nblacklist";
        else
        {
            programsList = programsList.replace(" ", "\n");
            //replace the space at the end of the string to a newline
            //so that a newline is printed before whitelist or a blacklist
        }
        if(sitesList.trim().equals(""))
            sitesList = "none";
        
        //also get rid of spaces in the sitesList
        sitesList = sitesList.replaceAll(" ", "");

        String command = "Programs\n" + programsList + "\nSites\n" + sitesList + "\n" + listType;

        Vector<Student> stus = new Vector<Student>();
        stus.add(this);
        //we just need to apply the policy for this particular student

        //set the command as a policy
        CommandThread ct = new CommandThread(command, stus, "Policy");
        ct.start();
    }

    public JCheckBox getClassroomNodeSelected()
    {
        return classroomNodeSelected;
    }

    public Component getComp()
    {
        return this.comp;
    }
    public int getSocketNum()
    {
        return this.socketnum;
    }
    public String getHostName()
    {
        return this.hostName;
    }
}
