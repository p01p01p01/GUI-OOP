package library.service;

import library.exception.LibraryException;
import library.model.Book;
import library.model.DVD;
import library.model.FacultyMember;
import library.model.Magazine;
import library.model.StudentMember;

import java.time.LocalDate;

/** Loads demo records so the GUI is not empty on first launch. */
public final class SampleData {
    private SampleData() { }

    public static void load(Library lib) {
        try {
            lib.addItem(new Book(lib.nextItemId("BK"), "Clean Code", "Robert C. Martin", 2008, "9780132350884"));
            lib.addItem(new Book(lib.nextItemId("BK"), "Head First Java", "Kathy Sierra", 2005, "9780596009205"));
            lib.addItem(new Book(lib.nextItemId("BK"), "Effective Java", "Joshua Bloch", 2018, "9780134685991"));
            lib.addItem(new Magazine(lib.nextItemId("MG"), "National Geographic", "National Geographic Society", 2024, 12));
            lib.addItem(new Magazine(lib.nextItemId("MG"), "Wired", "Conde Nast", 2024, 8));
            lib.addItem(new DVD(lib.nextItemId("DV"), "The Imitation Game", "Morten Tyldum", 2014, 114));
            lib.addItem(new DVD(lib.nextItemId("DV"), "Hidden Figures", "Theodore Melfi", 2016, 127));

            lib.addMember(new StudentMember(lib.nextMemberId(), "Maria Santos", "maria.santos@example.com"));
            lib.addMember(new StudentMember(lib.nextMemberId(), "Juan Dela Cruz", "juan.delacruz@example.com"));
            lib.addMember(new FacultyMember(lib.nextMemberId(), "Dr. Ana Reyes", "ana.reyes@example.com"));

            // One normal loan and one back-dated (overdue) loan so the Reports tab has data.
            lib.borrowItem("BK-002", "M-001");
            lib.borrowItem("DV-006", "M-002", LocalDate.now().minusDays(10));
        } catch (LibraryException e) {
            throw new IllegalStateException("Sample data failed to load: " + e.getMessage(), e);
        }
    }
}
