package library.service;

import library.exception.LibraryException;
import library.model.Loan;
import library.model.LibraryItem;
import library.model.Member;
import library.model.Searchable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The "brain" of the system. The GUI never touches the collections directly;
 * it calls these methods, which enforce every business rule.
 * (ENCAPSULATION: the collections are private and only exposed as read-only copies.)
 */
public class Library {
    private final Map<String, LibraryItem> items = new LinkedHashMap<>();
    private final Map<String, Member> members = new LinkedHashMap<>();
    private final List<Loan> loans = new ArrayList<>();
    private int itemCounter = 0;
    private int memberCounter = 0;
    private int loanCounter = 0;

    // ======================= ID generation =======================

    public String nextItemId(String prefix) {
        String id;
        do {
            itemCounter++;
            id = String.format("%s-%03d", prefix, itemCounter);
        } while (items.containsKey(id));
        return id;
    }

    public String nextMemberId() {
        String id;
        do {
            memberCounter++;
            id = String.format("M-%03d", memberCounter);
        } while (members.containsKey(id));
        return id;
    }

    // ======================= Items =======================

    public void addItem(LibraryItem item) throws LibraryException {
        if (item == null) {
            throw new LibraryException("No item to add.");
        }
        if (items.containsKey(item.getId())) {
            throw new LibraryException("An item with ID " + item.getId() + " already exists.");
        }
        items.put(item.getId(), item);
    }

    public LibraryItem getItem(String id) throws LibraryException {
        LibraryItem item = items.get(id);
        if (item == null) {
            throw new LibraryException("Item " + id + " was not found.");
        }
        return item;
    }

    /** Updates an item. If any value is invalid, every change is rolled back. */
    public void updateItem(String id, String title, String creator, int year, String extra)
            throws LibraryException {
        LibraryItem item = getItem(id);
        String oldTitle = item.getTitle();
        String oldCreator = item.getCreator();
        int oldYear = item.getYear();
        String oldExtra = item.getExtraInfo();
        try {
            item.setTitle(title);
            item.setCreator(creator);
            item.setYear(year);
            item.setExtraInfo(extra);
        } catch (IllegalArgumentException e) {
            item.setTitle(oldTitle);
            item.setCreator(oldCreator);
            item.setYear(oldYear);
            item.setExtraInfo(oldExtra);
            throw e;
        }
    }

    public void removeItem(String id) throws LibraryException {
        LibraryItem item = getItem(id);
        if (!item.isAvailable()) {
            throw new LibraryException("\"" + item.getTitle() + "\" is currently borrowed and cannot be deleted.");
        }
        items.remove(id);
    }

    public List<LibraryItem> getAllItems() {
        return Collections.unmodifiableList(new ArrayList<>(items.values()));
    }

    /** typeFilter may be null or "All" for no filtering. */
    public List<LibraryItem> searchItems(String keyword, String typeFilter) {
        List<LibraryItem> result = filter(items.values(), keyword);
        if (typeFilter != null && !typeFilter.equals("All")) {
            result.removeIf(i -> !i.getType().equalsIgnoreCase(typeFilter));
        }
        return result;
    }

    public List<LibraryItem> getAvailableItems() {
        List<LibraryItem> result = new ArrayList<>();
        for (LibraryItem item : items.values()) {
            if (item.isAvailable()) {
                result.add(item);
            }
        }
        return result;
    }

    // ======================= Members =======================

    public void addMember(Member member) throws LibraryException {
        if (member == null) {
            throw new LibraryException("No member to add.");
        }
        if (members.containsKey(member.getId())) {
            throw new LibraryException("A member with ID " + member.getId() + " already exists.");
        }
        for (Member m : members.values()) {
            if (m.getEmail().equalsIgnoreCase(member.getEmail())) {
                throw new LibraryException("A member with that email already exists.");
            }
        }
        members.put(member.getId(), member);
    }

