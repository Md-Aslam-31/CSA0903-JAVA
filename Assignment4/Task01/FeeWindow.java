import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/** UI boundary: collects values and displays payment results only. */
public final class FeeWindow extends JFrame {
    private final JTextField regNo = new JTextField(16);
    private final JTextField amount = new JTextField(12);
    private final JLabel status = new JLabel("Enter registration number and amount.");
    private final FeeService service;

    public FeeWindow(FeeService service) {
        super("Student Fee Payment");
        this.service = service;
        JPanel fields = new JPanel(new GridLayout(2, 2, 8, 8));
        fields.add(new JLabel("Registration number")); fields.add(regNo);
        fields.add(new JLabel("Amount (INR)")); fields.add(amount);
        JButton pay = new JButton("Pay");
        JButton clear = new JButton("Clear");
        JButton receipt = new JButton("Receipt");
        JPanel actions = new JPanel(); actions.add(pay); actions.add(clear); actions.add(receipt);
        add(fields, BorderLayout.NORTH); add(actions, BorderLayout.CENTER); add(status, BorderLayout.SOUTH);
        pay.addActionListener(e -> submitPayment());
        clear.addActionListener(e -> { regNo.setText(""); amount.setText(""); status.setText("Fields cleared."); });
        receipt.addActionListener(e -> JOptionPane.showMessageDialog(this, "Receipt is available after a successful payment."));
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dispose(); }
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack(); setLocationByPlatform(true);
    }

    private void submitPayment() {
        final String id = regNo.getText().trim();
        final String rawAmount = amount.getText().trim();
        final BigDecimal value;
        try { value = new BigDecimal(rawAmount); }
        catch (NumberFormatException ex) { status.setText("Enter a valid amount."); return; }
        setActionsEnabled(false); status.setText("Payment is being processed...");
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception { return service.pay(id, value); }
            @Override protected void done() {
                setActionsEnabled(true);
                try { status.setText(get()); }
                catch (Exception ex) { status.setText("Payment failed: " + ex.getCause().getMessage()); }
            }
        }.execute();
    }
    private void setActionsEnabled(boolean enabled) {
        for (java.awt.Component c : ((JPanel)getContentPane().getComponent(1)).getComponents()) c.setEnabled(enabled);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FeeWindow(new FeeService(new DemoFeeGateway())).setVisible(true));
    }
}

interface FeeGateway { String charge(String registrationNumber, BigDecimal amount) throws Exception; }

final class FeeService {
    private final FeeGateway gateway;
    FeeService(FeeGateway gateway) { this.gateway = gateway; }
    String pay(String registrationNumber, BigDecimal amount) throws Exception {
        if (registrationNumber == null || !registrationNumber.matches("[A-Za-z0-9-]{3,20}"))
            throw new IllegalArgumentException("Invalid registration number");
        if (amount == null || amount.signum() <= 0 || amount.scale() > 2)
            throw new IllegalArgumentException("Amount must be positive with at most two decimals");
        return gateway.charge(registrationNumber, amount);
    }
}

/** Demonstration adapter. Replace with a JDBC PreparedStatement-backed gateway in deployment. */
final class DemoFeeGateway implements FeeGateway {
    @Override public String charge(String registrationNumber, BigDecimal amount) {
        return "Payment accepted for " + registrationNumber + ": INR " + amount.toPlainString();
    }
}

/** Production adapter: prepared statement, bounded connection lifetime, no UI references. */
final class JdbcFeeGateway implements FeeGateway {
    private final String url, user, password;
    JdbcFeeGateway(String url, String user, String password) { this.url=url; this.user=user; this.password=password; }
    @Override public String charge(String registrationNumber, BigDecimal amount) throws java.sql.SQLException {
        try (java.sql.Connection c = java.sql.DriverManager.getConnection(url, user, password);
             java.sql.PreparedStatement p = c.prepareStatement("INSERT INTO fees(reg_no, amount) VALUES (?, ?)")) {
            p.setString(1, registrationNumber); p.setBigDecimal(2, amount); p.executeUpdate();
            return "Payment accepted. Receipt: " + registrationNumber + " / INR " + amount.toPlainString();
        }
    }
}
