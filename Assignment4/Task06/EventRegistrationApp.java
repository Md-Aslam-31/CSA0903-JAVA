import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/** Layered JDBC desktop app. Default demo mode uses H2; MySQL mode uses env settings. */
public final class EventRegistrationApp extends JFrame {
    private final JTextField studentId = new JTextField(12);
    private final JTextField studentName = new JTextField(14);
    private final JComboBox<String> event = new JComboBox<>(new String[]{"Cultural Night", "Hackathon", "Music Workshop"});
    private final JComboBox<String> filter = new JComboBox<>(new String[]{"All events", "Cultural Night", "Hackathon", "Music Workshop"});
    private final DefaultTableModel tableModel = new DefaultTableModel(new String[]{"ID", "Student ID", "Name", "Event", "Registered"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final RegistrationService service;
    private int offset;
    private static final int PAGE_SIZE = 25;

    EventRegistrationApp(RegistrationService service) {
        super("Campus Event Registration"); this.service = service;
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN); table.setRowHeight(24);
        int[] widths = {45, 105, 155, 150, 220};
        for (int i=0; i<widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        JMenuBar bar = new JMenuBar(); JMenu file = new JMenu("File"); JMenuItem export = new JMenuItem("Export registrations..."); JMenuItem exit = new JMenuItem("Exit");
        file.add(export); file.add(exit); bar.add(file); setJMenuBar(bar);
        JPanel form = new JPanel(new GridLayout(2, 3, 7, 5));
        form.add(new JLabel("Student ID")); form.add(new JLabel("Student name")); form.add(new JLabel("Event"));
        form.add(studentId); form.add(studentName); form.add(event);
        JButton register = new JButton("Register"); JPanel registration = new JPanel(new BorderLayout()); registration.add(form, BorderLayout.CENTER); registration.add(register, BorderLayout.EAST);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT)); controls.add(new JLabel("Filter")); controls.add(filter);
        JButton previous = new JButton("Previous"); JButton next = new JButton("Next"); controls.add(previous); controls.add(next);
        add(registration, BorderLayout.NORTH); add(new JScrollPane(table), BorderLayout.CENTER); add(controls, BorderLayout.SOUTH);
        register.addActionListener(e -> registerAsync()); filter.addActionListener(e -> { offset = 0; loadPageAsync(); });
        previous.addActionListener(e -> { offset = Math.max(0, offset - PAGE_SIZE); loadPageAsync(); });
        next.addActionListener(e -> { offset += PAGE_SIZE; loadPageAsync(); });
        export.addActionListener(e -> exportPage()); exit.addActionListener(e -> dispose());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); setSize(820, 480); setLocationByPlatform(true); loadPageAsync();
    }
    private String selectedFilter() { return "All events".equals(filter.getSelectedItem()) ? null : (String) filter.getSelectedItem(); }
    private void registerAsync() {
        String id = studentId.getText().trim(), name = studentName.getText().trim(), eventName = (String) event.getSelectedItem();
        try { RegistrationService.validate(id, name, eventName); }
        catch (IllegalArgumentException ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid registration", JOptionPane.ERROR_MESSAGE); return; }
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception { service.register(id, name, eventName); return null; }
            @Override protected void done() {
                try { get(); studentId.setText(""); studentName.setText(""); JOptionPane.showMessageDialog(EventRegistrationApp.this, "Registration saved."); offset = 0; loadPageAsync(); }
                catch (Exception ex) { JOptionPane.showMessageDialog(EventRegistrationApp.this, message(ex), "Registration failed", JOptionPane.ERROR_MESSAGE); }
            }
        }.execute();
    }
    private void loadPageAsync() {
        final String eventName = selectedFilter();
        new SwingWorker<List<Registration>, Void>() {
            @Override protected List<Registration> doInBackground() throws Exception { return service.page(eventName, PAGE_SIZE, offset); }
            @Override protected void done() {
                try { tableModel.setRowCount(0); for (Registration r : get()) tableModel.addRow(new Object[]{r.id, r.studentId, r.name, r.event, r.registeredAt}); }
                catch (Exception ex) { JOptionPane.showMessageDialog(EventRegistrationApp.this, message(ex), "Load failed", JOptionPane.ERROR_MESSAGE); }
            }
        }.execute();
    }
    private void exportPage() {
        JFileChooser chooser = new JFileChooser(); chooser.setSelectedFile(new java.io.File("registrations.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        final java.io.File destination = chooser.getSelectedFile(); final String eventName = selectedFilter();
        new SwingWorker<Integer, Void>() {
            @Override protected Integer doInBackground() throws Exception { return service.exportCsv(eventName, destination); }
            @Override protected void done() {
                try { JOptionPane.showMessageDialog(EventRegistrationApp.this, "Exported " + get() + " registrations."); }
                catch (Exception ex) { JOptionPane.showMessageDialog(EventRegistrationApp.this, message(ex), "Export failed", JOptionPane.ERROR_MESSAGE); }
            }
        }.execute();
    }
    private static String message(Exception ex) {
        Throwable cause = ex; while (cause.getCause() != null) cause = cause.getCause();
        if (cause instanceof SQLException && ((SQLException) cause).getSQLState() != null && ((SQLException) cause).getSQLState().startsWith("23")) return "This student is already registered for that event.";
        return cause.getMessage();
    }
    public static void main(String[] args) throws Exception {
        boolean demo = args.length == 0 || "--demo".equals(args[0]);
        String url = demo ? "jdbc:h2:mem:eventdemo;MODE=MySQL;DB_CLOSE_DELAY=-1" : env("EVENT_DB_URL", "jdbc:mysql://localhost:3306/campus_events");
        String user = demo ? "sa" : env("EVENT_DB_USER", "root"); String password = demo ? "" : env("EVENT_DB_PASSWORD", "");
        Class.forName(demo ? "org.h2.Driver" : "com.mysql.cj.jdbc.Driver");
        Database database = new Database(url, user, password); database.initialize(demo);
        RegistrationService service = new RegistrationService(database);
        SwingUtilities.invokeLater(() -> new EventRegistrationApp(service).setVisible(true));
    }
    private static String env(String name, String fallback) { String v = System.getenv(name); return v == null || v.isEmpty() ? fallback : v; }
}

final class Database {
    private final String url, user, password;
    Database(String url, String user, String password) { this.url=url; this.user=user; this.password=password; }
    Connection open() throws SQLException { return DriverManager.getConnection(url, user, password); }
    void initialize(boolean demo) throws SQLException {
        try (Connection c=open(); Statement s=c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS registrations (id BIGINT AUTO_INCREMENT PRIMARY KEY, student_id VARCHAR(30) NOT NULL, student_name VARCHAR(100) NOT NULL, event_name VARCHAR(80) NOT NULL, registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, CONSTRAINT uq_student_event UNIQUE (student_id,event_name))");
            if (demo) s.executeUpdate("INSERT INTO registrations(student_id,student_name,event_name) SELECT 'STU100','Asha Rao','Cultural Night' WHERE NOT EXISTS (SELECT 1 FROM registrations WHERE student_id='STU100' AND event_name='Cultural Night')");
        }
    }
}

final class Registration {
    final long id; final String studentId, name, event, registeredAt;
    Registration(long id, String studentId, String name, String event, String registeredAt) { this.id=id; this.studentId=studentId; this.name=name; this.event=event; this.registeredAt=registeredAt; }
}

final class RegistrationService {
    private final Database database;
    RegistrationService(Database database) { this.database=database; }
    static void validate(String studentId, String name, String event) {
        if (studentId == null || !studentId.matches("[A-Za-z0-9-]{3,30}")) throw new IllegalArgumentException("Student ID must be 3-30 letters, digits or hyphens.");
        if (name == null || name.trim().length() < 2 || name.length() > 100) throw new IllegalArgumentException("Enter a name between 2 and 100 characters.");
        if (event == null || event.trim().isEmpty()) throw new IllegalArgumentException("Select an event.");
    }
    void register(String id, String name, String event) throws SQLException {
        try (Connection c=database.open(); PreparedStatement p=c.prepareStatement("INSERT INTO registrations(student_id,student_name,event_name) VALUES(?,?,?)")) {
            p.setString(1,id); p.setString(2,name.trim()); p.setString(3,event); p.executeUpdate();
        }
    }
    List<Registration> page(String event, int limit, int offset) throws SQLException {
        String sql = event == null ? "SELECT id,student_id,student_name,event_name,registered_at FROM registrations ORDER BY id LIMIT ? OFFSET ?" : "SELECT id,student_id,student_name,event_name,registered_at FROM registrations WHERE event_name=? ORDER BY id LIMIT ? OFFSET ?";
        try (Connection c=database.open(); PreparedStatement p=c.prepareStatement(sql)) {
            int n=1; if(event!=null) p.setString(n++,event); p.setInt(n++,limit); p.setInt(n,offset);
            try(ResultSet rs=p.executeQuery()) { List<Registration> rows=new ArrayList<>(); while(rs.next()) rows.add(new Registration(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5))); return rows; }
        }
    }
    int exportCsv(String event, java.io.File file) throws SQLException, IOException {
        String sql = event == null ? "SELECT id,student_id,student_name,event_name,registered_at FROM registrations ORDER BY id" : "SELECT id,student_id,student_name,event_name,registered_at FROM registrations WHERE event_name=? ORDER BY id";
        try (Connection c=database.open(); PreparedStatement p=c.prepareStatement(sql)) {
            if(event!=null) p.setString(1,event);
            try(ResultSet rs=p.executeQuery(); BufferedWriter out=Files.newBufferedWriter(file.toPath(),StandardCharsets.UTF_8)) {
                out.write("id,student_id,name,event,registered_at\n"); int count=0;
                while(rs.next()) {
                    for(int i=1;i<=5;i++) { if(i>1) out.write(','); out.write(csv(rs.getString(i))); }
                    out.newLine(); count++;
                }
                return count;
            }
        }
    }
    private static String csv(String value) { return "\"" + (value == null ? "" : value.replace("\"","\"\"")) + "\""; }
}
