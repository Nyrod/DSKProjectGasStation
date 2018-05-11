package gasStation.carWash;

import gasStation.DefaultFederateAmbassador;

/**
 * Created by Michał on 2018-05-11.
 */
public class CarWashFederateAmbassador extends DefaultFederateAmbassador<CarWashFederate> {

    public CarWashFederateAmbassador(CarWashFederate federate) {
        super(federate);
    }

    @Override
    protected void log(String message) {
        System.out.println("CarWashFederateAmbassador: " + message);
    }
}
