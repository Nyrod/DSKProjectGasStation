package gasStation.statistics;

import gasStation.DefaultFederateAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.RTIexception;

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
        } else if (interactionClass.equals(federate.cashServiceStart)) {
            receiveCashServiceStart(log, theParameters, userSuppliedTag, theTime);
        } else if (interactionClass.equals(federate.endSimulation)) {
            receiveEndSimulation(log, theParameters, userSuppliedTag, theTime);
        }
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, SupplementalReflectInfo reflectInfo) throws FederateInternalError {
        StringBuilder builder = new StringBuilder("Reflection for object:");
        builder.append(" handle=" + theObject);
        if (externalObjectInstanceMap.get(theObject).equals(federate.distributorHandle)) {
            reflectDistributor(builder, tag, time, theAttributes);
        } else if (externalObjectInstanceMap.get(theObject).equals(federate.carClassHandle)) {
            reflectCar(builder, tag, time, theAttributes);
        } else if (externalObjectInstanceMap.get(theObject).equals(federate.carWashHandle)) {

        }
    }

    private void reflectDistributor(StringBuilder log, byte[] tag, LogicalTime time, AttributeHandleValueMap theAttributes) {
        log.append(" {DistributorObject in Statistics");
        log(log.toString());
        //logReflectObject(log, tag, time, theAttributes);
    }

    private void reflectCarWash(StringBuilder log, byte[] tag, LogicalTime time, AttributeHandleValueMap theAttributes) {
        log.append(" {CarWashObject in Statistics");
        log(log.toString());
        //logReflectObject(log, tag, time, theAttributes);
    }

    private void reflectCar(StringBuilder log, byte[] tag, LogicalTime time, AttributeHandleValueMap theAttributes) {
        externalEventList.add(federate.createUpdateCarInstanceEvent(theAttributes));
        log.append(" {CarObject in Statistics");
        log(log.toString());
        //logReflectObject(log, tag, time, theAttributes);
    }

    private void receiveDistributorServiceStart(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        try {
            externalEventList.add(federate.createDistributorStartServiceEvent(theParameters, theTime));
            log.append(" {DistributorServiceStart}");
            log(log.toString());
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
        //logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }

    private void receiveDistributorServiceFinish(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        try {
            externalEventList.add(federate.createDistributorFinishServiceEvent(theParameters, theTime));
            log.append(" {DistributorServiceFinish}");
            log(log.toString());
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
        //logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }

    private void receiveWantToPay(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        try {
            externalEventList.add(federate.createAddCarToCashQueueEvent(theParameters, theTime));
            log.append(" {WantToPay}");
            log(log.toString());
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
        //logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }

    private void receiveChooseDistributor(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        try {
            externalEventList.add(federate.createAddCarToDistributorQueueEvent(theParameters, theTime));
            log.append(" {ChooseDistributor}");
            log(log.toString());
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
        //logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }

    private void receiveCashServiceStart(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        try {
            externalEventList.add(federate.createCashStartServiceEvent(theParameters, theTime));
            log.append(" {CashServiceStart}");
            log(log.toString());
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
        //logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }

    private void receiveEndSimulation(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        externalEventList.add(federate.createEndSimulationEvent());
        log.append(" {EndSimulation}");
        log(log.toString());

        //logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }
}
