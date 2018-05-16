package gasStation.car;

import hla.rti1516e.ObjectInstanceHandle;

import java.util.Random;

public class Car {

    public static final int CARS_IN_SIMULATION = 3;
    private int carID;
    private CAR_STATUS carStatus;
    private String type;
    private boolean wantWash;

    private ObjectInstanceHandle objectHandle;

    public ObjectInstanceHandle getObjectHandle() {
        return objectHandle;
    }

    public void setObjectHandle(ObjectInstanceHandle objectHandle) {
        this.objectHandle = objectHandle;
    }

    private Car(String type, boolean wantWash) {
        this.carStatus = CAR_STATUS.ENTER_GAS_STATION;
        this.type = type;
        this.wantWash = wantWash;
    }

    public static Car createCar() {
        Random random = new Random();
        Car car = new Car(random.nextBoolean() ? "ON" : "GAS", random.nextBoolean());
        car.carID = 1;
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

    public int getCarID() {
        return carID;
    }

    public void setCarStatus(CAR_STATUS carStatus) {
        this.carStatus = carStatus;
    }

    @Override
    public String toString() {
        return "Car{" +
                "carID=" + carID +
                ", carStatus=" + carStatus +
                ", type='" + type + '\'' +
                ", wantWash=" + wantWash +
                '}';
    }

    private enum CAR_STATUS {
        ENTER_GAS_STATION,
        QUEUE_TO_DISTRIBUTOR,
        DISTRIBUTOR_SERVICE,
        QUEUE_TO_CASH,
        CASH_SERVICE,
        QUEUE_TO_CAR_WASH,
        CAR_WASH_SERVICE,
        LEFT_GAS_STATION
    }
}
