package com.abhinavsingh.fuge;

public interface ProducerAggregatorCallback<T2> {
	public void handleResult(T2 result);
}
