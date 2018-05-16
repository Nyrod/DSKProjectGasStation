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
    public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime theTime, OrderType receivedOrdering, SupplementalReflectInfo reflectInfo) throws FederateInternalError {
        externalEventList.add(federate.createUpdateCarInstanceEvent(theAttributes));
    }

    @Override
    protected void log(String message) {
        System.out.println("CarWashFederateAmbassador: " + message);
    }
}
