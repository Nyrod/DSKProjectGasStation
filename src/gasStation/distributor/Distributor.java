package gasStation.distributor;

import hla.rti1516e.ObjectInstanceHandle;

public class Distributor {

    public static int DISTRIBUTORS_IN_SIMULATION = 5;

    private ObjectInstanceHandle objectHandle;

    public ObjectInstanceHandle getObjectHandle() {
        return objectHandle;
    }

    public void setObjectHandle(ObjectInstanceHandle objectHandle) {
        this.objectHandle = objectHandle;
    }
}
