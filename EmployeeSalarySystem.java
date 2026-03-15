import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.beans.PropertyVetoException;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

class EmployeeSalarySystem {
    public static void main(String[] args) {
        FileDB.init();
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}

class Employee {
    String code, firstName, lastName, designation, address, phone;

    Employee(String code, String firstName, String lastName,
             String designation, String address, String phone) {
        this.code        = code;
        this.firstName   = firstName;
        this.lastName    = lastName;
        this.designation = designation;
        this.address     = address;
        this.phone       = phone;
    }

    String toCSV() {
        return escape(code) + "," + escape(firstName) + "," + escape(lastName) + ","
             + escape(designation) + "," + escape(address) + "," + escape(phone);
    }

    static Employee fromCSV(String line) {
        String[] p = splitCSV(line);
        if (p.length < 6) return null;
        return new Employee(p[0], p[1], p[2], p[3], p[4], p[5]);
    }

    static String   escape(String s) { return s == null ? "" : s.replace(",", ";;"); }
    static String[] splitCSV(String line) {
        String[] p = line.split(",", -1);
        for (int i = 0; i < p.length; i++) p[i] = p[i].replace(";;", ",").trim();
        return p;
    }
}

class Designation {
    int    id, basicPay;
    String name;
    boolean daPercent, hraPercent, waPercent;
    boolean gpfPercent, itPercent, gisPercent, pfPercent, licPercent;
    int daVal, hraVal, waVal, gpfVal, itVal, gisVal, pfVal, licVal;

    Designation() {}

    String toCSV() {
        return id + "," + Employee.escape(name) + "," + basicPay + ","
             + daPercent  + "," + hraPercent + "," + waPercent  + ","
             + gpfPercent + "," + itPercent  + "," + gisPercent + ","
             + pfPercent  + "," + licPercent + ","
             + daVal  + "," + hraVal + "," + waVal  + ","
             + gpfVal + "," + itVal  + "," + gisVal + ","
             + pfVal  + "," + licVal;
    }

    static Designation fromCSV(String line) {
        String[] p = Employee.splitCSV(line);
        if (p.length < 19) return null;
        Designation d = new Designation();
        try {
            d.id         = Integer.parseInt(p[0]);
            d.name       = p[1];
            d.basicPay   = Integer.parseInt(p[2]);
            d.daPercent  = Boolean.parseBoolean(p[3]);
            d.hraPercent = Boolean.parseBoolean(p[4]);
            d.waPercent  = Boolean.parseBoolean(p[5]);
            d.gpfPercent = Boolean.parseBoolean(p[6]);
            d.itPercent  = Boolean.parseBoolean(p[7]);
            d.gisPercent = Boolean.parseBoolean(p[8]);
            d.pfPercent  = Boolean.parseBoolean(p[9]);
            d.licPercent = Boolean.parseBoolean(p[10]);
            d.daVal      = Integer.parseInt(p[11]);
            d.hraVal     = Integer.parseInt(p[12]);
            d.waVal      = Integer.parseInt(p[13]);
            d.gpfVal     = Integer.parseInt(p[14]);
            d.itVal      = Integer.parseInt(p[15]);
            d.gisVal     = Integer.parseInt(p[16]);
            d.pfVal      = Integer.parseInt(p[17]);
            d.licVal     = Integer.parseInt(p[18]);
        } catch (Exception e) { return null; }
        return d;
    }

    int calc(int baseVal, boolean isPct, int basic) {
        return isPct ? (basic * baseVal / 100) : baseVal;
    }
    int daRs()  { return calc(daVal,  daPercent,  basicPay); }
    int hraRs() { return calc(hraVal, hraPercent, basicPay); }
    int waRs()  { return calc(waVal,  waPercent,  basicPay); }
    int gpfRs() { return calc(gpfVal, gpfPercent, basicPay); }
    int itRs()  { return calc(itVal,  itPercent,  basicPay); }
    int gisRs() { return calc(gisVal, gisPercent, basicPay); }
    int pfRs()  { return calc(pfVal,  pfPercent,  basicPay); }
    int licRs() { return calc(licVal, licPercent, basicPay); }
    int totalAllowance() { return daRs() + hraRs() + waRs(); }
    int totalDeduction() { return gpfRs() + itRs() + gisRs() + pfRs() + licRs(); }
    int netSalary()      { return (basicPay + totalAllowance()) - totalDeduction(); }
}

class FileDB {

    static final String EMP_FILE  = "employees.csv";
    static final String DES_FILE  = "settings.csv";
    static final String USR_FILE  = "users.csv";

    static void init() {
        if (!new File(USR_FILE).exists()) {
            writeLines(USR_FILE, Arrays.asList("admin,admin"));
        }
        if (!new File(EMP_FILE).exists()) {
            writeLines(EMP_FILE, new ArrayList<>());
        }
        if (!new File(DES_FILE).exists()) {
            List<String> rows = new ArrayList<>();
            rows.add("1,Professor,60000,true,true,true,true,true,false,true,false,50,30,10,10,5,0,12,500");
            rows.add("2,Lecturer,45000,true,true,true,true,false,false,true,false,45,20,8,10,0,0,12,300");
            rows.add("3,Staff,25000,true,true,false,false,false,false,true,false,40,15,0,0,0,0,12,200");
            writeLines(DES_FILE, rows);
        }
    }

