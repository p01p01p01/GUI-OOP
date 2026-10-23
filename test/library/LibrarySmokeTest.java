package library;

import library.exception.LibraryException;
import library.model.Book;
import library.model.DVD;
import library.model.FacultyMember;
import library.model.LibraryItem;
import library.model.Loan;
import library.model.Magazine;
import library.model.StudentMember;
import library.service.Library;

import java.time.LocalDate;

/**
 * Dependency-free tests. Run with:
 *   javac -d out $(find src test -name "*.java") && java -cp out library.LibrarySmokeTest
 */
public class LibrarySmokeTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        testValidation();
        testPolymorphism();
        testBorrowRules();
        testFees();
        testSearchAndDelete();
        testRollbackOnBadUpdate();
        System.out.println("\nPassed: " + passed + "  Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testValidation() {
        expectIllegalArgument("blank title rejected", () -> new Book("BK-1", " ", "A", 2000, "1234567890"));
        expectIllegalArgument("bad ISBN rejected", () -> new Book("BK-1", "T", "A", 2000, "123"));
        expectIllegalArgument("future year rejected", () -> new Book("BK-1", "T", "A", 3000, "1234567890"));
        expectIllegalArgument("issue 0 rejected", () -> new Magazine("MG-1", "T", "P", 2020, 0));
        expectIllegalArgument("runtime 0 rejected", () -> new DVD("DV-1", "T", "D", 2020, 0));
        expectIllegalArgument("bad email rejected", () -> new StudentMember("M-1", "Sam", "not-an-email"));
    }

    private static void testPolymorphism() {
        LibraryItem[] items = {
                new Book("BK-1", "B", "A", 2000, "1234567890"),
                new Magazine("MG-1", "M", "P", 2020, 3),
                new DVD("DV-1", "D", "R", 2010, 90)
        };
        check("Book loan = 14 days", items[0].getLoanPeriodDays() == 14);
        check("Magazine loan = 7 days", items[1].getLoanPeriodDays() == 7);
        check("DVD loan = 3 days", items[2].getLoanPeriodDays() == 3);
        check("Late fee differs by type (3 days)",
                items[0].calculateLateFee(3) == 15.0 && items[1].calculateLateFee(3) == 7.5 && items[2].calculateLateFee(3) == 30.0);
    }

    private static void testBorrowRules() throws Exception {
        Library lib = new Library();
        StudentMember s = new StudentMember(lib.nextMemberId(), "Stu", "stu@example.com");
        lib.addMember(s);
        for (int i = 1; i <= 4; i++) {
            lib.addItem(new Book(lib.nextItemId("BK"), "Book " + i, "Auth", 2000, "1234567890"));
        }
        lib.borrowItem("BK-001", s.getId());
        lib.borrowItem("BK-002", s.getId());
        lib.borrowItem("BK-003", s.getId());
        expectLibraryException("student limited to 3 loans", () -> lib.borrowItem("BK-004", s.getId()));
        expectLibraryException("cannot borrow an already borrowed item", () -> lib.borrowItem("BK-001", s.getId()));
        expectLibraryException("cannot delete a borrowed item", () -> lib.removeItem("BK-001"));
        expectLibraryException("cannot delete member with loans", () -> lib.removeMember(s.getId()));
    }

    private static void testFees() throws Exception {
        Library lib = new Library();
        StudentMember s = new StudentMember(lib.nextMemberId(), "Stu", "stu@example.com");
        FacultyMember f = new FacultyMember(lib.nextMemberId(), "Prof", "prof@example.com");
        lib.addMember(s);
        lib.addMember(f);
        lib.addItem(new DVD(lib.nextItemId("DV"), "Film A", "Dir", 2015, 100));
        lib.addItem(new DVD(lib.nextItemId("DV"), "Film B", "Dir", 2015, 100));

        LocalDate tenDaysAgo = LocalDate.now().minusDays(10); // due 3 days after => 7 days late
        Loan ls = lib.borrowItem("DV-001", s.getId(), tenDaysAgo);
        Loan lf = lib.borrowItem("DV-002", f.getId(), tenDaysAgo);
        check("student fee = 7 x 10 = 70", ls.calculateFee(LocalDate.now()) == 70.0);
        check("faculty fee = 50% off = 35", lf.calculateFee(LocalDate.now()) == 35.0);
        expectLibraryException("overdue member blocked from borrowing", () -> {
            lib.addItem(new Book(lib.nextItemId("BK"), "X", "Y", 2000, "1234567890"));
            lib.borrowItem("BK-003", s.getId());
        });
        lib.returnItem(ls.getId());
        check("item available again after return", lib.getItem("DV-001").isAvailable());
        expectLibraryException("cannot return twice", () -> lib.returnItem(ls.getId()));
    }

    private static void testSearchAndDelete() throws Exception {
        Library lib = new Library();
        lib.addItem(new Book(lib.nextItemId("BK"), "Clean Code", "Robert Martin", 2008, "9780132350884"));
        lib.addItem(new DVD(lib.nextItemId("DV"), "Clean Slate", "Someone", 1994, 107));
        check("search 'clean' finds 2", lib.searchItems("clean", "All").size() == 2);
        check("search 'clean' + DVD finds 1", lib.searchItems("clean", "DVD").size() == 1);
        check("search by author", lib.searchItems("martin", null).size() == 1);
        lib.removeItem("BK-001");
        check("delete removes item", lib.getAllItems().size() == 1);
    }

    private static void testRollbackOnBadUpdate() throws Exception {
        Library lib = new Library();
        lib.addItem(new Book(lib.nextItemId("BK"), "Original", "Writer", 2001, "1234567890"));
        try {
            lib.updateItem("BK-001", "Changed", "New Writer", 2002, "bad-isbn");
        } catch (IllegalArgumentException expected) {
            // expected
        }
        LibraryItem b = lib.getItem("BK-001");
        check("failed update leaves title unchanged", b.getTitle().equals("Original") && b.getCreator().equals("Writer") && b.getYear() == 2001);
    }

    // ---------- tiny test helpers ----------

    private interface Action { void run() throws Exception; }

    private static void expectIllegalArgument(String name, Action a) {
        try {
            a.run();
            check(name, false);
        } catch (IllegalArgumentException e) {
            check(name, true);
        } catch (Exception e) {
            check(name, false);
        }
    }

    private static void expectLibraryException(String name, Action a) {
        try {
            a.run();
            check(name, false);
        } catch (LibraryException e) {
            check(name, true);
        } catch (Exception e) {
            check(name, false);
        }
    }

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }
}
