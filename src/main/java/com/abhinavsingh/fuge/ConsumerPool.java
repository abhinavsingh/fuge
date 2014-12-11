package com.abhinavsingh.fuge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConsumerPool<T1, T2> implements Runnable {
	
	// state
	private int pendingJobs = 0;
	private int numConsumers = 0;
	private int poolSize = 10;
	
	final private ConcurrentLinkedQueue<T1> jobQueue;
	final private ConcurrentLinkedQueue<T2> resultQueue;
	final private List<Thread> consumers = new ArrayList<Thread>();
	final private Callback<T1, T2> callback;
	final private Dispatcher<T1, T2> dispatcher;
	final private Aggregator<T1, T2> aggregator;
	
	public ConsumerPool(ConcurrentLinkedQueue<T1> jobQueue, ConcurrentLinkedQueue<T2> resultQueue,
			Callback<T1, T2> callback, Dispatcher<T1, T2> dispatcher, 
			Aggregator<T1, T2> aggregator, int poolSize) {
		this.jobQueue = jobQueue;
		this.resultQueue = resultQueue;
		this.poolSize = poolSize;
		this.callback = callback;
		this.dispatcher = dispatcher;
		this.aggregator = aggregator;
	}
	
	private void startConsumers(int size) {
		System.out.format("[%s] Adding %d consumers to pool%n", Thread.currentThread().getName(), size);
		
		for (int i = 0; i < size; i++) {
			numConsumers++;
			Thread consumer = new Thread(new Consumer<T1, T2>(callback, jobQueue, resultQueue), 
					"Consumer#"+Integer.toString(numConsumers));
			consumer.start();
			consumers.add(consumer);
		}
	}
	
	@Override
	public void run() {
		startConsumers(poolSize);
		while (true) {
			try {
				Thread.sleep(3000);
				
				// pending jobs
				int totalDispatched = dispatcher.getTotalDispatched();
				int totalAggregated = aggregator.getTotalAggregated();
				pendingJobs = totalDispatched - totalAggregated;
				System.out.format("[%s] dispatched %d, aggregated %d, pending %d%n", Thread.currentThread().getName(), totalDispatched, totalAggregated, pendingJobs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
