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

public abstract class DefaultFederate<FederateAmbassador extends DefaultFederateAmbassador> {

    public static final String READY_TO_RUN = "ReadyToRun";

    protected RTIambassador rtiamb;
    protected FederateAmbassador fedamb;
    protected HLAfloat64TimeFactory timeFactory;
    protected EncoderFactory encoderFactory;

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
    protected abstract URL[] modulesToJoin() throws MalformedURLException;
    protected abstract void publishAndSubscribe() throws NameNotFound, NotConnected, RTIinternalError, FederateNotExecutionMember, InvalidObjectClassHandle, AttributeNotDefined, ObjectClassNotDefined, RestoreInProgress, SaveInProgress, InteractionClassNotDefined;
    protected abstract void registerObjects() throws SaveInProgress, RestoreInProgress, ObjectClassNotPublished, ObjectClassNotDefined, FederateNotExecutionMember, RTIinternalError, NotConnected;
    protected abstract void deleteObjects() throws ObjectInstanceNotKnown, RestoreInProgress, DeletePrivilegeNotHeld, SaveInProgress, FederateNotExecutionMember, RTIinternalError, NotConnected;
    protected abstract void mainSimulationLoop();

    public void runFederate(String federateName, String federateType) throws Exception {
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
            URL module = new File("foms/StartFom.xml").toURI().toURL();
            rtiamb.createFederationExecution("GasStationFederation", module);
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
        URL[] joinModules = modulesToJoin();

        rtiamb.joinFederationExecution(federateName,
                federateType,
                "GasStationFederation",
                joinModules);

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
        log("Published and Subscribed");

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

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// Helper Methods //////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    /**
     * This method will attempt to enable the various time related properties for
     * the federate
     */
    private void enableTimePolicy() throws Exception {
        // NOTE: Unfortunately, the LogicalTime/LogicalTimeInterval create code is
        //       Portico specific. You will have to alter this if you move to a
        //       different RTI implementation. As such, we've isolated it into a
        //       method so that any change only needs to happen in a couple of spots
        HLAfloat64Interval lookahead = timeFactory.makeInterval(fedamb.federateLookahead);

        ////////////////////////////
        // enable time regulation //
        ////////////////////////////
        this.rtiamb.enableTimeRegulation(lookahead);

        // tick until we get the callback
        while (fedamb.isRegulating == false) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }

        /////////////////////////////
        // enable time constrained //
        /////////////////////////////
        this.rtiamb.enableTimeConstrained();

        // tick until we get the callback
        while (fedamb.isConstrained == false) {
            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
        }
    }

    /**
     * This method will inform the RTI about the types of data that the federate will
     * be creating, and the types of data we are interested in hearing about as other
     * federates produce it.
     */
//    private void publishAndSubscribe() throws RTIexception {
//        ///////////////////////////////////////////////
//        // publish all attributes of Food.Drink.Soda //
//        ///////////////////////////////////////////////
//        // before we can register instance of the object class Food.Drink.Soda and
//        // update the values of the various attributes, we need to tell the RTI
//        // that we intend to publish this information
//
//        // get all the handle information for the attributes of Food.Drink.Soda
//        this.sodaHandle = rtiamb.getObjectClassHandle("HLAobjectRoot.Food.Drink.Soda");
//        this.cupsHandle = rtiamb.getAttributeHandle(sodaHandle, "NumberCups");
//        this.flavHandle = rtiamb.getAttributeHandle(sodaHandle, "Flavor");
//        // package the information into a handle set
//        AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
//        attributes.add(cupsHandle);
//        attributes.add(flavHandle);
//
//        // do the actual publication
//        rtiamb.publishObjectClassAttributes(sodaHandle, attributes);
//
//        ////////////////////////////////////////////////////
//        // subscribe to all attributes of Food.Drink.Soda //
//        ////////////////////////////////////////////////////
//        // we also want to hear about the same sort of information as it is
//        // created and altered in other federates, so we need to subscribe to it
//        rtiamb.subscribeObjectClassAttributes(sodaHandle, attributes);
//
//        //////////////////////////////////////////////////////////
//        // publish the interaction class FoodServed.DrinkServed //
//        //////////////////////////////////////////////////////////
//        // we want to send interactions of type FoodServed.DrinkServed, so we need
//        // to tell the RTI that we're publishing it first. We don't need to
//        // inform it of the parameters, only the class, making it much simpler
//        String iname = "HLAinteractionRoot.CustomerTransactions.FoodServed.DrinkServed";
//        servedHandle = rtiamb.getInteractionClassHandle(iname);
//
//        // do the publication
//        rtiamb.publishInteractionClass(servedHandle);
//
//        /////////////////////////////////////////////////////////
//        // subscribe to the FoodServed.DrinkServed interaction //
//        /////////////////////////////////////////////////////////
//        // we also want to receive other interaction of the same type that are
//        // sent out by other federates, so we have to subscribe to it first
//        rtiamb.subscribeInteractionClass(servedHandle);
//    }

