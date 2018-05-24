package gasStation.statistics;

import gasStation.DefaultFederate;
import gasStation.Event;
import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Time;

/**
 * Created by Michał on 2018-05-11.
 */
public class StatisticsFederate extends DefaultFederate<StatisticsFederateAmbassador> {

    protected Statistics statistics;
    // obiekt car
    protected ObjectClassHandle carClassHandle;
    protected AttributeHandle carID;
    protected AttributeHandle wantWash;
    protected AttributeHandle payForWash;
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
    protected InteractionClassHandle endSimulation;


    public StatisticsFederate() {
        super();
        this.statistics = new Statistics();
    }

    @Override
    protected StatisticsFederateAmbassador createFederateAmbassador() {
        StatisticsFederateAmbassador statisticsFederateAmbassador = new StatisticsFederateAmbassador(this);
        return statisticsFederateAmbassador;
    }

    @Override
    protected void beforeSimulationLoop() throws RTIexception {

    }

    @Override
    protected void afterSimulationLoop() throws RTIexception {
        statistics.printDistributorQueueStatistics();
        statistics.printDistributorServiceStatistics();
        statistics.printCashQueueStatistics();
        statistics.printCasWashQueueStatistics();
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

    public Event createAddCarToDistributorQueueEvent(ParameterHandleValueMap theParameters, LogicalTime theTime) throws RTIexception {
        ParameterHandle carIDParameter = rtiamb.getParameterHandle(chooseDistributor, "CarID");
        int carID = theParameters.getValueReference(carIDParameter).getInt();
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                statistics.addStartService(Statistics.STAT_CLASS.DIST_QUEUE, carID, ((HLAfloat64Time) theTime).getValue());
            }
        };

    }

    public Event createDistributorStartServiceEvent(ParameterHandleValueMap theParameters, LogicalTime theTime) throws RTIexception {
        ParameterHandle carIDParameter = rtiamb.getParameterHandle(distributorServiceStart, "CarID");
        int carID = theParameters.getValueReference(carIDParameter).getInt();
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                statistics.addFinishService(Statistics.STAT_CLASS.DIST_QUEUE, carID, ((HLAfloat64Time) theTime).getValue());
                statistics.addStartService(Statistics.STAT_CLASS.DIST_SERVICE, carID, ((HLAfloat64Time) theTime).getValue());
            }
        };
    }

    public Event createDistributorFinishServiceEvent(ParameterHandleValueMap theParameters, LogicalTime theTime) throws RTIexception {
        ParameterHandle carIDParameter = rtiamb.getParameterHandle(distributorServiceFinish, "CarID");
        int carID = theParameters.getValueReference(carIDParameter).getInt();
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                statistics.addFinishService(Statistics.STAT_CLASS.DIST_SERVICE, carID, ((HLAfloat64Time) theTime).getValue());
            }
        };
    }

    public Event createAddCarToCashQueueEvent(ParameterHandleValueMap theParameters, LogicalTime theTime) throws RTIexception {
        try {
            ParameterHandle carIDParameter = rtiamb.getParameterHandle(wantToPay, "CarID");
            int carID = theParameters.getValueReference(carIDParameter).getInt();
            return new Event(timeFactory.makeTime(0.0)) {
                @Override
                public void runEvent() throws RTIexception {
                    if (statistics.carPayForWash(carID)) {
                        statistics.addStartService(Statistics.STAT_CLASS.CASH_WASH_QUEUE, carID, ((HLAfloat64Time) theTime).getValue());
                    } else {
                        statistics.addStartService(Statistics.STAT_CLASS.CASH_QUEUE, carID, ((HLAfloat64Time) theTime).getValue());
                    }
                }
            };
        } catch (NullPointerException e) {
            return new Event(timeFactory.makeTime(0.0)) {
                @Override
                public void runEvent() throws RTIexception {

                }
            };
        }
    }

    public Event createCashStartServiceEvent(ParameterHandleValueMap theParameters, LogicalTime theTime) throws RTIexception {
        ParameterHandle carIDParameter = rtiamb.getParameterHandle(cashServiceStart, "CarID");
        int carID = theParameters.getValueReference(carIDParameter).getInt();
        return new Event(timeFactory.makeTime(0.0)) {
            @Override
            public void runEvent() throws RTIexception {
                if (statistics.carPayForWash(carID)) {
                    statistics.addFinishService(Statistics.STAT_CLASS.CASH_WASH_QUEUE, carID, ((HLAfloat64Time) theTime).getValue());
                } else {
                    statistics.addFinishService(Statistics.STAT_CLASS.CASH_QUEUE, carID, ((HLAfloat64Time) theTime).getValue());
                }
            }
        };
    }

    private void updateCarInstance(AttributeHandleValueMap theAttributes) {
        int carID = theAttributes.getValueReference(this.carID).getInt();
        boolean payForWash = theAttributes.getValueReference(this.payForWash).getInt() == 1;

        statistics.updateCarFOMInstance(carID, payForWash);

        log("Updated Car Instance: carID=" + carID + ", payForWash=" + payForWash);
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

        attributes.clear();
        carClassHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Car");
        carID = rtiamb.getAttributeHandle(carClassHandle, "CarID");
        wantWash = rtiamb.getAttributeHandle(carClassHandle, "WantWash");
        payForWash = rtiamb.getAttributeHandle(carClassHandle, "PayForWash");
        attributes.add(carID);
        attributes.add(wantWash);
        attributes.add(payForWash);
        rtiamb.subscribeObjectClassAttributes(carClassHandle, attributes);

        // INTERAKCJE
        chooseDistributor = rtiamb.getInteractionClassHandle("HLAinteractionRoot.ChooseDistributor");
        distributorServiceStart = rtiamb.getInteractionClassHandle("HLAinteractionRoot.DistributorServiceStart");
        distributorServiceFinish = rtiamb.getInteractionClassHandle("HLAinteractionRoot.DistributorServiceFinish");
        cashServiceStart = rtiamb.getInteractionClassHandle("HLAinteractionRoot.CashServiceStart");
        wantToPay = rtiamb.getInteractionClassHandle("HLAinteractionRoot.WantToPay");
        endSimulation = rtiamb.getInteractionClassHandle("HLAinteractionRoot.EndSimulation");

        rtiamb.subscribeInteractionClass(chooseDistributor);
        rtiamb.subscribeInteractionClass(wantToPay);
        rtiamb.subscribeInteractionClass(distributorServiceStart);
        rtiamb.subscribeInteractionClass(distributorServiceFinish);
        rtiamb.subscribeInteractionClass(cashServiceStart);
        rtiamb.subscribeInteractionClass(endSimulation);
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
