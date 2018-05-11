package gasStation.distributor;

import gasStation.DefaultFederate;
import hla.rti1516e.exceptions.*;

import java.net.MalformedURLException;
import java.net.URL;

public class DistributorFederate extends DefaultFederate<DistributorFederateAmbassador> {
    @Override
    protected DistributorFederateAmbassador createFederateAmbassador() {
        return new DistributorFederateAmbassador(this);
    }

    @Override
    protected URL[] modulesToJoin() throws MalformedURLException {
        return new URL[0];
    }

    @Override
    protected void publishAndSubscribe() throws NameNotFound, NotConnected, RTIinternalError, FederateNotExecutionMember, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, SaveInProgress, InteractionClassNotDefined {

    }

    @Override
    protected void registerObjects() throws SaveInProgress, RestoreInProgress, ObjectClassNotPublished, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, NotConnected {

    }

    @Override
    protected void deleteObjects() throws ObjectInstanceNotKnown, RestoreInProgress, DeletePrivilegeNotHeld, SaveInProgress, FederateNotExecutionMember, RTIinternalError, NotConnected {

    }

    @Override
    protected void enableTimePolicy() throws SaveInProgress, TimeConstrainedAlreadyEnabled, RestoreInProgress, NotConnected, CallNotAllowedFromWithinCallback, InTimeAdvancingState, RequestForTimeConstrainedPending, FederateNotExecutionMember, RTIinternalError, RequestForTimeRegulationPending, InvalidLookahead, TimeRegulationAlreadyEnabled {

    }

    @Override
    protected void mainSimulationLoop() {

    }
}
