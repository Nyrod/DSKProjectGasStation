package gasStation.car;

import gasStation.DefaultFederateAmbassador;

public class CarFederateAmbassador extends DefaultFederateAmbassador<CarFederate> {

    public CarFederateAmbassador(CarFederate federate) {
        super(federate);
    }

    @Override
    protected void log(String message) {
        System.out.println("CarFederateAmbassador: " + message);
    }
}
