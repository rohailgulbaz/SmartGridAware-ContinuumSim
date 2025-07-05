package my_package;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class DynamicTaskArrivalExampleCPriorityAustraliaE {
	private static int containersTypes = 19;
    public static void main(String[] args) throws Exception {
        // Initialize CloudSim
        CloudSim.init(1, Calendar.getInstance(), false);

        NodeFactory nf=new NodeFactory();
        
        int[] RAMperHost = new int[] { //all hosts have same RAM (25600/1024=25GB) but Container specifications can vary.
        		17592, 17592, 17592,  17592, 17592,
        		//8796, 17592, 17592,  8796, 8796,
        		25600, 25600, 25600, 25600, 25600,
    		    25600, 25600, 25600, 25600, 25600,
    		    25600, 25600, 25600, 25600, 25600,
    		    25600, 25600, 25600, 25600, 25600,
    		    25600, 25600, 25600, 25600, 25600,
    		    25600, 25600, 25600, 25600, 25600,
    		    25600, 25600, 25600, 25600, 25600,
    		    25600, 25600, 25600, 25600, 25600,
    		    25600, 25600, 25600, 25600, 25600,
    		    25600, 25600, 25600, 25600, 25600    		     
        }; // Assuming one array element per host
        
        long[] StorageperHost = {
        	    100000000, 100000000, 100000000, 100000000, 100000000, 
        	    100000000, 100000000, 100000000, 100000000, 100000000, 
        	    100000000, 100000000, 100000000, 100000000, 100000000, 
        	    100000000, 100000000, 100000000, 100000000, 100000000, 
        	    100000000, 100000000, 100000000, 100000000, 100000000, 
        	    100000000, 100000000, 100000000, 100000000, 100000000, 
        	    100000000, 100000000, 100000000, 100000000, 100000000,
        	    100000000, 100000000, 100000000, 100000000, 100000000, 
        	    100000000, 100000000, 100000000, 100000000, 100000000, 
        	    100000000, 100000000, 100000000, 100000000, 100000000
        	}; //100000000/1000=100TB

    	int[] BWperHost = {
    	    1000000, 1000000, 1000000, 1000000, 1000000,
    	    1000000, 1000000, 1000000, 1000000, 1000000,
    	    1000000, 1000000, 1000000, 1000000, 1000000,
    	    1000000, 1000000, 1000000, 1000000, 1000000,
    	    1000000, 1000000, 1000000, 1000000, 1000000,
    	    1000000, 1000000, 1000000, 1000000, 1000000,
    	    1000000, 1000000, 1000000, 1000000, 1000000,
    	    1000000, 1000000, 1000000, 1000000, 1000000,
    	    1000000, 1000000, 1000000, 1000000, 1000000,
    	    1000000, 1000000, 1000000, 1000000, 1000000
    	}; //1TB

    	int[] MIPSperHost = {
    			14000, 17000, 20000, 18000, 18000,
    			//1500, 5000, 6000, 1200, 600,
    		    //4000, 5000, 6000, 7000, 6000,
    			//7000, 19000, 
    			//51200, 51200, 51200, 51200, 51200,
    			51200, 51200, 51200, 51200, 51200,
    		    51200, 51200, 51200, 51200, 51200,
    		    51200, 51200, 51200, 51200, 51200,
    		    51200, 51200, 51200, 51200, 51200,
    		    51200, 51200, 51200, 51200, 51200,
    		    51200, 51200, 51200, 51200, 51200,
    		    51200, 51200, 51200, 51200, 51200,
    		    51200, 51200, 51200, 51200, 51200,
    		    51200, 51200, 51200, 51200, 51200,
    		    51200, 51200, 51200, 51200, 51200
    	};
    	
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;
        
        int numDualCoreHosts = 1; 
        int numQuadCoreHosts = 2; 
        // Create a datacenter
        Datacenter datacenter1 = nf.createDatacenterDynamicTask(1, "Datacenter_1", 0.0, numDualCoreHosts, numQuadCoreHosts, RAMperHost, StorageperHost, BWperHost, MIPSperHost, cost, costPerMem, costPerStorage, costPerBw); //onsite power is set to 0.0 for this experiment
        //ID assigned to datacenter in above line is useless, always use the Unique ID assigned by SimEntity
         
        numDualCoreHosts = 1; 
        numQuadCoreHosts = 2; 
        // Create a datacenter
        Datacenter datacenter2 = nf.createDatacenterDynamicTask(2, "Datacenter_2", 0.0, numDualCoreHosts, numQuadCoreHosts, RAMperHost, StorageperHost, BWperHost, MIPSperHost, cost, costPerMem, costPerStorage, costPerBw); //onsite power is set to 0.0 for this experiment
        //ID assigned to datacenter in above line is useless, always use the Unique ID assigned by SimEntity
        
        numDualCoreHosts = 2; 
        numQuadCoreHosts = 1; 
        // Create a datacenter
        Datacenter datacenter3 = nf.createDatacenterDynamicTask(3, "Datacenter_3", 0.0, numDualCoreHosts, numQuadCoreHosts, RAMperHost, StorageperHost, BWperHost, MIPSperHost, cost, costPerMem, costPerStorage, costPerBw); //onsite power is set to 0.0 for this experiment
        //ID assigned to datacenter in above line is useless, always use the Unique ID assigned by SimEntity
        
        numDualCoreHosts = 2; 
        numQuadCoreHosts = 1; 
        // Create a datacenter
        Datacenter datacenter4 = nf.createDatacenterDynamicTask(4, "Datacenter_4", 0.0, numDualCoreHosts, numQuadCoreHosts, RAMperHost, StorageperHost, BWperHost, MIPSperHost, cost, costPerMem, costPerStorage, costPerBw); //onsite power is set to 0.0 for this experiment
        //ID assigned to datacenter in above line is useless, always use the Unique ID assigned by SimEntity
        
        numDualCoreHosts = 1; 
        numQuadCoreHosts = 2;
        // Create a datacenter
        Datacenter datacenter5 = nf.createDatacenterDynamicTask(5, "Datacenter_5", 0.0, numDualCoreHosts, numQuadCoreHosts, RAMperHost, StorageperHost, BWperHost, MIPSperHost, cost, costPerMem, costPerStorage, costPerBw); //onsite power is set to 0.0 for this experiment
        //ID assigned to datacenter in above line is useless, always use the Unique ID assigned by SimEntity

        numDualCoreHosts = 1; 
        numQuadCoreHosts = 2;
        // Create a datacenter
        Datacenter datacenter6 = nf.createDatacenterDynamicTask(6, "Datacenter_6", 0.0, numDualCoreHosts, numQuadCoreHosts, RAMperHost, StorageperHost, BWperHost, MIPSperHost, cost, costPerMem, costPerStorage, costPerBw); //onsite power is set to 0.0 for this experiment
        //ID assigned to datacenter in above line is useless, always use the Unique ID assigned by SimEntity

        numDualCoreHosts = 2; 
        numQuadCoreHosts = 1;
        // Create a datacenter
        Datacenter datacenter7 = nf.createDatacenterDynamicTask(7, "Datacenter_7", 0.0, numDualCoreHosts, numQuadCoreHosts, RAMperHost, StorageperHost, BWperHost, MIPSperHost, cost, costPerMem, costPerStorage, costPerBw); //onsite power is set to 0.0 for this experiment
        //ID assigned to datacenter in above line is useless, always use the Unique ID assigned by SimEntity
        
        numDualCoreHosts = 2; 
        numQuadCoreHosts = 1;
        // Create a datacenter
        Datacenter datacenter8 = nf.createDatacenterDynamicTask(8, "Datacenter_8", 0.0, numDualCoreHosts, numQuadCoreHosts, RAMperHost, StorageperHost, BWperHost, MIPSperHost, cost, costPerMem, costPerStorage, costPerBw); //onsite power is set to 0.0 for this experiment
        //ID assigned to datacenter in above line is useless, always use the Unique ID assigned by SimEntity

        
        // Create a broker
        DatacenterBrokerNormalFaaSCPriorityAustraliaE broker3 = nf.createBrokerNormalFaaSCPriorityAustraliaE ("Broker3"); //createBroker();
        int brokerId3 = broker3.getId();
        System.out.println("broker Id= "+brokerId3);
        
        // Create cloudlets (tasks) with specific arrival times
        List<Cloudlet> cloudletList = createCloudlets("AzureFunctionTraces.csv");


        // Assign cloudlets to broker
        broker3.submitCloudletList(cloudletList);
        
      //DC list for Broker: DC no. 1
		List<Datacenter> specificList1 = new ArrayList<>();
		specificList1.add(datacenter1); //cloudsim DC no. 
        specificList1.add(datacenter2);
        //specificList1.add(datacenter3);
        //specificList1.add(datacenter4);
        specificList1.add(datacenter5);
        specificList1.add(datacenter6);
        specificList1.add(datacenter7);
        specificList1.add(datacenter8);
        
        //specificList1.add(datacenter9);
        //specificList1.add(datacenter10);
		//*/
		broker3.submitDCList(specificList1);
		

		//Regions
		List<List<Datacenter>> GeoRegions = new ArrayList<>();
		
		List<Datacenter> Arizona = new ArrayList<>();
		Arizona.add(datacenter1);
		Arizona.add(datacenter2);
		//GeoRegions.add(Arizona);
		
		List<Datacenter> USSouthWest = new ArrayList<>();
		USSouthWest.add(datacenter3);
		USSouthWest.add(datacenter4);
		//GeoRegions.add(USSouthWest);
	
		List<Datacenter> NewSouthWales = new ArrayList<>();
		NewSouthWales.add(datacenter1);
		NewSouthWales.add(datacenter2);
		NewSouthWales.add(datacenter5);
		GeoRegions.add(NewSouthWales);
		
		List<Datacenter> Victoria = new ArrayList<>();
		Victoria.add(datacenter6);
		Victoria.add(datacenter7);
		Victoria.add(datacenter8);
		GeoRegions.add(Victoria);
		broker3.submitGeoRegions(GeoRegions);
		//Regions till here
		
		
    	//Container Images differentiated through Service Type
    	//These are just specifications not yet registered as running Containers in the Simulation
		
        int pesNumber = 1;
        String vmm = "Xen";
        long[] size = {
    			100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 
	    	    100, 100, 100, 100, 100, 100, 100, 100, 100, 100
    	};
    	long[] bw= {
	    	    100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 
	    	    100, 100, 100, 100, 100, 100, 100, 100, 100, 100
    	};
    	int[] mipsPerContainer = {
    			//100, 200, 300, 400, 500, 600, 700, 800, 900, 1000,
        	    //1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000
        	    
        	    //100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 200, 
        	    //100, 110, 120, 130, 140, 150, 160, 170, 180, 190, 200 
        	    
        	    202, 201, 201, 200, 203, 202, 203, 204, 200, 202,
        	    202, 201, 201, 200, 203, 202, 203, 204, 200, 202,
        	    
        	    //102, 101, 101, 100, 103, 102, 103, 104, 100, 102
        };
    	int[] ram = {
    			128, 256, 512, 128, 256, 512, 128, 256, 512, 128, 
    			128, 256, 512, 128, 256, 512, 128, 256, 512, 128 
	    };
    	int[] service = {
    			0, 1 , 2, 3, 4, 5, 6, 7, 8, 9,
    	    	10, 11, 12, 13, 14, 15, 16, 17, 18, 19
    	};
    	//List<ContainerImage> imageList=new ArrayList<ContainerImage>();
    	List<Map.Entry<Integer, ContainerImage>> imageList = new ArrayList<>();
    	for(int i=0; i<containersTypes; i++) {
    		ContainerImage ci = new ContainerImage(mipsPerContainer[i], ram[i], size[i], bw[i], pesNumber, vmm, service[i]);
    		imageList.add(new AbstractMap.SimpleEntry<>(service[i], ci));
    		//System.out.println("services "+ service[i]);
    	}
    	broker3.submitImageList(imageList);
    	
        // Start simulation
        CloudSim.startSimulation();

        // Finalize the simulation
        CloudSim.stopSimulation();

        // Print results
        //printResults(broker);
		List<Cloudlet> newList3 = broker3.getCloudletReceivedList();
		printCloudletList(newList3);
    }


      public static List<Cloudlet> createCloudlets(String csvFilePath) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        long fileSize = 300;
        long outputSize = 300;
        int pesNumber = 1;
        UtilizationModel utilizationModel = new UtilizationModelFull();
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean firstLine = true;
            int cloudletId = 0; // Cloudlet IDs should be unique

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip the header row
                    continue;
                }

                String[] values = line.split(",");

                //if (values.length < 5) continue; // Ensure valid row
                if (values.length < 6) continue; // Ensure valid row

                int functionId = Integer.parseInt(values[0].trim()); // Column A
                double arrivalTime = Double.parseDouble(values[3].trim()); // Column D
                //int millionInstructions = (int) Double.parseDouble(values[4].trim());  // Column E
                int millionInstructions = (int) Double.parseDouble(values[5].trim()); //Column F
                
                Cloudlet cloudlet = new Cloudlet(
                    cloudletId++, millionInstructions, pesNumber, fileSize, outputSize,
                    utilizationModel, utilizationModel, utilizationModel, 1, 500, 500
                );
                
                cloudlet.setSubmissionDelay(arrivalTime);
                cloudlet.setService(functionId);
                cloudletList.add(cloudlet);
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return cloudletList;
    }

    
	/**
	 * Prints the Cloudlet objects.
	 *
	 * @param list list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Job ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "Container ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + cloudlet.getResourceName(cloudlet.getResourceId()) + indent
						+ indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent + indent
						+ dft.format(cloudlet.getFinishTime()));
			}
		}
	}
}
