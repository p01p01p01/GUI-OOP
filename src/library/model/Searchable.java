package library.model;

/**
 * ABSTRACTION (interface): anything that can be matched against a search keyword.
 * Implemented by both LibraryItem and Member, so the Library can search them
 * with one generic method.
 */
public interface Searchable {
    boolean matches(String keyword);
}
