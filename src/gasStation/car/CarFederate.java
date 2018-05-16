package gasStation.car;

import gasStation.DefaultFederate;
import gasStation.Event;
import gasStation.TimedEventComparator;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CarFederate extends DefaultFederate<CarFederateAmbassador> {

    protected List<Car> carList;

    protected ObjectClassHandle carHandle;
    protected AttributeHandle carID;
    protected AttributeHandle wantWash;
    protected AttributeHandle payForWash;
    protected InteractionClassHandle chooseDistributor;
    protected InteractionClassHandle wantToPay;

    protected ObjectClassHandle distributorHandle;
    protected AttributeHandle distributorID;
    protected AttributeHandle distributorType;
    protected AttributeHandle queueSize;
    protected InteractionClassHandle distributorServiceStart;
    protected InteractionClassHandle distributorServiceFinish;
    protected InteractionClassHandle cashServiceStart;
    protected InteractionClassHandle cashServiceFinish;

    public CarFederate() {
        super();
        this.carList = new ArrayList<>();
    }

    @Override
    protected CarFederateAmbassador createFederateAmbassador() {
        return new CarFederateAmbassador(this);
    }

    @Override
    protected void mainSimulationLoop() throws RTIexception {
        createChooseDistributorInternalEvent();
//        while (true) {
//            double timeToAdvance = fedamb.federateTime + fedamb.federateLookahead;
//            HLAfloat64Time nextEventTime = timeFactory.makeTime(timeToAdvance);
//
//
//            if (!fedamb.externalEventList.isEmpty()) {
//                for(int i = fedamb.externalEventList.size() - 1; i >= 0; i++) {
//                    fedamb.externalEventList.remove(i).runEvent();
//                }
//            }
//
//            if (!internalEventList.isEmpty()) {
//                internalEventList.sort(new TimedEventComparator());
//                nextEventTime = internalEventList.get(0).getTime().add(timeFactory.makeInterval(1));
//                if (fedamb.federateTime <= nextEventTime.getValue()) {
//                    internalEventList.remove(0).runEvent();
//                    advanceTime(nextEventTime);
//                    fedamb.federateTime = nextEventTime.getValue();
//                } else {
//                    internalEventList.remove(0);
//                }
//            } else {
//                advanceTime(nextEventTime);
//            }
//            //            if(!internalEventList.isEmpty())
//            //{
////                internalEventList.sort(new TimedEventComparator());
////                nextEventTime = internalEventList.get(0).getTime().add(timeFactory.makeInterval(1));
////                internalEventList.remove(0).runEvent();
////                advanceTime(nextEventTime);
////            }
//
//            if (fedamb.grantedTime == timeToAdvance) {
//                fedamb.federateTime = timeToAdvance;
//                log("Time advanced to: " + timeToAdvance);
//            }
//            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
//        }
        boolean isAdvancing = false;
        double timeToAdvance = fedamb.federateTime + fedamb.federateLookahead;
        HLAfloat64Time nextEventTime;

        while (true) {
            if (!fedamb.externalEventList.isEmpty()) {
                for (int i = fedamb.externalEventList.size() - 1; i >= 0; i++) {
                    fedamb.externalEventList.remove(i).runEvent();
                }
            }

            if(!isAdvancing) {
                if (!internalEventList.isEmpty()) {
                    internalEventList.sort(new TimedEventComparator());
                    nextEventTime = internalEventList.get(0).getTime();
                    timeToAdvance = nextEventTime.getValue();
                    if(nextEventTime.getValue() - fedamb.federateTime <= fedamb.federateLookahead) {
                        advanceTime(nextEventTime);
                    } else {
                        timeToAdvance = fedamb.federateTime + fedamb.federateLookahead;
                        nextEventTime = timeFactory.makeTime(timeToAdvance);
                        advanceTime(nextEventTime);
                    }

                } else {
                    timeToAdvance = fedamb.federateTime + fedamb.federateLookahead;
                    nextEventTime = timeFactory.makeTime(timeToAdvance);
                    advanceTime(nextEventTime);
                }
                isAdvancing = true;
            }

            if (fedamb.grantedTime == timeToAdvance) {
                fedamb.federateTime = timeToAdvance;
                log("Time advanced to: " + timeToAdvance);

                if(!internalEventList.isEmpty()) {
                    internalEventList.sort(new TimedEventComparator());
                    while(!internalEventList.isEmpty() && internalEventList.get(0).getTime().getValue() == fedamb.federateTime) {
                        internalEventList.remove(0).runEvent();
                    }
                }

                isAdvancing = false;
            }
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    @Override
    protected void publishAndSubscribe() throws NameNotFound, NotConnected, RTIinternalError, FederateNotExecutionMember, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, SaveInProgress, InteractionClassNotDefined, FederateServiceInvocationsAreBeingReportedViaMOM {
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
        queueSize = rtiamb.getAttributeHandle(distributorHandle, "QueueSize");

        attributes.clear();
        attributes.add(distributorID);
        attributes.add(distributorType);
        attributes.add(queueSize);

        rtiamb.subscribeObjectClassAttributes(distributorHandle, attributes);

        // INTERACTIONS //
        chooseDistributor = rtiamb.getInteractionClassHandle("HLAinteractionRoot.ChooseDistributor");
        wantToPay = rtiamb.getInteractionClassHandle("HLAinteractionRoot.WantToPay");
        distributorServiceStart = rtiamb.getInteractionClassHandle("HLAinteractionRoot.DistributorServiceStart");
        distributorServiceFinish = rtiamb.getInteractionClassHandle("HLAinteractionRoot.DistributorServiceFinish");
        cashServiceStart = rtiamb.getInteractionClassHandle("HLAinteractionRoot.CashServiceStart");
        cashServiceFinish = rtiamb.getInteractionClassHandle("HLAinteractionRoot.CashServiceFinish");

        rtiamb.publishInteractionClass(chooseDistributor);
        rtiamb.publishInteractionClass(wantToPay);
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
            addUpdateCarAttributeInternalEvent(carToAdd.getCarID());
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

    private void createChooseDistributorInternalEvent() throws RTIexception {
        Random rand = new Random();
        double nextTime = fedamb.federateTime;
        for (int i = 0; i < Car.CARS_IN_SIMULATION; i++) {
            nextTime += rand.nextInt(15) + 1;
            internalEventList.add(new Event(timeFactory.makeTime(nextTime)) {
                @Override
                public void runEvent() throws RTIexception {
                    sendInteractionChooseDistributor(2, 3, getTime());
                }
            });
        }
    }

    public void addUpdateCarAttributeInternalEvent(int carID) throws RTIexception {
        double time = fedamb.federateTime + fedamb.federateLookahead;
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                updateCarAttributes(carList.get(carID), time);
            }
        });
    }

    private void sendInteractionChooseDistributor(int distributorID, int carID, HLAfloat64Time time) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);

        ParameterHandle parameterHandle = rtiamb.getParameterHandle(chooseDistributor, "DistributorID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(distributorID).toByteArray());

        parameterHandle = rtiamb.getParameterHandle(chooseDistributor, "CarID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Interval add = timeFactory.makeInterval(fedamb.federateLookahead);
        rtiamb.sendInteraction(chooseDistributor, parameters, generateTag(), time.add(add));

        log("Interaction Send: handle=" + chooseDistributor + " {CarChooseDistributor}, time=" + time.toString());
    }

    private void sendInteractionWantToPay(int carID) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);

        ParameterHandle parameterHandle = rtiamb.getParameterHandle(wantToPay, "CarID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + 2); // +1?
        rtiamb.sendInteraction(wantToPay, parameters, generateTag(), time);

        log("Interaction Send: handle=" + wantToPay + " {CarWantToPay}, time=" + time.toString());
    }

    private void updateCarAttributes(Car car, double time) throws RTIexception {
        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(2);
        attributes.put(carID, encoderFactory.createHLAinteger32BE(car.getCarID()).toByteArray());
        attributes.put(wantWash, encoderFactory.createHLAboolean(car.isWantWash()).toByteArray());
        attributes.put(payForWash, encoderFactory.createHLAboolean(car.isWantWash()).toByteArray());

        HLAfloat64Time theTime = timeFactory.makeTime(time + 1);
        log("Updated Car Attributes: " + car.toString() + "   time=" + theTime.toString());
        rtiamb.updateAttributeValues(car.getObjectHandle(), attributes, generateTag(), theTime);
    }

    public static void main(String[] args) {
        try {
            new CarFederate().runFederate("CarFederate");
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
