package gasStation.cash;

import gasStation.DefaultFederate;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.exceptions.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Michał on 2018-05-11.
 */
public class CashFederate extends DefaultFederate<CashFederateAmbassador> {

    protected Cash cash;
    protected ObjectClassHandle cashClassHandle;
    protected AttributeHandle queueSize;
    protected InteractionClassHandle cashServiceStart;
    protected InteractionClassHandle cashServiceFinish;

    @Override
    protected CashFederateAmbassador createFederateAmbassador() {
        return new CashFederateAmbassador(this);
    }

    @Override
    protected void mainSimulationLoop() {

    }

    @Override
    protected URL[] modulesToJoin() throws MalformedURLException {
        return new URL[]{
                (new File("foms/Cash.xml")).toURI().toURL()
        };
    }

    @Override
    protected void publishAndSubscribe() throws NameNotFound, NotConnected, RTIinternalError, FederateNotExecutionMember, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, SaveInProgress, InteractionClassNotDefined {
        // OBJECTS //
        cashClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Cash");
        queueSize = rtiamb.getAttributeHandle(cashClassHandle, "QueueLength");

        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
        attributes.add(queueSize);

        rtiamb.publishObjectClassAttributes(cashClassHandle, attributes);

        // INTERACTIONS //
        cashServiceStart = rtiamb.getInteractionClassHandle("HLAinteractionRoot.CashServiceStart");
        cashServiceFinish = rtiamb.getInteractionClassHandle("HLAinteractionRoot.CashServiceFinish");

        rtiamb.publishInteractionClass(cashServiceStart);
        rtiamb.publishInteractionClass(cashServiceFinish);
    }

    @Override
    protected void registerObjects() throws SaveInProgress, RestoreInProgress, ObjectClassNotPublished, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, NotConnected {
        rtiamb.registerObjectInstance(cashClassHandle);
        log("Registered Object, handle=" + cash.cashInstanceHandle);
    }

    @Override
    protected void deleteObjects() throws ObjectInstanceNotKnown, RestoreInProgress, DeletePrivilegeNotHeld, SaveInProgress, FederateNotExecutionMember, RTIinternalError, NotConnected {
        rtiamb.deleteObjectInstance(cash.cashInstanceHandle, generateTag());
    }

    @Override
    protected void enableTimePolicy() throws SaveInProgress, TimeConstrainedAlreadyEnabled, RestoreInProgress, NotConnected, CallNotAllowedFromWithinCallback, InTimeAdvancingState, RequestForTimeConstrainedPending, FederateNotExecutionMember, RTIinternalError, RequestForTimeRegulationPending, InvalidLookahead, TimeRegulationAlreadyEnabled {
        enableTimeRegulation();
        enableTimeConstrained();
    }

    @Override
    protected void log(String message) {
        System.out.println("CashFederate   : " + message);
    }
}
