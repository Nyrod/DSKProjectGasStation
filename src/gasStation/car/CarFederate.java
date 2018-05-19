package gasStation.car;

import gasStation.DefaultFederate;
import gasStation.Event;
import hla.rti1516e.*;
import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.HLAunicodeString;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CarFederate extends DefaultFederate<CarFederateAmbassador> {

    protected List<Car> carList;
    private List<DistributorFOM> distributorFOMList;
    private CarWashFOM carWashFOM;

    protected ObjectClassHandle carHandle;
    protected AttributeHandle carID;
    protected AttributeHandle wantWash;
    protected AttributeHandle payForWash;
    protected InteractionClassHandle chooseDistributor;
    protected InteractionClassHandle wantToPay;
    protected InteractionClassHandle endSimulation;

    protected ObjectClassHandle distributorHandle;
    protected AttributeHandle distributorID;
    protected AttributeHandle distributorType;
    protected AttributeHandle distributorQueueSize;
    protected ObjectClassHandle carWashClassHandle;
    protected AttributeHandle carWashQueueSize;
    protected AttributeHandle currentServiceCarID;
    protected InteractionClassHandle distributorServiceStart;
    protected InteractionClassHandle distributorServiceFinish;
    protected InteractionClassHandle cashServiceStart;
    protected InteractionClassHandle cashServiceFinish;

    public CarFederate() {
        super();
        this.carList = new ArrayList<>();
        this.distributorFOMList = new ArrayList<>();
        this.carWashFOM = new CarWashFOM();
        this.carWashFOM.currentServiceCarID = -1;
    }

    @Override
    protected CarFederateAmbassador createFederateAmbassador() {
        return new CarFederateAmbassador(this);
    }

    @Override
    protected void beforeSimulationLoop() throws RTIexception {
        Random rand = new Random();
        double time = rand.nextInt(5) + 5;
        for (int i = 0; i < carList.size(); i++) {
            createChooseDistributorEvent(carList.get(i), time);
            time += rand.nextInt(4) + fedamb.federateLookahead;
        }
    }

    @Override
    protected void afterSimulationLoop() throws RTIexception {

    }

    public Event createUpdateDistributorInstanceEvent(AttributeHandleValueMap theAttributes) {
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                updateDistributorInstance(theAttributes);
            }
        };
    }

    public Event createUpdateCarWashInstanceEvent(AttributeHandleValueMap theAttributes) {
        int currentServiceCarID = theAttributes.getValueReference(this.currentServiceCarID).getInt();
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                int finishServiceCarID = updateCarWashInstance(theAttributes);
                if (currentServiceCarID != -1) {
                    Car car = findCarByID(currentServiceCarID);
                    car.setCarStatus(Car.CAR_STATUS.CAR_WASH_SERVICE);
                } else if (currentServiceCarID == -1 && finishServiceCarID != -1) {
                    Car car = findCarByID(finishServiceCarID);
                    car.setCarStatus(Car.CAR_STATUS.QUEUE_TO_CASH);
                    createWantToPayEvent(car.getCarID(), fedamb.federateTime + fedamb.federateLookahead);
                }
//                if (finishServiceCarID == -1 ) {
//                    Car car = findCarByID(finishServiceCarID);
//                    car.setCarStatus(Car.CAR_STATUS.QUEUE_TO_CASH);
//                    createWantToPayEvent(car.getCarID(), fedamb.federateTime + fedamb.federateLookahead);
//                } else {
//                    Car car = findCarByID(finishServiceCarID);
//                    car.setCarStatus(Car.CAR_STATUS.CAR_WASH_SERVICE);
//                }
            }
        };
    }

    public Event createDistributorStartServiceEvent(ParameterHandleValueMap theParameters) throws RTIexception {
        ParameterHandle carIDParameter = rtiamb.getParameterHandle(distributorServiceStart, "CarID");
        int carID = theParameters.getValueReference(carIDParameter).getInt();
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                findCarByID(carID).setCarStatus(Car.CAR_STATUS.DISTRIBUTOR_SERVICE);
            }
        };
    }

    public Event createDistributorFinishServiceEvent(ParameterHandleValueMap theParameters) throws RTIexception {
        ParameterHandle carIDParameter = rtiamb.getParameterHandle(distributorServiceFinish, "CarID");
        int carID = theParameters.getValueReference(carIDParameter).getInt();
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                findCarByID(carID).setCarStatus(Car.CAR_STATUS.QUEUE_TO_CASH);
                createWantToPayEvent(carID, fedamb.federateTime + fedamb.federateLookahead);
            }
        };
    }

    public Event createCashStartServiceEvent(ParameterHandleValueMap theParameters) throws RTIexception {
        ParameterHandle carIDParameter = rtiamb.getParameterHandle(cashServiceStart, "CarID");
        int carID = theParameters.getValueReference(carIDParameter).getInt();
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                findCarByID(carID).setCarStatus(Car.CAR_STATUS.CASH_SERVICE);
            }
        };
    }

    public Event createCashFinishServiceEvent(ParameterHandleValueMap theParameters) throws RTIexception {
        ParameterHandle carIDParameter = rtiamb.getParameterHandle(cashServiceFinish, "CarID");
        int carID = theParameters.getValueReference(carIDParameter).getInt();
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                Car car = findCarByID(carID);
                if (car.isWantWash()) {
                    car.setCarStatus(Car.CAR_STATUS.LEFT_GAS_STATION);
                } else {
                    if (car.isInternalWantWash()) {
                        car.setWantWash(true);
                        car.setPayForWash(true);
                        car.setCarStatus(Car.CAR_STATUS.QUEUE_TO_CAR_WASH);
                        createUpdateCarAttributeInstanceEvent(car.getCarID(), fedamb.federateTime + fedamb.federateLookahead);
                    } else {
                        car.setCarStatus(Car.CAR_STATUS.LEFT_GAS_STATION);
                    }
                }
                if (ifAllCarsLeftGasStation()) {
                    createEndSimulationEvent(fedamb.federateTime + fedamb.federateLookahead);
                    finishSimulation();
                }
            }
        };
    }

    private void createEndSimulationEvent(double time) throws RTIexception {
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                sendInteractionEndSimulation(time);
            }
        });
    }

    private void createChooseDistributorEvent(Car car, double time) throws RTIexception {
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                int distributorID = findDistributorWithShortestQueue(car);
                if (distributorID == -1) {
                    log("Not distributor with type=" + car.getType() + ", time=" + time);
                    car.setCarStatus(Car.CAR_STATUS.LEFT_GAS_STATION);
                    if (ifAllCarsLeftGasStation()) {
                        finishSimulation();
                    }
                } else {
                    car.setCarStatus(Car.CAR_STATUS.QUEUE_TO_DISTRIBUTOR);
                    sendInteractionChooseDistributor(distributorID, car.getCarID(), time);
                }
            }
        });
    }

    private void createWantToPayEvent(int carID, double time) {
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                sendInteractionWantToPay(carID, time);
            }
        });
    }

    public void createUpdateCarAttributeInstanceEvent(int carID, double time) throws RTIexception {
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                updateCarAttributes(carList.get(carID), time);
            }
        });
    }

    private void updateDistributorInstance(AttributeHandleValueMap theAttributes) {
        HLAunicodeString hlaString = encoderFactory.createHLAunicodeString();
        try {
            hlaString.decode(theAttributes.getValueReference(this.distributorType));
        } catch (DecoderException e) {
            e.printStackTrace();
        }
        String distributorType = hlaString.getValue();
        int distributorID = theAttributes.getValueReference(this.distributorID).getInt();
        int queueSize = theAttributes.getValueReference(this.distributorQueueSize).getInt();

        DistributorFOM distributorFOM = findDistributorFOMByID(distributorID);
        if (distributorFOM != null) {
            distributorFOM.type = distributorType;
            distributorFOM.queueSize = queueSize;
        } else {
            distributorFOM = new DistributorFOM();
            distributorFOM.distributorID = distributorID;
            distributorFOM.type = distributorType;
            distributorFOM.queueSize = queueSize;
            distributorFOMList.add(distributorFOM);
        }

        log("Updated Distributor Instance: distributorID=" + distributorID + ", distributorType=" + distributorType + ", queueSize=" + queueSize);
    }

    private int updateCarWashInstance(AttributeHandleValueMap theAttributes) {
        int currentServiceCarID = theAttributes.getValueReference(this.currentServiceCarID).getInt();
        int finishServiceCarID = carWashFOM.currentServiceCarID;

        carWashFOM.currentServiceCarID = currentServiceCarID;

        log("Updated CarWash Instance: finishServiceCarID=" + finishServiceCarID + ", currentServiceCarID=" + currentServiceCarID);

        return finishServiceCarID;
    }

    private void updateCarAttributes(Car car, double time) throws RTIexception {
        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(2);
        attributes.put(carID, encoderFactory.createHLAinteger32BE(car.getCarID()).toByteArray());
        attributes.put(wantWash, encoderFactory.createHLAboolean(car.isWantWash()).toByteArray());
        attributes.put(payForWash, encoderFactory.createHLAboolean(car.isWantWash()).toByteArray());

        HLAfloat64Time theTime = timeFactory.makeTime(time + fedamb.federateLookahead);
        rtiamb.updateAttributeValues(car.getObjectHandle(), attributes, generateTag(), theTime);

        log("Updated Car Attributes: " + car.toString() + ", time=" + theTime.toString());
    }

    private void sendInteractionChooseDistributor(int distributorID, int carID, double time) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);

        ParameterHandle parameterHandle = rtiamb.getParameterHandle(chooseDistributor, "DistributorID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(distributorID).toByteArray());

        parameterHandle = rtiamb.getParameterHandle(chooseDistributor, "CarID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Time theTime = timeFactory.makeTime(time + fedamb.federateLookahead);
        rtiamb.sendInteraction(chooseDistributor, parameters, generateTag(), theTime);

        log("Interaction Send: handle=" + chooseDistributor + " {CarChooseDistributor}, " + "CarID= " + carID + ", DistributorID= " + distributorID + ", time=" + theTime.toString());
    }

    private void sendInteractionWantToPay(int carID, double time) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);

        ParameterHandle parameterHandle = rtiamb.getParameterHandle(wantToPay, "CarID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Time theTime = timeFactory.makeTime(time + fedamb.federateLookahead);
        rtiamb.sendInteraction(wantToPay, parameters, generateTag(), theTime);

        log("Interaction Send: handle=" + wantToPay + " {CarWantToPay}, " + "CarID= " + carID + ", time=" + theTime.toString());
    }

    private void sendInteractionEndSimulation(double time) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(0);

        HLAfloat64Time theTime = timeFactory.makeTime(time + fedamb.federateLookahead);
        rtiamb.sendInteraction(wantToPay, parameters, generateTag(), theTime);

        log("Interaction Send: handle=" + endSimulation + " {EndSimulation}, time=" + theTime.toString());
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        // OBJECTS //
        carHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Car");
        carID = rtiamb.getAttributeHandle(carHandle, "CarID");
        wantWash = rtiamb.getAttributeHandle(carHandle, "WantWash");
        payForWash = rtiamb.getAttributeHandle(carHandle, "PayForWash");
        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
        attributes.add(carID);
        attributes.add(wantWash);
        attributes.add(payForWash);
        rtiamb.publishObjectClassAttributes(carHandle, attributes);

        distributorHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Distributor");
        distributorID = rtiamb.getAttributeHandle(distributorHandle, "DistributorID");
        distributorType = rtiamb.getAttributeHandle(distributorHandle, "DistributorType");
        distributorQueueSize = rtiamb.getAttributeHandle(distributorHandle, "QueueSize");
        attributes.clear();
        attributes.add(distributorID);
        attributes.add(distributorType);
        attributes.add(distributorQueueSize);
        rtiamb.subscribeObjectClassAttributes(distributorHandle, attributes);

        carWashClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.CarWash");
        carWashQueueSize = rtiamb.getAttributeHandle(carWashClassHandle, "QueueSize");
        currentServiceCarID = rtiamb.getAttributeHandle(carWashClassHandle, "CurrentServiceCarID");
        attributes.clear();
        attributes.add(carWashQueueSize);
        attributes.add(currentServiceCarID);
        rtiamb.subscribeObjectClassAttributes(carWashClassHandle, attributes);

        // INTERACTIONS //
        chooseDistributor = rtiamb.getInteractionClassHandle("HLAinteractionRoot.ChooseDistributor");
        wantToPay = rtiamb.getInteractionClassHandle("HLAinteractionRoot.WantToPay");
        endSimulation = rtiamb.getInteractionClassHandle("HLAinteractionRoot.EndSimulation");
        distributorServiceStart = rtiamb.getInteractionClassHandle("HLAinteractionRoot.DistributorServiceStart");
        distributorServiceFinish = rtiamb.getInteractionClassHandle("HLAinteractionRoot.DistributorServiceFinish");
        cashServiceStart = rtiamb.getInteractionClassHandle("HLAinteractionRoot.CashServiceStart");
        cashServiceFinish = rtiamb.getInteractionClassHandle("HLAinteractionRoot.CashServiceFinish");

        rtiamb.publishInteractionClass(chooseDistributor);
        rtiamb.publishInteractionClass(wantToPay);
        rtiamb.publishInteractionClass(endSimulation);
        rtiamb.subscribeInteractionClass(distributorServiceStart);
        rtiamb.subscribeInteractionClass(distributorServiceFinish);
        rtiamb.subscribeInteractionClass(cashServiceStart);
        rtiamb.subscribeInteractionClass(cashServiceFinish);
    }

    @Override
    protected void registerObjects() throws RTIexception {
        carList = new ArrayList<>();
        for (int i = 0; i < Car.CARS_IN_SIMULATION; i++) {
            Car carToAdd = Car.createCar();
            carToAdd.setObjectHandle(rtiamb.registerObjectInstance(carHandle));
            carList.add(carToAdd);
            createUpdateCarAttributeInstanceEvent(carToAdd.getCarID(), fedamb.federateTime + fedamb.federateLookahead);
            log("Registered Object, handle=" + carToAdd.getObjectHandle());
            log(carToAdd.toString());
        }
    }

    @Override
    protected void deleteObjects() throws RTIexception {
        for (int i = Car.CARS_IN_SIMULATION - 1; i >= 0; i--) {
            rtiamb.deleteObjectInstance(carList.remove(i).getObjectHandle(), generateTag());
        }
    }

    @Override
    protected void enableTimePolicy() throws RTIexception {
        enableTimeRegulation();
        enableTimeConstrained();
    }

    @Override
    protected void log(String message) {
        System.out.println("CarFederate   : " + message);
    }

    private int findDistributorWithShortestQueue(Car car) {
        int distributorID = -1;
        int queueSize = Integer.MAX_VALUE;
        for (int i = 0; i < distributorFOMList.size(); i++) {
            DistributorFOM distributor = distributorFOMList.get(i);
            if (distributor.type.equals(car.getType())) {
                if (distributor.queueSize == 0)
                    return distributor.distributorID;
                if (distributor.queueSize < queueSize) {
                    distributorID = distributor.distributorID;
                    queueSize = distributor.queueSize;
                }
            }
        }
        return distributorID;
    }

    private boolean ifAllCarsLeftGasStation() {
        for (int i = 0; i < carList.size(); i++) {
            if (!carList.get(i).ifCarLeftGasStation())
                return false;
        }
        return true;
    }

    private Car findCarByID(int carID) {
        return carList.stream()
                .filter(c -> c.getCarID() == carID)
                .findFirst()
                .get();
    }

    private DistributorFOM findDistributorFOMByID(int distributorFOMID) {
        for (int i = 0; i < distributorFOMList.size(); i++) {
            if (distributorFOMList.get(i).distributorID == distributorFOMID)
                return distributorFOMList.get(i);
        }
        return null;
    }

    private class CarWashFOM {
        public int currentServiceCarID;
    }

    private class DistributorFOM {
        public int distributorID;
        public int queueSize;
        public String type;
    }

    public static void main(String[] args) {
        try {
            new CarFederate().runFederate("CarFederate");
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
