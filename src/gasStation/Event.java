package gasStation;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.exceptions.RTIexception;
import hla.rti1516e.time.HLAfloat64Time;

public abstract class Event {

    private HLAfloat64Time time;

    public Event(HLAfloat64Time time) {
        this.time = time;
    }

    public abstract void runEvent() throws RTIexception;

    public HLAfloat64Time getTime() {
        return time;
    }
}
