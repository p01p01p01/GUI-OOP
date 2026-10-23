# Library Management System

A Java Swing desktop application for managing a library's catalog, members, and borrowing.
Built for an Object-Oriented Programming course project demonstrating the **Four Pillars of OOP**:
Encapsulation, Inheritance, Polymorphism, and Abstraction.

## Features

1. **Catalog management** – add, update, and delete Books, Magazines, and DVDs
2. **Search and filter** – keyword search across items and members, plus a type filter
3. **Member management** – register, update, and delete Student and Faculty members
4. **Borrow items** – with rules: availability, per-member loan limits, and a block on members with overdue items
5. **Return items** – calculates the late fee automatically
6. **Reports** – dashboard counts and an overdue-loans table with fees
7. **Input validation and error handling** – on every form (see `docs/PROJECT_DOCUMENTATION.md`)

## Requirements

- JDK 17 or newer (no external libraries needed)

## Build and run

From the repository root:

**macOS / Linux**
```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out library.Main
```

**Windows (PowerShell)**
```powershell
mkdir out -Force
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out library.Main
```

### Run the tests
```bash
javac -d out $(find src test -name "*.java")
java -cp out library.LibrarySmokeTest
```

The app starts with sample data. To start empty, delete the `SampleData.load(library);` line in `src/library/Main.java`.
Data is kept in memory only, so it resets when the app closes.

## Project structure

```
src/library/
  Main.java                  entry point
  model/                     domain classes (the OOP core)
    Searchable.java            interface
    LibraryItem.java           abstract class
    Book.java, Magazine.java, DVD.java
    Member.java                abstract class
    StudentMember.java, FacultyMember.java
    Loan.java
  service/
    Library.java               business rules and data management
    SampleData.java            demo records
  exception/
    LibraryException.java      custom checked exception
  gui/                       Swing user interface
    MainFrame.java, CatalogPanel.java, MembersPanel.java,
    TransactionsPanel.java, ReportsPanel.java, UiUtil.java
test/library/LibrarySmokeTest.java
docs/
  PROJECT_DOCUMENTATION.md   documentation sections required by the course
  UML.md                     UML class diagram (Mermaid)
```

## OOP at a glance

| Pillar | Where |
|---|---|
| Encapsulation | Private fields with validated getters/setters in `LibraryItem`, `Member`, `Loan`; `Library` hides its collections |
| Inheritance | `Book`, `Magazine`, `DVD` extend `LibraryItem`; `StudentMember`, `FacultyMember` extend `Member` |
| Polymorphism | Overridden `getLoanPeriodDays()`, `getDailyLateFee()`, `getMaxLoans()`, `getFeeDiscount()`, `getExtraInfo()`; `Library` treats all items as `LibraryItem` and all members as `Member` |
| Abstraction | Abstract classes `LibraryItem` and `Member`; interface `Searchable` |

Full explanation: [`docs/PROJECT_DOCUMENTATION.md`](docs/PROJECT_DOCUMENTATION.md)
