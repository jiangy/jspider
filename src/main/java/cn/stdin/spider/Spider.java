package cn.stdin.spider;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.log4j.Logger;

/**
 * @author jiangyong.hn@gmail.com
 */
public class Spider {
	static Logger logger = Logger.getLogger(Spider.class);
	int threadNum;
	private Pattern recordPattern;
	private List<Pattern> fieldPatternList;
	
	
	public Spider(String regRecord, List<String> regFieldList, int threadNum) {
		recordPattern = Pattern.compile(regRecord);
		fieldPatternList = new ArrayList<Pattern>();
		for(String regField : regFieldList) {
			fieldPatternList.add(Pattern.compile(regField));
		}
		this.threadNum = threadNum;
	}

	public int getThreadNum() {
		return threadNum;
	}

	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum > 0 ? threadNum : 1;
	}

	public Pattern getRecordPattern() {
		return recordPattern;
	}

	public void setRecordPattern(Pattern recordPattern) {
		this.recordPattern = recordPattern;
	}

	public List<Pattern> getFieldPatternList() {
		return fieldPatternList;
	}

	public void setFieldPatternList(List<Pattern> fieldPatternList) {
		this.fieldPatternList = fieldPatternList;
	}

	public List<List<String>> crawl(List<String> urlList) {
		PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
		cm.setMaxTotal(threadNum);
		HttpClient httpClient = new DefaultHttpClient(cm);
		ExecutorService exec = Executors.newFixedThreadPool(threadNum);
		List<Future<List<List<String>>>> theadResult = new ArrayList<Future<List<List<String>>>>();
		List<List<String>> result = new ArrayList<List<String>>();
		try {
			for(String url : urlList) {
				theadResult.add(exec.submit(new SpiderTask(httpClient, url)));
			}
			for(Future<List<List<String>>> fs : theadResult) {
				try {
					result.addAll(fs.get());
				} catch(InterruptedException e) {
					e.printStackTrace();
				} catch(ExecutionException e) {
					e.printStackTrace();
				} finally {
					exec.shutdown();
				}
			}
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}
	public List<List<String>> crawl(String url) {
		List<String> urlList = new ArrayList<String>();
		urlList.add(url);
		return crawl(urlList);
	}
	
	
	private class SpiderTask implements Callable<List<List<String>>> {
		private final HttpClient httpClient;
		private final String url;
		public SpiderTask(HttpClient httpClient, String url) {
			this.httpClient = httpClient;
			this.url = url;
		}
		public List<List<String>> call() {
			List<List<String>> result = new ArrayList<List<String>>();
			
			HttpGet httpGet = new HttpGet(url);
			logger.info("executing request " + httpGet.getURI());
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			try {
				String responseBody = httpClient.execute(httpGet, responseHandler);
				List<Matcher> matcherList = new ArrayList<Matcher>();
				for(Pattern pattern : fieldPatternList) {
					matcherList.add(pattern.matcher(""));
				}
				Matcher recordMatcher = recordPattern.matcher(responseBody);
				
				while(recordMatcher.find()) {
					result.add(getField(matcherList, recordMatcher.group()));
				}
			} catch (ClientProtocolException e) {
				logger.error(e);
			} catch (IOException e) {
				logger.error(e);
			}
			
			return result;
		}
		
		private List<String> getField(List<Matcher> matcherList, String record) {
			List<String> result = new ArrayList<String>(matcherList.size());
			for(Matcher matcher : matcherList) {
				matcher.reset(record);
				result.add(matcher.find() ? matcher.group(1) : null);
			}
			logger.debug("get a result : " + result + " from record:\n" + record);
			return result;
		}
	}	
}