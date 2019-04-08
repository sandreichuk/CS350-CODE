import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class simulator {
	double lambda;
	double response_time;
	
	//We initialize the values for lambda and response time to zero in the constructor
	public simulator() {
		lambda = 0.0;
		response_time = 0.0;	
	}
	
	// The exponential random generator from previous assignment
	public static double exp(double lambda) {
		Random num = new Random();
		//With the next line we generate the random value of CDF
		//between 0 and 1 that we will later use to compute x (result) 
		// according to the CDF formula for exponential distribution.
		double rand_cdf = num.nextDouble();
		//This formula represents the exponential CDF in terms of x (result).
		double result = Math.log(1.0 - rand_cdf)/(-lambda);
		return result;
	}
	//Our main non-static method for class simulator that only accepts time as one parameter
	public void simulate(double time) {
		
		//We initialize runsum time for the purpose of creating array
		// of arrivals and services for the desired interval "time"
		double runsum_time = 0.0;
		
		//We initialize variables arrival and ts to fill up the array later
		double arrival = 0.0;
		double ts = 0.0;
		
		//We initialize arrays arrivals and services to keep arrival and ts.
		double[] arrivals = new double[(int)(lambda*time*2)];
		double[] services = new double[(int)(lambda*time*2)];
		
		//we initialize counter to keep track of how many arrivals we will have in total
		int count = 0;
		while (runsum_time < time) {
			arrival = exp(lambda);
			runsum_time = runsum_time + arrival;
			arrivals[count] = runsum_time;
			ts = exp(response_time);
			services[count] = ts;
			count++;
		}
		
		//we create finite arrays that will be the size of counters
		// to keep track of arrival times and service times and copy from
		// previous larger than needed arrays.
		double[] arrivals_new = new double[count];
		double[] services_new = new double[count];
		for (int i=0; i<count; i++) {
			arrivals_new[i] = arrivals[i];
			services_new[i] = services[i];
		}
		
		//We initialize the final array that will contain all ARR, START and DONE
		Object[][] final_array = new Object[count*3][3];
		
		//First, I place all of the arrivals
		for (int i=0; i<count; i++) {
			final_array[i][0] = (String) ("R"+i);
			final_array[i][1] = (String) ("ARR: ");
			final_array[i][2] = arrivals[i];
		}
		
		//Then I create a variable x for the special case of first start of the simulation
		double x = arrivals_new[0];
		
		//Create a counter of idle time
		double idle_time = 0.0;
		
		//Then I fill up the final_array with START and DONE 
		for (int i=0; i<count; i++) {
			
			if (arrivals_new[i] > x) {
				idle_time += ((double) arrivals_new[i] - x);
				final_array[i+count][0] = (String) "R" + i;
				final_array[i+count][1] = (String) ("START: ");
				final_array[i+count][2] = arrivals_new[i];
				x = arrivals_new[i];
			} else {
				final_array[i+count][0] = (String) "R" + i;
				final_array[i+count][1] = (String) ("START: ");
				final_array[i+count][2] = x;
			}
			
			x = x + services_new[i];
			
			final_array[i+count*2][0] = (String) "R" + i;
			final_array[i+count*2][1] = (String) ("DONE: ");
			final_array[i+count*2][2] = x;
		}
		
		//Then I create the variable to keep track of average Tq and calcualte it
		double avg_tq = 0.0;
		double temp = 0.0;
		for (int i=0; i<count; i++) {
			temp = (double) final_array[i+count*2][2] - (double) final_array[i][2];
			avg_tq+=temp;
		}
		avg_tq = avg_tq/count;
		
		//We calculate the utilization
		double util = 1 - idle_time/time;
		
		//Part of the code to sort the final array based on column with times
		Object[][] sortedArray = Arrays.stream(final_array)
		        .sorted(Comparator.comparingDouble(v -> (double) v[2]))
		        .toArray(Object[][]::new);
		
		for (int i=0; i<sortedArray.length-1; i++) {
			if ((double) sortedArray[i][2] == (double) sortedArray[i+1][2] && sortedArray[i+1][1] == "DONE: ") {
				Object [] toSwap = sortedArray[i];
				sortedArray[i] = sortedArray[i+1];
				sortedArray[i+1]= toSwap;
			}
		}
		
		//double avg_queue = util/(1-util);
		
		for (int i=0; i<sortedArray.length; i++) {
			System.out.print((String)sortedArray[i][0] + " ");
			System.out.print((String)sortedArray[i][1]);
			System.out.println((Double)sortedArray[i][2]);
			
		}
		
		//Counting average queue for 100 intervals
		int qs = 0;
		int interval  = (int) (0.01*time);
		int countA = 0;
		int countS = 0;
		for(int i=0; i<100; i++) {
			Object[][]partArray = Arrays.copyOfRange(sortedArray, i*interval, (i+1)*interval);
			for (int j=0; j<partArray.length; j++) {
				if (partArray[j][1] == "ARR: ") {
					countA++;
				} else if (partArray[j][1] == "START: ") {
					countS++;
				}
			}
			if(countA > countS) {
				qs += (countA - countS);
			} else {
				qs += 0;
			}
		}
		
		//Calculating the average length of the queue
		// which may vary widely, because we are not using the best 
		// method to do so. Since we cannot assume the system is in steady state 
		// and calculate the queue using the utilization, then we have to use 
		// the method of checking the queue at random time intervals.
		double avg_queue = (double) (qs/100);
		
		
		System.out.println();
		System.out.print("UTIL: ");
		System.out.println(Double.toString(util));
		System.out.print("QLEN: ");
		System.out.println(Double.toString(avg_queue));
		System.out.print("TRESP: ");
		System.out.println(Double.toString(avg_tq));
	}
	
	public static void main(String[] args) {
		double time = Double.parseDouble(args[0]);
		simulator sim = new simulator();
		sim.lambda = Double.parseDouble(args[1]);
		sim.response_time = Double.parseDouble(args[2]);
		sim.simulate(time);
	}
}
