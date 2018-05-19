package gasStation.distributor;

import hla.rti1516e.ObjectInstanceHandle;

import java.util.ArrayList;
import java.util.Random;

public class Distributor {

    public static int DISTRIBUTORS_IN_SIMULATION = 3;
    private static int NEXT_DISTRIBUTOR_ID = 0;

    private ArrayList<Integer> queue;
    private int distributorID;
    private String type;
    private double nextServiceTime;

    private ObjectInstanceHandle objectInstanceHandle;


    private Distributor() {
        Random rand = new Random();
        this.queue = new ArrayList<>();
        this.distributorID = NEXT_DISTRIBUTOR_ID;
        this.type = rand.nextBoolean() ? "ON" : "GAS";
        nextServiceTime = 0.0;

        NEXT_DISTRIBUTOR_ID++;
    }

    public static Distributor getNextDistributor() {
        return new Distributor();
    }

    public void addCar(int carId) {
        queue.add(carId);
    }

    public int getCar() {
        return queue.remove(0);
    }

    public int getNextServiceCar() {
        return queue.get(0);
    }

    public int getQueueSize() {
        return queue.size();
    }

    public boolean haveCarInQueue() {
        return !queue.isEmpty();
    }

    public ObjectInstanceHandle getObjectInstanceHandle() {
        return objectInstanceHandle;
    }

    public void setObjectInstanceHandle(ObjectInstanceHandle objectInstanceHandle) {
        this.objectInstanceHandle = objectInstanceHandle;
    }

    public String getType() {
        return type;
    }

    public int getDistributorID() {
        return distributorID;
    }

    public double getNextServiceTime() {
        return nextServiceTime;
    }

    public void setNextServiceTime(double nextServiceTime) {
        this.nextServiceTime = nextServiceTime;
    }

    @Override
    public String toString() {
        return "Distributor{" +
                "distributorID=" + distributorID +
                ", type='" + type + '\'' +
                ", distributorQueueSize=" + queue.size() +
                '}';
    }
}
