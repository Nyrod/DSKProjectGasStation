package gasStation.cash;

import gasStation.DefaultFederateAmbassador;

/**
 * Created by Michał on 2018-05-11.
 */
public class CashFederateAmbassador extends DefaultFederateAmbassador<CashFederate> {

    public CashFederateAmbassador(CashFederate federate) {
        super(federate);
    }

    @Override
    protected void log(String message) {
        System.out.println("CashFederateAmbassador: " + message);
    }
}
