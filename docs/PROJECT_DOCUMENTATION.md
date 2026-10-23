# Project Documentation

## 1. Project Title
Library Management System

## 2. Project Description
A Java Swing desktop application that lets library staff manage a catalog of books, magazines, and DVDs,
register members, lend and receive items, and track overdue loans and late fees.

## 3. Problem Statement
Small libraries that track loans on paper or in spreadsheets often lose track of which items are
available, who borrowed them, and which loans are overdue. Late fees are calculated by hand and
borrowing rules are applied inconsistently. This system centralizes those records and enforces the rules automatically.

## 4. Objectives
- Apply the four pillars of OOP (encapsulation, inheritance, polymorphism, abstraction) in a working system.
- Provide a GUI for managing items, members, and loans.
- Enforce borrowing rules and calculate late fees automatically.
- Validate user input and handle errors without crashing.

## 5. System Features
| # | Feature | Where |
|---|---|---|
| 1 | Add / update / delete catalog items (Book, Magazine, DVD) | Catalog tab |
| 2 | Search items by keyword and filter by type | Catalog tab |
| 3 | Register / update / delete members (Student, Faculty) and search them | Members tab |
| 4 | Borrow an item (availability, loan limit, and overdue checks) | Borrow / Return tab |
| 5 | Return an item and calculate the late fee | Borrow / Return tab |
| 6 | Dashboard summary and overdue-loans report with fees | Reports tab |

### Business rules
| Rule | Student | Faculty |
|---|---|---|
| Max items borrowed at once | 3 | 7 |
| Late-fee discount | none | 50% |

| Item | Loan period | Late fee per day |
|---|---|---|
| Book | 14 days | PHP 5.00 |
| Magazine | 7 days | PHP 2.50 |
| DVD | 3 days | PHP 10.00 |

Final fee = days overdue x item's daily fee x (1 - member's discount).
A member with an overdue item cannot borrow again. Borrowed items and members with active loans cannot be deleted.

## 6. OOP Implementation

### Encapsulation
- `LibraryItem`, `Member`, `Loan`, `Book`, `Magazine`, `DVD`: every field is `private`.
- Data is read through getters and changed only through setters that validate the value
  (e.g. `setYear`, `setEmail`, `setIsbn`) and throw `IllegalArgumentException` on bad input.
- `Library`: its `items`, `members`, and `loans` collections are private. Callers get read-only copies and must use
  methods such as `borrowItem()`, so the business rules cannot be bypassed.

### Inheritance
- `LibraryItem` (parent) -> `Book`, `Magazine`, `DVD` (children). Children reuse the shared fields
  (id, title, creator, year, availability), the search logic, and the fee calculation, and add their own fields (ISBN, issue number, runtime).
- `Member` (parent) -> `StudentMember`, `FacultyMember` (children). Children reuse id, name, email, validation, and search.

### Polymorphism
- **Method overriding**: `getLoanPeriodDays()`, `getDailyLateFee()`, `getExtraInfo()`, and `getType()` behave differently
  for each item type. `getMaxLoans()` and `getFeeDiscount()` behave differently for each member type.
- **Substitutability**: `Library` stores items as `LibraryItem` and members as `Member`. `Loan.calculateFee()` calls
  `item.calculateLateFee(...)` and `member.getFeeDiscount()` without knowing the concrete types.
- **Interface polymorphism**: `Library.filter()` searches both items and members through `Searchable`.
- **Method overloading**: `Library.borrowItem(itemId, memberId)` and `borrowItem(itemId, memberId, date)`;
  the same for `returnItem`.

### Abstraction
- `LibraryItem` is an abstract class with abstract methods (`getType`, `getLoanPeriodDays`, `getDailyLateFee`,
  `getExtraLabel`, `getExtraInfo`, `setExtraInfo`). It defines *what* every item must provide; subclasses define *how*.
- `Member` is an abstract class with abstract `getMemberType`, `getMaxLoans`, `getFeeDiscount`.
- `Searchable` is an interface describing the single capability "can be matched by a keyword".

## 7. UML Class Diagram
See [`UML.md`](UML.md). Export the rendered diagram as an image for your submission.

## 8. GUI Screenshots
Run the app and add screenshots to `docs/screenshots/`, then link them here:

- Catalog tab: `![Catalog](screenshots/catalog.png)`
- Members tab: `![Members](screenshots/members.png)`
- Borrow / Return tab: `![Borrow](screenshots/borrow.png)`
- Reports tab: `![Reports](screenshots/reports.png)`
- An error dialog (for validation evidence): `![Error](screenshots/error.png)`

## 9. Testing

### Automated tests
`test/library/LibrarySmokeTest.java` runs 24 checks (validation, polymorphism, borrow rules, fees, search, delete, update rollback).
All 24 pass. Run instructions are in the README.

### Manual GUI test cases (fill in the Result column as you test)
| # | Action | Expected result | Result |
|---|---|---|---|
| 1 | Catalog: add a Book with valid data | Item appears in table with status Available | |
| 2 | Catalog: add with empty title | Error "Title is required." | |
| 3 | Catalog: add Book with ISBN `123` | Error about 10 or 13 digits | |
| 4 | Catalog: year = `abc` | Error "Year must be a whole number." | |
| 5 | Catalog: search "clean" | Only matching items shown | |
| 6 | Catalog: select item, change title, Update | Table shows new title | |
| 7 | Catalog: delete an available item | Item removed after confirmation | |
| 8 | Catalog: delete a borrowed item | Error: item is currently borrowed | |
| 9 | Members: add with email `abc` | Error "valid email address" | |
| 10 | Members: add with an existing email | Error: email already exists | |
| 11 | Borrow: student borrows a 4th item | Error: loan limit reached | |
| 12 | Borrow: borrow an item, check due date | Due date = today + loan period of that item type | |
| 13 | Return: return an on-time loan | "No late fee" message | |
| 14 | Return: return the overdue sample loan | Late fee message with PHP amount | |
| 15 | Reports: open tab with overdue sample loan | Loan listed with days overdue and fee | |

## 10. Conclusion
Write this part yourself after you have worked through the code. Suggested points: what each pillar made easier
(e.g. adding a new item type only needs one new subclass), what was hard, and what you would add next (file/database storage, user login).