    static List<String> readLines(String file) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) lines.add(line);
            }
        } catch (IOException ignored) {}
        return lines;
    }

    static void writeLines(String file, List<String> lines) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, false))) {
            for (String l : lines) pw.println(l);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving data: " + e.getMessage(),
                "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    static boolean checkLogin(String user, String pass) {
        for (String line : readLines(USR_FILE)) {
            String[] p = line.split(",", 2);
            if (p.length == 2 && p[0].trim().equals(user) && p[1].trim().equals(pass))
                return true;
        }
        return false;
    }

    static List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        for (String line : readLines(EMP_FILE)) {
            Employee e = Employee.fromCSV(line);
            if (e != null) list.add(e);
        }
        return list;
    }

    static Employee findEmployee(String code) {
        for (Employee e : getAllEmployees())
            if (e.code.equalsIgnoreCase(code)) return e;
        return null;
    }

    static boolean addEmployee(Employee emp) {
        if (findEmployee(emp.code) != null) return false;
        List<String> lines = readLines(EMP_FILE);
        lines.add(emp.toCSV());
        writeLines(EMP_FILE, lines);
        return true;
    }

    static boolean updateEmployee(Employee emp) {
        List<String> lines = readLines(EMP_FILE);
        boolean found = false;
        for (int i = 0; i < lines.size(); i++) {
            Employee e = Employee.fromCSV(lines.get(i));
            if (e != null && e.code.equalsIgnoreCase(emp.code)) {
                lines.set(i, emp.toCSV());
                found = true; break;
            }
        }
        if (found) writeLines(EMP_FILE, lines);
        return found;
    }

    static boolean deleteEmployee(String code) {
        List<String> lines = readLines(EMP_FILE);
        int before = lines.size();
        lines.removeIf(line -> {
            Employee e = Employee.fromCSV(line);
            return e != null && e.code.equalsIgnoreCase(code);
        });
        if (lines.size() < before) { writeLines(EMP_FILE, lines); return true; }
        return false;
    }

    static List<Designation> getAllDesignations() {
        List<Designation> list = new ArrayList<>();
        for (String line : readLines(DES_FILE)) {
            Designation d = Designation.fromCSV(line);
            if (d != null) list.add(d);
        }
        return list;
    }

    static Designation findDesignation(String name) {
        for (Designation d : getAllDesignations())
            if (d.name.equalsIgnoreCase(name)) return d;
        return null;
    }

    static int nextDesignationId() {
        int max = 0;
        for (Designation d : getAllDesignations()) if (d.id > max) max = d.id;
        return max + 1;
    }

    static boolean addDesignation(Designation d) {
        if (findDesignation(d.name) != null) return false;
        d.id = nextDesignationId();
        List<String> lines = readLines(DES_FILE);
        lines.add(d.toCSV());
        writeLines(DES_FILE, lines);
        return true;
    }

    static boolean updateDesignation(Designation d) {
        List<String> lines = readLines(DES_FILE);
        boolean found = false;
        for (int i = 0; i < lines.size(); i++) {
            Designation x = Designation.fromCSV(lines.get(i));
            if (x != null && x.id == d.id) { lines.set(i, d.toCSV()); found = true; break; }
        }
        if (found) writeLines(DES_FILE, lines);
        return found;
    }

    static boolean deleteDesignation(String name) {
        List<String> lines = readLines(DES_FILE);
        int before = lines.size();
        lines.removeIf(line -> {
            Designation d = Designation.fromCSV(line);
            return d != null && d.name.equalsIgnoreCase(name);
        });
        if (lines.size() < before) { writeLines(DES_FILE, lines); return true; }
        return false;
    }

    static String[] getDesignationNames() {
        return getAllDesignations().stream().map(d -> d.name).toArray(String[]::new);
    }
}

class UI {
    static final Color DARK_BLUE = new Color(31, 73, 125);
    static final Color LIGHT_BG  = new Color(245, 248, 252);

    static JButton btn(String text, String iconPath) {
        File f = new File(iconPath);
        JButton b = f.exists() ? new JButton(text, new ImageIcon(iconPath)) : new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return b;
    }

    static JButton toolbarBtn(String tip, String icon, String cmd, ActionListener al) {
        File f = new File(icon);
        JButton b = f.exists() ? new JButton(new ImageIcon(icon)) : new JButton(cmd);
        b.setToolTipText(tip);
        b.setActionCommand(cmd);
        b.addActionListener(al);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        return b;
    }

    static JMenu menu(String text) {
        JMenu m = new JMenu(text);
        m.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return m;
    }

    static JMenuItem menuItem(String text, String icon) {
        JMenuItem mi = new JMenuItem(text);
        File f = new File(icon);
        if (f.exists()) mi.setIcon(new ImageIcon(icon));
        mi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        mi.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return mi;
    }

    static JTextField ro() {
        JTextField t = new JTextField(12);
        t.setEditable(false);
        t.setBackground(new Color(240, 240, 240));
        return t;
    }

    static JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
    }

    static void numericOnly(JTextField tf) {
        tf.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE)
                    e.consume();
            }
        });
    }

    static void refreshCombo(JComboBox<String> cb) {
        cb.removeAllItems();
        for (String n : FileDB.getDesignationNames()) cb.addItem(n);
    }

    static TitledBorder titled(String t) {
        TitledBorder b = BorderFactory.createTitledBorder(t);
        b.setTitleFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setTitleColor(DARK_BLUE);
        return b;
    }
}

class LoginFrame extends JFrame implements ActionListener {
    private JTextField     tfUser = new JTextField(16);
    private JPasswordField tfPass = new JPasswordField(16);
    private JButton        btnLogin, btnExit;

    LoginFrame() {
        super("Employee Salary Management System \u2013 Login");
        setSize(420, 240);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(UI.DARK_BLUE);
        header.setPreferredSize(new Dimension(420, 46));
        JLabel title = new JLabel("  \uD83D\uDCBC  EMPLOYEE SALARY MANAGEMENT SYSTEM");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        header.add(title);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 10));
        form.setBorder(new EmptyBorder(18, 35, 8, 35));
        form.setBackground(UI.LIGHT_BG);
        tfUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tfPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(UI.lbl("Username :")); form.add(tfUser);
        form.add(UI.lbl("Password :")); form.add(tfPass);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        btns.setBackground(UI.LIGHT_BG);
        btnLogin = UI.btn("  Login", "images/Key.gif");
        btnExit  = UI.btn("  Exit",  "images/exit.png");
        btnLogin.setBackground(UI.DARK_BLUE);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogin.addActionListener(this);
        btnExit.addActionListener(this);
        tfPass.addActionListener(this);
        btns.add(btnLogin); btns.add(btnExit);

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(UI.LIGHT_BG);
        center.add(form, BorderLayout.CENTER);
        center.add(btns, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnExit) { System.exit(0); return; }
        String user = tfUser.getText().trim();
        String pass = new String(tfPass.getPassword()).trim();
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.",
                "Required", JOptionPane.WARNING_MESSAGE); return;
        }
        if (FileDB.checkLogin(user, pass)) {
            dispose();
            new MainMenu(user);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password!",
                "Login Failed", JOptionPane.WARNING_MESSAGE);
            tfPass.setText("");
        }
    }
}

class MainMenu extends JFrame implements ActionListener {
    private JDesktopPane desktop = new JDesktopPane();
    private JMenuItem miExit,miAdd,miEdit,miDelete,miSettings,miCalc,miNote,miReport,miAbout,miHelp;

    MainMenu(String user) {
        super("Employee Salary Management System  [v1.0]   \u2014   User: " + user);
        setSize(960, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 3));
        status.setBackground(new Color(230, 235, 245));
        status.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        status.add(UI.lbl("  Logged in as: " + user));
        status.add(UI.lbl(" | " + new SimpleDateFormat("EEE, dd MMM yyyy  HH:mm").format(new Date())));
        status.setPreferredSize(new Dimension(0, 26));

