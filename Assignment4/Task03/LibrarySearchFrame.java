import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/** Nested BorderLayout/FlowLayout screen; results absorb resize changes. */
public final class LibrarySearchFrame extends JFrame {
    private final JTextArea results = new JTextArea("Search results will appear here.\nResize the window: this area grows.");
    private final JLabel status = new JLabel("Ready");
    LibrarySearchFrame() {
        super("Campus Library Search");
        JPanel root = new JPanel(new BorderLayout(8, 8)); root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JLabel title = new JLabel("Campus Library Catalog");
        JPanel titleBar = new JPanel(new FlowLayout(FlowLayout.LEFT)); titleBar.add(title);
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField query = new JTextField(24); JComboBox<String> category = new JComboBox<>(new String[]{"All", "Books", "Journals", "Media"});
        JButton search = new JButton("Search"); searchRow.add(new JLabel("Search")); searchRow.add(query); searchRow.add(category); searchRow.add(search);
        JPanel top = new JPanel(new GridLayout(2, 1, 0, 3)); top.add(titleBar); top.add(searchRow);
        results.setEditable(false); JPanel center = new JPanel(new BorderLayout()); center.add(new JScrollPane(results), BorderLayout.CENTER);
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER)); nav.add(new JButton("Prev")); nav.add(new JButton("Next")); nav.add(new JButton("Reserve"));
        JPanel bottom = new JPanel(new BorderLayout()); bottom.add(nav, BorderLayout.CENTER); bottom.add(status, BorderLayout.SOUTH);
        root.add(top, BorderLayout.NORTH); root.add(center, BorderLayout.CENTER); root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root); search.addActionListener(e -> { results.setText("Query: " + query.getText() + "\nCategory: " + category.getSelectedItem() + "\nNo live catalog connected in this layout demonstration."); status.setText("Search complete"); });
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); setSize(800, 520); setLocationByPlatform(true);
    }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> new LibrarySearchFrame().setVisible(true)); }
}
