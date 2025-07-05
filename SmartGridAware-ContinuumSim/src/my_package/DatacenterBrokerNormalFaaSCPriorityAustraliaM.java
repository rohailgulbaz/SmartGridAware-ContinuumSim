/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package my_package;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;

import org.cloudbus.cloudsim.*;

/**
 * DatacentreBroker represents a broker acting on behalf of a user. It hides VM management, as vm
 * creation, sumbission of cloudlets to this VMs and destruction of VMs.
 */
public class DatacenterBrokerNormalFaaSCPriorityAustraliaM extends SimEntity {

	//TickBasedDelaySimulator delaySimulator = new TickBasedDelaySimulator();
	
	/** The vm list. */
	protected List<? extends Vm> vmList;

	/** The vms created list. */
	protected List<? extends Vm> vmsCreatedList;

	/** The cloudlet list. */
	protected List<? extends Cloudlet> cloudletList;

	/** The cloudlet submitted list. */
	protected List<? extends Cloudlet> cloudletSubmittedList;

	/** The cloudlet received list. */
	protected List<? extends Cloudlet> cloudletReceivedList;

	/** The cloudlets submitted. */
	protected int cloudletsSubmitted;

	/** The vms requested. */
	protected int vmsRequested;

	/** The vms acks. */
	protected int vmsAcks;

	/** The vms destroyed. */
	protected int vmsDestroyed;

	/** The datacenter ids list. */
	protected List<Integer> datacenterIdsList;

	/** The datacenter requested ids list. */
	protected List<Integer> datacenterRequestedIdsList;

	/** The vms to datacenters map. */
	protected Map<Integer, Integer> vmsToDatacentersMap;

	/** The datacenter characteristics list. */
	protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;
	
	
	private double lastSimTime = 0.0; // Tracks last processed event time

	private List<Datacenter> SpecificDCList4theBroker;
	
	private List<Map.Entry<Double, Datacenter>> sortedList = new ArrayList<>(); //list of DCs with highest resource availability
	private Map<Datacenter, Integer> lastUsedIndexMap = new HashMap<>(); // Track Round Robin index for Host per Datacenter
	private List<Map.Entry<Integer, ContainerImage>> imageList = new ArrayList<>();//Keeps a list of Images i.e. only specifications of containers to be created
	private int ContainerIDShift = -1; //global ID for a container
	private static HashMap<Host, ArrayList<Integer>> hostContainerServices = new HashMap<>();
	private static Map<Host, ArrayList<Vm>> hostVmMap = new HashMap<>();
	private static HashMap<Host, Double> hostLastContainerTime = new HashMap<>();
	private double[] scaledCarbonData;
	private List<List<Datacenter>> GeoRegions = new ArrayList<>();
	
	private int inflight = 2; //limit of concurrent jobs in each container
	private double FailureRetryTime = 60; //after 60 seconds resend job if container created fails
	
	private static final String FILE_PATH = "Logs\\CPriorityAustraliaMResults.csv";
	
	private int JobRow = 0;
	private int ContainerRow = 0;
	private int ContainerFailedRow = 0;
	
	private Map<Datacenter, Double> datacenterCarbonMap = new HashMap<>();
	
	/**
	 * Created a new DatacenterBroker object.
	 * 
	 * @param name name to be associated with this entity (as required by Sim_entity class from
	 *            simjava package)
	 * @throws Exception the exception
	 * @pre name != null
	 * @post $none
	 */
	public DatacenterBrokerNormalFaaSCPriorityAustraliaM(String name) throws Exception {
		super(name);

		setVmList(new ArrayList<Vm>());
		setVmsCreatedList(new ArrayList<Vm>());
		setCloudletList(new ArrayList<Cloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());

		cloudletsSubmitted = 0;
		setVmsRequested(0);
		setVmsAcks(0);
		setVmsDestroyed(0);

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
	}

	/**
	 * This method is used to send to the broker the list with virtual machines that must be
	 * created.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList(List<? extends Vm> list) {
		getVmList().addAll(list);
	}

	/**
	 * This method is used to send to the broker the list of cloudlets.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitCloudletList(List<? extends Cloudlet> list) {
		getCloudletList().addAll(list);
	}

	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine.
	 * 
	 * @param cloudletId ID of the cloudlet being bount to a vm
	 * @param vmId the vm id
	 * @pre cloudletId > 0
	 * @pre id > 0
	 * @post $none
	 */
	public void bindCloudletToVm(int cloudletId, int vmId) {
		CloudletList.getById(getCloudletList(), cloudletId).setVmId(vmId);
	}

