package com.abhinavsingh.fuge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConsumerPool<T1, T2> implements Runnable {

	final private int numConsumers;
	final private ConcurrentLinkedQueue<T1> jobQueue;
	final private ConcurrentLinkedQueue<T2> resultQueue;
	final private List<Thread> consumers = new ArrayList<Thread>();
	final private ConsumerCallback<T1, T2> ccb;
	final private ProducerDispatcher<T1> dispatcher;
	final private ProducerAggregator<T2> aggregator;
	
	public ConsumerPool(int numConsumers, 
			ConcurrentLinkedQueue<T1> jobQueue, ConcurrentLinkedQueue<T2> resultQueue,
			ConsumerCallback<T1, T2> ccb, ProducerDispatcher<T1> dispatcher, ProducerAggregator<T2> aggregator) {
		this.numConsumers = numConsumers;
		this.jobQueue = jobQueue;
		this.resultQueue = resultQueue;
		this.ccb = ccb;
		this.dispatcher = dispatcher;
		this.aggregator = aggregator;
	}
	
	private void initializePool() {
		for (int i = 0; i < numConsumers; i++) {
			Thread consumer = new Thread(new Consumer<T1, T2>(ccb, jobQueue, resultQueue), 
					"Consumer#"+Integer.toString(i+1));
			consumer.start();
			consumers.add(consumer);
		}
	}
	
	@Override
	public void run() {
		initializePool();
		
		while (true) {
			// based upon totalDispatched - totalAggregated growth, auto-scale consumer pool
			try {
				Thread.sleep(1000);
				
				int totalDispatched = dispatcher.getTotalDispatched();
				int totalAggregated = aggregator.getTotalAggregated();
				
				System.out.format("[%s] TotalDispatched %d, TotalProcessed %d, Pending %d%n", 
						Thread.currentThread().getName(),
						totalDispatched, totalAggregated, 
						totalDispatched - totalAggregated);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
