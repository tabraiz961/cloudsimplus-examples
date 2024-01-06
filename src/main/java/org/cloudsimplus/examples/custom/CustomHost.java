package org.cloudsimplus.examples.custom;


import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.resources.Pe;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
public class CustomHost extends HostSimple {
    private ArrayList<Pe> peList;
    public CustomHost(long ramCapacity, long bwCapacity, long storage, ArrayList<Pe> peList) {
        super( ramCapacity, bwCapacity, storage, null);
        this.peList = peList;
    }

    public CustomHost createRandomHost() {
        // Create a Random object
        Random random = new Random();

        // Define the range for random bandwidth and memory consumption
        long minBandwidth = 1000;
        long maxBandwidth = 2000;
        long minMemory = 4096;
        long maxMemory = 8192;
        long storage = 2000;

        // Generate random values within the specified range
        long randomBandwidth = minBandwidth + random.nextLong() % (maxBandwidth - minBandwidth + 1);
        long randomMemory = minMemory + random.nextLong() % (maxMemory - minMemory + 1);

        // Create a new CustomHost with random values
        return new CustomHost(randomMemory, randomBandwidth,(int)(random.nextDouble() * storage),  this.peList);
    }

}
