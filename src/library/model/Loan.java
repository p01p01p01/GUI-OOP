package library.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Records that one Member borrowed one LibraryItem (association between the two).
 * The due date and late fee are computed polymorphically from the item and member types.
 */
public class Loan {
    private final int id;
    private final LibraryItem item;
    private final Member member;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;

    public Loan(int id, LibraryItem item, Member member, LocalDate borrowDate) {
        this.id = id;
        this.item = item;
        this.member = member;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(item.getLoanPeriodDays()); // depends on the item's type
    }

    public int getId() { return id; }
    public LibraryItem getItem() { return item; }
    public Member getMember() { return member; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public boolean isReturned() { return returnDate != null; }

    public void markReturned(LocalDate date) {
        this.returnDate = date;
    }

    public long getDaysOverdue(LocalDate asOf) {
        LocalDate end = isReturned() ? returnDate : asOf;
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, end));
    }

    public boolean isOverdue(LocalDate asOf) {
        return !isReturned() && getDaysOverdue(asOf) > 0;
    }

    /** Item fee rule x member's discount. T */
    public double calculateFee(LocalDate asOf) {
        double base = item.calculateLateFee(getDaysOverdue(asOf));
        return base * (1.0 - member.getFeeDiscount());
    }
}
