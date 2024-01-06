package org.cloudsimplus.examples.custom;

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
// import org.cloudsimplus.allocationpolicies;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicy;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyAbstract;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.HostResourceStats;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
/**
 * A <b>Round-Robin VM allocation policy</b>
 * which finds the next Host having suitable resources to place a given VM
 * in a circular way. That means when it selects a suitable Host to place a VM,
 * it moves to the next suitable Host when a new VM has to be placed.
 * This is a high time-efficient policy with a best-case complexity O(1)
 * and a worst-case complexity O(N), where N is the number of Hosts.
 *
 * <p>
 *     <b>NOTES:</b>
 *     <ul>
 *         <li>This policy doesn't perform optimization of VM allocation by means of VM migration.</li>
 *         <li>It has a low computational complexity (high time-efficient) but may return
 *         and inactive Host that will be activated, while there may be active Hosts
 *         suitable for the VM.</li>
 *         <li>Despite the low computational complexity, such a policy will increase the number of active Hosts,
 *         that increases power consumption.</li>
 *     </ul>
 * </p>
 *
 * @author Manoel Campos da Silva Filho
 * @since CloudSim Plus 4.4.2
 */
public class CustomVmAllocationPolicy extends VmAllocationPolicyAbstract implements VmAllocationPolicy {
    /**
     * The index of the last host used to place a VM.
     */
    private int lastHostIndex;
    private List<Host> hostlList;

    
	private double[][] POP_SOL_DIST; 		// Population/Solution (BATS x SOL_SIZE) 
	private double[][] V; 		// Velocities (BATS x SOL_SIZE)
	private double[][] FRE; 	// Frequency : 0 to F_MAX (BATS x 1)
	private double[] FIT;		// Fitness (BATS)
	private double PR; 			// Pulse Rate : 0 to 1
	private double L; 			// Louadness : L_MIN to L_MAX
	private double[][] lb;		// Lower bound (1 x SOL_SIZE)
	private double[][] ub;		// Upper bound (1 x SOL_SIZE)
	private double fmin; 		// Minimum fitness from FIT
 	private double[] BEST;			// Best solution array from POP_SOL_DIST (SOL_SIZE)	

	private final int BATS; 		// Number of bats
	private final int MAX; 		// Number of iterations
	private final double F_MIN = 0.0;
	private final double F_MAX = 2.0;
	private final double L_MIN;
	private final double L_MAX;
	private final double PR_MIN;
	private final double PR_MAX; 
	private final int SOL_SIZE = 10;
	
	private final Random rand = new Random();

    @Override
    protected Optional<Host> defaultFindHostForVm(final Vm vm) {
        final var hostList = getHostList();
        /* The for loop just defines the maximum number of Hosts to try.
         * When a suitable Host is found, the method returns immediately. */
        final int maxTries = hostList.size();
        for (Host host : hostlList) {
            final HostResourceStats cpuStats = host.getCpuUtilizationStats();
            
            final double utilizationPercentMean = cpuStats.getMean();
            
            final double watts = host.getPowerModel().getPower(utilizationPercentMean);
            // System.err.println(utilizationPercentMean);
            // System.err.println(host.getBw());
            // System.err.println(host.getRam());
        } 
        for (int i = 0; i < maxTries; i++) {
            final var host = hostList.get(lastHostIndex);
            //Different from the FirstFit policy, it always increments the host index.
            lastHostIndex = ++lastHostIndex % hostList.size();
            // host.getResource(null)
            // System.err.println(host.getBwUtilization());
            int bw = extractUsedAndTotal(host.getBw().toString())[0];

            int ram = extractUsedAndTotal(host.getRam().toString())[0];
            // System.err.println(host.getRam());
            // System.err.println(host.getCpuPercentRequested());
            // System.err.println(host.getPowerModel().getPower());
            // System.err.println(host.getCpuMipsUtilization());
            // System.err.println(host.getVmList().size());
            
            if (host.isSuitableForVm(vm)) {
                return Optional.of(host);
            }
        }

        return Optional.empty();
    }
    public CustomVmAllocationPolicy(List<Host> hostList){
        hostlList = hostList;
        this.BATS = 20;
		this.MAX = 100000;
		this.PR_MAX = 1.0;
		this.PR_MIN = 0.0;
		this.L_MAX = 1.0;
		this.L_MIN = 0.0;
        this.initialize();
    }
    private void initialize(){
        
		this.POP_SOL_DIST = new double[BATS][SOL_SIZE];
		this.V = new double[BATS][SOL_SIZE];
		this.FRE = new double[BATS][1];
		this.FIT = new double[BATS];
		this.PR = (PR_MAX + PR_MIN) / 3;
		this.L = (L_MIN + L_MAX) / 3;

		// Initialize bounds
		this.lb = new double[1][SOL_SIZE];
		for ( int i = 0; i < SOL_SIZE; i++ ){
			this.lb[0][i] = -2.0;
		}
		this.ub = new double[1][SOL_SIZE];
		for ( int i = 0; i < SOL_SIZE; i++ ){
			this.ub[0][i] = 2.0;
		}

		// Initialize FRE and V
		for ( int i = 0; i < BATS; i++ ){
			this.FRE[i][0] = 0.0;
		}
		for ( int i = 0; i < BATS; i++ ){
			for ( int j = 0; j < SOL_SIZE; j++ ) {
				this.V[i][j] = 0.0;
			}
		}

		// Initialize POP_SOL_DIST
		for ( int i = 0; i < BATS; i++ ){
			for ( int j = 0; j < SOL_SIZE; j++ ){
				this.POP_SOL_DIST[i][j] = lb[0][j] + (ub[0][j] - lb[0][j]) * rand.nextDouble(); 
			}
			this.FIT[i] = objective(0,POP_SOL_DIST[i],0);
		}

		// Find initial best solution
		int fmin_i = 0;
		for ( int i = 0; i < BATS; i++ ){
			if ( FIT[i] < FIT[fmin_i] )
				fmin_i = i;
		}

		// Store minimum fitness and it's index.
		// BEST holds the best solution array[1xD]
		this.fmin = FIT[fmin_i];
		this.BEST = POP_SOL_DIST[fmin_i]; // (1xD)
    }
    private static int[] extractUsedAndTotal(String input) {
        int[] result = new int[2];
        Pattern pattern = Pattern.compile("used (\\d+) of (\\d+)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            result[0] = Integer.parseInt(matcher.group(1)); // Used value
            result[1] = Integer.parseInt(matcher.group(2)); // Total value
        }

        return result;
    }
    
    public static double objective(double sla, double[] rrr, double pc) {
        double omega1 = 0.1;
        double omega2 = 0.8;
        double omega3 = 0.1;
        if(omega1+ omega2 +omega3 > 1){
//         You need to make sure that w1 +w2+ w3 =1
//          So you can decide the weights accordingly
            try {
                throw new Exception(new Throwable());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return ((omega1* sla) +(omega2 * rrr) +(omega3* pc)) / 3  ;
    }
}

