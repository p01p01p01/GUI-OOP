package library.model;

/** INHERITANCE: DVD is-a LibraryItem. Shortest loan period, highest late fee. */
public class DVD extends LibraryItem {
    private int runtimeMinutes;

    public DVD(String id, String title, String director, int year, int runtimeMinutes) {
        super(id, title, director, year);
        setRuntimeMinutes(runtimeMinutes);
    }

    public int getRuntimeMinutes() { return runtimeMinutes; }

    public void setRuntimeMinutes(int runtimeMinutes) {
        if (runtimeMinutes < 1 || runtimeMinutes > 600) {
            throw new IllegalArgumentException("Runtime must be between 1 and 600 minutes.");
        }
        this.runtimeMinutes = runtimeMinutes;
    }

    @Override public String getType() { return "DVD"; }
    @Override public int getLoanPeriodDays() { return 3; }
    @Override public double getDailyLateFee() { return 10.00; }
    @Override public String getExtraLabel() { return "Runtime (min)"; }
    @Override public String getExtraInfo() { return String.valueOf(runtimeMinutes); }

    @Override
    public void setExtraInfo(String value) {
        try {
            setRuntimeMinutes(Integer.parseInt(value.trim()));
        } catch (NumberFormatException | NullPointerException e) {
            throw new IllegalArgumentException("Runtime must be a whole number of minutes.");
        }
    }

    @Override public String getCreatorLabel() { return "Director"; }
}
