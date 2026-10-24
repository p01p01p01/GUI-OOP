package library.model;

/**
 * ABSTRACTION + ENCAPSULATION: base class for library members.
 * Each kind of member decides its own borrowing limit and fee discount.
 */
public abstract class Member implements Searchable {
    private final String id;
    private String name;
    private String email;

    protected Member(String id, String name, String email) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Member ID is required.");
        }
        this.id = id.trim();
        setName(name);
        setEmail(email);
    }

    public abstract String getMemberType();

    /** Maximum number of items this member may borrow at the same time. */
    public abstract int getMaxLoans();

    /** Fraction removed from late fees (0.0 = none, 0.5 = half price). */
    public abstract double getFeeDiscount();

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public final void setName(String name) {
    if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("Name is required.");
    }
    if (!name.trim().matches("[\\p{L}][\\p{L} .'-]*")) {
        throw new IllegalArgumentException(
                "Name can only contain letters, spaces, periods, hyphens, and apostrophes.");
    }
    this.name = name.trim();
}

    public final void setEmail(String email) {
        if (email == null || !email.trim().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        this.email = email.trim();
    }

    @Override
    public boolean matches(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String k = keyword.trim().toLowerCase();
        return id.toLowerCase().contains(k)
                || name.toLowerCase().contains(k)
                || email.toLowerCase().contains(k)
                || getMemberType().toLowerCase().contains(k);
    }

    @Override
    public String toString() {
        return id + " - " + name + " (" + getMemberType() + ")";
    }
}
