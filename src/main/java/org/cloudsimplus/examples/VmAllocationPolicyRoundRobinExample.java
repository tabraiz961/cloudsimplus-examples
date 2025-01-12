/*
 * CloudSim Plus: A modern, highly-extensible and easier-to-use Framework for
 * Modeling and Simulation of Cloud Computing Infrastructures and Services.
 * http://cloudsimplus.org
 *
 *     Copyright (C) 2015-2021 Universidade da Beira Interior (UBI, Portugal) and
 *     the Instituto Federal de Educação Ciência e Tecnologia do Tocantins (IFTO, Brazil).
 *
 *     This file is part of CloudSim Plus.
 *
 *     CloudSim Plus is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     CloudSim Plus is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with CloudSim Plus. If not, see <http://www.gnu.org/licenses/>.
 */
package org.cloudsimplus.examples;

import org.cloudsimplus.allocationpolicies.VmAllocationPolicy;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyRoundRobin;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicySimple;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.examples.custom.CustomHost;
import org.cloudsimplus.examples.custom.CustomVmAllocationPolicy;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.power.models.PowerModelHostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelDynamic;
import org.cloudsimplus.vms.HostResourceStats;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * An example that show the usage of the {@link VmAllocationPolicyRoundRobin},
 * that cyclically places VMs into Hosts. This way, it places a VM into a Host
 * and moves to the next Host.
 * All hosts are not powered-on when created. As VMs need to be placed,
 * Hosts are activated on demand, as can be checked in the logs.
 *
 * <p>Keep in mind that such a policy is very naive and increases the number of active Hosts,
 * leading to higher power consumption.</p>
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 4.4.2
 */
public class VmAllocationPolicyRoundRobinExample {
    private static final int HOSTS = 4;
    private static final int HOST_PES = 8;

    private static final int VMS = 16;
    private static final int VM_PES = 2;

    private static final int CLOUDLETS = 16;
    private static final int CLOUDLET_PES = 2;
    private static final int CLOUDLET_LENGTH = 10000;
    /**
     * Defines the power a Host uses, even if it's idle (in Watts).
     */ 
    private static final double STATIC_POWER = 15;

    /**
     * The max power a Host uses (in Watts).
     */
    private static final int MAX_POWER = 50;

    private final CloudSimPlus simulation;
    private final DatacenterBroker broker0;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private Datacenter datacenter0;

    public static void main(String[] args) {
        new VmAllocationPolicyRoundRobinExample();
    }

    private VmAllocationPolicyRoundRobinExample() {
        /*Enables just some level of log messages.
          Make sure to import org.cloudsimplus.util.Log;*/
        //Log.setLevel(ch.qos.logback.classic.Level.WARN);

        simulation = new CloudSimPlus();
        datacenter0 = createDatacenter();

        //Creates a broker that is a software acting on behalf of a cloud customer to manage his/her VMs and Cloudlets
        broker0 = new DatacenterBrokerSimple(simulation);

        vmList = createVms();
        cloudletList = createCloudlets();
        broker0.submitVmList(vmList);
        broker0.submitCloudletList(cloudletList);

        simulation.start();
        // System.err.println(datacenter0.getHostList().size());
        // System.err.println();
        // for (Host host : datacenter0.getHostList()) {
        //     final HostResourceStats cpuStats = host.getCpuUtilizationStats();
            
        //     final double utilizationPercentMean = cpuStats.getMean();
            
        //     final double watts = host.getPowerModel().getPower(utilizationPercentMean);
        //     System.err.println(watts);
        //     // System.err.println(host.getBw());
        //     // System.err.println(host.getRam());
        // } 
        // for (Cloudlet cloudletSubmittedList : broker0.getCloudletSubmittedList()) {
        //     cloudletSubmittedList.getFinishedLengthSoFar()
        // }
        // final var cloudletFinishedList = broker0.getCloudletFinishedList();
        // cloudletFinishedList.sort(Comparator.comparingLong(cloudlet -> cloudlet.getVm().getId()));
        // new CloudletsTableBuilder(cloudletFinishedList).build();
    }

    /**
     * Creates a Datacenter and its Hosts.
     */
    private Datacenter createDatacenter() {
        final var hostList = new ArrayList<Host>(HOSTS);
        for(int i = 0; i < HOSTS; i++) {
            final var host = createHost();
            hostList.add(host);
        }

        return new DatacenterSimple(simulation, hostList, new CustomVmAllocationPolicy(hostList));
    }
    // private Optional<Host> batAlgoBased(VmAllocationPolicy allocationPolicy, Vm vm) {
    //     for (Host host : allocationPolicy.getHostList()) {
    //         System.err.println(host.getPowerModel().getPower());
    //     }
    //     return allocationPolicy
    //         .getHostList()
    //         .stream()
    //         .filter(host -> host.isSuitableForVm(vm))
    //         .min(Comparator.comparingInt(Host::getFreePesNumber));
    // }

    private Host createHost() {
        final var peList = new ArrayList<Pe>(HOST_PES);
        //List of Host's CPUs (Processing Elements, PEs)
        for (int i = 0; i < HOST_PES; i++) {
            //Uses a PeProvisionerSimple by default to provision PEs for VMs
            peList.add(new PeSimple(1000));
        }

        final long ram = (long)(2048 ); //in Megabytes
        final long bw = (long)(10000); //in Megabits/s
        final long storage = (long)(1000000); //in Megabytes
        /*
        Uses ResourceProvisionerSimple by default for RAM and BW provisioning
        and VmSchedulerSpaceShared for VM scheduling.
        */
        final var host = new HostSimple(ram, bw, storage, peList);
        host.setPowerModel(new PowerModelHostSimple(MAX_POWER, STATIC_POWER));
        host.enableUtilizationStats();
        return host;
    }

    /**
     * Creates a list of VMs.
     */
    private List<Vm> createVms() {
        final var list = new ArrayList<Vm>(VMS);
        Random random = new Random();
        for (int i = 0; i < VMS; i++) {
            //Uses a CloudletSchedulerTimeShared by default to schedule Cloudlets
            final Vm vm = new VmSimple(1000* random.nextDouble(), VM_PES);
            vm.setRam((long)(512* random.nextDouble())).setBw((long)(1000* random.nextDouble())).setSize((long)(10000* random.nextDouble()));
            list.add(vm);
        }

        return list;
    }

    /**
     * Creates a list of Cloudlets.
     */
    private List<Cloudlet> createCloudlets() {
        final var list = new ArrayList<Cloudlet>(CLOUDLETS);

        //UtilizationModel defining the Cloudlets use only 50% of any resource all the time
        
        for (int i = 0; i < CLOUDLETS; i++) {
            final var utilizationModel = new UtilizationModelDynamic((new Random()).nextDouble());
            // System.err.println(utilizationModel.getUtilization());
            final var cloudlet = new CloudletSimple(CLOUDLET_LENGTH, CLOUDLET_PES, utilizationModel);
            cloudlet.setSizes(1024);
            list.add(cloudlet);
        }

        return list;
    }
    private void createNewCloudletasds(){

    }
}
