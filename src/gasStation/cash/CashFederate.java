package gasStation.cash;

import gasStation.DefaultFederate;
import gasStation.Event;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Time;

import java.util.Random;

/**
 * Created by Michał on 2018-05-11.
 */
public class CashFederate extends DefaultFederate<CashFederateAmbassador> {

    public Cash cash;
    protected ObjectClassHandle cashClassHandle;
    protected AttributeHandle queueSize;
    protected InteractionClassHandle cashServiceStart;
    protected InteractionClassHandle cashServiceFinish;

    protected ObjectClassHandle carClassHandle;
    protected AttributeHandle carID;
    protected AttributeHandle wantWash;
    protected AttributeHandle payForWash;
    protected InteractionClassHandle wantToPay;
    protected InteractionClassHandle endSimulation;

    public CashFederate() {
        super();
        this.cash = new Cash();
    }

    @Override
    protected CashFederateAmbassador createFederateAmbassador() {
        return new CashFederateAmbassador(this);
    }

    @Override
    protected void beforeSimulationLoop() throws RTIexception {

    }

    @Override
    protected void afterSimulationLoop() throws RTIexception {

    }

    public Event createUpdateCarInstanceEvent(AttributeHandleValueMap theAttributes) {
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                updateCarInstance(theAttributes);
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

    public Event createAddToQueueCarEvent(ParameterHandleValueMap theParameters) throws RTIexception {
        try {
            ParameterHandle carIDParameter = rtiamb.getParameterHandle(wantToPay, "CarID");
            int carID = theParameters.getValueReference(carIDParameter).getInt();
            return new Event(timeFactory.makeTime(0.0)) {
                @Override
                public void runEvent() throws RTIexception {
                    int position = cash.addCarToQueue(carID);
                    createUpdateCashInstanceEvent(fedamb.federateTime + fedamb.federateLookahead);
                    if (position != cash.getQueueSize() - 1) {

                    }
                    if (cash.getFinishCurrentServiceTime() == 0 || cash.getFinishCurrentServiceTime() < fedamb.federateTime + fedamb.federateLookahead) {
                        cash.setFinishCurrentServiceTime(fedamb.federateTime + fedamb.federateLookahead);
                    }
                    createStartServiceEvent(carID, cash.getFinishCurrentServiceTime());
                }
            };
        } catch (NullPointerException r) {
            return new Event(timeFactory.makeTime(0.0)) {
                @Override
                public void runEvent() throws RTIexception {

                }
            };
        }
    }

    protected void createUpdateCashInstanceEvent(double time) {
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                updateCashAttributes(cash, time);
            }
        });
    }

    protected void createStartServiceEvent(int carID, double time) {
        Random rand = new Random();
        double finishServiceTime = time + rand.nextInt(15) + 5;
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                cash.getCarIDFromQueue();
                sendCashServiceStartInteraction(carID, this.getTime().getValue());
                updateCashAttributes(cash, this.getTime().getValue());
            }
        });
        cash.setFinishCurrentServiceTime(finishServiceTime);
        createFinishServiceEvent(carID, finishServiceTime);
    }

    protected void createFinishServiceEvent(int carID, double time) {
        internalEventList.add(new Event(timeFactory.makeTime(time)) {
            @Override
            public void runEvent() throws RTIexception {
                sendCashServiceFinishInteraction(carID, this.getTime().getValue());
            }
        });
    }

    private void updateCarInstance(AttributeHandleValueMap theAttributes) {
        int carID = theAttributes.getValueReference(this.carID).getInt();
        boolean payForWash = theAttributes.getValueReference(this.payForWash).getInt() == 1;

        cash.updateCarFOMInstance(carID, payForWash);

        log("Updated Car Instance: carID=" + carID + ", payForWash=" + payForWash);
    }

    private void updateCashAttributes(Cash cash, double time) throws RTIexception {
        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(1);
        attributes.put(queueSize, encoderFactory.createHLAinteger32BE(cash.getQueueSize()).toByteArray());

        HLAfloat64Time theTime = timeFactory.makeTime(time + fedamb.federateLookahead);
        rtiamb.updateAttributeValues(cash.cashInstanceHandle, attributes, generateTag(), theTime);

        log("Updated Cash Attributes: " + cash.toString() + ", time=" + theTime.toString());
    }

    private void sendCashServiceStartInteraction(int carID, double time) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);

        ParameterHandle parameterHandle = rtiamb.getParameterHandle(cashServiceStart, "CarID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Time theTime = timeFactory.makeTime(time + fedamb.federateLookahead);
        rtiamb.sendInteraction(cashServiceStart, parameters, generateTag(), theTime);

        log("Interaction Send: handle=" + cashServiceStart + " {CashServiceStart}, carID=" + carID + ", time=" + theTime.toString());
    }

    private void sendCashServiceFinishInteraction(int carID, double time) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);

        ParameterHandle parameterHandle = rtiamb.getParameterHandle(cashServiceFinish, "CarID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Time theTime = timeFactory.makeTime(time + fedamb.federateLookahead);
        rtiamb.sendInteraction(cashServiceFinish, parameters, generateTag(), theTime);

        log("Interaction Send: handle=" + cashServiceFinish + " {CashServiceFinish}, carID=" + carID + ", time=" + theTime.toString());
    }

    @Override
    protected void publishAndSubscribe() throws RTIexception {        // OBJECTS //
        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();

        cashClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Cash");
        queueSize = rtiamb.getAttributeHandle(cashClassHandle, "QueueSize");
        attributes.add(queueSize);

        rtiamb.publishObjectClassAttributes(cashClassHandle, attributes);
        attributes.clear();

        carClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Car");
        carID = rtiamb.getAttributeHandle(carClassHandle, "CarID");
        wantWash = rtiamb.getAttributeHandle(carClassHandle, "WantWash");
        payForWash = rtiamb.getAttributeHandle(carClassHandle, "PayForWash");
        attributes.add(carID);
        attributes.add(wantWash);
        attributes.add(payForWash);
        rtiamb.subscribeObjectClassAttributes(carClassHandle, attributes);

        // INTERACTIONS //
        cashServiceStart = rtiamb.getInteractionClassHandle("HLAinteractionRoot.CashServiceStart");
        cashServiceFinish = rtiamb.getInteractionClassHandle("HLAinteractionRoot.CashServiceFinish");
        wantToPay = rtiamb.getInteractionClassHandle("HLAinteractionRoot.WantToPay");
        endSimulation = rtiamb.getInteractionClassHandle("HLAinteractionRoot.EndSimulation");

        rtiamb.publishInteractionClass(cashServiceStart);
        rtiamb.publishInteractionClass(cashServiceFinish);
        rtiamb.subscribeInteractionClass(wantToPay);
        rtiamb.subscribeInteractionClass(endSimulation);
    }

    @Override
    protected void registerObjects() throws SaveInProgress, RestoreInProgress, ObjectClassNotPublished, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, NotConnected {
        cash.cashInstanceHandle = rtiamb.registerObjectInstance(cashClassHandle);
        createUpdateCashInstanceEvent(fedamb.federateTime + fedamb.federateLookahead);
        log("Registered Object, handle=" + cash.cashInstanceHandle);
    }

    @Override
    protected void deleteObjects() throws ObjectInstanceNotKnown, RestoreInProgress, DeletePrivilegeNotHeld, SaveInProgress, FederateNotExecutionMember, RTIinternalError, NotConnected {
        rtiamb.deleteObjectInstance(cash.cashInstanceHandle, generateTag());
    }

    @Override
    protected void enableTimePolicy() throws SaveInProgress, TimeConstrainedAlreadyEnabled, RestoreInProgress, NotConnected, CallNotAllowedFromWithinCallback, InTimeAdvancingState, RequestForTimeConstrainedPending, FederateNotExecutionMember, RTIinternalError, RequestForTimeRegulationPending, InvalidLookahead, TimeRegulationAlreadyEnabled {
        enableTimeRegulation();
        enableTimeConstrained();
    }

    @Override
    protected void log(String message) {
        System.out.println("CashFederate   : " + message);
    }

    public static void main(String[] args) {
        try {
            new CashFederate().runFederate("CashFederate");
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
