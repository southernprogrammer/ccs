



/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Interface.java
 *
 * Created on Apr 1, 2009, 8:21:48 PM
 */

/**
 *
 * @author Bryan
 */

import edu.columbia.bonaha.*;
import java.util.*;
import java.sql.*;

public class Interface extends javax.swing.JFrame implements BListener{

    private BService service;
    private Vector<BNode> nodes;
    private Vector<Student> students;
    private int multicastPort;
    private String multicastAddress;
    private int multicastProxyPort;
    private int vncPort;
    private int socketnum;
    private Configuration conf;
    
    public Interface() {
        initComponents();

        conf = new Configuration("c:\\program files\\ccs\\CCS_Teacher\\Configuration.dat", this);
        //what address the student's multicast vnc client listens to
        this.multicastAddress = conf.get("multicastaddress");
        //an intermediary port that is needed
        this.multicastPort = Integer.parseInt(conf.get("multicastport"));
        //what port multicastvnc uses as a proxy, it's what the client listens to
        this.multicastProxyPort = Integer.parseInt(conf.get("multicastproxyport"));
        //the port set in RealVNC
        this.vncPort = Integer.parseInt(conf.get("vncport"));
        //the socket that the student computer is listening to for TCP connections
        this.socketnum = Integer.parseInt(conf.get("socketnum"));

        nodes = new Vector<BNode>();
        students = new Vector<Student>();
        try{
            java.net.InetAddress i = java.net.InetAddress.getLocalHost();
            Hostname = i.getHostName();
        }
        catch(Exception e){
            System.err.println("Could not get hostname of server");
        }
        db = new DBHandler(this, conf.get("commonappdata"));
        ResultSet addedCompLoad = db.getAddedComputers();

        try{
            while(addedCompLoad.next())
            {
                //load the added computer list up with
                addedModel.addElement(addedCompLoad.getString("hostName"));
                //now add to the classroom view
                Student newStudent = new Student(addedCompLoad.getInt("Width"), addedCompLoad.getInt("Height"), addedCompLoad.getInt("X"), addedCompLoad.getInt("Y"), addedCompLoad.getString("hostName"), false, this, this.vncPort, this.socketnum, conf.get("vncviewerpath"), conf.get("vncserverpath"));
                students.add(newStudent);
                //add it to the desktop pane
                jDesktopPane1.add(newStudent);
            }
            //finally close the result set
            addedCompLoad.close();
        }
        catch(Exception e)
        {
            System.out.println("Could not load stored student computers");
        }

        //now load teacher information
        ResultSet rs = db.getTeachers();
        teachers = new Vector<String>();
        try{
            while(rs.next())
            {
                teachers.add(rs.getString("name"));
            }
            rs.close();
        }
        catch(Exception e)
        {
            System.out.println("Could not get teachers");
        }
        
        teacherSelector.removeAllItems();
        Iterator itr = teachers.iterator();
        while(itr.hasNext())
        {
            teacherSelector.addItem((String)(itr.next()));
        }

        //add the frame for adding teachers
        addTeachForm = new AddTeacherForm(db, teachers, teacherSelector);
        addTeachForm.setVisible(false);

        //add the frame for adding
        addPolicyForm = new AddPolicyForm(this, db, policies, teacherSelector, programsList, sitesList, sitesWhitelist);
        addPolicyForm.setVisible(false);

        //listen for incoming nodes on the network
        this.listen();
    }

