package com.abhinavsingh.fuge;

import java.util.concurrent.ConcurrentLinkedQueue;

class Consumer<T1, T2> implements Runnable {
	
	final private ConsumerCallback<T1, T2> cb;
	final private ConcurrentLinkedQueue<T1> jobQueue;
	final private ConcurrentLinkedQueue<T2> resultQueue;

	Consumer(ConsumerCallback<T1, T2> cb, 
			ConcurrentLinkedQueue<T1> jobQueue, ConcurrentLinkedQueue<T2> resultQueue) {
		this.cb = cb;
		this.jobQueue = jobQueue;
		this.resultQueue = resultQueue;
	}
	
	@Override public void run() {
		while (true) {
			// Wait for incoming job
			T1 job = jobQueue.poll();
			
			if (job != null) {
				// do it
				resultQueue.add(cb.handleJob(job));
			}
		}
	}
}
