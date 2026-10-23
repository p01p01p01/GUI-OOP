package library.gui;

import library.service.Library;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.Dimension;

/** Main window: one tab per area of the system. */
public class MainFrame extends JFrame {
    private final CatalogPanel catalogPanel;
    private final MembersPanel membersPanel;
    private final TransactionsPanel transactionsPanel;
    private final ReportsPanel reportsPanel;

    public MainFrame(Library library) {
        super("Library Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Every panel tells the frame when data changes, so all tabs stay in sync.
        Runnable refreshAll = this::refreshAll;
        catalogPanel = new CatalogPanel(library, refreshAll);
        membersPanel = new MembersPanel(library, refreshAll);
        transactionsPanel = new TransactionsPanel(library, refreshAll);
        reportsPanel = new ReportsPanel(library);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Catalog", catalogPanel);
        tabs.addTab("Members", membersPanel);
        tabs.addTab("Borrow / Return", transactionsPanel);
        tabs.addTab("Reports", reportsPanel);
        tabs.addChangeListener(e -> refreshAll());

        add(tabs);
        setMinimumSize(new Dimension(900, 520));
        setSize(1000, 600);
        setLocationRelativeTo(null);
    }

    private void refreshAll() {
        catalogPanel.refresh();
        membersPanel.refresh();
        transactionsPanel.refresh();
        reportsPanel.refresh();
    }
}
