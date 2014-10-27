package com.abhinavsingh.fuge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConsumerPool<T1, T2> implements Runnable {

	// Pool tries to maintain a processing rate of jobProcessingRate
	// while maintaining queue size to maximumPendingJobs. If constraints 
	// are not met, Fuge will refuse to accept new jobs. It is responsibility
	// of the target application to cache and retry sending jobs, or it 
	// can be written as a Fuge plugin
	final private int jobProcessingRate; // per second
	
	// state
	final private long startTime;
	private float jobsPerSecond = 0;
	private float processingPerSecond = 0;
	private float pendingGrowthRate = 0;
	private int pendingJobs = 0;
	private int numConsumers = 0;
	
	final private ConcurrentLinkedQueue<T1> jobQueue;
	final private ConcurrentLinkedQueue<T2> resultQueue;
	final private List<Thread> consumers = new ArrayList<Thread>();
	final private ConsumerCallback<T1, T2> ccb;
	final private ProducerDispatcher<T1> dispatcher;
	final private ProducerAggregator<T2> aggregator;
	
	public ConsumerPool(ConcurrentLinkedQueue<T1> jobQueue, ConcurrentLinkedQueue<T2> resultQueue,
			ConsumerCallback<T1, T2> ccb, int jobProcessingRate,
			ProducerDispatcher<T1> dispatcher, ProducerAggregator<T2> aggregator) {
		this.startTime = System.currentTimeMillis();
		this.jobProcessingRate = jobProcessingRate;
		this.jobQueue = jobQueue;
		this.resultQueue = resultQueue;
		this.ccb = ccb;
		this.dispatcher = dispatcher;
		this.aggregator = aggregator;
	}
	
	private void startConsumers(int size) {
		System.out.format("[%s] Adding %d consumers to pool%n", Thread.currentThread().getName(), size);
		
		for (int i = 0; i < size; i++) {
			numConsumers++;
			Thread consumer = new Thread(new Consumer<T1, T2>(ccb, jobQueue, resultQueue), 
					"Consumer#"+Integer.toString(numConsumers));
			consumer.start();
			consumers.add(consumer);
		}
	}
	
	@Override
	public void run() {
		startConsumers(jobProcessingRate);
		//startConsumers(jobProcessingRate / 10);
		
		while (true) {
			// auto-scale consumer pool to meet jobProcessingRate
			try {
				Thread.sleep(3000);
				
				// pending jobs
				int totalDispatched = dispatcher.getTotalDispatched();
				int totalAggregated = aggregator.getTotalAggregated();
				pendingJobs = totalDispatched - totalAggregated;
				System.out.format("[%s] dispatched %d, aggregated %d, pending %d%n", Thread.currentThread().getName(), totalDispatched, totalAggregated, pendingJobs);
				
				// job arrival and processing rate
				/*long timeDiff = (System.currentTimeMillis() - startTime ) / 1000;
				processingPerSecond = (float) totalAggregated / timeDiff;
				jobsPerSecond = (float) totalDispatched / timeDiff;
				pendingGrowthRate = (float) pendingJobs / timeDiff;
				
				System.out.format("[%s] totalDispatched %d, totalAggregated %d, totalPending %d, timeDiff %d, "
						+ "processingPerSecond %f, jobProcessingRate %d, jobsPerSecond %f, totalConsumers %d, "
						+ "dispatcher paused %b, pendingGrowthRate %f%n", 
						Thread.currentThread().getName(), totalDispatched, totalAggregated, 
						totalDispatched - totalAggregated, timeDiff, 
						processingPerSecond, jobProcessingRate, 
						jobsPerSecond, numConsumers, dispatcher.isPaused(), pendingGrowthRate);
				System.out.println("");
				
				// if not processing enough
				if (processingPerSecond > 0 
						&& processingPerSecond < jobProcessingRate
						&& processingPerSecond < jobsPerSecond
						&& pendingJobs > 0) {
					int size = (int) Math.ceil((float) jobsPerSecond / processingPerSecond);
					startConsumers(size);
					dispatcher.pause();
				}
				// if we are processing enough and in case dispatcher is paused, resume it
				else if (dispatcher.isPaused()
						&& true) {
					dispatcher.resume();
				}*/
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
