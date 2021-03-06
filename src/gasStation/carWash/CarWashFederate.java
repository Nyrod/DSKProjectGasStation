package gasStation.carWash;

import gasStation.DefaultFederate;
import gasStation.Event;
import hla.rti1516e.*;
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
    protected InteractionClassHandle endSimulation;

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

    @Override
    protected void afterSimulationLoop() throws RTIexception {

    }

    public Event createUpdateCarInstanceEvent(AttributeHandleValueMap theAttributes) {
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                boolean wantWash = updateCarInstance(theAttributes);
                if (wantWash) {
                    if (carWash.nextServiceCar == 0 || carWash.nextServiceCar < fedamb.federateTime + fedamb.federateLookahead) {
                        carWash.nextServiceCar = fedamb.federateTime + fedamb.federateLookahead;
                    }
                    createStartServiceEvent(carWash.nextServiceCar);
                }
            }
        };
    }

    public Event createEndSimulationEvent() {
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                finishSimulation();
            }
        };
    }

    protected void createStartServiceEvent(double time) {
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                carWash.setCurrentServiceCarID(carWash.getCar());
                updateCarWashAttributes(time);
            }
        });
        carWash.nextServiceCar += 5;
        createFinishServiceEvent(carWash.nextServiceCar);
    }

    protected void createFinishServiceEvent(double time) {
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                carWash.setCurrentServiceCarID(-1);
                updateCarWashAttributes(time);
            }
        });
    }

    protected void createUpdateCarWashInstanceEvent(double time) {
        Random rand = new Random();
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                if (carWash.haveCarInQueue()) {
                    createUpdateCarWashInstanceEvent(time + rand.nextInt(10) + 5);
                }
                carWash.setCurrentServiceCarID(carWash.getCar());
                updateCarWashAttributes(time);
            }
        });
    }

    private boolean updateCarInstance(AttributeHandleValueMap theAttributes) {
        int carID = theAttributes.getValueReference(this.carID).getInt();
        boolean wantWash = theAttributes.getValueReference(this.wantWash).getInt() == 1;

        if (wantWash) {
            carWash.addCarToQueue(carID);
        }

        log("Updated Car Instance: carID=" + carID + ", wantWash=" + wantWash);

        return wantWash;
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

        carClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Car");
        carID = rtiamb.getAttributeHandle(carClassHandle, "CarID");
        wantWash = rtiamb.getAttributeHandle(carClassHandle, "WantWash");
        payForWash = rtiamb.getAttributeHandle(carClassHandle, "PayForWash");
        attributes.clear();
        attributes.add(carID);
        attributes.add(wantWash);
        attributes.add(payForWash);
        rtiamb.subscribeObjectClassAttributes(carClassHandle, attributes);

        endSimulation = rtiamb.getInteractionClassHandle("HLAinteractionRoot.EndSimulation");
        rtiamb.subscribeInteractionClass(endSimulation);
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
