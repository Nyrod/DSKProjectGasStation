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

    @Override
    public void reflectAttributeValues( ObjectInstanceHandle theObject,
                                        AttributeHandleValueMap theAttributes,
                                        byte[] tag,
                                        OrderType sentOrder,
                                        TransportationTypeHandle transport,
                                        SupplementalReflectInfo reflectInfo )
            throws FederateInternalError
    {
        // just pass it on to the other method for printing purposes
        // passing null as the time will let the other method know it
        // it from us, not from the RTI
        reflectAttributeValues( theObject,
                theAttributes,
                tag,
                sentOrder,
                transport,
                null,
                sentOrder,
                reflectInfo );
    }

    @Override
    public void reflectAttributeValues (ObjectInstanceHandle theObject,
                                        AttributeHandleValueMap theAttributes,
                                        byte[] tag,
                                        OrderType sentOrdering,
                                        TransportationTypeHandle theTransport,
                                        LogicalTime time,
                                        OrderType receivedOrdering,
                                        SupplementalReflectInfo reflectInfo)
            throws FederateInternalError
    {
        StringBuilder builder = new StringBuilder( "Reflection for object:" );
        builder.append( " handle=" + theObject );
        reflectDistributor(builder, tag, time, theAttributes);
    }

    private void reflectDistributor(StringBuilder log, byte[] tag, LogicalTime time, AttributeHandleValueMap theAttributes) {
        log.append(" {DistributorObject");
        logReflectObject(log, tag, time, theAttributes);
    }


    private void receiveDistributorServiceStart(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        log.append(" {DistributorServiceStart}");
        logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }

    private void receiveDistributorServiceFinish(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        log.append(" {DistributorServiceFinish}");
        logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
        try {
            federate.addUpdateCarAttributeInternalEvent();
        } catch (RTIexception rtIexception) {
            rtIexception.printStackTrace();
        }
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
