package gasStation.carWash;

import gasStation.DefaultFederate;
import gasStation.Event;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Time;

import java.util.Random;

/**
 * Created by Michał on 2018-05-11.
 */
public class CarWashFederate extends DefaultFederate<CarWashFederateAmbassador> {

    protected CarWash carWash;

    protected ObjectClassHandle carWashClassHandle;
    protected AttributeHandle queueSize;
    protected AttributeHandle currentServiceCarID;

    protected ObjectClassHandle carClassHandle;
    protected AttributeHandle carID;
    protected AttributeHandle wantWash;
    protected AttributeHandle payForWash;

    public CarWashFederate() {
        super();
        this.carWash = new CarWash();
    }

    @Override
    protected CarWashFederateAmbassador createFederateAmbassador() {
        return new CarWashFederateAmbassador(this);
    }

    @Override
    protected void beforeSimulationLoop() {

    }

    public Event createUpdateCarInstanceEvent(AttributeHandleValueMap theAttributes) {
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                updateCarInstance(theAttributes);
                if (carWash.haveCarInQueue() && carWash.getCurrentServiceCarID() == -1) {
                    createUpdateCarWashInstanceEvent(fedamb.federateTime + fedamb.federateLookahead);
                }
            }
        };
    }

    protected void createUpdateCarWashInstanceEvent(double time) {
        Random rand = new Random();
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                carWash.setCurrentServiceCarID(carWash.getCar());
                updateCarWashAttributes(time);
                if (carWash.haveCarInQueue()) {
                    createUpdateCarWashInstanceEvent(time + rand.nextInt(10) + 5);
                }
            }
        });
    }

    private void updateCarInstance(AttributeHandleValueMap theAttributes) {
        int carID = theAttributes.getValueReference(this.carID).getInt();
        boolean wantWash = theAttributes.getValueReference(this.wantWash).getInt() == 1;

        if (wantWash) {
            carWash.addCarToQueue(carID);
        }

        log("Updated Car Instance: carID=" + carID + ", wantWash=" + wantWash);
    }

    private void updateCarWashAttributes(double time) throws RTIexception {
        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(2);
        attributes.put(queueSize, encoderFactory.createHLAinteger32BE(carWash.getQueueSize()).toByteArray());
        attributes.put(currentServiceCarID, encoderFactory.createHLAinteger32BE(carWash.getCurrentServiceCarID()).toByteArray());

        HLAfloat64Time theTime = timeFactory.makeTime(time + fedamb.federateLookahead);
        rtiamb.updateAttributeValues(carWash.getCarWashInstanceHandle(), attributes, generateTag(), theTime);

        log("Updated CarWash Attributes: " + carWash.toString() + ", time=" + theTime.toString());
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        // OBJECTS //
        carWashClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.CarWash");
        queueSize = rtiamb.getAttributeHandle(carWashClassHandle, "QueueSize");
        currentServiceCarID = rtiamb.getAttributeHandle(carWashClassHandle, "CurrentServiceCarID");

        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
        attributes.add(queueSize);
        attributes.add(currentServiceCarID);

        rtiamb.publishObjectClassAttributes(carWashClassHandle, attributes);

        attributes.clear();

        carClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Car");
        carID = rtiamb.getAttributeHandle(carClassHandle, "CarID");
        wantWash = rtiamb.getAttributeHandle(carClassHandle, "WantWash");
        payForWash = rtiamb.getAttributeHandle(carClassHandle, "PayForWash");
        rtiamb.subscribeObjectClassAttributes(carClassHandle, attributes);
    }

    @Override
    protected void registerObjects() throws RTIexception {
        carWash.carWashInstanceHandle = rtiamb.registerObjectInstance(carWashClassHandle);
        updateCarWashAttributes(fedamb.federateTime + fedamb.federateLookahead);
        log("Registered Object, handle=" + carWash.carWashInstanceHandle);
    }

    @Override
    protected void deleteObjects() throws RTIexception {
        rtiamb.deleteObjectInstance(carWash.carWashInstanceHandle, generateTag());
    }

    @Override
    protected void enableTimePolicy() throws RTIexception {
        enableTimeRegulation();
        enableTimeConstrained();
    }

    @Override
    protected void log(String message) {
        System.out.println("CarWashFederate   : " + message);
    }

    public static void main(String[] args) {
        try {
            new CarWashFederate().runFederate("CarWashFederate");
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
