package gasStation.car;

import gasStation.DefaultFederateAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.exceptions.RTIexception;

public class CarFederateAmbassador extends DefaultFederateAmbassador<CarFederate> {

    public CarFederateAmbassador(CarFederate federate) {
        super(federate);
    }


    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] tag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime time, OrderType receivedOrdering, SupplementalReflectInfo reflectInfo) throws FederateInternalError {
        StringBuilder builder = new StringBuilder("Reflection for object:");
        builder.append(" handle=" + theObject);
        if (externalObjectInstanceMap.get(theObject).equals(federate.distributorHandle)) {
            reflectDistributor(builder, tag, time, theAttributes);
        } else if (externalObjectInstanceMap.get(theObject).equals(federate.carWashClassHandle)) {
            reflectCarWash(builder, tag, time, theAttributes);
        }
    }

    private void reflectDistributor(StringBuilder log, byte[] tag, LogicalTime time, AttributeHandleValueMap theAttributes) {
        log.append(" {DistributorObject");
        logReflectObject(log, tag, time, theAttributes);
        externalEventList.add(federate.createUpdateDistributorInstanceEvent(theAttributes));
    }

    private void reflectCarWash(StringBuilder log, byte[] tag, LogicalTime time, AttributeHandleValueMap theAttributes) {
        log.append(" {DistributorObject");
        logReflectObject(log, tag, time, theAttributes);
        externalEventList.add(federate.createUpdateCarWashInstanceEvent(theAttributes));
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
        try {
            externalEventList.add(federate.createDistributorStartServiceEvent(theParameters));
            log.append(" {DistributorServiceStart}");
            logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }

    private void receiveDistributorServiceFinish(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        try {
            externalEventList.add(federate.createDistributorFinishServiceEvent(theParameters));
            log.append(" {DistributorServiceFinish}");
            logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }

    private void receiveCashServiceStart(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        try {
            externalEventList.add(federate.createCashStartServiceEvent(theParameters));
            log.append(" {CashServiceStart}");
            logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }

    private void receiveCashServiceFinish(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        try {
            externalEventList.add(federate.createCashFinishServiceEvent(theParameters));
            log.append(" {CashServiceFinish}");
            logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
    }

    @Override
    protected void log(String message) {
        System.out.println("CarFederateAmbassador: " + message);
    }
}
