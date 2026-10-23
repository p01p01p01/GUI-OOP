package library.model;

/** INHERITANCE: Magazine is-a LibraryItem. Short loan period, cheaper late fee. */
public class Magazine extends LibraryItem {
    private int issueNumber;

    public Magazine(String id, String title, String publisher, int year, int issueNumber) {
        super(id, title, publisher, year);
        setIssueNumber(issueNumber);
    }

    public int getIssueNumber() { return issueNumber; }

    public void setIssueNumber(int issueNumber) {
        if (issueNumber <= 0) {
            throw new IllegalArgumentException("Issue number must be greater than 0.");
        }
        this.issueNumber = issueNumber;
    }

    @Override public String getType() { return "Magazine"; }
    @Override public int getLoanPeriodDays() { return 7; }
    @Override public double getDailyLateFee() { return 2.50; }
    @Override public String getExtraLabel() { return "Issue No."; }
    @Override public String getExtraInfo() { return String.valueOf(issueNumber); }

    @Override
    public void setExtraInfo(String value) {
        try {
            setIssueNumber(Integer.parseInt(value.trim()));
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Issue number must be a whole number.");
        }
    }

    @Override public String getCreatorLabel() { return "Publisher"; }
}
