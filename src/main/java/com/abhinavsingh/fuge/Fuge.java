package com.abhinavsingh.fuge;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Fuge, micro job processing framework.
 * 
 * Fuge works by starting a pool of consumer threads which consume incoming job objects <T1>
 * from the jobQueue, process the job and send result <T2> objects over to resultQueue.
 * 
 * Input to jobQueue is managed by Dispatcher thread that queues up job objects <T1> into jobQueue.
 * Aggregator thread consume processed job result objects <T2> on the other side.
 * 
 * In gist, within Fuge world, Dispatcher dispatches the job, Consumer consume and process the job,
 * Aggregator handle processed job results. Fuge framework provide Callback on key events like 
 * dispatchJob, processJob and handleResult.
 * 
 * @author abhinavsingh
 *
 * @param <T1> job object type
 * @param <T2> result object type
 */
public class Fuge<T1, T2> {
	
	// job and result queues accessed by producer and consumers thread
	final private ConcurrentLinkedQueue<T1> jobQueue = new ConcurrentLinkedQueue<T1>();
	final private ConcurrentLinkedQueue<T2> resultQueue = new ConcurrentLinkedQueue<T2>();
	
	// internals
	final private ConsumerPool<T1, T2> pool;
	final private Dispatcher<T1, T2> dispatcher;
	final private Aggregator<T1, T2> aggregator;
	
	public Fuge(Callback<T1, T2> callback, int poolSize) {
		this.dispatcher = new Dispatcher<T1, T2>(jobQueue, callback);
		this.aggregator = new Aggregator<T1, T2>(resultQueue, callback);
		this.pool = new ConsumerPool<T1, T2>(jobQueue, resultQueue, callback, dispatcher, aggregator, poolSize);
	}
	
	public Dispatcher<T1, T2> getDispatcher() {
		return dispatcher;
	}
	
	public Aggregator<T1, T2> getAggregator() {
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
