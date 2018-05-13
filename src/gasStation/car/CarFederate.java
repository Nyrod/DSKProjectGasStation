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
    private List<Event> internalEventList;

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
        this.carList = new ArrayList<>();
        this.internalEventList = new ArrayList<>();
    }

    @Override
    protected CarFederateAmbassador createFederateAmbassador() {
        return new CarFederateAmbassador(this);
    }

    @Override
    protected void mainSimulationLoop() throws RTIexception {
        createChooseDistiributorInternalEvent();
        while (true) {
            double timeToAdvance = fedamb.federateTime + fedamb.federateLookahead;
            HLAfloat64Time nextEventTime = timeFactory.makeTime(timeToAdvance);
            advanceTime(nextEventTime);

            if(!internalEventList.isEmpty()) {
                internalEventList.sort(new TimedEventComparator());
                nextEventTime = internalEventList.get(0).getTime().add(timeFactory.makeInterval(1));
//                if((nextEventTime.getValue() - fedamb.federateTime) > fedamb.federateLookahead) {
//                    advanceTime(timeFactory.makeTime(fedamb.federateTime + 2*fedamb.federateLookahead));
//                }
                if(fedamb.federateTime <= nextEventTime.getValue()) {
                    internalEventList.remove(0).runEvent();
                    advanceTime(nextEventTime);
                    fedamb.federateTime = nextEventTime.getValue();
                } else {
                    internalEventList.remove(0);
                }
            }
//            if(!internalEventList.isEmpty()) {
//                internalEventList.sort(new TimedEventComparator());
//                nextEventTime = internalEventList.get(0).getTime().add(timeFactory.makeInterval(1));
//                internalEventList.remove(0).runEvent();
//                advanceTime(nextEventTime);
//            }

            if(fedamb.grantedTime == timeToAdvance) {
                fedamb.federateTime = timeToAdvance;
                log("Time advanced to: " + timeToAdvance);
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
    protected void registerObjects() throws SaveInProgress, RestoreInProgress, ObjectClassNotPublished, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, NotConnected {
        carList = new ArrayList<>();
        for (int i = 0; i < Car.CARS_IN_SIMULATION; i++) {
            Car carToAdd = Car.createCar();
            carToAdd.setObjectHandle(rtiamb.registerObjectInstance(carHandle));
            carList.add(carToAdd);
            log("Registered Object, handle=" + carToAdd.getObjectHandle());
        }
    }

    @Override
    protected void deleteObjects() throws ObjectInstanceNotKnown, RestoreInProgress, DeletePrivilegeNotHeld, SaveInProgress, FederateNotExecutionMember, RTIinternalError, NotConnected {
        for (int i = Car.CARS_IN_SIMULATION -1; i >= 0; i--) {
            rtiamb.deleteObjectInstance(carList.remove(i).getObjectHandle(), generateTag());
        }
    }

    @Override
    protected void enableTimePolicy() throws SaveInProgress, TimeConstrainedAlreadyEnabled, RestoreInProgress, NotConnected, CallNotAllowedFromWithinCallback, InTimeAdvancingState, RequestForTimeConstrainedPending, FederateNotExecutionMember, RTIinternalError, RequestForTimeRegulationPending, InvalidLookahead, TimeRegulationAlreadyEnabled {
        enableTimeRegulation();
        enableTimeConstrained();
    }

    @Override
    protected void log(String message) {
        System.out.println("CarFederate   : " + message);
    }

    private void createChooseDistiributorInternalEvent() throws RTIexception {
        Random rand = new Random();
        double nextTime = fedamb.federateTime;
        for(int i = 0; i < Car.CARS_IN_SIMULATION; i++) {
            nextTime += rand.nextInt(15) + 1;
            internalEventList.add(new Event(timeFactory.makeTime(nextTime)) {
                @Override
                public void runEvent() throws RTIexception {
                    sendInteractionChooseDistributor(2, 3, getTime());
                }
            });
        }
    }

    private void sendInteractionChooseDistributor(int distributorID, int carID, HLAfloat64Time time) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);

        ParameterHandle parameterHandle = rtiamb.getParameterHandle(chooseDistributor, "DistributorID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(distributorID).toByteArray());

        parameterHandle = rtiamb.getParameterHandle(chooseDistributor, "CarID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Interval add = timeFactory.makeInterval(1);
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

    private void updateCarAttributes( ObjectInstanceHandle objectHandle, int idCar, boolean wantToWash, boolean payWash) throws RTIexception {

        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(2);
        attributes.put(carID, encoderFactory.createHLAinteger32BE(idCar).toByteArray());
        attributes.put(wantWash , encoderFactory.createHLAboolean(wantToWash).toByteArray());
        attributes.put(payForWash, encoderFactory.createHLAboolean(payWash).toByteArray());

        HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime+fedamb.federateLookahead);

        rtiamb.updateAttributeValues( objectHandle, attributes, generateTag(), time );
    }


    public static void main(String[] args) {
        try {
            new CarFederate().runFederate("CarFederate", "CarFederateType");
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
