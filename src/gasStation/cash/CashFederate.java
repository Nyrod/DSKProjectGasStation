package gasStation.cash;

import gasStation.DefaultFederate;
import gasStation.car.Car;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Time;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

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
    protected AttributeHandle carId;
    protected AttributeHandle wantWash;
    protected AttributeHandle payForWash;

    public CashFederate() {
        this.cash = new Cash();
    }

    @Override
    protected CashFederateAmbassador createFederateAmbassador() {
        return new CashFederateAmbassador(this);
    }

    @Override
    protected void mainSimulationLoop() {

    }

    public void addDiscoverCarInstance(ObjectInstanceHandle objectInstance) {
        
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

    private void sendCashServiceFinishInteraction(int carID, double time) throws RTIexception{
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
        carId = rtiamb.getAttributeHandle(carClassHandle, "CarID");
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
