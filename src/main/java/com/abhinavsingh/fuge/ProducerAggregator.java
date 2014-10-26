package com.abhinavsingh.fuge;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ProducerAggregator<T2> implements Runnable {
	
	final private ConcurrentLinkedQueue<T2> resultQueue;
	final private ProducerAggregatorCallback<T2> cb;
	
	public ProducerAggregator(ConcurrentLinkedQueue<T2> resultQueue, ProducerAggregatorCallback<T2> cb) {
		this.resultQueue = resultQueue;
		this.cb = cb;
	}
	
	@Override public void run() {
		while (true) {
			T2 result = resultQueue.poll();
			if (result != null) {
				cb.handleResult(result);
			}
		}
	}

}
