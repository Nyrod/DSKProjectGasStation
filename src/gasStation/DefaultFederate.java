package gasStation;

import hla.rti1516e.*;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.*;
import hla.rti1516e.time.HLAfloat64Interval;
import hla.rti1516e.time.HLAfloat64TimeFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class DefaultFederate<FederateAmbassador extends DefaultFederateAmbassador> {

    public static final String READY_TO_RUN = "ReadyToRun";
    protected List<Event> internalEventList;

    protected RTIambassador rtiamb;
    protected FederateAmbassador fedamb;
    protected HLAfloat64TimeFactory timeFactory;
    protected EncoderFactory encoderFactory;

    public DefaultFederate() {
        this.internalEventList = new ArrayList<>();
    }

    protected void log(String message) {
        System.out.println("DefaultFederate   : " + message);
    }

    private void waitForUser() {
        log(" >>>>>>>>>> Press Enter to Continue <<<<<<<<<<");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (Exception e) {
            log("Error while waiting for user input: " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected abstract FederateAmbassador createFederateAmbassador();

    protected abstract void mainSimulationLoop() throws RTIexception;

    protected abstract void publishAndSubscribe() throws RTIexception;

    protected abstract void registerObjects() throws RTIexception;

    protected abstract void deleteObjects() throws RTIexception;

    protected abstract void enableTimePolicy() throws RTIexception;

    public void runFederate(String federateName) throws Exception {
        //////////////////////////////////////////
        // create the RTIambassador and Connect //
        //////////////////////////////////////////
        log("Creating RTIambassador");
        rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
        encoderFactory = RtiFactoryFactory.getRtiFactory().getEncoderFactory();

        log("Connecting...");
        fedamb = createFederateAmbassador();
        rtiamb.connect(fedamb, CallbackModel.HLA_EVOKED);

        ///////////////////////////
        // create the federation //
        ///////////////////////////
        log("Creating Federation...");
        try {
            URL[] modules = new URL[]{
                    new File("foms/Distributor.xml").toURI().toURL(),
                    new File("foms/Cash.xml").toURI().toURL(),
                    new File("foms/CarWash.xml").toURI().toURL(),
                    new File("foms/Car.xml").toURI().toURL(),
                    new File("foms/Statistics.xml").toURI().toURL()
            };
            rtiamb.createFederationExecution("GasStationFederation", modules);
            log("Created Federation");
        } catch (FederationExecutionAlreadyExists exists) {
            log("Didn't create federation, it already existed");
        } catch (MalformedURLException urle) {
            log("Exception loading one of the FOM modules from disk: " + urle.getMessage());
            urle.printStackTrace();
            return;
        }

        /////////////////////////
        // join the federation //
        /////////////////////////
        rtiamb.joinFederationExecution(federateName,
                "GasStationFederation");

        log("Joined Federation as " + federateName);

        // cache the time factory for easy access
        this.timeFactory = (HLAfloat64TimeFactory) rtiamb.getTimeFactory();

        /////////////////////////////
        // announce the sync point //
        /////////////////////////////
        rtiamb.registerFederationSynchronizationPoint(READY_TO_RUN, null);
        while (fedamb.isAnnounced == false) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
        waitForUser();

        ////////////////////////////////////////////////////
        // achieve the point and wait for synchronization //
        ////////////////////////////////////////////////////
        rtiamb.synchronizationPointAchieved(READY_TO_RUN);
        log("Achieved sync point: " + READY_TO_RUN + ", waiting for federation...");
        while (fedamb.isReadyToRun == false) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }

        //////////////////////////
        // enable time policies //
        //////////////////////////
        enableTimePolicy();
        log("Time Policy Enabled");

        ///////////////////////////
        // publish and subscribe //
        ///////////////////////////
        publishAndSubscribe();

        //////////////////////////////////
        // register an object to update //
        //////////////////////////////////
        registerObjects();

        /////////////////////////////////
        // do the main simulation loop //
        /////////////////////////////////
        mainSimulationLoop();

        //////////////////////////////////
        // delete the object we created //
        //////////////////////////////////
        deleteObjects();

        ////////////////////////////////
        // resign from the federation //
        ////////////////////////////////
        rtiamb.resignFederationExecution(ResignAction.DELETE_OBJECTS);
        log("Resigned from Federation");

        ////////////////////////////////////
        // try and destroy the federation //
        ////////////////////////////////////
        try {
            rtiamb.destroyFederationExecution("GasStationFederation");
            log("Destroyed Federation");
        } catch (FederationExecutionDoesNotExist dne) {
            log("No need to destroy federation, it doesn't exist");
        } catch (FederatesCurrentlyJoined fcj) {
            log("Didn't destroy federation, federates still joined");
        }
    }

    public void advanceTime(LogicalTime timestep) throws RTIexception {
        fedamb.isAdvancing = true;
        rtiamb.timeAdvanceRequest(timestep);
        while (fedamb.isAdvancing) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    protected void enableTimeRegulation() throws SaveInProgress, TimeRegulationAlreadyEnabled, RequestForTimeRegulationPending, RestoreInProgress, InvalidLookahead, InTimeAdvancingState, RTIinternalError, FederateNotExecutionMember, NotConnected, CallNotAllowedFromWithinCallback {
        HLAfloat64Interval lookahead = timeFactory.makeInterval(fedamb.federateLookahead);

        rtiamb.enableTimeRegulation(lookahead);

        while (!fedamb.isRegulating) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    protected void enableTimeConstrained() throws SaveInProgress, TimeConstrainedAlreadyEnabled, RestoreInProgress, RTIinternalError, InTimeAdvancingState, FederateNotExecutionMember, RequestForTimeConstrainedPending, NotConnected, CallNotAllowedFromWithinCallback {
        rtiamb.enableTimeConstrained();

        while (!fedamb.isConstrained) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    private short getTimeAsShort() {
        return (short) fedamb.federateTime;
    }

    protected byte[] generateTag() {
        return ("(timestamp) " + System.currentTimeMillis()).getBytes();
    }
}
