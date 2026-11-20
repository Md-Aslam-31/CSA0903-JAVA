import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Launch with self, named, anonymous or lambda to compare event wiring styles. */
public final class ConverterStyles extends JFrame implements ActionListener {
    private final String style;
    private final JTextField amount = new JTextField(9);
    private final JComboBox<String> from = new JComboBox<>(new String[]{"INR", "USD", "EUR"});
    private final JComboBox<String> to = new JComboBox<>(new String[]{"USD", "EUR", "INR"});
    private final JButton convert = new JButton("Convert");
    private final JLabel result = new JLabel("Result appears here");
    ConverterStyles(String style) {
        super("Currency Converter - " + style); this.style = style;
        setLayout(new FlowLayout()); add(new JLabel("Amount")); add(amount); add(from); add(new JLabel("to")); add(to); add(convert); add(result);
        if ("self".equals(style)) { convert.addActionListener(this); amount.addActionListener(this); }
        else if ("named".equals(style)) { NamedHandler h = new NamedHandler(); convert.addActionListener(h); amount.addActionListener(h); }
        else if ("anonymous".equals(style)) {
            ActionListener h = new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doConvert(); } };
            convert.addActionListener(h); amount.addActionListener(h);
        } else {
            ActionListener h = e -> doConvert(); convert.addActionListener(h); amount.addActionListener(h);
        }
        addWindowListener(new WindowAdapter() { @Override public void windowClosing(WindowEvent e) { dispose(); } });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); pack(); setLocationByPlatform(true);
    }
    @Override public void actionPerformed(ActionEvent e) { doConvert(); }
    private final class NamedHandler implements ActionListener { @Override public void actionPerformed(ActionEvent e) { doConvert(); } }
    private void doConvert() {
        try {
            double value = Double.parseDouble(amount.getText().trim());
            if (!Double.isFinite(value) || value < 0) throw new NumberFormatException();
            double inr = "INR".equals(from.getSelectedItem()) ? value : value * ("USD".equals(from.getSelectedItem()) ? 83.0 : 90.0);
            double converted = "INR".equals(to.getSelectedItem()) ? inr : inr / ("USD".equals(to.getSelectedItem()) ? 83.0 : 90.0);
            String text = new DecimalFormat("0.00").format(converted) + " " + to.getSelectedItem();
            result.setText(text); JOptionPane.showMessageDialog(this, text, "Conversion", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) { result.setText("Invalid amount"); JOptionPane.showMessageDialog(this, "Invalid amount", "Conversion error", JOptionPane.ERROR_MESSAGE); }
    }
    public static void main(String[] args) {
        String style = args.length == 0 ? "lambda" : args[0].toLowerCase();
        if (!java.util.Arrays.asList("self", "named", "anonymous", "lambda").contains(style)) style = "lambda";
        final String selected = style; SwingUtilities.invokeLater(() -> new ConverterStyles(selected).setVisible(true));
    }
}
