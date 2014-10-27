package com.abhinavsingh.fuge;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Fuge<T1, T2> {
	
	// job and result queues accessed by producer and consumers
	final private ConcurrentLinkedQueue<T1> jobQueue = new ConcurrentLinkedQueue<T1>();
	final private ConcurrentLinkedQueue<T2> resultQueue = new ConcurrentLinkedQueue<T2>();
	
	// internals
	final private ConsumerPool<T1, T2> pool;
	final private ProducerDispatcher<T1> dispatcher;
	final private ProducerAggregator<T2> aggregator;
	
	public Fuge(ProducerDispatcherCallback<T1> pdcb, 
			ProducerAggregatorCallback<T2> pacb, ConsumerCallback<T1, T2> ccb,
			int jobProcessingRate) {
		this.dispatcher = new ProducerDispatcher<T1>(jobQueue, pdcb);
		this.aggregator = new ProducerAggregator<T2>(resultQueue, pacb);
		this.pool = new ConsumerPool<T1, T2>(jobQueue, resultQueue, ccb, jobProcessingRate, dispatcher, aggregator);
	}
	
	public ProducerDispatcher<T1> getDispatcher() {
		return dispatcher;
	}
	
	public ProducerAggregator<T2> getAggregator() {
		return aggregator;
	}
	
	public void run() {
		// start consumer pool
		new Thread(pool, "ConsumerPool").start();
		
		// start result aggregator
		new Thread(aggregator, "Aggregator").start();
		
		// start job dispatcher
		new Thread(dispatcher, "Dispatcher").start();
	}
	
}
