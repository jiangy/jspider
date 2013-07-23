package cn.stdin.spider.examples;

import java.util.ArrayList;
import java.util.List;

import cn.stdin.spider.Spider;

public class SimpleExample {
	public final static void main(String[] args) throws Exception {
		// Set the record split regular expression. Spider will scan every string match the regex
		// in the web as a record.
		String regRecord = "(?s)<li class='pipelistli'>.*?<div class='pipelistli_ft'></div></li>";
		
		// Set the field split regular expression list. Spider will try a time to match every record
		// from the begin to the end using every regex. For every record if it match the regex the
		// group one will return else return null. Every record will return a list
		List<String> regFieldList = new ArrayList<String>();
		regFieldList.add("href='/pipes/pipe.info\\?_id=(\\w+)'");
		regFieldList.add("href='(/pipes/person.info\\?guid=\\w+)'");
		regFieldList.add("<span class='date''>(\\d{2}/\\d{2}/\\d{2})");
		regFieldList.add("<span class=\"number\">(\\d*)</span>");
		
		// Create a Spider with above record split regex, field split regex list and one thread.
		Spider spider = new Spider(regRecord, regFieldList, 1);
		
		// Crawl a url and print result.
		spider.crawl("http://pipes.yahoo.com/pipes/pipes.popular");
		System.out.println(spider.getResult());
		
		// Create a url list
		List<String> urlList = new ArrayList<String>();
		urlList.add("http://pipes.yahoo.com/pipes/pipes.popular?page=2");
		urlList.add("http://pipes.yahoo.com/pipes/pipes.popular?page=3");
		urlList.add("http://pipes.yahoo.com/pipes/pipes.popular?page=4");
		urlList.add("http://pipes.yahoo.com/pipes/pipes.popular?page=5");
		urlList.add("http://pipes.yahoo.com/pipes/pipes.popular?page=6");
		
		// Reset the threadNum and recrawl a list url and print the results.
		spider.setThreadNum(5);
		spider.crawl(urlList);
		System.out.println(spider.getResult());
		
		
	}

}
