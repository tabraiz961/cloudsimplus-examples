package org.cloudsimplus.examples.custom;

import org.cloudsimplus.core.Simulation;
import org.cloudsimplus.utilizationmodels.UtilizationModel;

import java.util.Random;

public class RandomMemoryUtilizationModel implements UtilizationModel {

    private final double minUtilization;
    private final double maxUtilization;
    private final Random random;

    public RandomMemoryUtilizationModel(double minUtilization, double maxUtilization) {
        this.minUtilization = minUtilization;
        this.maxUtilization = maxUtilization;
        this.random = new Random();
    }

    @Override
    public double getUtilization(double time) {
        // Generate a random utilization between minUtilization and maxUtilization
        return minUtilization + (maxUtilization - minUtilization) * random.nextDouble();
    }
    @Override
    public Simulation getSimulation() {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public UtilizationModel setOverCapacityRequestAllowed(boolean allow) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Unit getUnit() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UtilizationModel setSimulation(Simulation simulation) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSimulation'");
    }

    @Override
    public double getUtilization() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getUtilization'");
    }

    @Override
    public boolean isOverCapacityRequestAllowed() {
        // TODO Auto-generated method stub
        return false;
    }
}

