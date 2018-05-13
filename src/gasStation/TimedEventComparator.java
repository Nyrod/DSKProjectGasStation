package gasStation;

import java.util.Comparator;

public class TimedEventComparator implements Comparator<Event> {

    @Override
    public int compare(Event o1, Event o2) {
        return o1.getTime().compareTo(o2.getTime());
    }
}
