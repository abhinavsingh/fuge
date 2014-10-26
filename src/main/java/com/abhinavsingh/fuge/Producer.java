package com.abhinavsingh.fuge;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Starts two thread, one for dispatching jobs and another for processing job results.
 * 
 * @author abhinavsingh
 *
 * @param <T1> Job queue type
 * @param <T2> Result queue type
 */
public class Producer<T1, T2> {

	final private ProducerDispatcherCallback<T1> pdcb;
	final private ProducerAggregatorCallback<T2> pacb;
	final private ConcurrentLinkedQueue<T1> jobQueue;
	final private ConcurrentLinkedQueue<T2> resultQueue;
	
	private Thread dispatcher;
	private Thread aggregator;
	
	Producer(ProducerDispatcherCallback<T1> pdcb, ProducerAggregatorCallback<T2> pacb, 
			ConcurrentLinkedQueue<T1> jobQueue, ConcurrentLinkedQueue<T2> resultQueue) {
		this.pdcb = pdcb;
		this.pacb = pacb;
		this.jobQueue = jobQueue;
		this.resultQueue = resultQueue;
	}
	
	public void run() {
		// start result aggregrator thread
		aggregator = new Thread(new ProducerAggregator<T2>(resultQueue, pacb), "Aggregator");
		aggregator.start();
		
		// start job dispatcher thread
		dispatcher = new Thread(new ProducerDispatcher<T1>(jobQueue, pdcb), "Dispatcher");
		dispatcher.start();
	}
}
