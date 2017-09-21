package com.asiainfo.aiedri.rest.filter;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

public class ResponseFilter implements ContainerResponseFilter {
	Logger logger=Logger.getLogger(this.getClass());
	public void filter(ContainerRequestContext context,
			ContainerResponseContext resp) throws IOException {
		logger.debug("-----into response filter----------");
	   	//如果不是认证请求，需要验证用户名和密码
	   /*	UriInfo uriInfo=context.getUriInfo();
	    String path=uriInfo.getPath();
	    if(path.equals("authentication")){
	    	Calendar cal=Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, 1);
			Date date=new Date(cal.getTimeInMillis());
			resp.getHeaders().add("Set-Cookie", new NewCookie("liulx", "key=123456","/","1",1,"",3600,date,false,false));
	    }*/
	}

}