package gasStation.distributor;

import gasStation.DefaultFederateAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.RTIexception;

public class DistributorFederateAmbassador extends DefaultFederateAmbassador<DistributorFederate> {

    public DistributorFederateAmbassador(DistributorFederate federate) {
        super(federate);
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime theTime, OrderType receivedOrdering, SupplementalReceiveInfo receiveInfo) throws FederateInternalError {
        StringBuilder log = new StringBuilder("Interaction Received:");
        log.append(" handle=" + interactionClass);

        if(interactionClass.equals(federate.chooseDistributor)) {
            receiveChooseDistributor(log, theParameters, userSuppliedTag, theTime);
        }
    }

    private void receiveChooseDistributor(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        try {
            externalEventList.add(federate.createAddToQueueCarEvent(theParameters));
            log.append(" {ChooseDistributor}");
            logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }

    @Override
    protected void log(String message) {
        System.out.println("DistributorFederateAmbassador   : " + message);
    }

}
