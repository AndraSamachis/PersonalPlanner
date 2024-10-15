import java.util.ArrayList;
import java.util.List;

public class Task {
    private int day;
    private int month;
    private int year;
    private List<String> eventList;

    public Task(int day, int month, int year, String initialEvent) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.eventList = new ArrayList<>();
        addEvent(initialEvent);
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public List<String> getEventList() {
        return eventList;
    }
    public void addEvent(String event) {
        if (event != null && !event.trim().isEmpty()) {
            eventList.add(event.trim());
        }
    }
}
