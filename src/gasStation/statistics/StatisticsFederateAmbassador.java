package gasStation.statistics;

import gasStation.DefaultFederateAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;

/**
 * Created by Michał on 2018-05-11.
 */
public class StatisticsFederateAmbassador extends DefaultFederateAmbassador<StatisticsFederate> {

    public StatisticsFederateAmbassador(StatisticsFederate federate) {
        super(federate);
    }

    @Override
    protected void log(String message) {
        System.out.println("StatisticsFederateAmbassador: " + message);
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime theTime, OrderType receivedOrdering, SupplementalReceiveInfo receiveInfo) throws FederateInternalError {
        StringBuilder log = new StringBuilder("Interaction Received:");
        log.append(" handle=" + interactionClass);

        if (interactionClass.equals(federate.chooseDistributor)) {
            receiveChooseDistributor(log, theParameters, userSuppliedTag, theTime);
        } else if (interactionClass.equals(federate.distributorServiceStart)) {
            receiveDistributorServiceStart(log, theParameters, userSuppliedTag, theTime);
        } else if (interactionClass.equals(federate.distributorServiceFinish)) {
            receiveDistributorServiceFinish(log, theParameters, userSuppliedTag, theTime);
        } else if (interactionClass.equals(federate.wantToPay)) {
            receiveWantToPay(log, theParameters, userSuppliedTag, theTime);
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

    private void receiveWantToPay(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        log.append(" {WantToPay}");
        logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }

    private void receiveChooseDistributor(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        log.append(" {ChooseDistributor}");
        logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }
}
