package gasStation.car;

import hla.rti1516e.ObjectInstanceHandle;

import java.util.Random;

public class Car {

    public static final int CARS_IN_SIMULATION = 3;
    public static int NEXT_CAR_ID = 0;
    private int carID;
    private CAR_STATUS carStatus;
    private String type;
    private boolean wantWash;
    private boolean payForWash;
    private boolean internalWantWash;

    private ObjectInstanceHandle objectHandle;

    public ObjectInstanceHandle getObjectHandle() {
        return objectHandle;
    }

    public void setObjectHandle(ObjectInstanceHandle objectHandle) {
        this.objectHandle = objectHandle;
    }

    private Car(String type, boolean internalWantWash) {
        this.carStatus = CAR_STATUS.ENTER_GAS_STATION;
        this.type = type;
        this.wantWash = false;
        this.payForWash = false;
        this.internalWantWash = internalWantWash;
    }

    public static Car createCar() {
        Random random = new Random();
        Car car = new Car(random.nextBoolean() ? "ON" : "GAS", random.nextBoolean());
        car.carID = NEXT_CAR_ID;
        NEXT_CAR_ID++;
        return car;
    }

    public String getType() {
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
        System.out.println(this.toString());
    }

    public boolean isInternalWantWash() {
        return internalWantWash;
    }

    public void setPayForWash(boolean payForWash) {
        this.payForWash = payForWash;
    }

    public boolean isPayForWash() {
        return payForWash;
    }

    public boolean ifCarLeftGasStation() {
        return carStatus.equals(CAR_STATUS.LEFT_GAS_STATION);
    }

    @Override
    public String toString() {
        return "Car{" +
                "carID=" + carID +
                ", carStatus=" + carStatus +
                ", type='" + type + '\'' +
                ", wantWash=" + wantWash +
                ", payForWash=" + payForWash +
                ", internalWantWash=" + internalWantWash +
                '}';
    }

    protected enum CAR_STATUS {
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
