package gasStation.cash;

import gasStation.DefaultFederate;
import gasStation.Event;
import gasStation.TimedEventComparator;
import hla.rti1516e.*;
import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Time;

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

    public CashFederate() {
        super();
        this.cash = new Cash();
    }

    @Override
    protected CashFederateAmbassador createFederateAmbassador() {
        return new CashFederateAmbassador(this);
    }

    @Override
    protected void mainSimulationLoop() throws RTIexception {
        boolean isAdvancing = false;
        double timeToAdvance = fedamb.federateTime + fedamb.federateLookahead;
        HLAfloat64Time nextEventTime;

        while (true) {
            if (!fedamb.externalEventList.isEmpty()) {
                for (int i = fedamb.externalEventList.size() - 1; i >= 0; i++) {
                    fedamb.externalEventList.remove(i).runEvent();
                }
            }

            if(!isAdvancing) {
                if (!internalEventList.isEmpty()) {
                    internalEventList.sort(new TimedEventComparator());
                    nextEventTime = internalEventList.get(0).getTime();
                    timeToAdvance = nextEventTime.getValue();
                    if(nextEventTime.getValue() - fedamb.federateTime <= fedamb.federateLookahead) {
                        advanceTime(nextEventTime);
                    } else {
                        timeToAdvance = fedamb.federateTime + fedamb.federateLookahead;
                        nextEventTime = timeFactory.makeTime(timeToAdvance);
                        advanceTime(nextEventTime);
                    }

                } else {
                    timeToAdvance = fedamb.federateTime + fedamb.federateLookahead;
                    nextEventTime = timeFactory.makeTime(timeToAdvance);
                    advanceTime(nextEventTime);
                }
                isAdvancing = true;
            }

            if (fedamb.grantedTime == timeToAdvance) {
                fedamb.federateTime = timeToAdvance;
                log("Time advanced to: " + timeToAdvance);

                if(!internalEventList.isEmpty()) {
                    internalEventList.sort(new TimedEventComparator());
                    while(!internalEventList.isEmpty() && internalEventList.get(0).getTime().getValue() == fedamb.federateTime) {
                        internalEventList.remove(0).runEvent();
                    }
                }

                isAdvancing = false;
            }
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    public Event createUpdateCarInstanceEvent(AttributeHandleValueMap theAttributes) {
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                updateCarInstance(theAttributes);
            }
        };
    }

    private void updateCarInstance(AttributeHandleValueMap theAttributes) {
        int carID = theAttributes.getValueReference(this.carID).getInt();
        boolean payForWash = theAttributes.getValueReference(this.payForWash).getInt() == 1;

        log("Updated Car Instance: carID=" + carID + ", payForWash=" + payForWash);

        cash.updateCarFOMInstance(carID, payForWash);
    }

    private void updateCashAttributes(Cash cash, double time) throws RTIexception {
        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(1);
        attributes.put(queueSize, encoderFactory.createHLAinteger32BE(cash.getQueueSize()).toByteArray());

        HLAfloat64Time theTime = timeFactory.makeTime(time);
        rtiamb.updateAttributeValues(cash.cashInstanceHandle, attributes, generateTag(), theTime);

        log("Updated Cash Attributes: time=" + theTime.toString());
    }

    private void sendCashServiceStartInteraction(int carID, double time) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);

        ParameterHandle parameterHandle = rtiamb.getParameterHandle(cashServiceStart, "CarID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Time theTime = timeFactory.makeTime(time);
        rtiamb.sendInteraction(cashServiceStart, parameters, generateTag(), theTime);

        log("Interaction Send: handle=" + cashServiceStart + " {CashServiceStart}, time=" + theTime.toString());
    }

    private void sendCashServiceFinishInteraction(int carID, double time) throws RTIexception {
        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(1);

        ParameterHandle parameterHandle = rtiamb.getParameterHandle(cashServiceFinish, "CarID");
        parameters.put(parameterHandle, encoderFactory.createHLAinteger32BE(carID).toByteArray());

        HLAfloat64Time theTime = timeFactory.makeTime(time);
        rtiamb.sendInteraction(cashServiceFinish, parameters, generateTag(), theTime);

        log("Interaction Send: handle=" + cashServiceFinish + " {CashServiceFinish}, time=" + theTime.toString());
    }

    @Override
    protected void publishAndSubscribe() throws NameNotFound, NotConnected, RTIinternalError, FederateNotExecutionMember, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, SaveInProgress, InteractionClassNotDefined {
        // OBJECTS //
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
        rtiamb.subscribeObjectClassAttributes(carClassHandle, attributes);

        // INTERACTIONS //
        cashServiceStart = rtiamb.getInteractionClassHandle("HLAinteractionRoot.CashServiceStart");
        cashServiceFinish = rtiamb.getInteractionClassHandle("HLAinteractionRoot.CashServiceFinish");

        rtiamb.publishInteractionClass(cashServiceStart);
        rtiamb.publishInteractionClass(cashServiceFinish);
    }

    @Override
    protected void registerObjects() throws SaveInProgress, RestoreInProgress, ObjectClassNotPublished, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, NotConnected {
        cash.cashInstanceHandle = rtiamb.registerObjectInstance(cashClassHandle);
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