	/*
	private void simulateTickBasedDelay(double scale) {
	    double timeDiff = CloudSim.clock() - lastSimTime; // Get time difference
	    lastSimTime = CloudSim.clock(); // Update last processed time

	    long realDelay = (long) (timeDiff * scale); // Convert sim time to real time delay
	    if (realDelay > 0) {
	        try {
	            Thread.sleep(realDelay); // Pause based on simulation event delay
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	        }
	    }
	}
*/
	
	/**
	 * Processes events available for this Broker.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		//delaySimulator.simulateTickBasedDelay(0); //showing delay Tick behavior
		switch (ev.getTag()) {
			case CloudSimTags.CLOUDLET_ARRIVAL:  //At Arrival time of cloudlet this event is called
				//delaySimulator.simulateTickBasedDelay(500); //showing delay Tick behavior
	            handleCloudletArrival(ev);
	            break;
			case CloudSimTags.CONTAINER_CREATE_ACK:  //After Arrival time of cloudlet this event is called
				//delaySimulator.simulateTickBasedDelay(400); //showing delay Tick behavior
	            handleContainerCreateAck(ev);
	            break;
			case CloudSimTags.JOB_FINISHED:
				//delaySimulator.simulateTickBasedDelay(50);
				processJobReturn(ev);
	            break;    
            // Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST: 
				//simulateTickBasedDelay(); //showing delay Tick behavior
				processResourceCharacteristicsRequest(ev); 
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				//simulateTickBasedDelay(); //showing delay Tick behavior
				processResourceCharacteristics(ev);
				break;
			// VM Creation answer
			case CloudSimTags.VM_CREATE_ACK:
				//simulateTickBasedDelay(); //showing delay Tick behavior
				//processVmCreate(ev);
				break;
			// A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN:
				//simulateTickBasedDelay(); //showing delay Tick behavior
				processCloudletReturn(ev);
				//processJobReturn(ev);
				break;
			// if the simulation finishes 
			case CloudSimTags.END_OF_SIMULATION: //not called
				//simulateTickBasedDelay(); //showing delay Tick behavior
				shutdownEntity();
				break;
			// other unknown tags are processed by this method
			default:
				//simulateTickBasedDelay(); //showing delay Tick behavior
				processOtherEvent(ev);
				break;
		}
	}

	private void handleCloudletArrival(SimEvent ev) {	
		Cloudlet cloudlet = (Cloudlet) ev.getData();
	    getDatacenterResourceInfo(); //Resource Availability of all DC
	    Datacenter d=getHighestScoreDatacenter(); 
	    Host h=getNextHost(d); //object of host to place Function or Invoke Existing
	    Vm vm = checkVmsInHost(h, cloudlet);
	    if(vm == null) {  
		    	ContainerImage CIobj = imageList.get(cloudlet.getService()).getValue(); //use this Container Image to Create Container/Vm
		    	ContainerIDShift++;
		    	Vm newVm = new Vm(ContainerIDShift, this.getId(), CIobj.getMips(), CIobj.getServiceType(), CIobj.getPesNumber(), CIobj.getRam(), CIobj.getBw(), CIobj.getSize(), CIobj.getVmm(), new CloudletSchedulerTimeShared()); //Vm object creation but next it should be registered with host in the Simulation through Send
		    	newVm.setHost(h); //Host already decided, just register it later and check other issues
		    	newVm.setFirstCloudet(cloudlet);
		    	newVm.setService(cloudlet.getService());
		    	hostLastContainerTime.put(h, CloudSim.clock()); 
		    	addContainerService(h, cloudlet.getService()); //All requested Container Services
		    	addVmToHost(h, newVm);
		    	Object[] data = new Object[]{newVm, cloudlet, ContainerIDShift, h, getCarbonData(d)};
		    	sendNow(d.getId(), CloudSimTags.CONTAINER_CREATE_ACK, data);
	    } else { //map to existing container
	    	cloudlet.setUserId(getId());
			cloudlet.setVmId(vm.getId());
	    	sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
	    	
	    	//Logging
			JobRow++;
	    	try {    		
	    		//Some jobs start running when Container is instantiated, and for some Container is already running
		    	//If container cannot be created due to any reason then there is no need to keep logs for that job	
	    		writeToCSV(JobRow, 0, cloudlet.getCloudletId()); //Job_ID
	    		writeToCSV(JobRow, 1, h.getId()); //Job Host_ID
	    		writeToCSV(JobRow, 2, h.getDatacenter().getId()); //Job DC_ID
	    		
	    		writeToCSV(JobRow, 4, getCarbonData(d));
	    		
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    	
	    }
	}


	/**
     * Adds a new container service value to a host.
     */
    public static void addContainerService(Host host, int value) {
        hostContainerServices.computeIfAbsent(host, k -> new ArrayList<>()).add(value);
    }

