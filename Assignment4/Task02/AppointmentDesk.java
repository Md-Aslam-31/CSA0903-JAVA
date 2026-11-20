import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Runnable event model for the outpatient appointment desk. */
public final class AppointmentDesk extends JFrame {
    private final JTextField patientId = new JTextField(14);
    private final JComboBox<String> department = new JComboBox<>(new String[]{"Cardiology", "Dermatology", "Orthopedics"});
    private final JComboBox<String> doctor = new JComboBox<>();
    private final JComboBox<String> slot = new JComboBox<>(new String[]{"09:00", "10:30", "13:00", "15:30"});
    private final Map<String, List<String>> doctors = new LinkedHashMap<>();
    private boolean bookingInProgress;
    private final JButton book = new JButton("Book appointment");

    AppointmentDesk() {
        super("Outpatient Appointment Desk");
        doctors.put("Cardiology", Arrays.asList("Dr Rao", "Dr Iyer"));
        doctors.put("Dermatology", Arrays.asList("Dr Sen", "Dr Thomas"));
        doctors.put("Orthopedics", Arrays.asList("Dr Khan", "Dr Das"));
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.add(new JLabel("Patient ID")); form.add(patientId);
        form.add(new JLabel("Department")); form.add(department);
        form.add(new JLabel("Doctor")); form.add(doctor);
        form.add(new JLabel("Time slot")); form.add(slot);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.add(book);
        add(form, BorderLayout.CENTER); add(actions, BorderLayout.SOUTH);
        department.addActionListener(e -> refreshDoctors());
        book.addActionListener(e -> bookAppointment());
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (bookingInProgress && JOptionPane.showConfirmDialog(AppointmentDesk.this,
                        "A booking is in progress. Close anyway?", "Confirm close", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
                dispose();
            }
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); refreshDoctors(); pack(); setLocationByPlatform(true);
    }
    private void refreshDoctors() {
        doctor.removeAllItems();
        for (String name : doctors.getOrDefault((String) department.getSelectedItem(), List.of())) doctor.addItem(name);
    }
    private void bookAppointment() {
        String id = patientId.getText().trim();
        if (!id.matches("[A-Za-z0-9-]{3,20}")) { JOptionPane.showMessageDialog(this, "Enter a valid patient ID.", "Validation", JOptionPane.ERROR_MESSAGE); return; }
        final String details = id + " / " + department.getSelectedItem() + " / " + doctor.getSelectedItem() + " / " + slot.getSelectedItem();
        bookingInProgress = true; book.setEnabled(false); setStatus("Booking appointment...");
        new javax.swing.SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception { Thread.sleep(1500); return details; }
            @Override protected void done() {
                bookingInProgress = false; book.setEnabled(true);
                try { setStatus("Appointment confirmed: " + get()); }
                catch (Exception ex) { setStatus("Booking failed: " + ex.getMessage()); }
            }
        }.execute();
    }
    private void setStatus(String text) { setTitle("Outpatient Appointment Desk - " + text); }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new AppointmentDesk().setVisible(true)); }
}
