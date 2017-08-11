package me.aj.cds.services;
import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import me.aj.cds.httputils.MyHttpClientPoolUtil;
import me.aj.cds.vo.HTTPAssemblyLineConstants;
import me.aj.cds.vo.MyHTTPServletRequest;

@RequestMapping("/ServicesHandlerSpring")
public class HttpHandlerServicesSpring {

	@RequestMapping(value = "/hi", method = RequestMethod.GET)
	  public String logout(HttpServletRequest request, HttpServletResponse response) {
	    return "Hi From @RequestMapping";
	  }
	
		@RequestMapping(value = "/realProxy", method = RequestMethod.POST)
	public @ResponseBody String realProxy(@PathVariable("apiName") String apiName, HttpServletRequest httpServletRequest,
		      HttpServletResponse httpServletResponse) {
		apiName = "getArticle";
		String requestPayload="";
		try {
			requestPayload = httpServletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String targetUrl = HTTPAssemblyLineConstants.TARGET_SERVICE_BASE_PATH+"/"+apiName;
		MyHTTPServletRequest req = MyHTTPServletRequest.buildTheVo(httpServletRequest, requestPayload,targetUrl);
		
		MyHttpClientPoolUtil httpClient = new MyHttpClientPoolUtil();
		HttpResponse response = httpClient.executeService(req);
		HttpEntity r_entity = response.getEntity();
		String responseStr="";
		try {
			responseStr=EntityUtils.toString(r_entity).toString().trim();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return responseStr;
	}

}
