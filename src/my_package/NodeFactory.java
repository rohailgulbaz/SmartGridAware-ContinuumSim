package my_package;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.ContainerAllocationPolicyFixedHost; 
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

//NodeFactory creates cloud node, fog node, or edge node; practically it creates a datacenter for cloud, fog or edge.
public class NodeFactory {
    private int hostId=-1;
    public List<Host> globalHostList = new ArrayList<Host>();

	public NodeFactory() {
		//System.out.println("inside NodeFactory constructor");
	}
    
	
	public Datacenter createDatacenterDynamicTask(int id, String name, double Onsite, int numDualCoreHosts, int numQuadCoreHosts, int []RAMperHost, long []StorageperHost, int []BWperHost, int[] MIPSperHost, double cost, double costPerMem, double costPerStorage, double costPerBw) {
		
			//We need to create a list to store our machines
			List<Host> hostList = new ArrayList<Host>();
		    //A Machine contains one or more processing elements PEs or CPUs/Cores. Create a list to store these PEs before creating a Machine.		
			List<List<Pe>> peList = new ArrayList<>();
			
			double busyPower= 540; //540 watts or joules per second                       
			double idlePower= 60; //60 w i.e 10% of full  
	 		//Create processors
	 		for (int i = 0; i < numDualCoreHosts; i++) {
	 			hostId++;
	 			List<Pe> peList1 = new ArrayList<Pe>(); //a list for dual-cores machines
		        peList1.add(new Pe(0, new PeProvisionerSimple(MIPSperHost[i]/2))); // need to store Pe id and MIPS
		        peList1.add(new Pe(1, new PeProvisionerSimple(MIPSperHost[i]/2)));
		        System.out.println("MipsPerHost="+(MIPSperHost[i]/2));
		        peList.add(peList1);
		        
		 		System.out.println("Dual Core Host id = "+hostId+" DC id is "+id);
		        
		        hostList.add(
		        		new Host(
	                        hostId, busyPower/2, idlePower,
	                        new RamProvisionerSimple(RAMperHost[i]),
	                        new BwProvisionerSimple(BWperHost[i]),
	                        StorageperHost[i],
	                        peList1,
	                       // new VmSchedulerTimeShared(peList1), new EdgeLinearPowerModel(busyPower, idlePower)
	                        new VmSchedulerTimeShared(peList1)
	                    )
	                ); 
		        
		        globalHostList.add(hostList.getLast());
		    }
		    
	 		busyPower= 675; //675 watts or joules per second       
			idlePower= 75; //75w i.e 10% of full      
	 		// Create processors 
		    for (int i = 0; i < numQuadCoreHosts; i++) {
		    	hostId++;
		    	List<Pe> peList2 = new ArrayList<Pe>(); //Another list, for a quad-cores machines
			    
		        peList2.add(new Pe(0, new PeProvisionerSimple(MIPSperHost[i+numDualCoreHosts]/4))); // need to store Pe id and MIPS
		        peList2.add(new Pe(1, new PeProvisionerSimple(MIPSperHost[i+numDualCoreHosts]/4)));
		        peList2.add(new Pe(2, new PeProvisionerSimple(MIPSperHost[i+numDualCoreHosts]/4)));
		        peList2.add(new Pe(3, new PeProvisionerSimple(MIPSperHost[i+numDualCoreHosts]/4)));
		        peList.add(peList2);
		        
		     	System.out.println("Quad Core Host id = "+hostId+" DC id is "+id);		        
		        
				hostList.add(
	         			new Host(
	                        hostId, busyPower, idlePower,
	                        new RamProvisionerSimple(RAMperHost[i+numDualCoreHosts]),
	                        new BwProvisionerSimple(BWperHost[i+numDualCoreHosts]),
	                        StorageperHost[i+numDualCoreHosts],
	                        peList2,
	                        new VmSchedulerTimeShared(peList2)
	                    )
	                ); 
				
				globalHostList.add(hostList.getLast());
		    }
		    
		    // Create data center characteristics
		    String arch = "x86";
		    String os = "Linux";
		    String vmm = "Xen";
		    double time_zone = 10.0;
		    LinkedList<Storage> storageList = new LinkedList<>();

		    DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		    // Create the data center
		    Datacenter datacenter = null;
		    try {
		    	datacenter = new Datacenter(id, name, characteristics, new ContainerAllocationPolicyFixedHost(hostList), storageList, 0, Onsite);
			    } catch (Exception e) {
		        e.printStackTrace();
		    }

		    return datacenter;
	}	
	
