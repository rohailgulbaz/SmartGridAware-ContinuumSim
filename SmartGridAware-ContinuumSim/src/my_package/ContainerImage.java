package my_package;

public class ContainerImage {
    int mips;
    int ram;
    long size;
    long bw;
    int pesNumber;
    String vmm;
    int serviceType;

    public ContainerImage(int mips, int ram, long size, long bw, int pesNumber, String vmm, int serviceType) {
        this.mips = mips;
        this.ram = ram;
        this.size = size;
        this.bw = bw;
        this.pesNumber = pesNumber;
        this.vmm = vmm;
        this.serviceType = serviceType;
    }
    
    public int getMips() {
    	return mips;
    }
    public int getRam() {
    	return ram;
    }
    public long getSize() {
    	return size;
    }
    public long getBw() {
    	return bw;
    }
    public int getPesNumber() {
    	return pesNumber;
    }
    public String getVmm() {
    	return vmm;
    }
    public int getServiceType() {
    	return serviceType;
    }
}
