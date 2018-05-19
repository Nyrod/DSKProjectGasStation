package gasStation.statistics;

import java.util.*;

/**
 * Created by Michał on 2018-05-11.
 */
public class Statistics {

    private List<CarFOM> carList;

    private Map<Integer, ServiceTime> distributorServiceTime;
    private Map<Integer, ServiceTime> distributorQueueWaitingTime;
    private Map<Integer, ServiceTime> cashQueueWaitingTime;
    private Map<Integer, ServiceTime> cashWashQueueWaitingTime;

    public Statistics() {
        carList = new ArrayList<>();
        distributorServiceTime = new HashMap<>();
        distributorQueueWaitingTime = new HashMap<>();
        cashQueueWaitingTime = new HashMap<>();
        cashWashQueueWaitingTime = new HashMap<>();
    }

    public void printDistributorServiceStatistics() {
        System.out.println("DISTRIBUTOR SERVICE STATISTICS");
        System.out.println("MAX SERVICE TIME = " + getMaxValue(distributorServiceTime));
        System.out.println("MIN SERVICE TIME = " + getMinValue(distributorServiceTime));
        System.out.println("AVG SERVICE TIME = " + getAvgValue(distributorServiceTime));
    }

    public void printDistributorQueueStatistics() {
        System.out.println("DISTRIBUTOR QUEUE STATISTICS");
        System.out.println("MAX WAITING TIME = " + getMaxValue(distributorQueueWaitingTime));
        System.out.println("MIN WAITING TIME = " + getMinValue(distributorQueueWaitingTime));
        System.out.println("AVG WAITING TIME = " + getAvgValue(distributorQueueWaitingTime));
    }

    public void printCashQueueStatistics() {
        System.out.println("CASH PAY FOR PETROL QUEUE STATISTICS");
        System.out.println("MAX WAITING TIME = " + getMaxValue(cashQueueWaitingTime));
        System.out.println("MIN WAITING TIME = " + getMinValue(cashQueueWaitingTime));
        System.out.println("AVG WAITING TIME = " + getAvgValue(cashQueueWaitingTime));
    }

    public void printCasWashQueueStatistics() {
        System.out.println("CASH PAY FOR CAR WASH STATISTICS");
        System.out.println("MAX WAITING TIME = " + getMaxValue(cashWashQueueWaitingTime));
        System.out.println("MIN WAITING TIME = " + getMinValue(cashWashQueueWaitingTime));
        System.out.println("AVG WAITING TIME = " + getAvgValue(cashWashQueueWaitingTime));
    }

    public void addStartService(STAT_CLASS statClass, int ID, double startServiceTime) {
        ServiceTime s = new ServiceTime();
        s.startServiceTime = startServiceTime;

        if (statClass.equals(STAT_CLASS.DIST_SERVICE)) {
            distributorServiceTime.putIfAbsent(ID, s);
        } else if (statClass.equals(STAT_CLASS.DIST_QUEUE)) {
            distributorQueueWaitingTime.putIfAbsent(ID, s);
        } else if (statClass.equals(STAT_CLASS.CASH_QUEUE)) {
            cashQueueWaitingTime.putIfAbsent(ID, s);
        } else if (statClass.equals(STAT_CLASS.CASH_WASH_QUEUE)) {
            cashWashQueueWaitingTime.putIfAbsent(ID, s);
        }
    }

    public void addFinishService(STAT_CLASS statClass, int ID, double finishServiceTime) {
        if (statClass.equals(STAT_CLASS.DIST_SERVICE)) {
            addFinishService(distributorServiceTime, ID, finishServiceTime);
        } else if (statClass.equals(STAT_CLASS.DIST_QUEUE)) {
            addFinishService(distributorQueueWaitingTime, ID, finishServiceTime);
        } else if (statClass.equals(STAT_CLASS.CASH_QUEUE)) {
            addFinishService(cashQueueWaitingTime, ID, finishServiceTime);
        } else if (statClass.equals(STAT_CLASS.CASH_WASH_QUEUE)) {
            addFinishService(cashWashQueueWaitingTime, ID, finishServiceTime);
        }
    }

    private void addFinishService(Map<Integer, ServiceTime> mapServiceTime, int ID, double finishServiceTime) {
        ServiceTime s = mapServiceTime.get(ID);
        s.finishServiceTime = finishServiceTime;
    }

    private double getAvgValue(Map<Integer, ServiceTime> statistics) {
        Set<Integer> keySet = statistics.keySet();
        double sum = 0.0;
        for (Integer ID : keySet) {
            sum += statistics.get(ID).getServiceTime();
        }
        return sum / keySet.size();
    }

    private double getMinValue(Map<Integer, ServiceTime> statistics) {
        Set<Integer> keySet = statistics.keySet();
        double minValue = Integer.MAX_VALUE;
        for (Integer ID : keySet) {
            if (statistics.get(ID).getServiceTime() < minValue) {
                minValue = statistics.get(ID).getServiceTime();
            }
        }
        return minValue;
    }

    private double getMaxValue(Map<Integer, ServiceTime> statistics) {
        Set<Integer> keySet = statistics.keySet();
        double maxValue = -1;
        for (Integer ID : keySet) {
            if (statistics.get(ID).getServiceTime() > maxValue) {
                maxValue = statistics.get(ID).getServiceTime();
            }
        }
        return maxValue;
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

    public boolean carPayForWash(int carID) {
        return getCarFOMByID(carID).payForWash;
    }

    private CarFOM getCarFOMByID(int carID) {
        int i = 0;
        while (i < carList.size() && carID != carList.get(i).carID) {
            i++;
        }
        return i < carList.size() ? carList.get(i) : null;
    }

    public enum STAT_CLASS {
        DIST_SERVICE,
        DIST_QUEUE,
        CASH_QUEUE,
        CASH_WASH_QUEUE
    }

    private class CarFOM {
        public int carID;
        public boolean payForWash;
    }

    private class ServiceTime {
        private double startServiceTime;
        private double finishServiceTime;

        public ServiceTime() {
            startServiceTime = 0.0;
            finishServiceTime = 0.0;
        }

        public double getServiceTime() {
            return finishServiceTime - startServiceTime;
        }
    }

}
