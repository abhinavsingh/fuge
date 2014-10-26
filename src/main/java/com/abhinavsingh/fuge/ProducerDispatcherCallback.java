package com.abhinavsingh.fuge;

import java.util.concurrent.ConcurrentLinkedQueue;

public interface ProducerDispatcherCallback<T1> {
	public int dispatchJob(ConcurrentLinkedQueue<T1> jobQueue);
}
