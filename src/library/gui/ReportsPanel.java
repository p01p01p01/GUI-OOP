package library.gui;

import library.model.LibraryItem;
import library.model.Loan;
import library.service.Library;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;

/** Tab that summarises the library and lists overdue loans with their fees. */
public class ReportsPanel extends JPanel {
    private final Library library;
    private final JLabel summaryLabel = new JLabel();
    private final DefaultTableModel tableModel =
            UiUtil.readOnlyModel("Loan #", "Item", "Member", "Due", "Days Overdue", "Fee (PHP)");
    private final JTable table = new JTable(tableModel);

    public ReportsPanel(Library library) {
        this.library = library;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        JButton refreshBtn = new JButton("Refresh");
        top.add(summaryLabel);
        top.add(refreshBtn);
        refreshBtn.addActionListener(e -> refresh());

        table.setRowHeight(24);
        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createTitledBorder("Overdue loans"));
        center.add(new JScrollPane(table), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        LocalDate today = LocalDate.now();
        List<LibraryItem> all = library.getAllItems();
        List<Loan> overdue = library.getOverdueLoans(today);

        summaryLabel.setText(String.format("Items: %d   |   Available: %d   |   Borrowed: %d   |   Members: %d   |   Overdue: %d",
                all.size(), library.getAvailableItems().size(), library.getActiveLoans().size(),
                library.getAllMembers().size(), overdue.size()));

        tableModel.setRowCount(0);
        for (Loan loan : overdue) {
            tableModel.addRow(new Object[]{
                    loan.getId(), loan.getItem().getTitle(), loan.getMember().getName(),
                    loan.getDueDate(), loan.getDaysOverdue(today),
                    String.format("%.2f", loan.calculateFee(today))
            });
        }
    }
}