        desktop.setBackground(new Color(215, 228, 248));
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        JToolBar tb = buildToolbar();
        JPanel top = new JPanel(new BorderLayout());
        top.add(tb);

        add(top,     BorderLayout.NORTH);
        add(desktop, BorderLayout.CENTER);
        add(status,  BorderLayout.SOUTH);
        setJMenuBar(buildMenu());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { confirmExit(); }
        });
        setVisible(true);
    }

    private JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu mFile = UI.menu("File");
        JMenu mEmp  = UI.menu("Employee");
        JMenu mTool = UI.menu("Tools");
        JMenu mRpt  = UI.menu("Reports");
        JMenu mHelp = UI.menu("Help");

        miExit     = UI.menuItem("Quit",             "images/exit.png");
        miAdd      = UI.menuItem("Add Employee",     "images/employee.png");
        miEdit     = UI.menuItem("Edit Employee",    "images/edit.png");
        miDelete   = UI.menuItem("Delete Employee",  "images/delete.png");
        miSettings = UI.menuItem("Salary Settings",  "images/setting.png");
        miCalc     = UI.menuItem("Calculator",       "images/calc.png");
        miNote     = UI.menuItem("Notepad",          "images/notepad.png");
        miReport   = UI.menuItem("Employee Pay Slip","images/emp_rpt.png");
        miAbout    = UI.menuItem("About",            "images/author.png");
        miHelp     = UI.menuItem("User Guide",       "images/help.png");

        mFile.add(miExit);
        mEmp.add(miAdd); mEmp.add(miEdit); mEmp.addSeparator(); mEmp.add(miDelete);
        mTool.add(miSettings); mTool.add(miCalc); mTool.addSeparator(); mTool.add(miNote);
        mRpt.add(miReport);
        mHelp.add(miAbout); mHelp.add(miHelp);

        for (JMenuItem m : new JMenuItem[]{miExit,miAdd,miEdit,miDelete,miSettings,miCalc,miNote,miReport,miAbout,miHelp})
            m.addActionListener(this);

        bar.add(mFile); bar.add(mEmp); bar.add(mTool); bar.add(mRpt); bar.add(mHelp);
        return bar;
    }

    private JToolBar buildToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.setBackground(new Color(245, 247, 252));
        tb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        ActionListener al = e -> dispatch(e.getActionCommand());
        tb.add(UI.toolbarBtn("Exit",            "images/exit.png",    "EXIT",    al)); tb.addSeparator();
        tb.add(UI.toolbarBtn("Add Employee",    "images/employee.png","ADD",     al));
        tb.add(UI.toolbarBtn("Edit Employee",   "images/edit.png",    "EDIT",    al));
        tb.add(UI.toolbarBtn("Delete Employee", "images/delete.png",  "DELETE",  al)); tb.addSeparator();
        tb.add(UI.toolbarBtn("Salary Settings", "images/setting.png", "SETTINGS",al));
        tb.add(UI.toolbarBtn("Calculator",      "images/calc.png",    "CALC",    al));
        tb.add(UI.toolbarBtn("Notepad",         "images/notepad.png", "NOTE",    al)); tb.addSeparator();
        tb.add(UI.toolbarBtn("Pay Slip Report", "images/emp_rpt.png", "REPORT",  al)); tb.addSeparator();
        tb.add(UI.toolbarBtn("About",           "images/author.png",  "ABOUT",   al));
        tb.add(UI.toolbarBtn("User Guide",      "images/help.png",    "HELP",    al));
        return tb;
    }

    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();
        if (s==miExit) confirmExit();
        else if (s==miAdd)      dispatch("ADD");
        else if (s==miEdit)     dispatch("EDIT");
        else if (s==miDelete)   dispatch("DELETE");
        else if (s==miSettings) dispatch("SETTINGS");
        else if (s==miCalc)     dispatch("CALC");
        else if (s==miNote)     dispatch("NOTE");
        else if (s==miReport)   dispatch("REPORT");
        else if (s==miAbout)    dispatch("ABOUT");
        else if (s==miHelp)     dispatch("HELP");
    }

    private void dispatch(String cmd) {
        switch (cmd) {
            case "EXIT":     confirmExit(); break;
            case "ADD":      open("Add Employee",    new AddEmployeeWindow()); break;
            case "EDIT":     open("Edit Employee",   new EditEmployeeWindow()); break;
            case "DELETE":   open("Delete Employee", new DeleteEmployeeWindow()); break;
            case "SETTINGS": open("Salary Settings", new SettingsWindow()); break;
            case "REPORT":   open("Employee Pay Slip", new PaySlipWindow()); break;
            case "ABOUT":    open("About",           new AboutWindow()); break;
            case "HELP":     open("User Guide",      new HelpWindow()); break;
            case "CALC":     runApp("calc.exe"); break;
            case "NOTE":     runApp("notepad.exe"); break;
        }
    }

    private void open(String title, JInternalFrame frame) {
        for (JInternalFrame f : desktop.getAllFrames()) {
            if (f.getTitle().equalsIgnoreCase(title)) {
                try { f.setIcon(false); f.setSelected(true); } catch (PropertyVetoException ex) {}
                f.toFront(); return;
            }
        }
        desktop.add(frame);
        frame.setVisible(true);
        try { frame.setSelected(true); } catch (PropertyVetoException ex) {}
    }

    private void runApp(String app) {
        try { Runtime.getRuntime().exec(app); }
        catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Cannot launch: " + app,
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void confirmExit() {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?",
            "Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) System.exit(0);
    }
}

class AddEmployeeWindow extends JInternalFrame implements ActionListener {
    private JTextField tfCode, tfFirst, tfLast, tfAddr, tfPhone;
    private JComboBox<String> cbDesi;
    private JButton btnAdd, btnReset, btnExit;

