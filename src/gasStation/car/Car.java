package gasStation.car;

import hla.rti1516e.ObjectInstanceHandle;

public class Car {

    public static int CARS_IN_SIMULATION = 10;

    private ObjectInstanceHandle objectHandle;

    public ObjectInstanceHandle getObjectHandle() {
        return objectHandle;
    }

    public void setObjectHandle(ObjectInstanceHandle objectHandle) {
        this.objectHandle = objectHandle;
    }
}
