package library.gui;

import library.exception.LibraryException;
import library.model.Book;
import library.model.DVD;
import library.model.LibraryItem;
import library.model.Magazine;
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** Tab for adding, searching, updating and deleting books, magazines and DVDs. */
public class CatalogPanel extends JPanel {
    private final Library library;
    private final Runnable onDataChanged;

    private final DefaultTableModel tableModel =
            UiUtil.readOnlyModel("ID", "Type", "Title", "Author / Publisher / Director", "Year", "Status", "Details");
    private final JTable table = new JTable(tableModel);

    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> filterBox = new JComboBox<>(new String[]{"All", "Book", "Magazine", "DVD"});

    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{"Book", "Magazine", "DVD"});
    private final JTextField titleField = new JTextField(16);
    private final JTextField creatorField = new JTextField(16);
    private final JTextField yearField = new JTextField(6);
    private final JTextField extraField = new JTextField(12);
    private final JLabel creatorLabel = new JLabel("Author:");
    private final JLabel extraLabel = new JLabel("ISBN:");

    public CatalogPanel(Library library, Runnable onDataChanged) {
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
        typeBox.addActionListener(e -> updateLabels());

        refresh();
    }

    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton searchBtn = new JButton("Search");
        JButton clearBtn = new JButton("Show All");
        bar.add(new JLabel("Search:"));
        bar.add(searchField);
        bar.add(new JLabel("Type:"));
        bar.add(filterBox);
        bar.add(searchBtn);
        bar.add(clearBtn);

        searchBtn.addActionListener(e -> refresh());
        searchField.addActionListener(e -> refresh());
        filterBox.addActionListener(e -> refresh());
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            filterBox.setSelectedIndex(0);
            refresh();
        });
        return bar;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Item details"));

        addField(form, new JLabel("Type:"), typeBox, 0, 0);
        addField(form, new JLabel("Title:"), titleField, 2, 0);
        addField(form, creatorLabel, creatorField, 4, 0);
        addField(form, new JLabel("Year:"), yearField, 0, 1);
        addField(form, extraLabel, extraField, 2, 1);

        JButton addBtn = new JButton("Add Item");
        JButton updateBtn = new JButton("Update Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton clearBtn = new JButton("Clear Form");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(addBtn);
        buttons.add(updateBtn);
        buttons.add(deleteBtn);
        buttons.add(clearBtn);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 2; c.gridwidth = 6; c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(8, 4, 4, 4);
        form.add(buttons, c);

        addBtn.addActionListener(e -> addItem());
        updateBtn.addActionListener(e -> updateItem());
        deleteBtn.addActionListener(e -> deleteItem());
        clearBtn.addActionListener(e -> clearForm());
        return form;
    }

    private void addField(JPanel panel, JLabel label, java.awt.Component field, int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.gridx = x;
        panel.add(label, c);
        c.gridx = x + 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
    }

    // ---------------- actions ----------------

    private void addItem() {
        try {
            String type = (String) typeBox.getSelectedItem();
            String title = titleField.getText();
            String creator = creatorField.getText();
            int year = UiUtil.parseInt(yearField.getText(), "Year");
            String extra = extraField.getText();

            // Each branch builds a different subclass; from here on the program
            // only sees them as LibraryItem (polymorphism).
            LibraryItem item;
            switch (type) {
                case "Book":
                    item = new Book(library.nextItemId("BK"), title, creator, year, extra);
                    break;
                case "Magazine":
                    item = new Magazine(library.nextItemId("MG"), title, creator, year,
                            UiUtil.parseInt(extra, "Issue number"));
                    break;
                default:
                    item = new DVD(library.nextItemId("DV"), title, creator, year,
                            UiUtil.parseInt(extra, "Runtime"));
                    break;
            }
            library.addItem(item);
            clearForm();
            onDataChanged.run();
            UiUtil.info(this, "Added: " + item.getId() + " - " + item.getTitle());
        } catch (IllegalArgumentException | LibraryException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void updateItem() {
        String id = selectedId();
        if (id == null) {
            UiUtil.error(this, "Select an item in the table first.");
            return;
        }
        try {
            library.updateItem(id, titleField.getText(), creatorField.getText(),
                    UiUtil.parseInt(yearField.getText(), "Year"), extraField.getText());
            onDataChanged.run();
            UiUtil.info(this, "Item " + id + " updated.");
        } catch (IllegalArgumentException | LibraryException ex) {
            UiUtil.error(this, ex.getMessage());
            refresh();
        }
    }

    private void deleteItem() {
        String id = selectedId();
        if (id == null) {
            UiUtil.error(this, "Select an item in the table first.");
            return;
        }
        if (!UiUtil.confirm(this, "Delete item " + id + "?")) {
            return;
        }
        try {
            library.removeItem(id);
            clearForm();
            onDataChanged.run();
        } catch (LibraryException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    // ---------------- helpers ----------------

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
            LibraryItem item = library.getItem(id);
            typeBox.setSelectedItem(item.getType());
            typeBox.setEnabled(false); // an existing item cannot change its type
            titleField.setText(item.getTitle());
            creatorField.setText(item.getCreator());
            yearField.setText(String.valueOf(item.getYear()));
            extraField.setText(item.getExtraInfo());
        } catch (LibraryException ex) {
            UiUtil.error(this, ex.getMessage());
        }
    }

    private void clearForm() {
        table.clearSelection();
        typeBox.setEnabled(true);
        titleField.setText("");
        creatorField.setText("");
        yearField.setText("");
        extraField.setText("");
        updateLabels();
    }

    private void updateLabels() {
        String type = (String) typeBox.getSelectedItem();
        if ("Magazine".equals(type)) {
            creatorLabel.setText("Publisher:");
            extraLabel.setText("Issue No.:");
        } else if ("DVD".equals(type)) {
            creatorLabel.setText("Director:");
            extraLabel.setText("Runtime (min):");
        } else {
            creatorLabel.setText("Author:");
            extraLabel.setText("ISBN:");
        }
    }

    /** Reloads the table using the current search text and type filter. */
    public void refresh() {
        tableModel.setRowCount(0);
        for (LibraryItem item : library.searchItems(searchField.getText(), (String) filterBox.getSelectedItem())) {
            tableModel.addRow(new Object[]{
                    item.getId(), item.getType(), item.getTitle(), item.getCreator(),
                    item.getYear(), item.isAvailable() ? "Available" : "Borrowed", item.getDetails()
            });
        }
    }
}
