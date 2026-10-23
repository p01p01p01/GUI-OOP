# UML Class Diagram

GitHub renders the Mermaid diagram below automatically.
For your submission, you can also paste the code into https://mermaid.live and export a PNG/SVG.

Legend: `+` public, `-` private, `#` protected. `<<abstract>>` and `<<interface>>` mark abstraction.

```mermaid
classDiagram
    direction TB

    class Searchable {
        <<interface>>
        +matches(String keyword) boolean
    }

    class LibraryItem {
        <<abstract>>
        -String id
        -String title
        -String creator
        -int year
        -boolean available
        #LibraryItem(String id, String title, String creator, int year)
        +getType()* String
        +getLoanPeriodDays()* int
        +getDailyLateFee()* double
        +getExtraLabel()* String
        +getExtraInfo()* String
        +setExtraInfo(String value)* void
        +calculateLateFee(long daysLate) double
        +getDetails() String
        +matches(String keyword) boolean
        +getId() String
        +getTitle() String
        +getCreator() String
        +getYear() int
        +isAvailable() boolean
        +setTitle(String title) void
        +setCreator(String creator) void
        +setYear(int year) void
        +setAvailable(boolean available) void
        +getCreatorLabel() String
    }

    class Book {
        -String isbn
        +Book(String id, String title, String author, int year, String isbn)
        +getIsbn() String
        +setIsbn(String isbn) void
        +getType() String
        +getLoanPeriodDays() int
        +getDailyLateFee() double
    }

    class Magazine {
        -int issueNumber
        +Magazine(String id, String title, String publisher, int year, int issueNumber)
        +getIssueNumber() int
        +setIssueNumber(int issueNumber) void
        +getType() String
        +getLoanPeriodDays() int
        +getDailyLateFee() double
    }

    class DVD {
        -int runtimeMinutes
        +DVD(String id, String title, String director, int year, int runtimeMinutes)
        +getRuntimeMinutes() int
        +setRuntimeMinutes(int runtimeMinutes) void
        +getType() String
        +getLoanPeriodDays() int
        +getDailyLateFee() double
    }

    class Member {
        <<abstract>>
        -String id
        -String name
        -String email
        #Member(String id, String name, String email)
        +getMemberType()* String
        +getMaxLoans()* int
        +getFeeDiscount()* double
        +getId() String
        +getName() String
        +getEmail() String
        +setName(String name) void
        +setEmail(String email) void
        +matches(String keyword) boolean
    }

    class StudentMember {
        +StudentMember(String id, String name, String email)
        +getMemberType() String
        +getMaxLoans() int
        +getFeeDiscount() double
    }

    class FacultyMember {
        +FacultyMember(String id, String name, String email)
        +getMemberType() String
        +getMaxLoans() int
        +getFeeDiscount() double
    }

    class Loan {
        -int id
        -LibraryItem item
        -Member member
        -LocalDate borrowDate
        -LocalDate dueDate
        -LocalDate returnDate
        +Loan(int id, LibraryItem item, Member member, LocalDate borrowDate)
        +markReturned(LocalDate date) void
        +getDaysOverdue(LocalDate asOf) long
        +isOverdue(LocalDate asOf) boolean
        +calculateFee(LocalDate asOf) double
        +isReturned() boolean
    }

    class Library {
        -Map~String,LibraryItem~ items
        -Map~String,Member~ members
        -List~Loan~ loans
        +addItem(LibraryItem item) void
        +updateItem(String id, String title, String creator, int year, String extra) void
        +removeItem(String id) void
        +searchItems(String keyword, String typeFilter) List~LibraryItem~
        +getAvailableItems() List~LibraryItem~
        +addMember(Member member) void
        +updateMember(String id, String name, String email) void
        +removeMember(String id) void
        +searchMembers(String keyword) List~Member~
        +borrowItem(String itemId, String memberId) Loan
        +returnItem(int loanId) Loan
        +getActiveLoans() List~Loan~
        +getOverdueLoans(LocalDate asOf) List~Loan~
        +countActiveLoans(Member member) int
        -filter(Collection source, String keyword) List
    }

    class LibraryException {
        +LibraryException(String message)
    }

    class MainFrame {
        -CatalogPanel catalogPanel
        -MembersPanel membersPanel
        -TransactionsPanel transactionsPanel
        -ReportsPanel reportsPanel
        -refreshAll() void
    }
    class CatalogPanel { -Library library +refresh() void }
    class MembersPanel { -Library library +refresh() void }
    class TransactionsPanel { -Library library +refresh() void }
    class ReportsPanel { -Library library +refresh() void }

    Searchable <|.. LibraryItem : implements
    Searchable <|.. Member : implements
    LibraryItem <|-- Book
    LibraryItem <|-- Magazine
    LibraryItem <|-- DVD
    Member <|-- StudentMember
    Member <|-- FacultyMember

    Loan --> LibraryItem : borrows
    Loan --> Member : borrowed by
    Library o-- LibraryItem : manages
    Library o-- Member : manages
    Library o-- Loan : records
    Library ..> LibraryException : throws

    MainFrame *-- CatalogPanel
    MainFrame *-- MembersPanel
    MainFrame *-- TransactionsPanel
    MainFrame *-- ReportsPanel
    CatalogPanel --> Library : uses
    MembersPanel --> Library : uses
    TransactionsPanel --> Library : uses
    ReportsPanel --> Library : uses
```
