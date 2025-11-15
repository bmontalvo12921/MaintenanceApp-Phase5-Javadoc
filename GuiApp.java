import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.awt.Desktop;
import java.util.List;
/**
 * GUI front end for the Maintenance Shop program.
 * This window handles all customer actions such as adding,
 * updating, searching, deleting, loading CSV files, and exporting data.
 * It connects the buttons and forms in the interface to the rest of
 * the project’s classes.
 */
public class GuiApp extends JFrame {

    // store is created after user selects DB
    private CustomerStore store;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Phone", "Name", "Address", "Email"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JTable table = new JTable(tableModel);
    private final JTextArea log = new JTextArea(5, 80);
    private final JTextField searchField = new JTextField(18);
    private TableRowSorter<DefaultTableModel> sorter;
    /** Launches Gui for the program */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GuiApp().setVisible(true));
    }
    /**
     * Constructs the main window, prompts for a database file,
     * builds the toolbar and main panel, and performs the initial
     * table refresh.
     */
    public GuiApp() {
        super("Maintenance Shop");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);


        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select SQLite Database (.db)");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this, "No database selected. Exiting.");
            System.exit(0);
        }
        String selectedDb = fc.getSelectedFile().getAbsolutePath();
        ConnectionManager.setDatabasePath(selectedDb);


        store = new CustomerStore();

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { doExit(); }
        });

        setLayout(new BorderLayout(5,5));
        buildToolbar();
        add(buildMainPanel(), BorderLayout.CENTER);

        // Log Sep up
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void scroll(){ log.setCaretPosition(log.getDocument().getLength()); }
            public void insertUpdate(javax.swing.event.DocumentEvent e){ scroll(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e){ scroll(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e){ scroll(); }
        });

        // first message
        logMsg("[DB] " + selectedDb);

        refreshTable();
    }
    /**
     * Shows an information dialog.
     * @param m message text to display
     */
    private void info(String m){ JOptionPane.showMessageDialog(this, m); }
    /**
     * Shows a warning dialog with a standard title.
     * @param m warning message to display
     */
    private void warn(String m){ JOptionPane.showMessageDialog(this, m, "Warning", JOptionPane.WARNING_MESSAGE); }
    /**
     * Appends a line to the log text area.
     * @param m message to append
     */
    private void logMsg(String m){ log.append(m + "\n"); }
    /**
     * Builds the center panel containing the table and the log area,
     * and wires the live-search filter on the search field.
     *
     * @return the constructed main {@link Component}
     */
    private Component buildMainPanel() {
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(5,5,5,5));
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(new JScrollPane(log), BorderLayout.SOUTH);

        // search
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
        });

        return p;
    }
    /**
     * Builds the top toolbar: load/refresh/add/update/delete/export,
     * clear log, exit, and the search box.
     */
    private void buildToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        tb.add(btn("Load CSV", e -> onLoadCsv()));
        tb.add(btn("Refresh", e -> refreshTable()));
        tb.addSeparator();
        tb.add(btn("Add", e -> onAdd()));
        tb.add(btn("Update", e -> onUpdate()));
        tb.add(btn("Delete", e -> onDelete()));
        tb.add(btn("Export All", e -> onExportCsv()));
        tb.addSeparator();
        tb.add(btn("Clear Log", e -> log.setText("")));
        tb.add(btn("Exit", e -> doExit()));

        tb.add(Box.createHorizontalGlue());
        tb.add(new JLabel("Search: "));
        tb.add(searchField);
        JButton clear=new JButton("✕");
        clear.addActionListener(e->searchField.setText(""));
        tb.add(clear);

        add(tb, BorderLayout.NORTH);
    }
    /**
     * Creates a JButton with a label and action listener.
     *
     * @param text button text
     * @param action action performed on click
     * @return configured {@link JButton}
     */
    private JButton btn(String t, java.awt.event.ActionListener a) {
        JButton b = new JButton(t);
        b.addActionListener(a);
        return b;
    }

    /**
     * loads CSV into the database
     */
    private void onLoadCsv() {
        JFileChooser c = new JFileChooser();
        if (c.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return;
        String msg = store.loadFromCsv(c.getSelectedFile().getAbsolutePath());
        info(msg);
        logMsg("[CSV] " + msg);
        refreshTable();
    }

    /** Exports database into a CSV file @param onExportCsv */
    private void onExportCsv() {
        JFileChooser c = new JFileChooser();
        c.setSelectedFile(new File("backup.csv"));
        if (c.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return;

        boolean ok = store.saveToCsv(c.getSelectedFile().getAbsolutePath());
        if(ok){
            String path = c.getSelectedFile().getAbsolutePath();
            info("Export OK\nPath: " + path);
            logMsg("[CSV] Exported: " + path);
            try { Desktop.getDesktop().open(c.getSelectedFile()); } catch(Exception ignored){}
        } else {
            warn("Export failed");
            logMsg("[CSV] Export failed");
        }
    }


    /**
     * Builds a simple input form for customer fields.
     *
     * @param ph phone field
     * @param nm name field
     * @param ad address field
     * @param em email field
     * @return a populated {@link JPanel} containing the labeled inputs
     */
    private JPanel makeForm(JTextField ph, JTextField nm, JTextField ad, JTextField em){
        JPanel p=new JPanel(new GridLayout(4,2,5,5));
        p.add(new JLabel("Phone (digits only):")); p.add(ph);
        p.add(new JLabel("Name:")); p.add(nm);
        p.add(new JLabel("Address:")); p.add(ad);
        p.add(new JLabel("Email (optional):")); p.add(em);
        return p;
    }

    /** Adds new customer to the Database @param onAdd */
    private void onAdd() {
        JTextField ph=new JTextField();
        JTextField nm=new JTextField();
        JTextField ad=new JTextField();
        JTextField em=new JTextField();

        JPanel form=makeForm(ph,nm,ad,em);
        if(JOptionPane.showConfirmDialog(this,form,"Add Customer",
                JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) return;

        String phone = CustomerStore.normalizePhone(ph.getText());
        String name  = nm.getText().trim();
        String addr  = ad.getText().trim();
        String email = em.getText().trim();

        if(!CustomerStore.isValidPhone(phone) || name.isEmpty() || addr.isEmpty()){
            warn("Phone must be 7–11 digits. Name and Address required.");
            return;
        }
        String emailErr = CustomerStore.emailError(email);
        if (emailErr != null) { warn(emailErr); return; }
        if (store.getByPhone(phone) != null) { warn("Phone already exists."); return; }

        if (!store.insert(new Customer(phone,name,addr,email))) { warn("Insert failed."); return; }
        logMsg("[ADD] " + phone + " | " + name);
        refreshTable();
    }

    /**
     * Updates selected row onUpdate
     */
    private void onUpdate() {
        int r = table.getSelectedRow();
        if(r<0){ warn("Select row"); return;}

        int m = table.convertRowIndexToModel(r);
        String phone = tableModel.getValueAt(m,0).toString();
        String name  = tableModel.getValueAt(m,1).toString();
        String addr  = tableModel.getValueAt(m,2).toString();
        String email = tableModel.getValueAt(m,3)==null?"":tableModel.getValueAt(m,3).toString();

        JTextField ph=new JTextField(phone); ph.setEditable(false);
        JTextField nm=new JTextField(name);
        JTextField ad=new JTextField(addr);
        JTextField em=new JTextField(email);

        JPanel form=makeForm(ph,nm,ad,em);
        if(JOptionPane.showConfirmDialog(this,form,"Edit Customer",
                JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) return;

        String newName=nm.getText().trim();
        String newAddr=ad.getText().trim();
        String newEmail=em.getText().trim();

        if(newName.isEmpty()||newAddr.isEmpty()){
            warn("Name and Address required.");
            return;
        }
        String emailErr = CustomerStore.emailError(newEmail);
        if (emailErr != null) { warn(emailErr); return; }

        if(!store.update(new Customer(phone,newName,newAddr,newEmail))){
            warn("Update failed.");
            return;
        }
        logMsg("[UPDATE] " + phone + " | " + newName);
        refreshTable();
    }

    /**
     * Deletes Selected Row
     * */
    private void onDelete() {
        int r=table.getSelectedRow();
        if(r<0){ warn("Select row"); return;}

        int m = table.convertRowIndexToModel(r);
        String ph = CustomerStore.normalizePhone(tableModel.getValueAt(m,0).toString());

        if(JOptionPane.showConfirmDialog(this,"Delete?","Confirm",
                JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            if (store.delete(ph)) {
                logMsg("[DELETE] " + ph);
            } else {
                logMsg("[DELETE] failed " + ph);
            }
            refreshTable();
        }
    }

    /**
     * Refreshes the table
     */
    private void refreshTable() {
        List<Customer> rows=store.listAll();
        tableModel.setRowCount(0);
        for(Customer c:rows){
            tableModel.addRow(new Object[]{c.getPhoneNumber(),c.getName(),c.getAddress(),c.getEmail()});
        }
        logMsg("[REFRESH] rows=" + rows.size());
    }

    private void doExit(){
        if(JOptionPane.showConfirmDialog(this,"Exit?","Confirm",
                JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){
            System.exit(0);
        }
    }

    /** Live Search */
    private void filter(){
        String t=searchField.getText().trim();
        if(t.isEmpty()) {
            sorter.setRowFilter(null);
            logMsg("[SEARCH] cleared");
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)"+t));
            logMsg("[SEARCH] '" + t + "'");
        }
    }
}