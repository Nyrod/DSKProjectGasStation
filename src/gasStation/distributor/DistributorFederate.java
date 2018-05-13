package gasStation.distributor;

import gasStation.DefaultFederate;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Time;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DistributorFederate extends DefaultFederate<DistributorFederateAmbassador> {

    private List<Distributor> distributorList;

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
        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + 5);
        fedamb.timeAdvanceGrant(time);
        sendInteractionDistributorServiceFinish(1,2);
        sendInteractionDistributorServiceStart(2, 3);
    }

    @Override
    protected URL[] modulesToJoin() throws MalformedURLException {
        return new URL[]{
                new File("foms/Distributor.xml").toURI().toURL()
        };
    }

    @Override
    protected void publishAndSubscribe() throws NameNotFound, NotConnected, RTIinternalError, FederateNotExecutionMember, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, SaveInProgress, InteractionClassNotDefined {
        // OBJECTS //
        distributorClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Distributor");
        distributorID = rtiamb.getAttributeHandle(distributorClassHandle, "DistributorID");
        distributorType = rtiamb.getAttributeHandle(distributorClassHandle, "DistributorType");
        queueSize = rtiamb.getAttributeHandle(distributorClassHandle, "QueueSize");

        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
        attributes.add(distributorID);
        attributes.add(distributorType);
        attributes.add(queueSize);

        rtiamb.publishObjectClassAttributes(distributorClassHandle, attributes);

        // INTERACTIONS //
        distributorServiceStart = rtiamb.getInteractionClassHandle("HLAinteractionRoot.DistributorServiceStart");
        distributorServiceFinish = rtiamb.getInteractionClassHandle("HLAinteractionRoot.DistributorServiceFinish");

        rtiamb.publishInteractionClass(distributorServiceStart);
        rtiamb.publishInteractionClass(distributorServiceFinish);
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
        enableTimeConstrained();
        enableTimeRegulation();
    }

    private void sendInteractionDistributorServiceStart(int distributorID, int carID) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);

        ParameterHandle parameterHandle = rtiamb.getParameterHandle(distributorServiceStart, "DistributorID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(distributorID).toByteArray());

        parameterHandle = rtiamb.getParameterHandle(distributorServiceStart, "CarID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + fedamb.federateLookahead);
        rtiamb.sendInteraction(distributorServiceStart, parameters, generateTag(), time);

        log("Interaction Send: handle=" + distributorServiceStart + " {DistributorServiceStart}, time=" + time.toString());
    }

    private void sendInteractionDistributorServiceFinish(int distributorID, int carID) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);

        ParameterHandle parameterHandle = rtiamb.getParameterHandle(distributorServiceFinish, "DistributorID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(distributorID).toByteArray());

        parameterHandle = rtiamb.getParameterHandle(distributorServiceFinish, "CarID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + fedamb.federateLookahead + fedamb.federateLookahead);
        rtiamb.sendInteraction(distributorServiceFinish, parameters, generateTag(), time);

        log("Interaction Send: handle=" + distributorServiceFinish + " {DistributorServiceStart}, time=" + time.toString());
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
