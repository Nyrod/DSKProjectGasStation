package gasStation.distributor;

import gasStation.DefaultFederateAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;

public class DistributorFederateAmbassador extends DefaultFederateAmbassador<DistributorFederate> {

    public DistributorFederateAmbassador(DistributorFederate federate) {
        super(federate);
    }

    @Override
    protected void log(String message) {
        System.out.println("DistributorFederateAmbassador   : " + message);
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime theTime, OrderType receivedOrdering, SupplementalReceiveInfo receiveInfo) throws FederateInternalError {
        System.out.println("Interaction Received:");
    }

    private void receiveDistributorServiceStart(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        log.append(" {DistributorServiceStart}");
        logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }

    private void receiveDistributorServiceFinish(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        log.append(" {DistributorServiceFinish}");
        logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }

}