    AddEmployeeWindow() {
        super("Add Employee", true, true, true, true);
        setSize(430, 320);
        setFrameIcon(icon("images/employee.png"));

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 10));
        form.setBorder(new EmptyBorder(16, 22, 10, 22));

        tfCode  = field(); tfFirst = field(); tfLast  = field();
        tfAddr  = field(); tfPhone = field();
        cbDesi  = new JComboBox<>();
        UI.numericOnly(tfPhone);
        UI.refreshCombo(cbDesi);

        form.add(UI.lbl("Employee Code :")); form.add(tfCode);
        form.add(UI.lbl("Designation   :")); form.add(cbDesi);
        form.add(UI.lbl("First Name    :")); form.add(tfFirst);
        form.add(UI.lbl("Last Name     :")); form.add(tfLast);
        form.add(UI.lbl("Address       :")); form.add(tfAddr);
        form.add(UI.lbl("Contact No    :")); form.add(tfPhone);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        btnAdd   = UI.btn("Add",   "images/ok.png");
        btnReset = UI.btn("Reset", "images/reset.png");
        btnExit  = UI.btn("Exit",  "images/exit.png");
        for (JButton b : new JButton[]{btnAdd,btnReset,btnExit}) { b.addActionListener(this); btns.add(b); }

        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnExit)  { dispose(); return; }
        if (e.getSource() == btnReset) { reset(); return; }
        String code  = tfCode.getText().trim();
        String first = tfFirst.getText().trim();
        String last  = tfLast.getText().trim();
        String desi  = (String) cbDesi.getSelectedItem();
        String addr  = tfAddr.getText().trim();
        String phone = tfPhone.getText().trim();
        if (code.isEmpty()||first.isEmpty()||last.isEmpty()||addr.isEmpty()||phone.isEmpty()) {
            warn("All fields are required."); return;
        }
        if (desi == null) { warn("No designations found. Add one in Salary Settings first."); return; }
        Employee emp = new Employee(code, first, last, desi, addr, phone);
        if (FileDB.addEmployee(emp)) {
            info("Employee \"" + code + "\" added successfully!"); reset();
        } else {
            warn("Employee Code \"" + code + "\" already exists.");
        }
    }

    private void reset() { tfCode.setText(""); tfFirst.setText(""); tfLast.setText(""); tfAddr.setText(""); tfPhone.setText(""); }
    private JTextField field() { JTextField t = new JTextField(14); t.setFont(new Font("Segoe UI",Font.PLAIN,12)); return t; }
    private void warn(String m) { JOptionPane.showMessageDialog(this,m,"Warning",JOptionPane.WARNING_MESSAGE); }
    private void info(String m) { JOptionPane.showMessageDialog(this,m,"Success",JOptionPane.INFORMATION_MESSAGE); }
    private Icon icon(String p) { File f=new File(p); return f.exists()?new ImageIcon(p):null; }
}

class EditEmployeeWindow extends JInternalFrame implements ActionListener {
    private JTextField tfCode, tfFirst, tfLast, tfAddr, tfPhone;
    private JComboBox<String> cbDesi;
    private JButton btnFind, btnSave, btnExit;

    EditEmployeeWindow() {
        super("Edit Employee", true, true, true, true);
        setSize(430, 320);
        setFrameIcon(icon("images/edit.png"));

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 10));
        form.setBorder(new EmptyBorder(16, 22, 10, 22));

        tfCode  = field(true);
        tfFirst = field(false); tfLast  = field(false);
        tfAddr  = field(false); tfPhone = field(false);
        cbDesi  = new JComboBox<>();
        cbDesi.setEnabled(false);
        UI.numericOnly(tfPhone);
        UI.refreshCombo(cbDesi);

        form.add(UI.lbl("Employee Code :")); form.add(tfCode);
        form.add(UI.lbl("Designation   :")); form.add(cbDesi);
        form.add(UI.lbl("First Name    :")); form.add(tfFirst);
        form.add(UI.lbl("Last Name     :")); form.add(tfLast);
        form.add(UI.lbl("Address       :")); form.add(tfAddr);
        form.add(UI.lbl("Contact No    :")); form.add(tfPhone);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        btnFind = UI.btn("Find", "images/search.png");
        btnSave = UI.btn("Save", "images/save_all.png");
        btnExit = UI.btn("Exit", "images/exit.png");
        for (JButton b : new JButton[]{btnFind,btnSave,btnExit}) { b.addActionListener(this); btns.add(b); }

        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnExit) { dispose(); return; }
        if (e.getSource() == btnFind) {
            String code = tfCode.getText().trim();
            if (code.isEmpty()) { warn("Enter Employee Code first."); return; }
            Employee emp = FileDB.findEmployee(code);
            if (emp == null) { warn("No employee found with code: " + code); clearFields(false); return; }
            tfFirst.setText(emp.firstName); tfFirst.setEditable(true);
            tfLast.setText(emp.lastName);   tfLast.setEditable(true);
            cbDesi.setSelectedItem(emp.designation); cbDesi.setEnabled(true);
            tfAddr.setText(emp.address);    tfAddr.setEditable(true);
            tfPhone.setText(emp.phone);     tfPhone.setEditable(true);
            return;
        }
        String code  = tfCode.getText().trim();
        String first = tfFirst.getText().trim();
        String last  = tfLast.getText().trim();
        String desi  = (String) cbDesi.getSelectedItem();
        String addr  = tfAddr.getText().trim();
        String phone = tfPhone.getText().trim();
        if (code.isEmpty()||first.isEmpty()||last.isEmpty()||addr.isEmpty()||phone.isEmpty()) {
            warn("All fields are required."); return;
        }
        Employee emp = new Employee(code, first, last, desi, addr, phone);
        if (FileDB.updateEmployee(emp)) {
            info("Employee updated successfully!"); clearFields(true);
        } else { warn("Employee not found."); }
    }

    private void clearFields(boolean clearCode) {
        if (clearCode) tfCode.setText("");
        tfFirst.setText(""); tfFirst.setEditable(false);
        tfLast.setText("");  tfLast.setEditable(false);
        tfAddr.setText("");  tfAddr.setEditable(false);
        tfPhone.setText(""); tfPhone.setEditable(false);
        cbDesi.setEnabled(false);
    }
    private JTextField field(boolean editable) {
        JTextField t = new JTextField(14); t.setEditable(editable);
        if (!editable) t.setBackground(new Color(240,240,240));
        t.setFont(new Font("Segoe UI",Font.PLAIN,12)); return t;
    }
    private void warn(String m) { JOptionPane.showMessageDialog(this,m,"Warning",JOptionPane.WARNING_MESSAGE); }
    private void info(String m) { JOptionPane.showMessageDialog(this,m,"Success",JOptionPane.INFORMATION_MESSAGE); }
    private Icon icon(String p) { File f=new File(p); return f.exists()?new ImageIcon(p):null; }
}

class DeleteEmployeeWindow extends JInternalFrame implements ActionListener {
    private JTextField tfCode,tfFirst,tfLast,tfDesi,tfAddr,tfPhone;
    private JButton btnFind, btnDelete, btnExit;

