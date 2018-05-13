package gasStation.carWash;

import gasStation.DefaultFederate;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.exceptions.*;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Michał on 2018-05-11.
 */
public class CarWashFederate extends DefaultFederate<CarWashFederateAmbassador> {

    protected CarWash carWash;
    protected ObjectClassHandle carWashClassHandle;
    protected AttributeHandle queueSize;
    protected AttributeHandle isFree;
    protected ObjectClassHandle carClassHandle;
    protected AttributeHandle carId;
    protected AttributeHandle wantWash;
    protected AttributeHandle payForWash;

    public CarWashFederate() {
        this.carWash = new CarWash();
    }

    @Override
    protected CarWashFederateAmbassador createFederateAmbassador() {
        return new CarWashFederateAmbassador(this);
    }

    @Override
    protected void mainSimulationLoop() {

    }

    @Override
    protected URL[] modulesToJoin() throws MalformedURLException {
        return new URL[]{
                (new File("foms/CarWash.xml")).toURI().toURL()
        };
    }

    @Override
    protected void publishAndSubscribe() throws NameNotFound, NotConnected, RTIinternalError, FederateNotExecutionMember, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, SaveInProgress, InteractionClassNotDefined {
        // OBJECTS //
        carWashClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.CarWash");
        queueSize = rtiamb.getAttributeHandle(carWashClassHandle, "QueueSize");
        isFree = rtiamb.getAttributeHandle(carWashClassHandle, "IsFree");

        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
        attributes.add(queueSize);
        attributes.add(isFree);

        rtiamb.publishObjectClassAttributes(carWashClassHandle, attributes);

        attributes.clear();

        carClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Car");
        carId = rtiamb.getAttributeHandle(carClassHandle, "CarID");
        wantWash = rtiamb.getAttributeHandle(carClassHandle, "WantWash");
        payForWash = rtiamb.getAttributeHandle(carClassHandle, "PayForWash");
        rtiamb.subscribeObjectClassAttributes(carClassHandle, attributes);
    }

    @Override
    protected void registerObjects() throws SaveInProgress, RestoreInProgress, ObjectClassNotPublished, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, NotConnected {
        carWash.carWashInstanceHandle = rtiamb.registerObjectInstance(carWashClassHandle);
        log("Registered Object, handle=" + carWash.carWashInstanceHandle);
    }

    @Override
    protected void deleteObjects() throws ObjectInstanceNotKnown, RestoreInProgress, DeletePrivilegeNotHeld, SaveInProgress, FederateNotExecutionMember, RTIinternalError, NotConnected {
            rtiamb.deleteObjectInstance(carWash.carWashInstanceHandle, generateTag());
    }

    @Override
    protected void enableTimePolicy() throws SaveInProgress, TimeConstrainedAlreadyEnabled, RestoreInProgress, NotConnected, CallNotAllowedFromWithinCallback, InTimeAdvancingState, RequestForTimeConstrainedPending, FederateNotExecutionMember, RTIinternalError, RequestForTimeRegulationPending, InvalidLookahead, TimeRegulationAlreadyEnabled {
        enableTimeRegulation();
        enableTimeConstrained();
    }

    @Override
    protected void log(String message) {
        System.out.println("CarWashFederate   : " + message);
    }

    public static void main(String[] args) {
        try {
            new CarWashFederate().runFederate("CarWashFederate", "CarWashFederateType");
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
