package org.cloudbus.cloudsim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.core.CloudSim;

/**
 * Simple VmAllocationPolicy that directly assigns a VM to the specified host 
 * rather than making a decision.
 */
public class ContainerAllocationPolicyFixedHost extends org.cloudbus.cloudsim.VmAllocationPolicy {
	  
	private final Map<String, Host> vm_table = new HashMap<String, Host>();
	
	public ContainerAllocationPolicyFixedHost(List<? extends Host> list) {
		super(list);
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {

		boolean vm_allocated = false;

        Host host = vm.getHost();  // Fixed host
        
        if (host != null && allocateHostForVm(vm, host)) {  
        	vm_allocated = true;
        }

		return vm_allocated;
	}

	@Override
	public boolean allocateHostForVm(Vm vm, Host host) 
	{
		if (host != null && host.vmCreate(vm)) 
		{
			vm_table.put(vm.getUid(), host);
			//host.getVmList().add(vm);
			Log.formatLine("%.4f: Container #" + vm.getId() + " has been allocated to the host#" + host.getId() + 
					" datacenter #" + host.getDatacenter().getName(), 
					CloudSim.clock());
			
			//get % of Share of Host for the Container
			double temp=(vm.getMips()/host.getTotalMips())*host.getBusyPower(); // Formula = VM_MIPS / HostMIPS * HostBusyPower
			vm.setBusyPower(temp);
			vm.setIdlePower(host.getIdlePower());
			
			//System.out.println("Host ID = "+host.getId()+" Container ID = "+vm.getId());
			//System.out.println("Host MIPS = "+host.getTotalMips()+" Container mips = "+vm.getMips());
			//System.out.println("Host Busy Power = "+host.getBusyPower()+" Container Busy Power "+temp);
			//System.out.println("Host Idle Power = "+host.getIdlePower()+" Container Idle Power "+vm.getIdlePower());
			
			return true;
		}
		return false;
	}

	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		return null;
	}

	@Override
	public void deallocateHostForVm(Vm vm) {
		Host host = this.vm_table.remove(vm.getUid());

		if (host != null) {
			host.vmDestroy(vm);
		}
	}

	@Override
	public Host getHost(Vm vm) {
		return this.vm_table.get(vm.getUid());
	}

	@Override
	public Host getHost(int vmId, int userId) {
		return this.vm_table.get(Vm.getUid(userId, vmId));
	}
}