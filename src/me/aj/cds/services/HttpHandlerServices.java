package me.aj.cds.services;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import me.aj.cds.KafkaUtils.KafkaUtil;
import me.aj.cds.vo.HTTPAssemblyLineConstants;
import me.aj.cds.vo.MyHTTPServletRequest;

@Path("/ServicesHandler")
public class HttpHandlerServices {

	@POST
	@Path("/createArticle")
	@Produces({ MediaType.TEXT_PLAIN })
	@Consumes({ MediaType.TEXT_PLAIN })
	public String createArticle(String createArticle, @Context HttpServletRequest httpRequest) {
		MyHTTPServletRequest req = MyHTTPServletRequest.buildTheVo(httpRequest, createArticle,null);
		KafkaUtil.publishHTTPRequest(req);
		return "Hi from POST CA" + httpRequest.getRemoteHost() + ":" + httpRequest.getRemotePort() + "!";
	}
	
	@POST
	@Path("/addArticleRating")
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.APPLICATION_JSON })
	public String addArticleRating(String addArticleRating, @Context HttpServletRequest httpRequest) {
		MyHTTPServletRequest req = MyHTTPServletRequest.buildTheVo(httpRequest, addArticleRating,null);
		KafkaUtil.publishHTTPRequest(req);
		String transId = req.getHeaders().get(HTTPAssemblyLineConstants.HTTPALINE_TRANS_ID);
		return "Request Recieved.\nThis Transaction Id :"+transId;
	}
	

	@GET
	@Path("/")
	@Produces({ MediaType.APPLICATION_JSON })
	public String ping(@Context HttpServletRequest httpRequest) {
		MyHTTPServletRequest req = MyHTTPServletRequest.buildTheVo(httpRequest, null,null);
		KafkaUtil.publishHTTPRequest(req);
		return "Hi from GET " + httpRequest.getRemoteHost() + ":" + httpRequest.getRemotePort() + "!";
	}
	
}
