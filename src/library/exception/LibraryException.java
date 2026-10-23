package library.exception;

/**
 * Custom checked exception for business-rule violations in the library
 * (e.g. borrowing an unavailable item, deleting a member who still has loans).
 */
public class LibraryException extends Exception {
    public LibraryException(String message) {
        super(message);
    }
}
