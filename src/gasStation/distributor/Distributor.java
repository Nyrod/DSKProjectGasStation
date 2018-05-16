package gasStation.distributor;

import hla.rti1516e.ObjectInstanceHandle;

import java.util.ArrayList;
import java.util.Random;

public class Distributor {

    public static int DISTRIBUTORS_IN_SIMULATION = 5;
    private static int NEXT_DISTRIBUTOR_ID = 0;

    private ArrayList<Integer> queue;
    private int distributorID;
    private String type;

    private ObjectInstanceHandle objectInstanceHandle;


    private Distributor() {
        Random rand = new Random();
        this.queue = new ArrayList<>();
        this.distributorID = NEXT_DISTRIBUTOR_ID;
        this.type = rand.nextBoolean() ? "ON" : "GAS";

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

    @Override
    public String toString() {
        return "Distributor{" +
                "distributorID=" + distributorID +
                ", type='" + type + '\'' +
                ", queueSize=" + queue.size() +
                '}';
    }
}
