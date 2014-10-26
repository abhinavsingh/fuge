package com.abhinavsingh.crawler;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.abhinavsingh.fuge.ConsumerCallback;
import com.abhinavsingh.fuge.Fuge;
import com.abhinavsingh.fuge.ProducerAggregatorCallback;
import com.abhinavsingh.fuge.ProducerDispatcherCallback;

public class WebCrawler {

	// input queue access by producer and main thread, including any producer / consumer callback classes
	final static ConcurrentLinkedQueue<String> inputQueue = new ConcurrentLinkedQueue<String>();
	
	public static void main(String[] args) throws InterruptedException {
		// Initialize producer / consumer manager with our results class as generic
		Fuge<String, Crawler> pc = new Fuge<String, Crawler>(100);

		// Producer callback to process incoming result objects from Consumers
		ProducerAggregatorCallback<Crawler> pacb = new ProducerAggregatorCallback<Crawler>() {
			
			@Override 
			public void handleResult(Crawler result) {
				List<String> links = result.getLinks();
				for (String link : links) {
					link = link.trim().replace("\"", "");
					if (link.startsWith("http")) {
						//System.out.format("[%s] Received link %s%n", Thread.currentThread().getName(), link);
						inputQueue.add(link);
					}
				}
			}
		
		};
		
		ProducerDispatcherCallback<String> pdcb = new ProducerDispatcherCallback<String>() {

			@Override
			public int dispatchJob(ConcurrentLinkedQueue<String> jobQueue) {
				String job = inputQueue.poll();
				if (job != null) {
					jobQueue.add(job);
					return 1;
				}
				return 0;
			}
			
		};
		
		// Consumer callback to process incoming job from the Producer
		ConsumerCallback<String, Crawler> ccb = new ConsumerCallback<String, Crawler>() {

			@Override
			public Crawler handleJob(String url) {
				Crawler crawler = new Crawler(url);
				crawler.run();
				return crawler;
			}
			
		};
		
		// Start producer / consumer manager
		pc.start(pdcb, pacb, ccb);
		
		// After brief sleep, seed initial job to Producer
		Thread.sleep(1000);
		inputQueue.add(args[0]);
		
		// Munch while work gets done
		while (true) {
			Thread.sleep(1000);
			//System.out.format("[***] TotalDispatched %d, TotalProcessed %d, Pending %d%n", pc.getTotalDispatched(), pc.getTotalProcessed(), pc.getTotalDispatched() - pc.getTotalProcessed());
		}
	}

}
