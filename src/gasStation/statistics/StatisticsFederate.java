package gasStation.statistics;

import gasStation.DefaultFederate;
import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Time;

/**
 * Created by Michał on 2018-05-11.
 */
public class StatisticsFederate extends DefaultFederate<StatisticsFederateAmbassador> {

    protected Statistics statistics;
    // obiekt dystrybutor
    protected ObjectClassHandle distributorHandle;
    protected AttributeHandle distributorID;
    protected AttributeHandle distributorType;
    protected AttributeHandle distributorQueueSize;
    // obiekt kasa
    protected ObjectClassHandle cashHandle;
    protected AttributeHandle cashQueueSize;
    // obiekt myjnia
    protected ObjectClassHandle carWashHandle;
    protected AttributeHandle carWashQueueSize;
    protected AttributeHandle currentServiceCarID;
    // interakcje
    protected InteractionClassHandle distributorServiceStart;
    protected InteractionClassHandle distributorServiceFinish;
    protected InteractionClassHandle cashServiceStart;
    protected InteractionClassHandle chooseDistributor;
    protected InteractionClassHandle wantToPay;


    public StatisticsFederate() {
        super();
        this.statistics = new Statistics();
    }

    @Override
    protected StatisticsFederateAmbassador createFederateAmbassador() {
        StatisticsFederateAmbassador statisticsFederateAmbassador = new StatisticsFederateAmbassador(this);
        statisticsFederateAmbassador.federateLookahead = 5;
        return statisticsFederateAmbassador;
    }

    @Override
    protected void mainSimulationLoop() throws RTIexception {
        while (true) {
            double timeToAdvance = fedamb.federateTime + fedamb.federateLookahead;
            HLAfloat64Time time = timeFactory.makeTime(timeToAdvance);
            advanceTime(time);

            if(fedamb.grantedTime == timeToAdvance) {
                fedamb.federateTime = timeToAdvance;
                log("Time advanced to: " + timeToAdvance);
            }
        }
    }

    @Override
    protected void publishAndSubscribe() throws NameNotFound, NotConnected, RTIinternalError, FederateNotExecutionMember, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, SaveInProgress, InteractionClassNotDefined, FederateServiceInvocationsAreBeingReportedViaMOM {
        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();

        distributorHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Distributor");
        distributorID = rtiamb.getAttributeHandle(distributorHandle, "DistributorID");
        distributorType = rtiamb.getAttributeHandle(distributorHandle, "DistributorType");
        distributorQueueSize = rtiamb.getAttributeHandle(distributorHandle, "QueueSize");
        attributes.add(distributorID);
        attributes.add(distributorType);
        attributes.add(distributorQueueSize);
        rtiamb.subscribeObjectClassAttributes(distributorHandle, attributes);

        attributes.clear();
        cashHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Cash");
        cashQueueSize = rtiamb.getAttributeHandle(cashHandle, "QueueSize");
        attributes.add(cashQueueSize);
        rtiamb.subscribeObjectClassAttributes(cashHandle, attributes);

        attributes.clear();
        carWashHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.CarWash");
        carWashQueueSize = rtiamb.getAttributeHandle(carWashHandle, "QueueSize");
        currentServiceCarID = rtiamb.getAttributeHandle(carWashHandle, "CurrentServiceCarID");
        attributes.add(carWashQueueSize);
        attributes.add(currentServiceCarID);
        rtiamb.subscribeObjectClassAttributes(carWashHandle, attributes);

        // INTERAKCJE
        chooseDistributor = rtiamb.getInteractionClassHandle("HLAinteractionRoot.ChooseDistributor");
        distributorServiceStart = rtiamb.getInteractionClassHandle("HLAinteractionRoot.DistributorServiceStart");
        distributorServiceFinish = rtiamb.getInteractionClassHandle("HLAinteractionRoot.DistributorServiceFinish");
        cashServiceStart = rtiamb.getInteractionClassHandle("HLAinteractionRoot.CashServiceStart");
        wantToPay = rtiamb.getInteractionClassHandle("HLAinteractionRoot.WantToPay");

        rtiamb.subscribeInteractionClass(chooseDistributor);
        rtiamb.subscribeInteractionClass(wantToPay);
        rtiamb.subscribeInteractionClass(distributorServiceStart);
        rtiamb.subscribeInteractionClass(distributorServiceFinish);
        rtiamb.subscribeInteractionClass(cashServiceStart);
    }

    @Override
    protected void registerObjects() throws SaveInProgress, RestoreInProgress, ObjectClassNotPublished, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, NotConnected {
    }

    @Override
    protected void deleteObjects() throws ObjectInstanceNotKnown, RestoreInProgress, DeletePrivilegeNotHeld, SaveInProgress, FederateNotExecutionMember, RTIinternalError, NotConnected {
    }

    @Override
    protected void enableTimePolicy() throws SaveInProgress, TimeConstrainedAlreadyEnabled, RestoreInProgress, NotConnected, CallNotAllowedFromWithinCallback, InTimeAdvancingState, RequestForTimeConstrainedPending, FederateNotExecutionMember, RTIinternalError, RequestForTimeRegulationPending, InvalidLookahead, TimeRegulationAlreadyEnabled {
        enableTimeConstrained();
        enableTimeRegulation();
    }

    public static void main(String[] args) {
        try {
            new StatisticsFederate().runFederate("StatisticsFederate");
        } catch (Exception rtie) {
            rtie.printStackTrace();
        }
    }
}
