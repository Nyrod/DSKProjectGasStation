package gasStation.car;

import hla.rti1516e.ObjectInstanceHandle;

import java.util.Random;

public class Car {

    public static final int CARS_IN_SIMULATION = 3;
    private String type;
    private boolean wantWash;

    private ObjectInstanceHandle objectHandle;

    public ObjectInstanceHandle getObjectHandle() {
        return objectHandle;
    }

    public void setObjectHandle(ObjectInstanceHandle objectHandle) {
        this.objectHandle = objectHandle;
    }

    public Car(String type, boolean wantWash) {
        this.type = type;
        this.wantWash = wantWash;
    }

    public static Car createCar() {
        Random random = new Random();
        Car car = new Car(random.nextBoolean() ? "ON" : "GAS", random.nextBoolean());
        return car;
    }

    public String getTypeype() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isWantWash() {
        return wantWash;
    }

    public void setWantWash(boolean wantWash) {
        this.wantWash = wantWash;
    }
}
