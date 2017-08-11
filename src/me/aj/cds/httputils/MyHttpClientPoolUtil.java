package me.aj.cds.httputils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.log4j.Logger;

import me.aj.cds.vo.HTTPAssemblyLineConstants;
import me.aj.cds.vo.MyHTTPServletRequest;

public class MyHttpClientPoolUtil {

	BasicHttpContext context = new BasicHttpContext();

	private static Logger _logger = Logger.getLogger(MyHttpClientPoolUtil.class.getName());

	public HttpResponse executeService(MyHTTPServletRequest httpRequest) {
		HttpPost httppost = null;
		HttpGet httpGet = null;
		HttpHead httpHead = null;
		CloseableHttpResponse response = null;
		CloseableHttpClient httpClient = new DefaultHttpClient();
		try {
			// TODO : Develop an intelligent HTTPClient
			//CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			
			
				try {
					httpClient = new DefaultHttpClient();
					System.out.println("Going to " + httpRequest.getMethod()+" to "+httpRequest.getURL());
					if ("POST".equals(httpRequest.getMethod())) {
						httppost = (HttpPost) setHeaders(httpRequest);
						StringEntity input = new StringEntity(httpRequest.getPayLoad());
						input.setContentType(httppost.getFirstHeader("content-type"));
						httppost.setEntity(input);
						response = httpClient.execute(httppost, context);
					} else if ("GET".equals(httpRequest.getMethod())) {
						httpGet = (HttpGet) setHeaders(httpRequest);
						response = httpClient.execute(httpGet, context);
					}else if("HEAD".equalsIgnoreCase(httpRequest.getMethod())){
						httpHead = (HttpHead) setHeaders(httpRequest);
						response = httpClient.execute(httpHead, context);
					}
					System.out.println("\tResponse code : " + response.getStatusLine());
					return response;
				} catch (HttpHostConnectException hostExep) {
					response.setStatusLine(new StatusLine() {
						
						@Override
						public int getStatusCode() {
							return 404;
						}
						
						@Override
						public String getReasonPhrase() {
							// TODO Auto-generated method stub
							return "CANNOT CONNECT TO HOST";
						}
						
						@Override
						public ProtocolVersion getProtocolVersion() {
							// TODO Auto-generated method stub
							return new ProtocolVersion("HTTP", 1, 1);
						}
					});
					return response;
				}

		} catch (ConnectTimeoutException e) {
			_logger.error("ConnectionTimeOut Exception from Mimosa = " + e);
		} catch (SocketTimeoutException e) {
			_logger.error("SocketTimeoutException from Mimosa = " + e);
		} catch (Exception e) {
			e.printStackTrace();
			_logger.error("Exception from Mimosa = " + e);
		}finally{
			try {
				httpClient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return response;
	}

	private void retry(int noOfRetriesSoFar) {
		System.out.println("Target host down. Will try after " + HTTPAssemblyLineConstants.HOST_DOWN_SLEEP
				+ " ms for " + noOfRetriesSoFar + " times");
		try {
			Thread.sleep(HTTPAssemblyLineConstants.HOST_DOWN_SLEEP);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * method to set headers to pooling connection manager
	 */
	private HttpRequestBase setHeaders(MyHTTPServletRequest httpRequest) {

		HttpRequestBase httpMethod = null;

		if ("POST".equals(httpRequest.getMethod())) {
			// httpMethod = new HttpPost(httpRequest.getURL());
			httpMethod = new HttpPost(getTargetURL(httpRequest.getURL()));
		} else if ("GET".equals(httpRequest.getMethod())) {
			httpMethod = new HttpGet(getTargetURL(httpRequest.getURL()));
		}else if("HEAD".equalsIgnoreCase(httpRequest.getMethod())){
			httpMethod = new HttpHead(getTargetURL(httpRequest.getURL()));
		}

		for (Entry<String, String> aHdr : httpRequest.getHeaders().entrySet()) {
			if (!aHdr.getKey().equalsIgnoreCase("content-length")) {
				httpMethod.setHeader(aHdr.getKey(), aHdr.getValue());
			}

		}
		return httpMethod;
	}

	private String getTargetURL(String url) {
		// TODO : find the actual target host
		return "http://localhost:8080/ContentServices/services//ContentService/addArticleRating";
	}

	public boolean isItUpRightNow(MyHTTPServletRequest httpRequest) {
		URL url=null;;
		try {
			url = new URL(httpRequest.getURL());
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String host = url.getHost();
		int port = url.getPort() > 0 ? url.getPort() : 80;
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), HTTPAssemblyLineConstants.IS_IT_UP_PING_TIMEOUT);
			return true;
		} catch (IOException e) {
			return false; // Either timeout or unreachable or failed DNS lookup.
		}
	}
	
	public static String getIpAddress(HttpServletRequest request) {
		String ipAddress = null;
		if (_logger.isDebugEnabled()) {
			_logger.debug("Inside getClientIP");
		}
		ipAddress = request.getHeader("x-user-addr");
		if (_logger.isDebugEnabled()) {
			_logger.debug("Inside getClientIP using request.getHeader(x-user-addr) ");
			_logger.debug("request.getHeader(x-user-addr) " + ipAddress);
		}
		if (null == ipAddress) {
			ipAddress = request.getRemoteAddr();
			if (_logger.isDebugEnabled()) {
				_logger.debug(
						"Inside getClientIP the default implementation is to get the address using request.getRemoteAddr() ");
				_logger.debug("request.getRemoteAddr() " + ipAddress);
			}

		}
		return ipAddress;
	}

}
