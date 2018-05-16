package gasStation.cash;

import gasStation.DefaultFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.FederateInternalError;

/**
 * Created by Michał on 2018-05-11.
 */
public class CashFederateAmbassador extends DefaultFederateAmbassador<CashFederate> {

    public CashFederateAmbassador(CashFederate federate) {
        super(federate);
    }

    @Override
    public void discoverObjectInstance(ObjectInstanceHandle theObject, ObjectClassHandle theObjectClass, String objectName) throws FederateInternalError {
        super.discoverObjectInstance(theObject, theObjectClass, objectName);
        if (theObjectClass.equals(federate.carClassHandle))
            federate.addDiscoverCarInstance(theObject);
    }

    @Override
    protected void log(String message) {
        System.out.println("CashFederateAmbassador: " + message);
    }
}
