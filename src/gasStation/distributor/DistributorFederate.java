package gasStation.distributor;

import gasStation.DefaultFederate;
import gasStation.Event;
import gasStation.TimedEventComparator;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DistributorFederate extends DefaultFederate<DistributorFederateAmbassador> {

    private List<Distributor> distributorList;
    private List<Event> internalEventList;

    private final double timeStep = 10.0;

    protected ObjectClassHandle distributorClassHandle;
    protected AttributeHandle distributorID;
    protected AttributeHandle distributorType;
    protected AttributeHandle queueSize;
    protected InteractionClassHandle distributorServiceStart;
    protected InteractionClassHandle distributorServiceFinish;

    protected InteractionClassHandle chooseDistributor;

    public DistributorFederate() {
        distributorList = new ArrayList<>();
        internalEventList = new ArrayList<>();
    }

    @Override
    protected DistributorFederateAmbassador createFederateAmbassador() {
        return new DistributorFederateAmbassador(this);
    }

    @Override
    protected void mainSimulationLoop() throws RTIexception {
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
        distributorClassHandle = rtiamb.getObjectClassHandle("ObjectRoot.Distributor");
        distributorID = rtiamb.getAttributeHandle(distributorClassHandle, "DistributorID");
        distributorType = rtiamb.getAttributeHandle(distributorClassHandle, "DistributorType");
        queueSize = rtiamb.getAttributeHandle(distributorClassHandle, "QueueSize");

        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
        attributes.add(distributorID);
        attributes.add(distributorType);
        attributes.add(queueSize);

        rtiamb.publishObjectClassAttributes(distributorClassHandle, attributes);

        // INTERACTIONS //
        distributorServiceStart = rtiamb.getInteractionClassHandle("InteractionRoot.DistributorServiceStart");
        distributorServiceFinish = rtiamb.getInteractionClassHandle("InteractionRoot.DistributorServiceFinish");
        chooseDistributor = rtiamb.getInteractionClassHandle("HLAinteractionRoot.ChooseDistributor");

        rtiamb.publishInteractionClass(distributorServiceStart);
        rtiamb.publishInteractionClass(distributorServiceFinish);

        rtiamb.subscribeInteractionClass(chooseDistributor);
    }

    @Override
    protected void registerObjects() throws SaveInProgress, RestoreInProgress, ObjectClassNotPublished, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, NotConnected {
        for (int i = 0; i < Distributor.DISTRIBUTORS_IN_SIMULATION; i++) {
            Distributor distributor = new Distributor();
            distributor.setObjectHandle(rtiamb.registerObjectInstance(distributorClassHandle));
            distributorList.add(distributor);
            log("Registered Object, handle=" + distributor.getObjectHandle());
        }
    }

    @Override
    protected void deleteObjects() throws ObjectInstanceNotKnown, RestoreInProgress, DeletePrivilegeNotHeld, SaveInProgress, FederateNotExecutionMember, RTIinternalError, NotConnected {
        for (int i = Distributor.DISTRIBUTORS_IN_SIMULATION - 1; i >= 0; i--) {
            rtiamb.deleteObjectInstance(distributorList.remove(i).getObjectHandle(), generateTag());
        }
    }

    @Override
    protected void enableTimePolicy() throws SaveInProgress, TimeConstrainedAlreadyEnabled, RestoreInProgress, NotConnected, CallNotAllowedFromWithinCallback, InTimeAdvancingState, RequestForTimeConstrainedPending, FederateNotExecutionMember, RTIinternalError, RequestForTimeRegulationPending, InvalidLookahead, TimeRegulationAlreadyEnabled {
        enableTimeRegulation();
        enableTimeConstrained();
    }

    public void addInternalEventStartService(int distributorID, int carID) {
        Random rand = new Random();
        double time = fedamb.federateTime + rand.nextInt(15) + fedamb.federateLookahead;
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                double finishTime = time + rand.nextInt(30);
                sendInteractionDistributorServiceStart(distributorID, carID);
                addInternalEventFinishService(distributorID, carID, finishTime);
            }
        });
    }

    public void addInternalEventFinishService(int distributorID, int carID, double time) {
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                sendInteractionDistributorServiceFinish(distributorID, carID, time);
            }
        });
    }

    private void sendInteractionDistributorServiceStart(int distributorID, int carID) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);

        ParameterHandle parameterHandle = rtiamb.getParameterHandle(distributorServiceStart, "DistributorID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(distributorID).toByteArray());

        parameterHandle = rtiamb.getParameterHandle(distributorServiceStart, "CarID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + 2);
        rtiamb.sendInteraction(distributorServiceStart, parameters, generateTag(), time);

        log("Interaction Send: handle=" + distributorServiceStart + " {DistributorServiceStart}, time=" + time.toString());
    }

    private void sendInteractionDistributorServiceFinish(int distributorID, int carID, double time) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);

        ParameterHandle parameterHandle = rtiamb.getParameterHandle(distributorServiceFinish, "DistributorID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(distributorID).toByteArray());

        parameterHandle = rtiamb.getParameterHandle(distributorServiceFinish, "CarID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Time theTime = timeFactory.makeTime(time + fedamb.federateLookahead);
        rtiamb.sendInteraction(distributorServiceFinish, parameters, generateTag(), theTime);

        log("Interaction Send: handle=" + distributorServiceFinish + " {DistributorServiceFinish}, time=" + theTime.toString());
    }

    private void updateDistributorAttributes( ObjectInstanceHandle objectHandle, int iDDistributor, String typeOfDistributor, int distributorQueueSiz) throws RTIexception {

        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(2);
        attributes.put(distributorID, encoderFactory.createHLAinteger32BE(iDDistributor).toByteArray());
        attributes.put(distributorType , encoderFactory.createHLAunicodeString(typeOfDistributor).toByteArray());
        attributes.put(queueSize, encoderFactory.createHLAinteger32BE(distributorQueueSiz).toByteArray());

        HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime+fedamb.federateLookahead );

        rtiamb.updateAttributeValues( objectHandle, attributes, generateTag(), time );
    }

    @Override
    protected void log(String message) {
        System.out.println("DistributorFederate   : " + message);
    }

    public static void main(String[] args) {
        try {
            new DistributorFederate().runFederate("DistributorFederate", "DistributorFederateType");
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
