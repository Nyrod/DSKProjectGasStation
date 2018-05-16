package gasStation.carWash;

import hla.rti1516e.ObjectInstanceHandle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michał on 2018-05-11.
 */
public class CarWash {

    protected ObjectInstanceHandle carWashInstanceHandle;

    private List<Integer> queue;
    private int currentServiceCarID;

    public CarWash() {
        this.queue = new ArrayList<>();
        this.currentServiceCarID = -1;
    }

    public void addCarToQueue(int carId) {
        queue.add(carId);
    }

    public int getCar() {
        if (queue.isEmpty())
            return -1;
        return queue.remove(0);
    }

    public int getQueueSize() {
        return queue.size();
    }

    public boolean haveCarInQueue() {
        return !queue.isEmpty();
    }


    public int getCurrentServiceCarID() {
        return currentServiceCarID;
    }

    public void setCurrentServiceCarID(int currentServiceCarID) {
        this.currentServiceCarID = currentServiceCarID;
    }

    public ObjectInstanceHandle getCarWashInstanceHandle() {
        return carWashInstanceHandle;
    }

    public void setCarWashInstanceHandle(ObjectInstanceHandle carWashInstanceHandle) {
        this.carWashInstanceHandle = carWashInstanceHandle;
    }

}
