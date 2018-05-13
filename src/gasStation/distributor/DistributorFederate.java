package gasStation.distributor;

import gasStation.DefaultFederate;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Time;

import java.util.ArrayList;
import java.util.List;

public class DistributorFederate extends DefaultFederate<DistributorFederateAmbassador> {

    private List<Distributor> distributorList;

    private final double timeStep = 10.0;

    protected ObjectClassHandle distributorClassHandle;
    protected AttributeHandle distributorID;
    protected AttributeHandle distributorType;
    protected AttributeHandle queueSize;
    protected InteractionClassHandle distributorServiceStart;
    protected InteractionClassHandle distributorServiceFinish;

    public DistributorFederate() {
        distributorList = new ArrayList<>();
    }

    @Override
    protected DistributorFederateAmbassador createFederateAmbassador() {
        return new DistributorFederateAmbassador(this);
    }

    @Override
    protected void mainSimulationLoop() throws RTIexception {
        while (true) {
            double timeToAdvance = fedamb.federateTime + fedamb.federateLookahead;
            HLAfloat64Time time = timeFactory.makeTime(timeToAdvance);
            advanceTime(time);

            //sendInteractionDistributorServiceStart(2, 3);
            //sendInteractionDistributorServiceFinish(1, 2);
            updateDistributorAttributes(distributorList.get(0).getObjectInstanceHandle(), 1, "ON", 3);

            if(fedamb.grantedTime == timeToAdvance) {
                fedamb.federateTime = timeToAdvance;
            }
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

        rtiamb.publishInteractionClass(distributorServiceStart);
        rtiamb.publishInteractionClass(distributorServiceFinish);

        rtiamb.subscribeInteractionClass(distributorServiceStart);
        rtiamb.subscribeInteractionClass(distributorServiceFinish);
    }

    @Override
    protected void registerObjects() throws SaveInProgress, RestoreInProgress, ObjectClassNotPublished, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, NotConnected {
        for (int i = 0; i < Distributor.DISTRIBUTORS_IN_SIMULATION; i++) {
            Distributor distributor = new Distributor();
            distributor.setObjectInstanceHandle(rtiamb.registerObjectInstance(distributorClassHandle));
            distributorList.add(distributor);
            log("Registered Object, handle=" + distributor.getObjectInstanceHandle());
        }
    }

    @Override
    protected void deleteObjects() throws ObjectInstanceNotKnown, RestoreInProgress, DeletePrivilegeNotHeld, SaveInProgress, FederateNotExecutionMember, RTIinternalError, NotConnected {
        for (int i = Distributor.DISTRIBUTORS_IN_SIMULATION - 1; i >= 0; i--) {
            rtiamb.deleteObjectInstance(distributorList.remove(i).getObjectInstanceHandle(), generateTag());
        }
    }

    @Override
    protected void enableTimePolicy() throws SaveInProgress, TimeConstrainedAlreadyEnabled, RestoreInProgress, NotConnected, CallNotAllowedFromWithinCallback, InTimeAdvancingState, RequestForTimeConstrainedPending, FederateNotExecutionMember, RTIinternalError, RequestForTimeRegulationPending, InvalidLookahead, TimeRegulationAlreadyEnabled {
        enableTimeRegulation();
        enableTimeConstrained();
    }

    private void sendInteractionDistributorServiceStart(int distributorID, int carID) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);

//        ParameterHandle parameterHandle = rtiamb.getParameterHandle(distributorServiceStart, "DistributorID");
//        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(distributorID).toByteArray());
//
//        parameterHandle = rtiamb.getParameterHandle(distributorServiceStart, "CarID");
//        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + 2);
        rtiamb.sendInteraction(distributorServiceStart, parameters, generateTag(), time);

        log("Interaction Send: handle=" + distributorServiceStart + " {DistributorServiceStart}, time=" + time.toString());
    }

    private void sendInteractionDistributorServiceFinish(int distributorID, int carID) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);

//        ParameterHandle parameterHandle = rtiamb.getParameterHandle(distributorServiceFinish, "DistributorID");
//        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(distributorID).toByteArray());
//
//        parameterHandle = rtiamb.getParameterHandle(distributorServiceFinish, "CarID");
//        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime);
        rtiamb.sendInteraction(distributorServiceFinish, parameters, generateTag(), time);

        log("Interaction Send: handle=" + distributorServiceFinish + " {DistributorServiceStart}, time=" + time.toString());
    }

    private void updateDistributorAttributes( ObjectInstanceHandle objectHandle, int iDDistributor, String typeOfDistributor, int distributorQueueSize) throws RTIexception {

        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(2);
        attributes.put(distributorID, encoderFactory.createHLAinteger32BE(iDDistributor).toByteArray());
        attributes.put(distributorType , encoderFactory.createHLAunicodeString(typeOfDistributor).toByteArray());
        attributes.put(queueSize, encoderFactory.createHLAinteger32BE(distributorQueueSize).toByteArray());

        HLAfloat64Time time = timeFactory.makeTime( fedamb.federateTime+2);
        log("updateDistributorAttributes");

        rtiamb.updateAttributeValues( objectHandle, attributes, generateTag(), time);
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
