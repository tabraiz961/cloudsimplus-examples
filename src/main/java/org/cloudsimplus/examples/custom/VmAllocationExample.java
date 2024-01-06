
package org.cloudsimplus.examples.custom;

import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
// import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;

public class VmAllocationExample {

    public static void main(String[] args) {
        // Number of hosts in the datacenter
        int numberOfHosts = 3;

        // Number of PEs (Processing Elements) in each host
        int numberOfPes = 4;

        // Create a list to store hosts
        List<Host> hostList = new ArrayList<>();

        // Create hosts and add them to the host list
        for (int i = 0; i < numberOfHosts; i++) {
            List<Pe> peList = new ArrayList<>();
            for (int j = 0; j < numberOfPes; j++) {
                peList.add(new PeSimple(1000)); // 1000 MIPS (Million Instructions Per Second) for each PE
            }
            Host host = new HostSimple(peList);
            hostList.add(host);
        }

        // Create a datacenter with the host list
        CloudSimPlus simulation = new CloudSimPlus();
        Datacenter datacenter = new DatacenterSimple(simulation, hostList, new VmAllocationPolicyBestFit());

        // Create a CloudSim object

        // Create a DatacenterBroker
        DatacenterBroker broker = new DatacenterBrokerSimple(simulation);

        // Create VMs and add them to the broker
        List<Vm> vmList = new ArrayList<>();
        for (int i = 0; i < numberOfPes; i++) {
            Vm vm = new VmSimple(1000, numberOfPes); // 1000 MIPS and 1 PE for each VM
            vmList.add(vm);
        }
        broker.submitVmList(vmList);

        // Create Cloudlets and add them to the broker
        List<Cloudlet> cloudletList = new ArrayList<>();
        for (int i = 0; i < numberOfPes; i++) {
            Cloudlet cloudlet = new CloudletSimple(1000, 1); // 1000 MI (Million Instructions) and length of 1 for each Cloudlet
            cloudletList.add(cloudlet);
        }
        broker.submitCloudletList(cloudletList);

        // Bind the broker to the datacenter
        broker.bindCloudletToVm(cloudletList.get(0), vmList.get(0));

        // Start the simulation
        simulation.start();

        // Print the results
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        for (Cloudlet cloudlet : finishedCloudlets) {
            System.out.println("Cloudlet " + cloudlet.getId() + " finished at " + cloudlet.getFinishTime());
        }
    }
}
