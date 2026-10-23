package library.gui;

import library.exception.LibraryException;
import library.model.LibraryItem;
import library.model.Loan;
import library.model.Member;
import library.service.Library;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;

/** Tab for borrowing and returning items. */
public class TransactionsPanel extends JPanel {
    private final Library library;
    private final Runnable onDataChanged;

    private final JComboBox<LibraryItem> itemBox = new JComboBox<>();
    private final JComboBox<Member> memberBox = new JComboBox<>();
    private final DefaultTableModel tableModel =
            UiUtil.readOnlyModel("Loan #", "Item", "Type", "Member", "Borrowed", "Due", "Status");
    private final JTable table = new JTable(tableModel);

    public TransactionsPanel(Library library, Runnable onDataChanged) {
        this.library = library;
        this.onDataChanged = onDataChanged;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildBorrowBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildReturnBar(), BorderLayout.SOUTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        refresh();
    }

    private JPanel buildBorrowBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setBorder(BorderFactory.createTitledBorder("Borrow an item"));
        JButton borrowBtn = new JButton("Borrow");
        bar.add(new JLabel("Item:"));
        bar.add(itemBox);
        bar.add(new JLabel("Member:"));
        bar.add(memberBox);
        bar.add(borrowBtn);
        borrowBtn.addActionListener(e -> borrow());
        return bar;
    }

    private JPanel buildReturnBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setBorder(BorderFactory.createTitledBorder("Return an item (select a loan above)"));
        JButton returnBtn = new JButton("Return Selected");
        bar.add(returnBtn);
        returnBtn.addActionListener(e -> returnSelected());
        return bar;
    }

    private void borrow() {
        LibraryItem item = (LibraryItem) itemBox.getSelectedItem();
        Member member = (Member) memberBox.getSelectedItem();
        if (item == null) {
            UiUtil.error(this, "There is no available item to borrow.");
            return;
        }
        if (member == null) {
            UiUtil.error(this, "Please register a member first.");
            return;
        }
        try {
            Loan loan = library.borrowItem(item.getId(), member.getId());
            onDataChanged.run();
            UiUtil.info(this, "\"" + item.getTitle() + "\" borrowed by " + member.getName()
                    + ".\nDue date: " + loan.getDueDate()
                    + " (" + item.getLoanPeriodDays() + "-day loan for a " + item.getType() + ")");
        } catch (LibraryException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void returnSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UiUtil.error(this, "Select a loan in the table first.");
            return;
        }
        int loanId = (Integer) tableModel.getValueAt(row, 0);
        try {
            Loan loan = library.returnItem(loanId);
            double fee = loan.calculateFee(LocalDate.now());
            onDataChanged.run();
            if (fee > 0) {
                UiUtil.info(this, String.format("Returned %d day(s) late.%nLate fee: PHP %.2f", loan.getDaysOverdue(LocalDate.now()), fee));
            } else {
                UiUtil.info(this, "Returned on time. No late fee.");
            }
        } catch (LibraryException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    public void refresh() {
        itemBox.removeAllItems();
        for (LibraryItem item : library.getAvailableItems()) {
            itemBox.addItem(item);
        }
        memberBox.removeAllItems();
        for (Member m : library.getAllMembers()) {
            memberBox.addItem(m);
        }

        tableModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (Loan loan : library.getActiveLoans()) {
            tableModel.addRow(new Object[]{
                    loan.getId(), loan.getItem().getTitle(), loan.getItem().getType(),
                    loan.getMember().getName(), loan.getBorrowDate(), loan.getDueDate(),
                    loan.isOverdue(today) ? "OVERDUE" : "On loan"
            });
        }
    }
}
