package library.model;

/** INHERITANCE: a student member borrows fewer items and pays full late fees. */
public class StudentMember extends Member {
    public StudentMember(String id, String name, String email) {
        super(id, name, email);
    }

    @Override public String getMemberType() { return "Student"; }
    @Override public int getMaxLoans() { return 3; }
    @Override public double getFeeDiscount() { return 0.0; }
}
