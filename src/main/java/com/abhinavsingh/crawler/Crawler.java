package com.abhinavsingh.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.abhinavsingh.fuge.Callback;
import com.abhinavsingh.fuge.Fuge;

/**
 * 
 * @author abhinavsingh
 *
 */
class Crawler {
	
	// used by producer dispatcher and aggregator threads
	final static ConcurrentLinkedQueue<String> inputQueue = new ConcurrentLinkedQueue<String>();
	final static ConcurrentHashMap<String, Crawler> visitedLinks = new ConcurrentHashMap<String, Crawler>();
	
	final private String job;
	private int statusCode;
	private String html;
	final private List<String> links = new ArrayList<String>();
	
	// See: http://www.mkyong.com/regular-expressions/how-to-extract-html-links-with-regular-expression/
	private final Pattern anchorTag = Pattern.compile("(?i)<a([^>]+)>(.+?)</a>");
	private final Pattern hrefLink = 
			Pattern.compile("\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))");

	public Crawler(String job) {
		this.job = job;
	}

	public List<String> getLinks() {
		return links;
	}

	public void run() {
		HttpURLConnection request;
		try {
			URL url = new URL(job);
			request = (HttpURLConnection) url.openConnection();
			statusCode = request.getResponseCode();
			//System.out.format("[%s] Url %s Response code %d%n", Thread.currentThread().getName(), url.toString(), statusCode);
			
			// read html
			String line;
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			html = builder.toString();
			
			// find anchor tags
			Matcher anchorTags = anchorTag.matcher(html);
			while (anchorTags.find()) {
				String href = anchorTags.group(1);
				
				// find link
				Matcher hrefLinks = hrefLink.matcher(href);
				while (hrefLinks.find()) {
					String link = hrefLinks.group(1);
					//System.out.println(link);
					links.add(link);
				}
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	// Consumer callback to process incoming job from the Producer
	static Callback<String, Crawler> callback = new Callback<String, Crawler>() {

		@Override
		public String dispatchJob() {
			String job = inputQueue.poll();
			if (job != null) {
				return job;
			}
			return null;
		}
		
		@Override
		public Crawler handleJob(String url) {
			Crawler crawler = new Crawler(url);
			crawler.run();
			visitedLinks.put(url, crawler);
			return crawler;
		}
		
		@Override 
		public void handleResult(Crawler result) {
			List<String> links = result.getLinks();
			for (String link : links) {
				link = link.trim().replace("\"", "");
				
				// If link is not already visited
				if (!visitedLinks.containsKey(link) 
						&& link.startsWith("http")) {
					//System.out.format("[%s] Received link %s%n", Thread.currentThread().getName(), link);
					inputQueue.add(link);
				}
			}
		}
		
	};
	
	public static void main(String[] args) throws InterruptedException {
		// Start producer / consumer manager
		Fuge<String, Crawler> fuge = new Fuge<String, Crawler>(callback, 10);
		fuge.run();
		
		// After brief sleep, seed initial job to Producer
		Thread.sleep(1000);
		inputQueue.add(args[0]);
		
		// Munch while work gets done
		while (true) {
			Thread.sleep(1000);
			
			// print visited links
			System.out.format("[%s] Total visited links %d%n", Thread.currentThread().getName(), visitedLinks.size());
		}
	}
	
}