    public void listen()
    {
        //all clients will be configured to have same name for service
        service = new BService("ccs_" + conf.get("classroomname"), "tcp");
        service.register();
        service.setListener(this);
    }
    public void serviceUpdated(BNode n) {
        if(n.getHostName().toLowerCase().startsWith(Hostname.toLowerCase()))
        {
            //if the service you detected was on this machine, do nothing
            return;
        }
        if(nodes.size() == 0)
        {
            //add your first person
            nodes.add(n);
            //if not in the added list
            if(!addedModel.contains(trimHost(n.getHostName())))
                connectedModel.addElement(trimHost(n.getHostName()));

            //now check if this host is in the classroom view
            //if it is set its hostAddress
            for(int i = 0; i<students.size(); i++)
            {
                if(((Student)(students.get(i))).toString().equals(trimHost(n.getHostName())))
                {
                    ((Student)(students.get(i))).setHostAddress(n.getHostAddress());
                }
            }
        }
        else {
            for(int i=0; i<nodes.size(); i++)
                if(nodes.get(i).getHostName().equals(n.getHostName()))
                {
                    //something changed
                    nodes.set(i, n); //replace with the current node

                    //now check if this host is in the classroom view
                    //if it is set its hostAddress
                    for(int j = 0; j<students.size(); j++)
                    {
                        if(((Student)(students.get(j))).toString().equals(trimHost(n.getHostName())))
                        {
                            ((Student)(students.get(j))).setHostAddress(n.getHostAddress());
                        }
                    }
                }
                else
                {
                    //else we must have gotten a new student
                    nodes.add(n);
                    //if not int he added list
                    if(!addedModel.contains(trimHost(n.getHostName())))
                        connectedModel.addElement(trimHost(n.getHostName()));

                    //now check if this host is in the classroom view
                    //if it is set its hostAddress
                    for(int k = 0; k<students.size(); k++)
                    {
                        if(((Student)(students.get(k))).toString().equals(trimHost(n.getHostName())))
                        {
                            ((Student)(students.get(k))).setHostAddress(n.getHostAddress());
                        }
                    }
                }
        }
    }

    public void serviceExited(BNode n) {
        //remove nodes who no longer exist
        System.out.println(trimHost(n.getHostName()) + " Has Left");
        for(int i=0; i<nodes.size(); i++)
            if(n.getHostName().equals(nodes.get(i).getHostName()))
            {
                nodes.remove(i);
                connectedModel.removeElement(trimHost(n.getHostName()));
            }

            //now check if this host is in the classroom view
            //if it is unset its hostAddress
            for(int i = 0; i<students.size(); i++)
            {
                if(((Student)(students.get(i))).toString().equals(trimHost(n.getHostName())))
                {
                    ((Student)(students.get(i))).unsetHostAddress();
                }
            }
    }

    public String trimHost(String host)
    {
        host = host.replace(".local.", "");
        return host;
    }

    public void resetPolicies()
    {
        //remove all of the policies from our policy vector
        policies.removeAllElements();
        policySelector.removeAllItems();
        //if there is at least 1 teacher
        if(teacherSelector.getItemCount() > 0)
        {
            //get his/her policies
            ResultSet rs = db.getPolicies((String)(teacherSelector.getSelectedItem()));
            try{
                while(rs.next())
                {
                    Policy currentPolicy = new Policy(rs.getString("policyname"), rs.getString("programblockstring"), rs.getString("siteblockstring"), rs.getBoolean("sitewhitelist"));
                    policies.add(currentPolicy);
                }
                rs.close();
            }
            catch(Exception e)
            {
                System.out.println("Could not get teachers");
            }
            if(policies.size() > 0)
            {
                //iterate through the policies
                Iterator itr = policies.iterator();
                while(itr.hasNext())
                {
                    Policy currPolicy = (Policy)(itr.next());
                    //add all policy names to the policySelector control
                    policySelector.addItem(currPolicy.getPolicyName());
                }
            }
            //set the policy selector to select the last thing in the list
            policySelector.setSelectedIndex(policySelector.getItemCount() - 1);
        }
    }