	public DatacenterBrokerNormalFaaSCPriority createBrokerNormalFaaSCPriority(String name) {
	      DatacenterBrokerNormalFaaSCPriority broker = null;
			try {
				broker = new DatacenterBrokerNormalFaaSCPriority(name);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return broker;
		}
	
	public DatacenterBrokerNormalFaaSCPriorityLessHost createBrokerNormalFaaSCPriorityLessHost(String name) {
	      DatacenterBrokerNormalFaaSCPriorityLessHost broker = null;
			try {
				broker = new DatacenterBrokerNormalFaaSCPriorityLessHost(name);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return broker;
		}
	public DatacenterBrokerNormalFaaSCPriorityLessDC createBrokerNormalFaaSCPriorityLessDC(String name) {
	      DatacenterBrokerNormalFaaSCPriorityLessDC broker = null;
			try {
				broker = new DatacenterBrokerNormalFaaSCPriorityLessDC(name);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return broker;
		}
	public DatacenterBrokerNormalFaaSCPriorityAustraliaE createBrokerNormalFaaSCPriorityAustraliaE(String name) {
	      DatacenterBrokerNormalFaaSCPriorityAustraliaE broker = null;
			try {
				broker = new DatacenterBrokerNormalFaaSCPriorityAustraliaE(name);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return broker;
		}
	public DatacenterBrokerNormalFaaSCPriorityAustraliaM createBrokerNormalFaaSCPriorityAustraliaM(String name) {
	      DatacenterBrokerNormalFaaSCPriorityAustraliaM broker = null;
			try {
				broker = new DatacenterBrokerNormalFaaSCPriorityAustraliaM(name);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return broker;
		}
	public DatacenterBrokerNormalFaaSCPriorityUSA createBrokerNormalFaaSCPriorityUSA(String name) {
	      DatacenterBrokerNormalFaaSCPriorityUSA broker = null;
			try {
				broker = new DatacenterBrokerNormalFaaSCPriorityUSA(name);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			return broker;
		}
	
	
	
	
	/**
	 * Creates virtual machines in the datacenter.
	 * 
	 * @param userId         The ID of the user creating the VMs.
	 * @param vms            The number of virtual machines to create.
	 * @param idShift        The shift value for generating VM IDs.
	 * @param mipsPerVM      An array containing the MIPS value for each VM.
	 * @param RAMperVM       An array containing the RAM size for each VM. VM memory (MB)
	 * @param StorageperVM   An array containing the storage size for each VM. Image size (MB)
	 * @param BWperVM        An array containing the bandwidth for each VM.
	 * @param pesNumber      The number of processing elements (CPU cores) for each VM.
	 * @param vmm            The virtual machine monitor (VMM) name.
	 * @return               A list of created virtual machines.
	 */
	public List<Vm> createVM(int userId, int vms, int idShift, int[] mipsPerVM, int[] service, int[] RAMperVM, long[] StorageperVM, long[] BWperVM, int pesNumber, String vmm){
		ArrayList<Vm> list = new ArrayList<Vm>();
		
		
		// Create VMs
	    Vm[] vm = new Vm[vms];
	    for (int i = 0; i < vms; i++) {
	        int mips = mipsPerVM[i]; // Assign MIPS value based on array
	        int cservice =  service[i]; // Assign service based on array
	        int ram = RAMperVM[i]; // Assign RAM value based on array
	        long storage = StorageperVM[i]; // Assign storage value based on array
	        long bandwidth = BWperVM[i]; // Assign bandwidth value based on array
	        
	        vm[i] = new Vm(idShift + i, userId, mips, cservice, pesNumber, ram, bandwidth, storage, vmm, new CloudletSchedulerTimeShared());
	        list.add(vm[i]);
	    }              
		return list;
	}


	
}
