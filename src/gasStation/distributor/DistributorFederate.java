package gasStation.distributor;

import gasStation.DefaultFederate;
import gasStation.Event;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DistributorFederate extends DefaultFederate<DistributorFederateAmbassador> {

    private List<Distributor> distributorList;

    private final double timeStep = 10.0;

    protected ObjectClassHandle distributorClassHandle;
    protected AttributeHandle distributorID;
    protected AttributeHandle distributorType;
    protected AttributeHandle queueSize;
    protected InteractionClassHandle distributorServiceStart;
    protected InteractionClassHandle distributorServiceFinish;

    protected InteractionClassHandle chooseDistributor;

    public DistributorFederate() {
        super();
        distributorList = new ArrayList<>();
    }

    @Override
    protected DistributorFederateAmbassador createFederateAmbassador() {
        return new DistributorFederateAmbassador(this);
    }

    @Override
    protected void beforeSimulationLoop() throws RTIexception {

    }

    public Event createAddToQueueCarEvent(ParameterHandleValueMap theParameters) throws RTIexception{
        ParameterHandle distributorIDParameter = rtiamb.getParameterHandle(chooseDistributor, "DistributorID");
        ParameterHandle carIDParameter = rtiamb.getParameterHandle(chooseDistributor, "CarID");
        int distributorID = theParameters.getValueReference(distributorIDParameter).getInt();
        int carID = theParameters.getValueReference(carIDParameter).getInt();
        Distributor distributor = getDistributorByID(distributorID);
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                if(!distributor.haveCarInQueue()) {
                    createStartServiceEvent(distributor.getDistributorID(), fedamb.federateTime + fedamb.federateLookahead);
                }
                distributor.addCar(carID);
                createUpdateDistributorInstanceEvent(distributor, fedamb.federateTime + fedamb.federateLookahead);
            }
        };
    }

    public void createUpdateDistributorInstanceEvent(Distributor distributor, double time) {
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                updateDistributorAttributes(distributor, time);
            }
        });
    }

    public void createStartServiceEvent(int distributorID, double time) {
        Random rand = new Random();
        Distributor distributor = getDistributorByID(distributorID);
        int carID = distributor.getCar();
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                sendInteractionDistributorServiceStart(distributor.getDistributorID(), carID, time);
                updateDistributorAttributes(distributor, time);
            }
        });
        createFinishServiceEvent(distributor, carID, time + rand.nextInt(20) + 10);
    }

    public void createFinishServiceEvent(Distributor distributor, int carID, double time) {
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                sendInteractionDistributorServiceFinish(distributor.getDistributorID(), carID, time);
                if (distributor.haveCarInQueue()) {
                    createStartServiceEvent(distributor.getDistributorID(), time + fedamb.federateLookahead);
                }
            }
        });
    }

    private void sendInteractionDistributorServiceStart(int distributorID, int carID, double time) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);

        ParameterHandle parameterHandle = rtiamb.getParameterHandle(distributorServiceStart, "DistributorID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(distributorID).toByteArray());

        parameterHandle = rtiamb.getParameterHandle(distributorServiceStart, "CarID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Time theTime = timeFactory.makeTime(time);
        rtiamb.sendInteraction(distributorServiceStart, parameters, generateTag(), theTime);

        log("Interaction Send: handle=" + distributorServiceStart + " {DistributorServiceStart}, time=" + theTime.toString());
    }

    private void sendInteractionDistributorServiceFinish(int distributorID, int carID, double time) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(2);

        ParameterHandle parameterHandle = rtiamb.getParameterHandle(distributorServiceFinish, "DistributorID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(distributorID).toByteArray());

        parameterHandle = rtiamb.getParameterHandle(distributorServiceFinish, "CarID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Time theTime = timeFactory.makeTime(time);
        rtiamb.sendInteraction(distributorServiceFinish, parameters, generateTag(), theTime);

        log("Interaction Send: handle=" + distributorServiceFinish + " {DistributorServiceFinish}, time=" + theTime.toString());
    }

    private void updateDistributorAttributes(Distributor distributor, double time) throws RTIexception {

        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(2);
        attributes.put(distributorID, encoderFactory.createHLAinteger32BE(distributor.getDistributorID()).toByteArray());
        attributes.put(distributorType, encoderFactory.createHLAunicodeString(distributor.getType()).toByteArray());
        attributes.put(queueSize, encoderFactory.createHLAinteger32BE(distributor.getQueueSize()).toByteArray());

        HLAfloat64Time theTime = timeFactory.makeTime(time + fedamb.federateLookahead);
        rtiamb.updateAttributeValues(distributor.getObjectInstanceHandle(), attributes, generateTag(), theTime);

        log("Updated Distributor Attributes: " + distributor.toString() + ", time=" + theTime.toString());
    }

    private Distributor getDistributorByID(int distributorID) {
        for(int i = 0; i < distributorList.size(); i++) {
            if(distributorList.get(i).getDistributorID() == distributorID)
                return distributorList.get(i);
        }
        return null;
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {
        // OBJECTS //
        distributorClassHandle = rtiamb.getObjectClassHandle("ObjectRoot.Distributor");
        distributorID = rtiamb.getAttributeHandle(distributorClassHandle, "DistributorID");
        distributorType = rtiamb.getAttributeHandle(distributorClassHandle, "DistributorType");
        queueSize = rtiamb.getAttributeHandle(distributorClassHandle, "QueueSize");

        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
        attributes.add(distributorID);
        attributes.add(distributorType);
        attributes.add(queueSize);

        rtiamb.publishObjectClassAttributes(distributorClassHandle, attributes);

        // INTERACTIONS //
        distributorServiceStart = rtiamb.getInteractionClassHandle("InteractionRoot.DistributorServiceStart");
        distributorServiceFinish = rtiamb.getInteractionClassHandle("InteractionRoot.DistributorServiceFinish");
        chooseDistributor = rtiamb.getInteractionClassHandle("HLAinteractionRoot.ChooseDistributor");

        rtiamb.publishInteractionClass(distributorServiceStart);
        rtiamb.publishInteractionClass(distributorServiceFinish);

        rtiamb.subscribeInteractionClass(chooseDistributor);
    }

    @Override
    protected void registerObjects() throws RTIexception {
        for (int i = 0; i < Distributor.DISTRIBUTORS_IN_SIMULATION; i++) {
            Distributor distributor = Distributor.getNextDistributor();
            distributor.setObjectInstanceHandle(rtiamb.registerObjectInstance(distributorClassHandle));
            distributorList.add(distributor);
            createUpdateDistributorInstanceEvent(distributor, fedamb.federateTime + fedamb.federateLookahead);
            log("Registered Object, handle=" + distributor.getObjectInstanceHandle());
        }
    }

    @Override
    protected void deleteObjects() throws RTIexception {
        for (int i = Distributor.DISTRIBUTORS_IN_SIMULATION - 1; i >= 0; i--) {
            rtiamb.deleteObjectInstance(distributorList.remove(i).getObjectInstanceHandle(), generateTag());
        }
    }

    @Override
    protected void enableTimePolicy() throws RTIexception {
        enableTimeRegulation();
        enableTimeConstrained();
    }

    @Override
    protected void log(String message) {
        System.out.println("DistributorFederate   : " + message);
    }

    public static void main(String[] args) {
        try {
            new DistributorFederate().runFederate("DistributorFederate");
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
