package com.abhinavsingh.fuge;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ProducerDispatcher<T1> implements Runnable {

	final private ConcurrentLinkedQueue<T1> jobQueue;
	final private ProducerDispatcherCallback<T1> cb;

	public ProducerDispatcher(ConcurrentLinkedQueue<T1> jobQueue, ProducerDispatcherCallback<T1> cb) {
		this.jobQueue = jobQueue;
		this.cb = cb;
	}
	
	@Override
	public void run() {
		while (true) {
			int numDispatched = cb.dispatchJob(jobQueue);
			//System.out.format("[%s] %d jobs dispatched%n", Thread.currentThread().getName(), numDispatched);
		}
	}

}