    /**
     * Checks if a specific container service value exists for a given host.
     * @return true if the value exists, otherwise false.
     */
    public static boolean findContainerService(Host host, int value) {
        return hostContainerServices.containsKey(host) && hostContainerServices.get(host).contains(value);
    }
	
 // Add a VM to a Host
    public static void addVmToHost(Host host, Vm vm) {
        hostVmMap.computeIfAbsent(host, k -> new ArrayList<>()).add(vm);
    }

    public static Vm findVmInHost(Host host, Cloudlet cl) {
        if (hostVmMap.containsKey(host)) {
            for (Vm vm : hostVmMap.get(host)) {
                if (vm.getService() == cl.getService()) {
                    return vm; // Return the VM with the matching service
                }
            }
        }
        return null; // Return null if no matching VM is found
    }

    
	private void handleContainerCreateAck(SimEvent ev) {		
	 // Extract the data (expecting an Object array)
        Object[] data = (Object[]) ev.getData();

        // Extract VM and Cloudlet from the array
        Vm vm = (Vm) data[0];
        Cloudlet cl = (Cloudlet) data[1];
        Host h = (Host) data[3];
        Double carbonData = (Double) data[4];
	    
		//Vm vm = (Vm) ev.getData();
		//Cloudlet cl=vm.getFirstCloudet();

		 //Handling rare possible i.e. Container Creation would fail due to insufficient MIPS, RAM etc.
		if (vm != null) { //Container was successfully registered by sendNow, i.e. No MIPS issues for creation		
			getVmsToDatacentersMap().put(vm.getId(), vm.getDCid());
			getVmsCreatedList().add(VmList.getById(getVmList(), vm.getId()));
			//Log.printLine(CloudSim.clock() + ": " + getName() + ": Container #" + vm.getId() + " has been created in Datacenter #" + vm.getDCid() + ", Host #" + VmList.getById(getVmsCreatedList(), vm.getId()).getHost().getId());
			
			//send cloudlet to the created container
			cl.setUserId(getId());
			cl.setVmId(vm.getId());
			//sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cl);
			
			
			//container is created so setup delay = 1 (delay = 1)
			send(getVmsToDatacentersMap().get(vm.getId()), 1, CloudSimTags.CLOUDLET_SUBMIT, cl);
			
			//Logging
			ContainerRow++;
			JobRow++;
	    	try {
	    		writeToCSV(ContainerRow, 6, vm.getId()); //Container_ID
	    		writeToCSV(ContainerRow, 7, vm.getHost().getId()); //Container Host_ID
	    		writeToCSV(ContainerRow, 8, vm.getBusyPower()); //Container Busy Power
	    		writeToCSV(ContainerRow, 9, vm.getHost().getIdlePower()); //Idle of Host of Container
	    		writeToCSV(ContainerRow, 10, (CloudSim.clock()+1) ); //Container Start Time ST
	    		
	    		//Some jobs start running when Container is instantiated, and for some Container is already running
		    	//If container cannot be created due to any reason then there is no need to keep logs for that job
	    		writeToCSV(JobRow, 0, cl.getCloudletId()); //Job_ID
	    		writeToCSV(JobRow, 1, h.getId()); //Job Host_ID
	    		writeToCSV(JobRow, 2, h.getDatacenter().getId()); //Job DC_ID
	    		writeToCSV(JobRow, 4, carbonData);
	    		
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			
		} else { //Container was not successfully registered by sendNow, i.e. No MIPS issues for creation
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of Container #" + data[2]);
			//delay submission
    		System.out.println("delaying submission of Cloudlet due to failure of Container Creation ");
    		send(getId(), FailureRetryTime, CloudSimTags.CLOUDLET_ARRIVAL, cl);
    		
    		//Logging
			ContainerFailedRow++;
	    	try {
	    		writeToCSV(ContainerFailedRow, 14, ContainerFailedRow); //Container Failed Count
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    	
		}
	 
	}
	
	/*
	 * Resource Availability is based on Accumulated Active Containers' MIPS limit
	 * Carbon Intensity is based on Data of 6 DC in Regions, got from Electricity Map
	 * Updates a sorted list of latest RA of all DC
	 * */
	public void getDatacenterResourceInfo() {
		//System.out.println("Get Datacenter Resource Info ");
		
		//..................................................................................................
		//Get, Scale and Display Carbon Intensity of each DC
	    //System.out.println("Get Carbon Intensity Info ");

	    String baseFilePath = "ElectricityMapsDataset\\"; // Base path
	    String[] regions = {"CI-DC5-DC6", "CI-DC7-DC8"}; // Add more regions dynamically if needed
	    String fileExtension = ".csv"; // File extension
	    

	    double[] carbonData = new double[regions.length];
	    int conversion = 60; //Divide by 60 to get minutes
	    double morning = 21600; //in other experiments time is 0 to 16740 for job arrival
	    //in this experiment Morning experiment begins from 21600=6 hours i.e. start reading file after 21600
	    int queryIndex = (int) ( (CloudSim.clock() + morning) /conversion ); //Divide Current Time by 60 to get which minute is ongoing, later scale down to floor using int casting
	    System.out.println("clock " + CloudSim.clock()+ "  queryIndex "+queryIndex);

	    // Fetch carbon intensity for each region
	    double maxCI = Double.MIN_VALUE;
	    for (int i = 0; i < regions.length; i++) {
	    //for (int i = 0; i < DCListSize; i++) {
	        String filePath = baseFilePath + regions[i] + fileExtension;
	        HistoricalCarbonIntensityFetcher reader = new HistoricalCarbonIntensityFetcher(filePath);
	        Double carbonIntensity = reader.getCarbonIntensity(queryIndex);
	        //System.out.println("Carbon Intensity Original = "+carbonIntensity);
	        if (carbonIntensity != null) {
	            carbonData[i] = carbonIntensity;
	            if (carbonIntensity > maxCI) {
	                maxCI = carbonIntensity; // Track max value
	                //System.out.println("Max Carbon Intensity = "+ maxCI);
	            }
	        } else {
	            carbonData[i] = 0; // Default value if no data
	        }
	    }

	    if (maxCI == Double.MIN_VALUE) {
	        System.out.println("No data available for this time step.");
	        return;
	    }

	    if(maxCI==0) {
    		maxCI=0.1; //to avoid division error, but this case is rare
    	}
	    // Scale carbon intensities and print
	    scaledCarbonData = new double[regions.length];
	    double minCI = Double.MAX_VALUE;
	    int minIndex=-1;
	    for (int i = 0; i < regions.length; i++) {
	        scaledCarbonData[i] = (carbonData[i] / maxCI) * 100;
	        if (scaledCarbonData[i] < minCI) {
                minCI = scaledCarbonData[i]; // Track max value
                minIndex = i;
            }
	        //System.out.println("Region: " + regions[i] + ", Scaled CI: " + scaledCarbonData[i]);
	    }
	    //................................................................................................
		
    	sortedList.clear();
    	
    	//clear OLD carbondata stored against each DC
    	datacenterCarbonMap.clear(); // Clears all stored entries
    	
    	for (Datacenter datacenter : GeoRegions.get(minIndex)) {
	        //System.out.println("Datacenter : " + datacenter.getName()); //global resource unique ID

	        List<Host> hosts = datacenter.getHostList();
	        //System.out.println("Total Hosts: " + hosts.size());

	        double totalMipsDC = 0;
	        double totalAvailableMipsDC = 0;
	        double totalAvailablePercentageDC = 0; //A very small DC with always less available MIPS would not get a job, so check percentage of utilization

	        for (Host host : hosts) {
	            double usedMips = host.getTotalMips() - host.getAvailableMips();
	            //System.out.println("Host ID: " + host.getId()+ " Host Mips: "+host.getTotalMips()+" Host Remaining Mips: "+host.getAvailableMips());
	            totalMipsDC += host.getTotalMips();
	            totalAvailableMipsDC += host.getAvailableMips();
	        }
	        
	        totalAvailablePercentageDC = (totalAvailableMipsDC * 100) / totalMipsDC;
	        
	        //System.out.println("Total Free Resources in Datacenter " + datacenter.getId() + ":");
	        //System.out.println("Total Mips of DC: "+ totalMipsDC + "  Available MIPS of DC: " + totalAvailableMipsDC + " Available Percentage of DC: " + totalAvailablePercentageDC);
	        
	           
	        addSorted(totalAvailablePercentageDC, datacenter);
	        //System.out.println("Score of DC " + totalAvailablePercentageDC);
	        
	        datacenterCarbonMap.put(datacenter, carbonData[minIndex]);
    	}
	    
	    //..................................................................................................
	}

	// Method to retrieve carbon data if we know the datacenter
    public Double getCarbonData(Datacenter datacenter) {
        return datacenterCarbonMap.get(datacenter); // Returns null if not found
    }

	
	public void addSorted(double totalAvailablePercentageDC, Datacenter datacenter) {
        sortedList.add(new AbstractMap.SimpleEntry<>(totalAvailablePercentageDC, datacenter));
        sortedList.sort((a, b) -> Double.compare(b.getKey(), a.getKey())); // Sort in descending order
    }

    public Datacenter getHighestScoreDatacenter() {
    	//System.out.println("highest value: "+sortedList.get(0).getKey());
        return sortedList.isEmpty() ? null : sortedList.get(0).getValue();
    }

    public Host getNextHost(Datacenter d) {
        List<Host> hosts = d.getHostList(); 
        if (hosts.isEmpty()) {
            return null; // No hosts available
        }

        int lastUsedIndex = lastUsedIndexMap.getOrDefault(d, -1); // Get last index for this datacenter
        lastUsedIndex = (lastUsedIndex + 1) % hosts.size(); // Move to next host

        lastUsedIndexMap.put(d, lastUsedIndex); // Update index tracking
        return hosts.get(lastUsedIndex);
    }
    
    public Vm checkVmsInHost(Host host, Cloudlet cl) {
        List<Vm> vms = host.getVmList(); 
        //System.out.println("Hosts vm list size "+vms.size());
        if (vms.isEmpty()) {
            //System.out.println("No Containers in this host.");
            return null;
        }else {
        	int service;
        	int runningJobs;
        	for (Vm vm : vms) {
        		service = vm.getService();
        		//System.out.println("Container Service "+service+"   cloudlet service "+cl.getService());
        		if(service==cl.getService()) { //If the required container is already in host
        			//Required Container is already running but check load of it
        			runningJobs = vm.getCloudletScheduler().runningCloudlets();
        			if( (runningJobs+vm.RequestedJobs) < inflight) {
        				//System.out.println("Required Container is already running with load " + (runningJobs+vm.RequestedJobs));
            			cl.setUserId(getId());
            			cl.setVmId(vm.getId());
            			vm.RequestedJobs++; //job is in Request category but once it starts running RequestedJobs--
            			return vm; //returns the first occurrence of the container of the service with load < inflight
        			} 	
        		}
            }
        	//System.out.println("Required Container is not already running OR runningContainers == inflight");
        	return null;
        }
    }

	public void submitImageList(List<Entry<Integer,ContainerImage>> imageList2) {
		this.imageList = imageList2;
	}

		public void submitDCList(List<Datacenter> specificList1) {
			//mapping is not available at this method call. Just receives a DCList by user for each Broker
			SpecificDCList4theBroker = specificList1;
		}
		

	/**
	 * Process the return of a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
			//createVmsInDatacenter(getDatacenterIdsList().get(0)); 
			submitCloudlets(); 
		}
	}

	/**
	 * Process a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with "
				+ getDatacenterIdsList().size() + " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}

	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
			Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
					+ " has been created in Datacenter #" + datacenterId + ", Host #"
					+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
		} else {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
		}

		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
	}

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Job " + cloudlet.getCloudletId()
				+ " received");
		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
			Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				createVmsInDatacenter(0);
			}

		}
	}
	
	protected void processJobReturn(SimEvent ev) {
		Object[] data = (Object[]) ev.getData();
	    Cloudlet cloudlet = (Cloudlet) data[0];
	    Vm vm = (Vm) data[1];
	    Host host = (Host) data[2];
	    Datacenter datacenter = (Datacenter) data[3];  // Extract Datacenter
		getCloudletReceivedList().add(cloudlet);
		Log.printLine("\n" + CloudSim.clock() + ": " + getName() + ": Job " + cloudlet.getCloudletId() +
                " executed on Container " + vm.getId() + " in Host " + host.getId() + 
                " at Datacenter: " + datacenter.getName());
		int runningContainers = vm.getCloudletScheduler().runningCloudlets() ;
		//System.out.println("Current running Jobs in the Container "+ vm.getId()+ " are "+ runningContainers);
		if (runningContainers == 0) {
			System.out.println("Sending request to destroy Container \n");
		    sendNow(datacenter.getId(), CloudSimTags.VM_DESTROY, vm);
		    
		    try {
	            // Finding row number based on column value
	            int row = findRowByColumnValue(6, vm.getId()); // Searching for value vm.getId() in column 6 i.e. Container-ID
	            if (row != -1) {
	            	writeToCSV(row, 11, CloudSim.clock());
	            } else {
	                System.out.println("Value not found");
	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		    
		}
		
		//Job Completion Time
		try {
            // Finding row number based on column value
            int row = findRowByColumnValue(0, cloudlet.getCloudletId()); // Searching for value cloudlet.getCloudletId() in column 0 i.e. Job-ID
            if (row != -1) {
            	writeToCSV(row, 3, CloudSim.clock());
            } else {
                System.out.println("Value not found");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

	}

	/**
	 * Overrides this method when making a new and different type of Broker. This method is called
	 * by {@link #body()} for incoming unknown tags.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}

		Log.printLine(getName() + ".processOtherEvent(): "
				+ "Error - event unknown by this DatacenterBroker.");
	}

	/**
	 * Create the virtual machines in a datacenter.
	 * 
	 * @param datacenterId Id of the chosen PowerDatacenter
	 * @pre $none
	 * @post $none
	 */
	protected void createVmsInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the next one
		int requestedVms = 0;
		String datacenterName = CloudSim.getEntityName(datacenterId);
		for (Vm vm : getVmList()) {
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
						+ " in " + datacenterName);
				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				requestedVms++;
			}
		}

		getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	/**
	 * Submit cloudlets to the FutureQueue and when their arrival Time would occur
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {
		///*
		for (Cloudlet cloudlet : getCloudletList()) {
			//System.out.println(getCloudletList().size());
			if (cloudlet.getVmId() == -1) {
				
				double delay = cloudlet.getSubmissionDelay() - CloudSim.clock(); // Time until the cloudlet's submission
				//System.out.println("Cloudlet delay "+delay+ " & destination i.e. Broker ID = "+getId());
				
				send(getId(), delay, CloudSimTags.CLOUDLET_ARRIVAL, cloudlet);
				
			}else {
				//System.out.println("Container to Job is assigned in advance, it is not dynamic ");
			}
		}
		//*/
		System.out.println("getCloudletList().size() = " + getCloudletList().size());
		
		
		
		/*
		int vmIndex = 0;
		for (Cloudlet cloudlet : getCloudletList()) {
			
			Vm vm;
			// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getVmId() == -1) {
				vm = getVmsCreatedList().get(vmIndex);
			} else { // submit to the specific vm
				//already created or not; if not then move to next cloudlet and postponing remaining part
				//In our scenario only always if part will execute because till task arrival no VM is 
				//specified for the cloudlet
				
				vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId() + ": bount VM not available");
					continue;
				}
				
			}

			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
			getCloudletSubmittedList().add(cloudlet);
		}

		// remove submitted cloudlets from waiting list
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}
		*/
	}

	/**
	 * Destroy the virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (Vm vm : getVmsCreatedList()) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
		}

		getVmsCreatedList().clear();
	}

	/**
	 * Send an internal event communicating the end of the simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void finishExecution() {
		sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#shutdownEntity()
	 */
	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");	
	    schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletList the new cloudlet list
	 */
	protected <T extends Cloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
	}

	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletSubmittedList the new cloudlet submitted list
	 */
	protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * Sets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletReceivedList the new cloudlet received list
	 */
	protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmsCreatedList() {
		return (List<T>) vmsCreatedList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmsCreatedList the vms created list
	 */
	protected <T extends Vm> void setVmsCreatedList(List<T> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}

	/**
	 * Gets the vms requested.
	 * 
	 * @return the vms requested
	 */
	protected int getVmsRequested() {
		return vmsRequested;
	}

	/**
	 * Sets the vms requested.
	 * 
	 * @param vmsRequested the new vms requested
	 */
	protected void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}

	/**
	 * Gets the vms acks.
	 * 
	 * @return the vms acks
	 */
	protected int getVmsAcks() {
		return vmsAcks;
	}

	/**
	 * Sets the vms acks.
	 * 
	 * @param vmsAcks the new vms acks
	 */
	protected void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}

	/**
	 * Increment vms acks.
	 */
	protected void incrementVmsAcks() {
		vmsAcks++;
	}

	/**
	 * Gets the vms destroyed.
	 * 
	 * @return the vms destroyed
	 */
	protected int getVmsDestroyed() {
		return vmsDestroyed;
	}

	/**
	 * Sets the vms destroyed.
	 * 
	 * @param vmsDestroyed the new vms destroyed
	 */
	protected void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}

	/**
	 * Gets the datacenter ids list.
	 * 
	 * @return the datacenter ids list
	 */
	protected List<Integer> getDatacenterIdsList() {
		return datacenterIdsList;
	}

	/**
	 * Sets the datacenter ids list.
	 * 
	 * @param datacenterIdsList the new datacenter ids list
	 */
	protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
	}

	/**
	 * Gets the vms to datacenters map.
	 * 
	 * @return the vms to datacenters map
	 */
	protected Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}

	/**
	 * Sets the vms to datacenters map.
	 * 
	 * @param vmsToDatacentersMap the vms to datacenters map
	 */
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}

	/**
	 * Gets the datacenter characteristics list.
	 * 
	 * @return the datacenter characteristics list
	 */
	protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
		return datacenterCharacteristicsList;
	}

	/**
	 * Sets the datacenter characteristics list.
	 * 
	 * @param datacenterCharacteristicsList the datacenter characteristics list
	 */
	protected void setDatacenterCharacteristicsList(
			Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
		this.datacenterCharacteristicsList = datacenterCharacteristicsList;
	}

	/**
	 * Gets the datacenter requested ids list.
	 * 
	 * @return the datacenter requested ids list
	 */
	protected List<Integer> getDatacenterRequestedIdsList() {
		return datacenterRequestedIdsList;
	}

	/**
	 * Sets the datacenter requested ids list.
	 * 
	 * @param datacenterRequestedIdsList the new datacenter requested ids list
	 */
	protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}
	
	public void submitGeoRegions(List<List<Datacenter>> geoRegions) {
		GeoRegions = geoRegions;
	}
	
	// Method to write data to a specific row and column
    public static void writeToCSV(int rowNum, int colNum, Object data) throws IOException {
        List<List<String>> rows = new ArrayList<>();

        // Read the existing CSV file into memory
        BufferedReader br = new BufferedReader(new FileReader(FILE_PATH));
        String line;
        while ((line = br.readLine()) != null) {
            rows.add(new ArrayList<>(Arrays.asList(line.split(","))));
        }
        br.close();

        // Ensure the row exists, add new rows if needed
        while (rows.size() <= rowNum) {
            rows.add(new ArrayList<>());
        }

        // Ensure the row has enough columns, add empty cells if needed
        while (rows.get(rowNum).size() <= colNum) {
            rows.get(rowNum).add("");
        }

        // Set the value at the specified row and column
        rows.get(rowNum).set(colNum, String.valueOf(data));

        // Write the updated rows back to the CSV
        BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH));
        for (List<String> row : rows) {
            bw.write(String.join(",", row));
            bw.newLine();
        }
        bw.close();
    }

    // Method to find row number based on a value in a specific column
    public static int findRowByColumnValue(int colNum, Object value) throws IOException {
        List<List<String>> rows = new ArrayList<>();

        // Read the existing CSV file into memory
        BufferedReader br = new BufferedReader(new FileReader(FILE_PATH));
        String line;
        while ((line = br.readLine()) != null) {
            rows.add(new ArrayList<>(Arrays.asList(line.split(","))));
        }
        br.close();

        // Search through each row in the specified column
        for (int rowNum = 0; rowNum < rows.size(); rowNum++) {
            List<String> row = rows.get(rowNum);
            if (row.size() > colNum && row.get(colNum).equals(String.valueOf(value))) {
                return rowNum;  // Return the row number where the value is found
            }
        }

        return -1;  // Return -1 if the value is not found
    }

}