    DeleteEmployeeWindow() {
        super("Delete Employee", true, true, true, true);
        setSize(430, 300);
        setFrameIcon(icon("images/delete.png"));

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 10));
        form.setBorder(new EmptyBorder(14, 22, 8, 22));

        tfCode  = new JTextField(14); tfCode.setFont(new Font("Segoe UI",Font.PLAIN,12));
        tfFirst = ro(); tfLast = ro(); tfDesi = ro(); tfAddr = ro(); tfPhone = ro();

        form.add(UI.lbl("Employee Code :")); form.add(tfCode);
        form.add(UI.lbl("First Name    :")); form.add(tfFirst);
        form.add(UI.lbl("Last Name     :")); form.add(tfLast);
        form.add(UI.lbl("Designation   :")); form.add(tfDesi);
        form.add(UI.lbl("Address       :")); form.add(tfAddr);
        form.add(UI.lbl("Contact No    :")); form.add(tfPhone);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        btnFind   = UI.btn("Find",   "images/search.png");
        btnDelete = UI.btn("Delete", "images/delete.png");
        btnExit   = UI.btn("Exit",   "images/exit.png");
        for (JButton b : new JButton[]{btnFind,btnDelete,btnExit}) { b.addActionListener(this); btns.add(b); }

        add(form, BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnExit) { dispose(); return; }
        if (e.getSource() == btnFind) {
            String code = tfCode.getText().trim();
            if (code.isEmpty()) { warn("Enter Employee Code first."); return; }
            Employee emp = FileDB.findEmployee(code);
            if (emp == null) { warn("No employee found: " + code); clear(); return; }
            tfFirst.setText(emp.firstName); tfLast.setText(emp.lastName);
            tfDesi.setText(emp.designation); tfAddr.setText(emp.address); tfPhone.setText(emp.phone);
            return;
        }
        String code = tfCode.getText().trim();
        if (code.isEmpty()) { warn("Find an employee first."); return; }
        int r = JOptionPane.showConfirmDialog(this, "Delete employee \"" + code + "\"?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION) {
            if (FileDB.deleteEmployee(code)) {
                info("Employee deleted."); tfCode.setText(""); clear();
            } else warn("Employee not found.");
        }
    }

    private void clear() { tfFirst.setText(""); tfLast.setText(""); tfDesi.setText(""); tfAddr.setText(""); tfPhone.setText(""); }
    private JTextField ro() {
        JTextField t = new JTextField(14); t.setEditable(false);
        t.setBackground(new Color(240,240,240)); t.setFont(new Font("Segoe UI",Font.PLAIN,12)); return t;
    }
    private void warn(String m) { JOptionPane.showMessageDialog(this,m,"Warning",JOptionPane.WARNING_MESSAGE); }
    private void info(String m) { JOptionPane.showMessageDialog(this,m,"Deleted",JOptionPane.INFORMATION_MESSAGE); }
    private Icon icon(String p) { File f=new File(p); return f.exists()?new ImageIcon(p):null; }
}

class SettingsWindow extends JInternalFrame implements ActionListener, ItemListener {
    private JComboBox<String> cbCat;
    private JTextField tfNewName, tfBasic;
    private JTextField tfDA1,tfHRA1,tfWA1,tfGPF1,tfIT1,tfGIS1,tfPF1,tfLIC1;
    private JCheckBox  chDA,chHRA,chWA,chGPF,chIT,chGIS,chPF,chLIC;
    private JButton    btnAddNew, btnSave, btnDelete, btnExit;
    private int currentId = -1;

    SettingsWindow() {
        super("Salary Settings", true, true, true, true);
        setSize(680, 580);
        setFrameIcon(icon("images/setting.png"));

        Container c = getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

        JPanel rowTop = row();
        cbCat = new JComboBox<>(); cbCat.addActionListener(this);
        tfNewName = new JTextField(14); tfNewName.setFont(new Font("Segoe UI",Font.PLAIN,12));
        UI.refreshCombo(cbCat);
        rowTop.add(UI.lbl("Select Designation:")); rowTop.add(cbCat);
        rowTop.add(Box.createHorizontalStrut(16));
        rowTop.add(UI.lbl("New Name:")); rowTop.add(tfNewName);

        JPanel rowBasic = row();
        tfBasic = numField(8);
        rowBasic.add(UI.lbl("Basic Pay (\u20b9) :")); rowBasic.add(tfBasic);

        JPanel allowHdr = row(); allowHdr.add(sectionLabel("ALLOWANCES  ( \u2713 = % of Basic  |  unchecked = Fixed \u20b9 )"));
        JPanel allowGrid = new JPanel(new GridLayout(3, 4, 6, 6));
        allowGrid.setBorder(new EmptyBorder(4, 18, 4, 18));
        chDA=ch("DA%"); chHRA=ch("HRA%"); chWA=ch("WA%");
        tfDA1=numField(7); tfHRA1=numField(7); tfWA1=numField(7);
        addRow4(allowGrid, UI.lbl("DA  Allowance :"), chDA,  tfDA1,  UI.lbl("(% or \u20b9)"));
        addRow4(allowGrid, UI.lbl("HRA Allowance :"), chHRA, tfHRA1, UI.lbl("(% or \u20b9)"));
        addRow4(allowGrid, UI.lbl("WA  Allowance :"), chWA,  tfWA1,  UI.lbl("(% or \u20b9)"));

        JPanel deduHdr = row(); deduHdr.add(sectionLabel("DEDUCTIONS   ( \u2713 = % of Basic  |  unchecked = Fixed \u20b9 )"));
        JPanel deduGrid = new JPanel(new GridLayout(5, 4, 6, 6));
        deduGrid.setBorder(new EmptyBorder(4, 18, 4, 18));
        chGPF=ch("GPF%"); chIT=ch("IT%"); chGIS=ch("GIS%"); chPF=ch("PF%"); chLIC=ch("LIC%");
        tfGPF1=numField(7); tfIT1=numField(7); tfGIS1=numField(7); tfPF1=numField(7); tfLIC1=numField(7);
        addRow4(deduGrid, UI.lbl("GPF Deduction :"), chGPF, tfGPF1, UI.lbl("(% or \u20b9)"));
        addRow4(deduGrid, UI.lbl("I.T Deduction :"), chIT,  tfIT1,  UI.lbl("(% or \u20b9)"));
        addRow4(deduGrid, UI.lbl("GIS Deduction :"), chGIS, tfGIS1, UI.lbl("(% or \u20b9)"));
        addRow4(deduGrid, UI.lbl("PF  Deduction :"), chPF,  tfPF1,  UI.lbl("(% or \u20b9)"));
        addRow4(deduGrid, UI.lbl("LIC Deduction :"), chLIC, tfLIC1, UI.lbl("(% or \u20b9)"));

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        btnAddNew = UI.btn("Add New",  "images/ok.png");
        btnSave   = UI.btn("Save",     "images/save_all.png");
        btnDelete = UI.btn("Delete",   "images/delete.png");
        btnExit   = UI.btn("Exit",     "images/exit.png");
        for (JButton b : new JButton[]{btnAddNew,btnSave,btnDelete,btnExit}) { b.addActionListener(this); btns.add(b); }

        for (JCheckBox ch : new JCheckBox[]{chDA,chHRA,chWA,chGPF,chIT,chGIS,chPF,chLIC}) ch.addItemListener(this);

        c.add(rowTop); c.add(rowBasic);
        c.add(allowHdr); c.add(allowGrid);
        c.add(deduHdr);  c.add(deduGrid);
        c.add(btns);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        if (cbCat.getItemCount() > 0) loadDesig((String) cbCat.getSelectedItem());
    }

