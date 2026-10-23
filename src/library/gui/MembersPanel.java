package library.gui;

import library.exception.LibraryException;
import library.model.FacultyMember;
import library.model.Member;
import library.model.StudentMember;
import library.service.Library;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/** Tab for registering, searching, updating and deleting library members. */
public class MembersPanel extends JPanel {
    private final Library library;
    private final Runnable onDataChanged;

    private final DefaultTableModel tableModel =
            UiUtil.readOnlyModel("ID", "Name", "Email", "Type", "Max Loans", "Borrowed Now");
    private final JTable table = new JTable(tableModel);

    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{"Student", "Faculty"});
    private final JTextField nameField = new JTextField(14);
    private final JTextField emailField = new JTextField(18);

    public MembersPanel(Library library, Runnable onDataChanged) {
        this.library = library;
        this.onDataChanged = onDataChanged;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildSearchBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedIntoForm();
            }
        });
        refresh();
    }

    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton searchBtn = new JButton("Search");
        JButton clearBtn = new JButton("Show All");
        bar.add(new JLabel("Search:"));
        bar.add(searchField);
        bar.add(searchBtn);
        bar.add(clearBtn);
        searchBtn.addActionListener(e -> refresh());
        searchField.addActionListener(e -> refresh());
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            refresh();
        });
        return bar;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        form.setBorder(BorderFactory.createTitledBorder("Member details"));
        JButton addBtn = new JButton("Add Member");
        JButton updateBtn = new JButton("Update Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton clearBtn = new JButton("Clear Form");

        form.add(new JLabel("Type:"));
        form.add(typeBox);
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(addBtn);
        form.add(updateBtn);
        form.add(deleteBtn);
        form.add(clearBtn);

        addBtn.addActionListener(e -> addMember());
        updateBtn.addActionListener(e -> updateMember());
        deleteBtn.addActionListener(e -> deleteMember());
        clearBtn.addActionListener(e -> clearForm());
        return form;
    }

    private void addMember() {
        try {
            String id = library.nextMemberId();
            Member member = "Faculty".equals(typeBox.getSelectedItem())
                    ? new FacultyMember(id, nameField.getText(), emailField.getText())
                    : new StudentMember(id, nameField.getText(), emailField.getText());
            library.addMember(member);
            clearForm();
            onDataChanged.run();
            UiUtil.info(this, "Registered: " + member);
        } catch (IllegalArgumentException | LibraryException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void updateMember() {
        String id = selectedId();
        if (id == null) {
            UiUtil.error(this, "Select a member in the table first.");
            return;
        }
        try {
            library.updateMember(id, nameField.getText(), emailField.getText());
            onDataChanged.run();
            UiUtil.info(this, "Member " + id + " updated.");
        } catch (IllegalArgumentException | LibraryException ex) {
            UiUtil.error(this, ex.getMessage());
            refresh();
        }
    }

    private void deleteMember() {
        String id = selectedId();
        if (id == null) {
            UiUtil.error(this, "Select a member in the table first.");
            return;
        }
        if (!UiUtil.confirm(this, "Delete member " + id + "?")) {
            return;
        }
        try {
            library.removeMember(id);
            clearForm();
            onDataChanged.run();
        } catch (LibraryException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private String selectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (String) tableModel.getValueAt(row, 0);
    }

    private void loadSelectedIntoForm() {
        String id = selectedId();
        if (id == null) {
            return;
        }
        try {
            Member m = library.getMember(id);
            typeBox.setSelectedItem(m.getMemberType());
            typeBox.setEnabled(false); // type cannot change after registration
            nameField.setText(m.getName());
            emailField.setText(m.getEmail());
        } catch (LibraryException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void clearForm() {
        table.clearSelection();
        typeBox.setEnabled(true);
        nameField.setText("");
        emailField.setText("");
    }

    public void refresh() {
        tableModel.setRowCount(0);
        for (Member m : library.searchMembers(searchField.getText())) {
            tableModel.addRow(new Object[]{
                    m.getId(), m.getName(), m.getEmail(), m.getMemberType(),
                    m.getMaxLoans(), library.countActiveLoans(m)
            });
        }
    }
}
