package com.abhinavsingh.fuge;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Dispatcher<T1, T2> implements Runnable {

	final private ConcurrentLinkedQueue<T1> jobQueue;
	final private Callback<T1, T2> cb;
	volatile private int totalDispatched;
	volatile private boolean paused = false;

	public Dispatcher(ConcurrentLinkedQueue<T1> jobQueue, Callback<T1, T2> cb) {
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
				T1 job = cb.dispatchJob();
				if (job != null) {
					jobQueue.add(job);
					totalDispatched += 1;
				}
			}
		}
	}

}
