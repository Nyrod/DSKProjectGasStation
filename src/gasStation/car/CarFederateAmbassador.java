package gasStation.car;

import gasStation.DefaultFederateAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;

public class CarFederateAmbassador extends DefaultFederateAmbassador<CarFederate> {

    public CarFederateAmbassador(CarFederate federate) {
        super(federate);
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, SupplementalReceiveInfo receiveInfo) throws FederateInternalError {
        StringBuilder log = new StringBuilder("Interaction Received:");
        log.append(" handle=" + interactionClass);
        logReceiveInteraction(log, theParameters, userSuppliedTag, null);
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime theTime, OrderType receivedOrdering, MessageRetractionHandle retractionHandle, SupplementalReceiveInfo receiveInfo) throws FederateInternalError {
        StringBuilder log = new StringBuilder("Interaction Received:");
        log.append(" handle=" + interactionClass);
        logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime theTime, OrderType receivedOrdering, SupplementalReceiveInfo receiveInfo) throws FederateInternalError {
        StringBuilder log = new StringBuilder("Interaction Received:");
        log.append(" handle=" + interactionClass);

        if (interactionClass.equals(federate.distributorServiceStart)) {
            receiveDistributorServiceStart(log, theParameters, userSuppliedTag, theTime);
        } else if (interactionClass.equals(federate.distributorServiceFinish)) {
            receiveDistributorServiceFinish(log, theParameters, userSuppliedTag, theTime);
        } else if (interactionClass.equals(federate.cashServiceStart)) {
            receiveCashServiceStart(log, theParameters, userSuppliedTag, theTime);
        } else if (interactionClass.equals(federate.cashServiceFinish)) {
            receiveCashServiceFinish(log, theParameters, userSuppliedTag, theTime);
        }
    }

    private void receiveDistributorServiceStart(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        log.append(" {DistributorServiceStart}");
        logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }

    private void receiveDistributorServiceFinish(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        log.append(" {DistributorServiceFinish}");
        logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }

    private void receiveCashServiceStart(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        log.append(" {CashServiceStart}");
        logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }

    private void receiveCashServiceFinish(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        log.append(" {CashServiceFinish}");
        logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }

    @Override
    protected void log(String message) {
        System.out.println("CarFederateAmbassador: " + message);
    }
}
