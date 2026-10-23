package library.model;

/** INHERITANCE: Book is-a LibraryItem. Adds an ISBN and defines its own loan rules. */
public class Book extends LibraryItem {
    private String isbn;

    public Book(String id, String title, String author, int year, String isbn) {
        super(id, title, author, year);
        setIsbn(isbn);
    }

    public String getIsbn() { return isbn; }

    public void setIsbn(String isbn) {
        if (isbn == null) {
            throw new IllegalArgumentException("ISBN is required.");
        }
        String digits = isbn.replaceAll("[\\s-]", "");
        if (!digits.matches("\\d{10}|\\d{13}")) {
            throw new IllegalArgumentException("ISBN must contain 10 or 13 digits.");
        }
        this.isbn = digits;
    }

    // POLYMORPHISM: overriding the abstract methods of LibraryItem
    @Override public String getType() { return "Book"; }
    @Override public int getLoanPeriodDays() { return 14; }
    @Override public double getDailyLateFee() { return 5.00; }
    @Override public String getExtraLabel() { return "ISBN"; }
    @Override public String getExtraInfo() { return isbn; }
    @Override public void setExtraInfo(String value) { setIsbn(value); }
    @Override public String getCreatorLabel() { return "Author"; }
}
