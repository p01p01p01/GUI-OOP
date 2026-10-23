package library;

import library.gui.MainFrame;
import library.service.Library;
import library.service.SampleData;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Program entry point. */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Falls back to the default Swing look and feel.
        }

        Library library = new Library();
        SampleData.load(library); // remove this line to start with an empty library

        SwingUtilities.invokeLater(() -> new MainFrame(library).setVisible(true));
    }
}