    /**
     * This method will register an instance of the Soda class and will
     * return the federation-wide unique handle for that instance. Later in the
     * simulation, we will update the attribute values for this instance
     */
//    private ObjectInstanceHandle registerObject() throws RTIexception {
//        return rtiamb.registerObjectInstance(sodaHandle);
//    }

    /**
     * This method will update all the values of the given object instance. It will
     * set the flavour of the soda to a random value from the options specified in
     * the FOM (Cola - 101, Orange - 102, RootBeer - 103, Cream - 104) and it will set
     * the number of cups to the same value as the current time.
     * <p/>
     * Note that we don't actually have to update all the attributes at once, we
     * could update them individually, in groups or not at all!
     */
//    private void updateAttributeValues(ObjectInstanceHandle objectHandle) throws RTIexception {
//        ///////////////////////////////////////////////
//        // create the necessary container and values //
//        ///////////////////////////////////////////////
//        // create a new map with an initial capacity - this will grow as required
//        AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(2);
//
//        // create the collection to store the values in, as you can see
//        // this is quite a lot of work. You don't have to use the encoding
//        // helpers if you don't want. The RTI just wants an arbitrary byte[]
//
//        // generate the value for the number of cups (same as the timestep)
//        HLAinteger16BE cupsValue = encoderFactory.createHLAinteger16BE(getTimeAsShort());
//        attributes.put(cupsHandle, cupsValue.toByteArray());
//
//        // generate the value for the flavour on our magically flavour changing drink
//        // the values for the enum are defined in the FOM
//        int randomValue = 101 + new Random().nextInt(3);
//        HLAinteger32BE flavValue = encoderFactory.createHLAinteger32BE(randomValue);
//        attributes.put(flavHandle, flavValue.toByteArray());
//
//        //////////////////////////
//        // do the actual update //
//        //////////////////////////
//        rtiamb.updateAttributeValues(objectHandle, attributes, generateTag());
//
//        // note that if you want to associate a particular timestamp with the
//        // update. here we send another update, this time with a timestamp:
//        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + fedamb.federateLookahead);
//        rtiamb.updateAttributeValues(objectHandle, attributes, generateTag(), time);
//    }

    /**
     * This method will send out an interaction of the type FoodServed.DrinkServed. Any
     * federates which are subscribed to it will receive a notification the next time
     * they tick(). This particular interaction has no parameters, so you pass an empty
     * map, but the process of encoding them is the same as for attributes.
     */
//    private void sendInteraction() throws RTIexception {
//        //////////////////////////
//        // send the interaction //
//        //////////////////////////
//        ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(0);
//        rtiamb.sendInteraction(servedHandle, parameters, generateTag());
//
//        // if you want to associate a particular timestamp with the
//        // interaction, you will have to supply it to the RTI. Here
//        // we send another interaction, this time with a timestamp:
//        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + fedamb.federateLookahead);
//        rtiamb.sendInteraction(servedHandle, parameters, generateTag(), time);
//    }

    /**
     * This method will request a time advance to the current time, plus the given
     * timestep. It will then wait until a notification of the time advance grant
     * has been received.
     */
//    private void advanceTime(double timestep) throws RTIexception {
//        // request the advance
//        fedamb.isAdvancing = true;
//        HLAfloat64Time time = timeFactory.makeTime(fedamb.federateTime + timestep);
//        rtiamb.timeAdvanceRequest(time);
//
//        // wait for the time advance to be granted. ticking will tell the
//        // LRC to start delivering callbacks to the federate
//        while (fedamb.isAdvancing) {
//            rtiamb.evokeMultipleCallbacks(0.1, 0.2);
//        }
//    }

    /**
     * This method will attempt to delete the object instance of the given
     * handle. We can only delete objects we created, or for which we own the
     * privilegeToDelete attribute.
     */
    private void deleteObject(ObjectInstanceHandle handle) throws RTIexception {
        rtiamb.deleteObjectInstance(handle, generateTag());
    }

    private short getTimeAsShort() {
        return (short) fedamb.federateTime;
    }

    protected byte[] generateTag() {
        return ("(timestamp) " + System.currentTimeMillis()).getBytes();
    }
}
