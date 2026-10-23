package library.model;

import java.time.Year;

/**
 * ABSTRACTION (abstract class): the common blueprint of everything the library lends.
 * It cannot be instantiated; subclasses (Book, Magazine, DVD) supply the details.
 *
 * ENCAPSULATION: all fields are private. They are read through getters and
 * changed only through validated setters.
 */
public abstract class LibraryItem implements Searchable {
    private final String id;
    private String title;
    private String creator;
    private int year;
    private boolean available = true;

    protected LibraryItem(String id, String title, String creator, int year) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Item ID is required.");
        }
        this.id = id.trim();
        setTitle(title);
        setCreator(creator);
        setYear(year);
    }

    // ---------- Abstract methods: each subclass MUST provide its own behavior ----------

    /** Human-readable type, e.g. "Book". */
    public abstract String getType();

    /** Number of days this kind of item can be borrowed. */
    public abstract int getLoanPeriodDays();

    /** Late fee charged per overdue day (PHP). */
    public abstract double getDailyLateFee();

    /** Label for the type-specific field, e.g. "ISBN". */
    public abstract String getExtraLabel();

    /** Value of the type-specific field as text. */
    public abstract String getExtraInfo();

    /** Validates and sets the type-specific field from text. */
    public abstract void setExtraInfo(String value);

    // ---------- Shared behavior ----------

    /** Late fee for this item. Subclasses change the result by overriding getDailyLateFee(). */
    public double calculateLateFee(long daysLate) {
        return daysLate <= 0 ? 0 : daysLate * getDailyLateFee();
    }

    public String getDetails() {
        return getExtraLabel() + ": " + getExtraInfo();
    }

    @Override
    public boolean matches(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String k = keyword.trim().toLowerCase();
        return id.toLowerCase().contains(k)
                || title.toLowerCase().contains(k)
                || creator.toLowerCase().contains(k)
                || getType().toLowerCase().contains(k)
                || getExtraInfo().toLowerCase().contains(k);
    }

    // ---------- Getters and validated setters (encapsulation) ----------

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCreator() { return creator; }
    public int getYear() { return year; }
    public boolean isAvailable() { return available; }

    public final void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required.");
        }
        this.title = title.trim();
    }

    public final void setCreator(String creator) {
        if (creator == null || creator.isBlank()) {
            throw new IllegalArgumentException(getCreatorLabel() + " is required.");
        }
        this.creator = creator.trim();
    }

    public final void setYear(int year) {
        int max = Year.now().getValue() + 1;
        if (year < 1000 || year > max) {
            throw new IllegalArgumentException("Year must be between 1000 and " + max + ".");
        }
        this.year = year;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    /** Label shown in the GUI for the creator field (Author / Publisher / Director). */
    public String getCreatorLabel() {
        return "Creator";
    }

    @Override
    public String toString() {
        return id + " - " + title;
    }
}
