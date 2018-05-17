package gasStation;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultFederateAmbassador<Federate extends DefaultFederate> extends NullFederateAmbassador {

    protected Federate federate;

    public double federateTime = 0.0;
    public double grantedTime = 0.0;
    public double federateLookahead = 1.0;

    protected boolean isRegulating = false;
    protected boolean isConstrained = false;
    protected boolean isAdvancing = false;

    protected boolean isAnnounced = false;
    protected boolean isReadyToRun = false;

    protected Map<ObjectInstanceHandle, ObjectClassHandle> externalObjectInstanceMap;
    public List<Event> externalEventList;

    public DefaultFederateAmbassador(Federate federate) {
        this.federate = federate;
        externalObjectInstanceMap = new HashMap<>();
        externalEventList = new ArrayList<>();
    }

    protected void log(String message) {
        System.out.println("DefaultFederateAmbassador: " + message);
    }

    @Override
    public void discoverObjectInstance(ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass, String objectName) throws FederateInternalError {
        log("Discover Object Instance: ObjectClassHandle=" + theObjectClass + " , ObjectInstanceHandle=" + theObject + ", ObjectName=" + objectName);
        externalObjectInstanceMap.putIfAbsent(theObject, theObjectClass);
    }

    @Override
    public void synchronizationPointRegistrationFailed(String label,
                                                       SynchronizationPointFailureReason reason) {
        log("Failed to register sync point: " + label + ", reason=" + reason);
    }

    @Override
    public void synchronizationPointRegistrationSucceeded(String label) {
        log("Successfully registered sync point: " + label);
    }

    @Override
    public void announceSynchronizationPoint(String label, byte[] tag) {
        log("Synchronization point announced: " + label);
        if (label.equals(DefaultFederate.READY_TO_RUN))
            this.isAnnounced = true;
    }

    @Override
    public void federationSynchronized(String label, FederateHandleSet failed) {
        log("Federation Synchronized: " + label);
        if (label.equals(DefaultFederate.READY_TO_RUN))
            this.isReadyToRun = true;
    }

    @Override
    public void timeRegulationEnabled(LogicalTime time) {
        this.federateTime = ((HLAfloat64Time) time).getValue();
        this.isRegulating = true;
    }

    @Override
    public void timeConstrainedEnabled(LogicalTime time) {
        this.federateTime = ((HLAfloat64Time) time).getValue();
        this.isConstrained = true;
    }

    public void timeAdvanceGrant(LogicalTime theTime) {
        this.grantedTime = ((HLAfloat64Time) theTime).getValue();
        this.isAdvancing = false;
    }

    @Override
    public void removeObjectInstance(ObjectInstanceHandle theObject, byte[] userSuppliedTag, OrderType sentOrdering, SupplementalRemoveInfo removeInfo) throws FederateInternalError {
        log("Object Removed: handle=" + theObject);
    }

    protected void logReceiveInteraction(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        log.append(", tag=" + new String(userSuppliedTag));
        log.append(", time=" + ((HLAfloat64Time) theTime).getValue());
        log.append("\n");
        for (ParameterHandle parameter : theParameters.keySet()) {
            log.append("\tparamHandle=");
            log.append(parameter);
            log.append(", paramValue=");
            log.append(theParameters.get(parameter).length);
            log.append(" bytes");
            log.append("\n");
        }
        log(log.toString());
    }

    protected void logReflectObject(StringBuilder builder, byte[] tag, LogicalTime time, AttributeHandleValueMap theAttributes) {
        builder.append(", tag=" + new String(tag));
        if (time != null) {
            builder.append(", time=" + ((HLAfloat64Time) time).getValue());
        }

        builder.append(", attributeCount=" + theAttributes.size());
        builder.append("\n");

        for (AttributeHandle attributeHandle : theAttributes.keySet()) {
            builder.append("\tattributeValue=");
            theAttributes.getValueReference(attributeHandle);
            builder.append("\n");
        }
        //log(builder.toString());
    }
}