    public void actionPerformed(ActionEvent e) {
        Object s = e.getSource();
        if (s == cbCat)    { loadDesig((String) cbCat.getSelectedItem()); return; }
        if (s == btnExit)  { dispose(); return; }
        if (s == btnAddNew){ doAdd(); return; }
        if (s == btnSave)  { doSave(); return; }
        if (s == btnDelete){ doDelete(); }
    }

    public void itemStateChanged(ItemEvent e) {}

    private void loadDesig(String name) {
        if (name == null) return;
        Designation d = FileDB.findDesignation(name);
        if (d == null) return;
        currentId = d.id;
        tfBasic.setText("" + d.basicPay);
        chDA.setSelected(d.daPercent);   tfDA1.setText(""+d.daVal);
        chHRA.setSelected(d.hraPercent); tfHRA1.setText(""+d.hraVal);
        chWA.setSelected(d.waPercent);   tfWA1.setText(""+d.waVal);
        chGPF.setSelected(d.gpfPercent); tfGPF1.setText(""+d.gpfVal);
        chIT.setSelected(d.itPercent);   tfIT1.setText(""+d.itVal);
        chGIS.setSelected(d.gisPercent); tfGIS1.setText(""+d.gisVal);
        chPF.setSelected(d.pfPercent);   tfPF1.setText(""+d.pfVal);
        chLIC.setSelected(d.licPercent); tfLIC1.setText(""+d.licVal);
    }

    private void doAdd() {
        String name = tfNewName.getText().trim();
        if (name.isEmpty() || tfBasic.getText().trim().isEmpty()) {
            warn("New Name and Basic Pay are required."); return;
        }
        Designation d = buildDesig(); d.name = name;
        if (FileDB.addDesignation(d)) {
            info("Designation \"" + name + "\" added!");
            cbCat.addItem(name); cbCat.setSelectedItem(name);
            tfNewName.setText("");
        } else warn("Designation \"" + name + "\" already exists.");
    }

    private void doSave() {
        if (currentId == -1) { warn("Select a designation first."); return; }
        if (tfBasic.getText().trim().isEmpty()) { warn("Basic Pay is required."); return; }
        Designation d = buildDesig();
        d.id   = currentId;
        d.name = (String) cbCat.getSelectedItem();
        if (FileDB.updateDesignation(d)) info("Designation saved!");
        else warn("Save failed.");
    }

    private void doDelete() {
        String name = (String) cbCat.getSelectedItem();
        if (name == null) { warn("Select a designation first."); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete \"" + name + "\"?", "Confirm",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            if (FileDB.deleteDesignation(name)) {
                cbCat.removeItem(name); info("Deleted."); currentId = -1;
            } else warn("Delete failed.");
        }
    }

    private Designation buildDesig() {
        Designation d = new Designation();
        d.basicPay   = intOf(tfBasic);
        d.daPercent  = chDA.isSelected();  d.daVal  = intOf(tfDA1);
        d.hraPercent = chHRA.isSelected(); d.hraVal = intOf(tfHRA1);
        d.waPercent  = chWA.isSelected();  d.waVal  = intOf(tfWA1);
        d.gpfPercent = chGPF.isSelected(); d.gpfVal = intOf(tfGPF1);
        d.itPercent  = chIT.isSelected();  d.itVal  = intOf(tfIT1);
        d.gisPercent = chGIS.isSelected(); d.gisVal = intOf(tfGIS1);
        d.pfPercent  = chPF.isSelected();  d.pfVal  = intOf(tfPF1);
        d.licPercent = chLIC.isSelected(); d.licVal = intOf(tfLIC1);
        return d;
    }

    private JPanel row() { JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,8,6)); return p; }
    private void addRow4(JPanel p, Component c1,Component c2,Component c3,Component c4) {
        p.add(c1); p.add(c2); p.add(c3); p.add(c4);
    }
    private JTextField numField(int cols) { JTextField t=new JTextField(cols); UI.numericOnly(t); t.setFont(new Font("Segoe UI",Font.PLAIN,12)); return t; }
    private JCheckBox ch(String l)   { JCheckBox c=new JCheckBox(l); c.setFont(new Font("Segoe UI",Font.PLAIN,11)); return c; }
    private int intOf(JTextField t)  { try{return Integer.parseInt(t.getText().trim());}catch(Exception e){return 0;} }
    private JLabel sectionLabel(String t) { JLabel l=new JLabel(t); l.setFont(new Font("Segoe UI",Font.BOLD,12)); l.setForeground(UI.DARK_BLUE); return l; }
    private void warn(String m) { JOptionPane.showMessageDialog(this,m,"Warning",JOptionPane.WARNING_MESSAGE); }
    private void info(String m) { JOptionPane.showMessageDialog(this,m,"Success",JOptionPane.INFORMATION_MESSAGE); }
    private Icon icon(String p) { File f=new File(p); return f.exists()?new ImageIcon(p):null; }
}

class PaySlipWindow extends JInternalFrame implements ActionListener {
    private JTextField tfCode, tfYear, tfName, tfDesi;
    private JComboBox<String> cbMonth;
    private JTextField tfBasic,tfDA,tfHRA,tfWA,tfAllow,tfGPF,tfIT,tfGIS,tfPF,tfLIC,tfDedu,tfNet;
    private JButton btnGen, btnPrint, btnExit;
    private Designation lastDesig; private Employee lastEmp;
    private static final String[] MONTHS = {"January","February","March","April","May","June",
        "July","August","September","October","November","December"};

