package gasStation.distributor;

import gasStation.DefaultFederateAmbassador;

public class DistributorFederateAmbassador extends DefaultFederateAmbassador<DistributorFederate> {

    public DistributorFederateAmbassador(DistributorFederate federate) {
        super(federate);
    }

    @Override
    protected void log(String message) {
        System.out.println("DistributorFederateAmbassador   : " + message);
    }
}