    public Member getMember(String id) throws LibraryException {
        Member member = members.get(id);
        if (member == null) {
            throw new LibraryException("Member " + id + " was not found.");
        }
        return member;
    }

    public void updateMember(String id, String name, String email) throws LibraryException {
        Member member = getMember(id);
        for (Member m : members.values()) {
            if (m != member && m.getEmail().equalsIgnoreCase(email == null ? "" : email.trim())) {
                throw new LibraryException("Another member already uses that email.");
            }
        }
        String oldName = member.getName();
        String oldEmail = member.getEmail();
        try {
            member.setName(name);
            member.setEmail(email);
        } catch (IllegalArgumentException e) {
            member.setName(oldName);
            member.setEmail(oldEmail);
            throw e;
        }
    }

    public void removeMember(String id) throws LibraryException {
        Member member = getMember(id);
        if (countActiveLoans(member) > 0) {
            throw new LibraryException(member.getName() + " still has borrowed items and cannot be deleted.");
        }
        members.remove(id);
    }

    public List<Member> getAllMembers() {
        return Collections.unmodifiableList(new ArrayList<>(members.values()));
    }

    public List<Member> searchMembers(String keyword) {
        return filter(members.values(), keyword);
    }

    // ======================= Transactions =======================

    public Loan borrowItem(String itemId, String memberId) throws LibraryException {
        return borrowItem(itemId, memberId, LocalDate.now());
    }

    /** The date parameter lets us create back-dated loans for demo data and testing. */
    public Loan borrowItem(String itemId, String memberId, LocalDate date) throws LibraryException {
        LibraryItem item = getItem(itemId);
        Member member = getMember(memberId);

        if (!item.isAvailable()) {
            throw new LibraryException("\"" + item.getTitle() + "\" is already borrowed.");
        }
        if (hasOverdueItems(member, date)) {
            throw new LibraryException(member.getName() + " has overdue items. Return them before borrowing again.");
        }
        int active = countActiveLoans(member);
        if (active >= member.getMaxLoans()) {
            throw new LibraryException(member.getName() + " reached the limit of "
                    + member.getMaxLoans() + " items for " + member.getMemberType() + " members.");
        }

        Loan loan = new Loan(++loanCounter, item, member, date);
        loans.add(loan);
        item.setAvailable(false);
        return loan;
    }

    public Loan returnItem(int loanId) throws LibraryException {
        return returnItem(loanId, LocalDate.now());
    }

    public Loan returnItem(int loanId, LocalDate date) throws LibraryException {
        for (Loan loan : loans) {
            if (loan.getId() == loanId) {
                if (loan.isReturned()) {
                    throw new LibraryException("Loan " + loanId + " was already returned.");
                }
                loan.markReturned(date);
                loan.getItem().setAvailable(true);
                return loan;
            }
        }
        throw new LibraryException("Loan " + loanId + " was not found.");
    }

    public List<Loan> getActiveLoans() {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            if (!loan.isReturned()) {
                result.add(loan);
            }
        }
        return result;
    }

    public List<Loan> getOverdueLoans(LocalDate asOf) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.isOverdue(asOf)) {
                result.add(loan);
            }
        }
        return result;
    }

    public int countActiveLoans(Member member) {
        int count = 0;
        for (Loan loan : loans) {
            if (!loan.isReturned() && loan.getMember().equals(member)) {
                count++;
            }
        }
        return count;
    }

    private boolean hasOverdueItems(Member member, LocalDate asOf) {
        for (Loan loan : loans) {
            if (loan.getMember().equals(member) && loan.isOverdue(asOf)) {
                return true;
            }
        }
        return false;
    }

    // ======================= Generic search =======================

    /** POLYMORPHISM via the Searchable interface: one method searches items AND members. */
    private <T extends Searchable> List<T> filter(Collection<T> source, String keyword) {
        List<T> result = new ArrayList<>();
        for (T t : source) {
            if (t.matches(keyword)) {
                result.add(t);
            }
        }
        return result;
    }
}