    PaySlipWindow() {
        super("Employee Pay Slip", true, true, true, true);
        setSize(530, 620);
        setFrameIcon(icon("images/emp_rpt.png"));

        Container c = getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

        JPanel inp = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        inp.setBorder(UI.titled("Search"));
        tfCode = new JTextField(10); tfCode.setFont(new Font("Segoe UI",Font.PLAIN,12));
        cbMonth = new JComboBox<>(MONTHS);
        int curYear = Calendar.getInstance().get(Calendar.YEAR);
        tfYear = new JTextField(String.valueOf(curYear), 5);
        btnGen = UI.btn("Generate", "images/preview.png");
        btnGen.addActionListener(this);
        inp.add(UI.lbl("Code:")); inp.add(tfCode);
        inp.add(UI.lbl("Month:")); inp.add(cbMonth);
        inp.add(UI.lbl("Year:")); inp.add(tfYear);
        inp.add(btnGen);

        JPanel ei = new JPanel(new GridLayout(2, 2, 6, 6));
        ei.setBorder(UI.titled("Employee"));
        tfName = UI.ro(); tfDesi = UI.ro();
        ei.add(UI.lbl("Name:")); ei.add(tfName);
        ei.add(UI.lbl("Designation:")); ei.add(tfDesi);

        JPanel sal = new JPanel(new GridLayout(12, 2, 6, 5));
        sal.setBorder(UI.titled("Salary Breakdown"));
        tfBasic=UI.ro(); tfDA=UI.ro(); tfHRA=UI.ro(); tfWA=UI.ro(); tfAllow=UI.ro();
        tfGPF=UI.ro(); tfIT=UI.ro(); tfGIS=UI.ro(); tfPF=UI.ro(); tfLIC=UI.ro();
        tfDedu=UI.ro(); tfNet=UI.ro();
        tfNet.setFont(new Font("Segoe UI",Font.BOLD,14)); tfNet.setForeground(new Color(0,128,0));

        sal.add(UI.lbl("Basic Pay \u20b9:")); sal.add(tfBasic);
        sal.add(sectionLbl("\u2500\u2500 Allowances \u2500\u2500")); sal.add(new JLabel());
        sal.add(UI.lbl("DA \u20b9:")); sal.add(tfDA);
        sal.add(UI.lbl("HRA \u20b9:")); sal.add(tfHRA);
        sal.add(UI.lbl("WA \u20b9:")); sal.add(tfWA);
        sal.add(UI.lbl("Total Allowances \u20b9:")); sal.add(tfAllow);
        sal.add(sectionLbl("\u2500\u2500 Deductions \u2500\u2500")); sal.add(new JLabel());
        sal.add(UI.lbl("GPF \u20b9:")); sal.add(tfGPF);
        sal.add(UI.lbl("Income Tax \u20b9:")); sal.add(tfIT);
        sal.add(UI.lbl("GIS \u20b9:")); sal.add(tfGIS);
        sal.add(UI.lbl("PF \u20b9:")); sal.add(tfPF);
        sal.add(UI.lbl("LIC \u20b9:")); sal.add(tfLIC);
        sal.add(UI.lbl("Total Deductions \u20b9:")); sal.add(tfDedu);

        JPanel netRow = new JPanel(new FlowLayout(FlowLayout.LEFT,10,6));
        netRow.setBorder(UI.titled("Net Salary"));
        netRow.add(UI.lbl("NET SALARY \u20b9:")); netRow.add(tfNet);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER,12,8));
        btnPrint = UI.btn("Preview & Print","images/prints.png");
        btnExit  = UI.btn("Exit","images/exit.png");
        btnPrint.addActionListener(this); btnExit.addActionListener(this);
        btns.add(btnPrint); btns.add(btnExit);

        for (JPanel p : new JPanel[]{inp,ei,sal,netRow,btns}) {
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height+8));
            c.add(p);
        }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnExit)  { dispose(); return; }
        if (e.getSource() == btnPrint) { doPrint(); return; }
        String code = tfCode.getText().trim();
        if (code.isEmpty()) { warn("Enter Employee Code."); return; }
        Employee emp = FileDB.findEmployee(code);
        if (emp == null) { warn("No employee found: " + code); return; }
        Designation d = FileDB.findDesignation(emp.designation);
        if (d == null) { warn("No salary settings for designation: " + emp.designation); return; }
        lastEmp = emp; lastDesig = d;
        tfName.setText(emp.firstName + " " + emp.lastName);
        tfDesi.setText(emp.designation);
        tfBasic.setText("" + d.basicPay);
        tfDA.setText(""  + d.daRs());  tfHRA.setText("" + d.hraRs()); tfWA.setText("" + d.waRs());
        tfAllow.setText("" + d.totalAllowance());
        tfGPF.setText("" + d.gpfRs()); tfIT.setText(""  + d.itRs());  tfGIS.setText("" + d.gisRs());
        tfPF.setText(""  + d.pfRs());  tfLIC.setText("" + d.licRs());
        tfDedu.setText("" + d.totalDeduction());
        tfNet.setText("\u20b9 " + d.netSalary());
    }

    private void doPrint() {
        if (lastDesig == null) { warn("Generate a pay slip first."); return; }
        String month = (String) cbMonth.getSelectedItem();
        String year  = tfYear.getText().trim();
        new PrintSlipFrame(lastEmp, lastDesig, month, year);
    }

    private JLabel sectionLbl(String t) { JLabel l=new JLabel(t); l.setFont(new Font("Segoe UI",Font.BOLD,11)); l.setForeground(UI.DARK_BLUE); return l; }
    private void warn(String m) { JOptionPane.showMessageDialog(this,m,"Warning",JOptionPane.WARNING_MESSAGE); }
    private Icon icon(String p) { File f=new File(p); return f.exists()?new ImageIcon(p):null; }
}

class PrintSlipFrame extends JFrame implements ActionListener, Printable {
    private JTextArea area;
    private JButton btnPrint, btnExit;
    private String text;

