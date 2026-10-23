package library.model;

/** INHERITANCE: a faculty member borrows more items and gets 50% off late fees. */
public class FacultyMember extends Member {
    public FacultyMember(String id, String name, String email) {
        super(id, name, email);
    }

    @Override public String getMemberType() { return "Faculty"; }
    @Override public int getMaxLoans() { return 7; }
    @Override public double getFeeDiscount() { return 0.5; }
}