    private void broadCastScreen()
    {
        Vector<Student> stusToExecute = new Vector<Student>();
        Iterator itr = students.iterator();
        while(itr.hasNext())
        {
            Student stu = (Student)(itr.next());
            //run the broadcast function on each selected student
            if(stu.getClassroomNodeSelected().isSelected())
                stusToExecute.add(stu);
        }

        //first start the local VNC Server
        Executor.execute(conf.get("vncserverpath") + " -noconsole");

        //then start the MulticastVNC Server
        String command = "java classpath='c:\\program files\\ccs\\MulticastVNC' vncviewer HOST ";
        command = command + "localhost PORT " + vncPort + " MULTICAST ";
        command = command + multicastAddress + ":" + multicastPort + "/16 ";
        command = command + "PROXYPORT " + multicastProxyPort;

        //we need the extra paramater stusToExecute here
        //because a commandwaiter will be used on MulticastVNC
        //and after the process stops running on the teacher's computer
        //it will send a command to all selected students to stop running the
        //viewer on them
        Executor.execute(stusToExecute, command);

        //now broadcast to each student

        itr = stusToExecute.iterator();
        while(itr.hasNext())
        {
            Student stu = (Student)(itr.next());
            //run the broadcast function on each selected student
            stu.vncBroadcast(Hostname, multicastProxyPort);
        }
    }
    private void turnOffMonitor()
    {
        Iterator itr = students.iterator();
        while(itr.hasNext())
        {
            Student stu = (Student)(itr.next());
            //run the apply policy function on each selected student
            if(stu.getClassroomNodeSelected().isSelected())
            {
                //send the turn off command
                stu.commandSender("c:\\program files\\ccs\\nircmd\\nircmd monitor off");
            }
        }
    }
    private void turnOffMachines()
    {
        Iterator itr = students.iterator();
        while(itr.hasNext())
        {
            Student stu = (Student)(itr.next());
            //run the apply policy function on each selected student
            if(stu.getClassroomNodeSelected().isSelected())
            {
                //send the turn off command
                stu.commandSender("c:\\program files\\ccs\\nircmd\\nircmd exitwin poweroff");
            }
        }
    }
    private void lockK_M()
    {
        Iterator itr = students.iterator();
        while(itr.hasNext())
        {
            Student stu = (Student)(itr.next());
            //run the apply policy function on each selected student
            if(stu.getClassroomNodeSelected().isSelected())
            {
                //send the command to be run under the administrative
                //privilege
                stu.keyboardAndMouseBlocker();
            }
        }
    }
    private void unlockK_M()
    {
        Iterator itr = students.iterator();
        while(itr.hasNext())
        {
            Student stu = (Student)(itr.next());
            //run the apply policy function on each selected student
            if(stu.getClassroomNodeSelected().isSelected())
            {
                //send the turn off command
                stu.keyboardAndMouseUnblocker();
            }
        }
    }
    private void applyOpenLab()
    {
        Iterator itr = students.iterator();
        while(itr.hasNext())
        {
            Student stu = (Student)(itr.next());
            //run the apply policy function on each selected student
            if(stu.getClassroomNodeSelected().isSelected())
            {
                //apply an empty blacklist on programs and sites
                stu.applyPolicy(" blacklist", "none", false);
            }
        }
    }
    private void applyPolicy()
    {

        Iterator itr = students.iterator();
        while(itr.hasNext())
        {
            Student stu = (Student)(itr.next());
            //run the apply policy function on each selected student
            if(stu.getClassroomNodeSelected().isSelected())
                stu.applyPolicy(programsList.getText(), sitesList.getText(), sitesWhitelist.isSelected());
        }
    }
    private void emptyBin()
    {
        Iterator itr = students.iterator();
        while(itr.hasNext())
        {
            Student stu = (Student)(itr.next());
            //run the apply policy function on each selected student
            if(stu.getClassroomNodeSelected().isSelected())
            {
                //send the turn off command
                stu.commandSender("c:\\program files\\ccs\\nircmd\\nircmd emptybin");
            }
        }
    }
    private void mute()
    {
        Iterator itr = students.iterator();
        while(itr.hasNext())
        {
            Student stu = (Student)(itr.next());
            //run the apply policy function on each selected student
            if(stu.getClassroomNodeSelected().isSelected())
            {
                //send the turn off command
                stu.commandSender("c:\\program files\\ccs\\nircmd\\nircmd mutesysvolume 1");
            }
        }
    }
    private void unmute()
    {
        Iterator itr = students.iterator();
        while(itr.hasNext())
        {
            Student stu = (Student)(itr.next());
            //run the apply policy function on each selected student
            if(stu.getClassroomNodeSelected().isSelected())
            {
                //send the turn off command
                stu.commandSender("c:\\program files\\ccs\\nircmd\\nircmd mutesysvolume 0");
            }
        }
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sitesWhitelistBlacklist = new javax.swing.ButtonGroup();
        tabbedPane = new javax.swing.JTabbedPane();
        classRoomFrame = new javax.swing.JInternalFrame();
        jScrollPane3 = new javax.swing.JScrollPane();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        btnSaveLayout = new javax.swing.JButton();
        btnSelectAll = new javax.swing.JButton();
        actionComboBox = new javax.swing.JComboBox();
        btnActionButton = new javax.swing.JButton();
        policyFrame = new javax.swing.JInternalFrame();
        teacherSelector = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        policySelector = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        btnAddPolicy = new javax.swing.JButton();
        btnRemovePolicy = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        programsList = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        sitesWhitelist = new javax.swing.JRadioButton();
        sitesBlacklist = new javax.swing.JRadioButton();
        jLabel7 = new javax.swing.JLabel();
        sitesList = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        btnUpdatePolicy = new javax.swing.JButton();
        btnProgramWhitelistBlacklist = new javax.swing.JButton();
        programLabel = new javax.swing.JLabel();
        btnAddTeacher = new javax.swing.JButton();
        btnRemoveTeacher = new javax.swing.JButton();
        detailFrame = new javax.swing.JInternalFrame();
        jScrollPane1 = new javax.swing.JScrollPane();
        connectedModel = new javax.swing.DefaultListModel();
        connectedComputers = new javax.swing.JList(connectedModel);
        jLabel1 = new javax.swing.JLabel();
        btnAddComputer = new javax.swing.JButton();
        brnRemoveComputer = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        addedModel = new javax.swing.DefaultListModel();
        addedComputers = new javax.swing.JList(addedModel);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jDesktopPane1.setAutoscrolls(true);
        jDesktopPane1.setPreferredSize(new java.awt.Dimension(5000, 5000));
        jScrollPane3.setViewportView(jDesktopPane1);

        btnSaveLayout.setText("Save Layout");
        btnSaveLayout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveLayoutActionPerformed(evt);
            }
        });

        btnSelectAll.setText("Select / Unselect All");
        btnSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectAllActionPerformed(evt);
            }
        });

        actionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Broadcast Screen", "Apply Policy", "Apply Open Lab", "Lock Keyboard and Mouse", "Unlock Keyboard and Mouse", "Empty Recycle Bin", "Mute Sound", "Unmute Sound", "Turn Off Monitor", "Turn Off" }));

        btnActionButton.setText("Apply Action");
        btnActionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActionButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout classRoomFrameLayout = new javax.swing.GroupLayout(classRoomFrame.getContentPane());
        classRoomFrame.getContentPane().setLayout(classRoomFrameLayout);
        classRoomFrameLayout.setHorizontalGroup(
            classRoomFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(classRoomFrameLayout.createSequentialGroup()
                .addGroup(classRoomFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(classRoomFrameLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(btnSaveLayout)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSelectAll)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(actionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnActionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 872, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(2, Short.MAX_VALUE))
        );
        classRoomFrameLayout.setVerticalGroup(
            classRoomFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, classRoomFrameLayout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(classRoomFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSelectAll)
                    .addComponent(btnSaveLayout)
                    .addComponent(actionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnActionButton))
                .addContainerGap())
        );

        tabbedPane.addTab("Classroom", classRoomFrame);

        policyFrame.setPreferredSize(new java.awt.Dimension(800, 600));
        policyFrame.getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        teacherSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        teacherSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                teacherSelectorActionPerformed(evt);
            }
        });
        policyFrame.getContentPane().add(teacherSelector, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 30, 200, -1));

        jLabel3.setText("Teacher");
        policyFrame.getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 10, -1, -1));

        policySelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        policySelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                policySelectorActionPerformed(evt);
            }
        });
        policyFrame.getContentPane().add(policySelector, new org.netbeans.lib.awtextra.AbsoluteConstraints(436, 30, 280, -1));

        jLabel4.setText("Policy");
        policyFrame.getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 10, -1, -1));

        btnAddPolicy.setText("Add Policy");
        btnAddPolicy.setEnabled(false);
        btnAddPolicy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddPolicyActionPerformed(evt);
            }
        });
        policyFrame.getContentPane().add(btnAddPolicy, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 60, -1, -1));

        btnRemovePolicy.setText("Remove Policy");
        btnRemovePolicy.setEnabled(false);
        btnRemovePolicy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemovePolicyActionPerformed(evt);
            }
        });
        policyFrame.getContentPane().add(btnRemovePolicy, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 60, -1, -1));

        jLabel5.setText("Programs");
        policyFrame.getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 110, -1, -1));

        programsList.setEditable(false);
        policyFrame.getContentPane().add(programsList, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 130, 620, -1));

        jLabel6.setText("On the Fly Policy");
        policyFrame.getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 110, -1, -1));

        sitesWhitelistBlacklist.add(sitesWhitelist);
        sitesWhitelist.setText("Whitelist");
        policyFrame.getContentPane().add(sitesWhitelist, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 180, -1, -1));

        sitesWhitelistBlacklist.add(sitesBlacklist);
        sitesBlacklist.setSelected(true);
        sitesBlacklist.setText("Blacklist");
        policyFrame.getContentPane().add(sitesBlacklist, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 180, -1, -1));

        jLabel7.setText("Websites");
        policyFrame.getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 160, -1, -1));
        policyFrame.getContentPane().add(sitesList, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 180, 620, 20));

        jLabel9.setText("Seperate All with Semicolons . Websites should be domains only. Ex: google.com;yahoo.com;etc.com");
        policyFrame.getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 210, -1, -1));

        btnUpdatePolicy.setText("Update Policy");
        btnUpdatePolicy.setEnabled(false);
        btnUpdatePolicy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdatePolicyActionPerformed(evt);
            }
        });
        policyFrame.getContentPane().add(btnUpdatePolicy, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 60, -1, -1));

        btnProgramWhitelistBlacklist.setText("Get Program Whitelist or Blacklist");
        btnProgramWhitelistBlacklist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProgramWhitelistBlacklistActionPerformed(evt);
            }
        });
        policyFrame.getContentPane().add(btnProgramWhitelistBlacklist, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, -1, -1));

        programLabel.setText("Launch all whitelist or blacklist applications and then click \"Get Program Whitelist or Blacklist\" to begin.");
        policyFrame.getContentPane().add(programLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 160, -1, -1));

        btnAddTeacher.setText("Add Teacher");
        btnAddTeacher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTeacherActionPerformed(evt);
            }
        });
        policyFrame.getContentPane().add(btnAddTeacher, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 60, -1, -1));

        btnRemoveTeacher.setText("Remove Teacher");
        btnRemoveTeacher.setEnabled(false);
        btnRemoveTeacher.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveTeacherActionPerformed(evt);
            }
        });
        policyFrame.getContentPane().add(btnRemoveTeacher, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 60, -1, -1));

        tabbedPane.addTab("Policies", policyFrame);

        detailFrame.setPreferredSize(new java.awt.Dimension(800, 600));

        connectedComputers.setName("connectedComputers"); // NOI18N
        jScrollPane1.setViewportView(connectedComputers);

        jLabel1.setText("Connected Computers");

        btnAddComputer.setText("Add Computer >");
        btnAddComputer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddComputerActionPerformed(evt);
            }
        });

        brnRemoveComputer.setText("< Remove Computer");
        brnRemoveComputer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                brnRemoveComputerActionPerformed(evt);
            }
        });

        jLabel2.setText("Added Computers");

        addedComputers.setName("addedComputers"); // NOI18N
        jScrollPane2.setViewportView(addedComputers);

        javax.swing.GroupLayout detailFrameLayout = new javax.swing.GroupLayout(detailFrame.getContentPane());
        detailFrame.getContentPane().setLayout(detailFrameLayout);
        detailFrameLayout.setHorizontalGroup(
            detailFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailFrameLayout.createSequentialGroup()
                .addGroup(detailFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(detailFrameLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addGroup(detailFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(brnRemoveComputer)
                            .addComponent(btnAddComputer)))
                    .addGroup(detailFrameLayout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(detailFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(360, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        detailFrameLayout.setVerticalGroup(
            detailFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(detailFrameLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(detailFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(detailFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(detailFrameLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addComponent(btnAddComputer)
                        .addGap(27, 27, 27)
                        .addComponent(brnRemoveComputer))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(226, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Detail", detailFrame);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 889, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 556, Short.MAX_VALUE)
                .addContainerGap())
        );

        tabbedPane.getAccessibleContext().setAccessibleName("Detail");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddComputerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddComputerActionPerformed

        Object connected[] = connectedComputers.getSelectedValues();
        for(int i = 0; i<connected.length; i++)
        {
            addedModel.addElement(connected[i]);
            connectedModel.removeElement(connected[i]);
            //also add these computers into the classroom view
            Student newStudent = new Student(130, 100, 0, 0, connected[i].toString(), true, this, this.vncPort, this.socketnum, conf.get("vncviewerpath"), conf.get("vncserverpath"));
            //notice they are online because we added from the connected list
            students.add(newStudent);
            //add it to the desktop pane
            jDesktopPane1.add(newStudent);
        }
        //add any new computers to the db
        if(connected.length != 0)
        {
            Vector v = new Vector(Arrays.asList(connected));
            //add student nodes to the db
            db.addStudents(v);
        }
    }//GEN-LAST:event_btnAddComputerActionPerformed

    private void brnRemoveComputerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_brnRemoveComputerActionPerformed
        Object added[] = addedComputers.getSelectedValues();
        //the added computers that are being removed
        for(int i = 0; i<added.length; i++)
        {
            //remove it from the added list
            addedModel.removeElement(added[i]);
            //if it's connected add it to the connected list
            Iterator itr = nodes.iterator();
            //we are iterating through the BNodes
            while(itr.hasNext())
            {
                String currStudent = trimHost(((BNode)(itr.next())).getHostName());
                if(currStudent.equals(added[i].toString()))
                {
                    //if the removed item is actually connected
                    //show that it is connected by putting in the connected list
                    connectedModel.addElement(added[i]);
                }
            }
            Vector v = new Vector(Arrays.asList(added));
            //remove the selected nodes from the db
            db.removeStudents(v);

            //remove teh selected nodes from the classroom view
            //we must iterate through the student list
            for(int k = 0; k<students.size(); k++)
            {
                //if the student's hostname is equal to the current hostname
                if(((Student)(students.get(k))).toString().equals(added[i].toString()))
                {
                    //remove the item from the interface
                    jDesktopPane1.remove(students.get(k));
                    //then remove the item at k from the student list
                    students.remove(k);
                }
            }
        }
    }//GEN-LAST:event_brnRemoveComputerActionPerformed

    private void btnProgramWhitelistBlacklistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProgramWhitelistBlacklistActionPerformed
        if(!programWhitelistBlacklistSet)
        {
            new WhitelistBlacklistGatherer(programsList).start();
            programWhitelistBlacklistSet = true;
            btnProgramWhitelistBlacklist.setText("Clear Whitelist or Blacklist");
        }
        else
        {
            programsList.setText("");
            programWhitelistBlacklistSet = false;
            btnProgramWhitelistBlacklist.setText("Get Program Whitelist or Blacklist");
        }
}//GEN-LAST:event_btnProgramWhitelistBlacklistActionPerformed

    private void policySelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_policySelectorActionPerformed
        //if we have policies loaded from the teacher
        if(policies.size() > 0 && policySelector.getSelectedItem() != null)
        {
            
            btnUpdatePolicy.setEnabled(true);
            btnRemovePolicy.setEnabled(true);

            String polName = (String)policySelector.getSelectedItem();
            Iterator itr = policies.iterator();
            Policy pol = null;
            while(itr.hasNext())
            {
                Policy currPol = (Policy)itr.next();
                if(currPol.getPolicyName().trim().equals(polName.trim()))
                {
                    pol = currPol;
                    break;
                }
            }
            if(pol != null)
            {
                programsList.setText(pol.getProgramsList());
                sitesList.setText(pol.getSitesList());
                if(pol.isSitesWhitelist())
                {
                    sitesBlacklist.setSelected(false);
                    sitesWhitelist.setSelected(true);
                }
                else
                {
                    sitesBlacklist.setSelected(true);
                    sitesWhitelist.setSelected(false);
                }
            }
        }
        else
        {
            btnUpdatePolicy.setEnabled(false);
            btnRemovePolicy.setEnabled(false);
        }
    }//GEN-LAST:event_policySelectorActionPerformed

    private void teacherSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_teacherSelectorActionPerformed
        //check if there are teachers to see if other controls
        //need enabling
        if(teachers.size() > 0)
        {
            btnRemoveTeacher.setEnabled(true);
            btnAddPolicy.setEnabled(true);
        }
        else
        {
            btnRemoveTeacher.setEnabled(false);
            btnAddPolicy.setEnabled(false);
        }
        //when a new teacher is selected, reset the policies list
        resetPolicies();
    }//GEN-LAST:event_teacherSelectorActionPerformed

    private void btnAddPolicyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPolicyActionPerformed
        addPolicyForm.setVisible(true);
    }//GEN-LAST:event_btnAddPolicyActionPerformed

    private void btnUpdatePolicyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdatePolicyActionPerformed
        //now update the policy
        db.updatePolicy((String)(policySelector.getSelectedItem()), (String)(teacherSelector.getSelectedItem()), programsList.getText(), sitesList.getText(), sitesWhitelist.isSelected());

        //update the policies list
        policies.setElementAt(new Policy((String)(policySelector.getSelectedItem()), programsList.getText(), sitesList.getText(), sitesWhitelist.isSelected()), policySelector.getSelectedIndex());
}//GEN-LAST:event_btnUpdatePolicyActionPerformed

    private void btnRemovePolicyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemovePolicyActionPerformed
        db.removePolicy((String)policySelector.getSelectedItem(), (String)teacherSelector.getSelectedItem());
        resetPolicies();
    }//GEN-LAST:event_btnRemovePolicyActionPerformed

    private void btnAddTeacherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTeacherActionPerformed
        addTeachForm.setVisible(true);
    }//GEN-LAST:event_btnAddTeacherActionPerformed

    private void btnRemoveTeacherActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveTeacherActionPerformed
         db.removeTeacher((String)teacherSelector.getSelectedItem());
        //remove the teacher from our list of teachers
        teachers.remove((String)teacherSelector.getSelectedItem());
        //now remove the teacher from the interface
        teacherSelector.removeItem((String)teacherSelector.getSelectedItem());
    }//GEN-LAST:event_btnRemoveTeacherActionPerformed

    private void btnSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectAllActionPerformed
        Iterator itr = students.iterator();
        while(itr.hasNext()) {
            Student stu = (Student)(itr.next());
            if(stu.getClassroomNodeSelected().isSelected())
                stu.getClassroomNodeSelected().setSelected(false);
            else if(stu.Online()) {
                //else check if the student is online
                //if so they can be selected
                stu.getClassroomNodeSelected().setSelected(true);
            }
        }
}//GEN-LAST:event_btnSelectAllActionPerformed

    private void btnSaveLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveLayoutActionPerformed
        db.saveLayout(students);
}//GEN-LAST:event_btnSaveLayoutActionPerformed

    private void btnActionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActionButtonActionPerformed
        if(((String)actionComboBox.getSelectedItem()).equals("Broadcast Screen"))
        {
            broadCastScreen();
        }
        else if(((String)actionComboBox.getSelectedItem()).equals("Apply Policy"))
        {
            applyPolicy();
        }
        else if(((String)actionComboBox.getSelectedItem()).equals("Apply Open Lab"))
        {
            applyOpenLab();
            unlockK_M();
        }
        else if(((String)actionComboBox.getSelectedItem()).equals("Turn Off Monitor"))
        {
            turnOffMonitor();
        }
        else if(((String)actionComboBox.getSelectedItem()).equals("Turn Off"))
        {
            turnOffMachines();
        }
        else if(((String)actionComboBox.getSelectedItem()).equals("Lock Keyboard and Mouse"))
        {
            lockK_M();
        }
        else if(((String)actionComboBox.getSelectedItem()).equals("Unlock Keyboard and Mouse"))
        {
            unlockK_M();
        }
        else if(((String)actionComboBox.getSelectedItem()).equals("Empty Recycle Bin"))
        {
            emptyBin();
        }
        else if(((String)actionComboBox.getSelectedItem()).equals("Mute Sound"))
        {
            mute();
        }
        else if(((String)actionComboBox.getSelectedItem()).equals("Unmute Sound"))
        {
            unmute();
        }

    }//GEN-LAST:event_btnActionButtonActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Interface().setVisible(true);
            }
        });
    }

    //represents currently selected teacher's policies
    private Vector<Policy> policies = new Vector<Policy>();
    //the form used to add a teacher
    private AddTeacherForm addTeachForm;
    //the forum used to add a policy
    private AddPolicyForm addPolicyForm;
    //holds teachers as we add and remove from the database
    private Vector<String> teachers;
    private boolean programWhitelistBlacklistSet = false;
    private DBHandler db;
    private String Hostname;
    private javax.swing.DefaultListModel connectedModel;
    private javax.swing.DefaultListModel addedModel;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox actionComboBox;
    private javax.swing.JList addedComputers;
    private javax.swing.JButton brnRemoveComputer;
    private javax.swing.JButton btnActionButton;
    private javax.swing.JButton btnAddComputer;
    private javax.swing.JButton btnAddPolicy;
    private javax.swing.JButton btnAddTeacher;
    private javax.swing.JButton btnProgramWhitelistBlacklist;
    private javax.swing.JButton btnRemovePolicy;
    private javax.swing.JButton btnRemoveTeacher;
    private javax.swing.JButton btnSaveLayout;
    private javax.swing.JButton btnSelectAll;
    private javax.swing.JButton btnUpdatePolicy;
    private javax.swing.JInternalFrame classRoomFrame;
    private javax.swing.JList connectedComputers;
    private javax.swing.JInternalFrame detailFrame;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JInternalFrame policyFrame;
    private javax.swing.JComboBox policySelector;
    private javax.swing.JLabel programLabel;
    private javax.swing.JTextField programsList;
    private javax.swing.JRadioButton sitesBlacklist;
    private javax.swing.JTextField sitesList;
    private javax.swing.JRadioButton sitesWhitelist;
    private javax.swing.ButtonGroup sitesWhitelistBlacklist;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JComboBox teacherSelector;
    // End of variables declaration//GEN-END:variables

}