    PrintSlipFrame(Employee emp, Designation d, String month, String year) {
        super("Pay Slip \u2013 Preview");
        setSize(520, 640);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        String today = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String ln    = "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\n";
        text =
            ln +
            "      EMPLOYEE SALARY MANAGEMENT SYSTEM\n" +
            "                  PAY SLIP\n" +
            ln +
            String.format("  Date          : %s%n", today) +
            String.format("  Employee Code : %s%n", emp.code) +
            String.format("  Name          : %s %s%n", emp.firstName, emp.lastName) +
            String.format("  Designation   : %s%n", emp.designation) +
            String.format("  Month / Year  : %s  %s%n", month, year) +
            ln +
            String.format("  Basic Pay     :  \u20b9 %,d%n", d.basicPay) +
            "\n  \u2500\u2500 ALLOWANCES \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\n" +
            String.format("  DA            :  \u20b9 %,d   (%s)%n", d.daRs(),  pctOrFixed(d.daPercent,  d.daVal)) +
            String.format("  HRA           :  \u20b9 %,d   (%s)%n", d.hraRs(), pctOrFixed(d.hraPercent, d.hraVal)) +
            String.format("  WA            :  \u20b9 %,d   (%s)%n", d.waRs(),  pctOrFixed(d.waPercent,  d.waVal)) +
            String.format("  Total Allow.  :  \u20b9 %,d%n", d.totalAllowance()) +
            "\n  \u2500\u2500 DEDUCTIONS \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\n" +
            String.format("  GPF           :  \u20b9 %,d   (%s)%n", d.gpfRs(), pctOrFixed(d.gpfPercent, d.gpfVal)) +
            String.format("  Income Tax    :  \u20b9 %,d   (%s)%n", d.itRs(),  pctOrFixed(d.itPercent,  d.itVal)) +
            String.format("  GIS           :  \u20b9 %,d   (%s)%n", d.gisRs(), pctOrFixed(d.gisPercent, d.gisVal)) +
            String.format("  PF            :  \u20b9 %,d   (%s)%n", d.pfRs(),  pctOrFixed(d.pfPercent,  d.pfVal)) +
            String.format("  LIC           :  \u20b9 %,d   (%s)%n", d.licRs(), pctOrFixed(d.licPercent, d.licVal)) +
            String.format("  Total Dedu.   :  \u20b9 %,d%n", d.totalDeduction()) +
            ln +
            String.format("  NET SALARY    :  \u20b9 %,d%n", d.netSalary()) +
            ln +
            "\n\n  Authorised Signature: _______________________\n";

        area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setBorder(new EmptyBorder(8, 12, 8, 12));

        btnPrint = UI.btn("  Print", "images/print.png");
        btnExit  = UI.btn("  Close", "images/exit.png");
        btnPrint.setBackground(new Color(31,73,125)); btnPrint.setForeground(Color.WHITE);
        btnPrint.addActionListener(this); btnExit.addActionListener(this);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        btns.add(btnPrint); btns.add(btnExit);

        add(new JScrollPane(area), BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnExit) { dispose(); return; }
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        if (job.printDialog()) {
            try { job.print(); }
            catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        if (page > 0) return NO_SUCH_PAGE;
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(pf.getImageableX(), pf.getImageableY());
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        int lh = g2.getFontMetrics().getHeight(), y = lh, x = 8;
        for (String line : text.split("\n")) { g2.drawString(line, x, y); y += lh; }
        return PAGE_EXISTS;
    }

    private String pctOrFixed(boolean pct, int val) { return pct ? val+"% of Basic" : "Fixed \u20b9"+val; }
}

class AboutWindow extends JInternalFrame implements ActionListener {
    AboutWindow() {
        super("About", true, true, true, true);
        setSize(400, 300);
        setFrameIcon(icon("images/author.png"));

        JPanel main = new JPanel(new BorderLayout(8,8));
        main.setBorder(new EmptyBorder(14,20,10,20));
        JLabel title = new JLabel("Employee Salary Management System", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(UI.DARK_BLUE);

        JTextArea info = new JTextArea(
            "\n  Version   : 1.0\n" +
            "  Language  : Java (Swing)\n" +
            "  Storage   : CSV Files (no database needed)\n\n" +
            "  Features:\n" +
            "    \u2022 Employee Management (Add/Edit/Delete)\n" +
            "    \u2022 Salary Designation Configuration\n" +
            "    \u2022 DA, HRA, WA Allowances\n" +
            "    \u2022 GPF, IT, GIS, PF, LIC Deductions\n" +
            "    \u2022 Pay Slip Generation & Printing\n\n" +
            "  Centurion University of Technology & Management"
        );
        info.setEditable(false); info.setOpaque(false);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton close = UI.btn("Close","images/exit.png");
        close.addActionListener(this);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btns.add(close);

        main.add(title, BorderLayout.NORTH);
        main.add(new JScrollPane(info), BorderLayout.CENTER);
        main.add(btns, BorderLayout.SOUTH);
        add(main);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }
    public void actionPerformed(ActionEvent e) { dispose(); }
    private Icon icon(String p) { File f=new File(p); return f.exists()?new ImageIcon(p):null; }
}

class HelpWindow extends JInternalFrame implements ActionListener {
    HelpWindow() {
        super("User Guide", true, true, true, true);
        setSize(500, 500);
        setFrameIcon(icon("images/help.png"));

        JTextArea help = new JTextArea();
        help.setEditable(false);
        help.setFont(new Font("Monospaced", Font.PLAIN, 12));
        help.setMargin(new Insets(10,14,10,14));
        help.setText(
            "========================================\n" +
            "  EMPLOYEE SALARY MANAGEMENT SYSTEM\n" +
            "              USER GUIDE\n" +
            "========================================\n\n" +
            "DEFAULT LOGIN:  admin  /  admin\n\n" +

            "EMPLOYEE MENU\n" +
            "--------------\n" +
            "Add Employee\n" +
            "  Fill all 6 fields and click Add.\n\n" +
            "Edit Employee\n" +
            "  Enter Code > Find > Edit fields > Save.\n\n" +
            "Delete Employee\n" +
            "  Enter Code > Find > Delete > Confirm.\n\n" +

            "TOOLS > SALARY SETTINGS\n" +
            "------------------------\n" +
            "  Select a designation to view/edit it.\n" +
            "  Or type a New Name and click Add New.\n" +
            "  Set Basic Pay.\n" +
            "  For each allowance/deduction:\n" +
            "    Tick   = value is % of Basic Pay\n" +
            "    Untick = value is fixed Rupees\n" +
            "  Click Save to update the selected one.\n" +
            "  Click Delete to remove it.\n\n" +

            "REPORTS > EMPLOYEE PAY SLIP\n" +
            "----------------------------\n" +
            "  1. Enter Employee Code.\n" +
            "  2. Select Month and Year.\n" +
            "  3. Click Generate.\n" +
            "  4. Click Preview & Print.\n" +
            "  5. In the print window, click Print.\n\n" +

            "DATA FILES (auto-created in project folder)\n" +
            "--------------------------------------------\n" +
            "  employees.csv  - employee records\n" +
            "  settings.csv   - salary designations\n" +
            "  users.csv      - login credentials\n"
        );

        JButton close = UI.btn("Close","images/exit.png");
        close.addActionListener(this);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btns.add(close);

        add(new JScrollPane(help), BorderLayout.CENTER);
        add(btns, BorderLayout.SOUTH);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }
    public void actionPerformed(ActionEvent e) { dispose(); }
    private Icon icon(String p) { File f=new File(p); return f.exists()?new ImageIcon(p):null; }
}