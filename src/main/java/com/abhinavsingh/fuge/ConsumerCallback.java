package com.abhinavsingh.fuge;

public interface ConsumerCallback<T1, T2> {
	public T2 handleJob(T1 job);
}
