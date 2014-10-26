package com.abhinavsingh.fuge;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Fuge<T1, T2> {
	
	// job and result queues accessed by producer and consumers
	final private ConcurrentLinkedQueue<T1> jobQueue = new ConcurrentLinkedQueue<T1>();
	final private ConcurrentLinkedQueue<T2> resultQueue = new ConcurrentLinkedQueue<T2>();
	
	// internals
	final private int numConsumers;
	final private List<Thread> consumers = new ArrayList<Thread>();
	private Producer<T1, T2> producer;
	
	public Fuge() {
		this.numConsumers = 10;
	}
	
	public Fuge(int numConsumers) {
		this.numConsumers = numConsumers;
	}
	
	public void start(ProducerDispatcherCallback<T1> pdcb, ProducerAggregatorCallback<T2> pacb, ConsumerCallback<T1, T2> ccb) {
		// setup consumers
		for (int i = 0; i < numConsumers; i++) {
			Thread consumer = new Thread(new Consumer<T1, T2>(ccb, jobQueue, resultQueue), 
					"Consumer#"+Integer.toString(i+1));
			consumer.start();
			consumers.add(consumer);
		}
		
		producer = new Producer<T1, T2>(pdcb, pacb, jobQueue, resultQueue);
		producer.run();
	}
	
}
