package gasStation.cash;

import hla.rti1516e.ObjectInstanceHandle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michał on 2018-05-11.
 */
public class Cash {

    protected ObjectInstanceHandle cashInstanceHandle;
    private List<CarFOM> carList;
    private List<CarFOM> queue;
    private double finishCurrentServiceTime;

    public Cash() {
        queue = new ArrayList<>();
        carList = new ArrayList<>();
        finishCurrentServiceTime = 0.0;
    }

    public int addCarToQueue(int carID) {
        CarFOM carToAdd = getCarFOMByID(carID);
        if (carToAdd != null) {
//            if (carToAdd.payForWash) {
//                int i = 0;
//                while (i < queue.size() && queue.get(i).payForWash) {
//                    i++;
//                }
//                queue.add(i, carToAdd);
//                return i;
//            } else {
                queue.add(carToAdd);
                return queue.size() - 1;
//            }
        }
        return -1;
    }

    public int getCarIDFromQueue() {
        return queue.remove(0).carID;
    }

    public int getNextServiceCarIDFromQueue() {
        return queue.get(0).carID;
    }

    public boolean haveCarInQueue() {
        return !queue.isEmpty();
    }

    public void addCarFOMInstance(int carID, boolean payForWash) {
        CarFOM car = new CarFOM();
        car.carID = carID;
        car.payForWash = payForWash;
        carList.add(car);
    }

    public void updateCarFOMInstance(int carID, boolean payForWash) {
        CarFOM car = getCarFOMByID(carID);
        if (car != null) {
            car.payForWash = payForWash;
        } else {
            addCarFOMInstance(carID, payForWash);
        }
    }

    public int getQueueSize() {
        return queue.size();
    }

    private CarFOM getCarFOMByID(int carID) {
        int i = 0;
        while (i < carList.size() && carID != carList.get(i).carID) {
            i++;
        }
        return i < carList.size() ? carList.get(i) : null;
    }

    public double getFinishCurrentServiceTime() {
        return finishCurrentServiceTime;
    }

    public void setFinishCurrentServiceTime(double finishCurrentServiceTime) {
        this.finishCurrentServiceTime = finishCurrentServiceTime;
    }

    @Override
    public String toString() {
        return "Cash{" +
                "distributorQueueSize=" + queue.size() +
                '}';
    }

    private class CarFOM {
        public int carID;
        public boolean payForWash;
    }
}
