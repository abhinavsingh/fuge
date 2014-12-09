package com.abhinavsingh.fuge;

public interface Callback<T1, T2> {
	public T1 dispatchJob();
	public T2 handleJob(T1 job);
	public void handleResult(T2 result);
}
