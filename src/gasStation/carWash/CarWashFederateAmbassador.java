package gasStation.carWash;

import gasStation.DefaultFederateAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;

/**
 * Created by Michał on 2018-05-11.
 */
public class CarWashFederateAmbassador extends DefaultFederateAmbassador<CarWashFederate> {

    public CarWashFederateAmbassador(CarWashFederate federate) {
        super(federate);
    }

    @Override
    public void receiveInteraction(InteractionClassHandle interactionClass, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime theTime, OrderType receivedOrdering, SupplementalReceiveInfo receiveInfo) throws FederateInternalError {
        StringBuilder log = new StringBuilder("Interaction Received:");
        log.append(" handle=" + interactionClass);
        if (interactionClass.equals(federate.endSimulation)) {
            receiveEndSimulation(log, theParameters, userSuppliedTag, theTime);
        }
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime theTime, OrderType receivedOrdering, SupplementalReflectInfo reflectInfo) throws FederateInternalError {
        externalEventList.add(federate.createUpdateCarInstanceEvent(theAttributes));
    }

    private void receiveEndSimulation(StringBuilder log, ParameterHandleValueMap theParameters, byte[] userSuppliedTag, LogicalTime theTime) {
        externalEventList.add(federate.createEndSimulationEvent());
        log.append(" {EndSimulation}");
        log(log.toString());

        //logReceiveInteraction(log, theParameters, userSuppliedTag, theTime);
    }

    @Override
    protected void log(String message) {
        System.out.println("CarWashFederateAmbassador: " + message);
    }
}
