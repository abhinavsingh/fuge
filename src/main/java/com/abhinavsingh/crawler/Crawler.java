package com.abhinavsingh.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Define result object that our consumers will produce
class Crawler {
	
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
			System.out.format("[%s] Url %s Response code %d%n", Thread.currentThread().getName(), url.toString(), statusCode);
			
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
			e.printStackTrace();
		}
	}
	
}
