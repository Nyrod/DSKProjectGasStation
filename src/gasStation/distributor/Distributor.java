package gasStation.distributor;

import hla.rti1516e.ObjectInstanceHandle;

import java.util.ArrayList;

public class Distributor {

    public static int DISTRIBUTORS_IN_SIMULATION = 5;
    private ArrayList<Integer> carIds;
    private String type;

    private ObjectInstanceHandle objectHandle;

    public ObjectInstanceHandle getObjectHandle() {
        return objectHandle;
    }

    public void setObjectHandle(ObjectInstanceHandle objectHandle) {
        this.objectHandle = objectHandle;
    }

    public Distributor() {
        this.carIds = new ArrayList<>();
    }

    public void addCar(int carId) {
        carIds.add(carId);
    }

    public int getCar() {
        return carIds.get(0);
    }

    public ArrayList<Integer> getCarIds() {
        return carIds;
    }

    public void setCarIds(ArrayList<Integer> carIds) {
        this.carIds = carIds;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
