package com.abhinavsingh.fuge;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ProducerDispatcher<T1> implements Runnable {

	final private ConcurrentLinkedQueue<T1> jobQueue;
	final private ProducerDispatcherCallback<T1> cb;
	volatile private int totalDispatched;
	volatile private boolean paused = false;

	public ProducerDispatcher(ConcurrentLinkedQueue<T1> jobQueue, ProducerDispatcherCallback<T1> cb) {
		this.jobQueue = jobQueue;
		this.cb = cb;
	}
	
	public int getTotalDispatched() {
		return totalDispatched;
	}
	
	public void pause() {
		paused = true;
	}
	
	public void resume() {
		paused = false;
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	@Override
	public void run() {
		while (true) {
			if (!paused) {
				totalDispatched += cb.dispatchJob(jobQueue);
				//System.out.format("[%s] %d jobs dispatched%n", Thread.currentThread().getName(), numDispatched);
			}
		}
	}

}
