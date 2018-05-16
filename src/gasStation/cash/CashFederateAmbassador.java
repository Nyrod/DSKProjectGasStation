package gasStation.cash;

import gasStation.DefaultFederateAmbassador;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.FederateInternalError;

/**
 * Created by Michał on 2018-05-11.
 */
public class CashFederateAmbassador extends DefaultFederateAmbassador<CashFederate> {

    public CashFederateAmbassador(CashFederate federate) {
        super(federate);
    }

    @Override
    public void reflectAttributeValues(ObjectInstanceHandle theObject, AttributeHandleValueMap theAttributes, byte[] userSuppliedTag, OrderType sentOrdering, TransportationTypeHandle theTransport, LogicalTime theTime, OrderType receivedOrdering, SupplementalReflectInfo reflectInfo) throws FederateInternalError {
        externalEventList.add(federate.createUpdateCarInstanceEvent(theAttributes));
        log("Updated object");
    }

    @Override
    protected void log(String message) {
        System.out.println("CashFederateAmbassador: " + message);
    }
}
