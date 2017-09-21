package com.asiainfo.aiedri.rest.filter;

import ainx.common.spring.BeanFactoryUtil;
import com.asiainfo.aiedri.bo.IAiedriRuleConfigBo;
import com.asiainfo.aiedri.rest.constant.RespStatus;
import com.asiainfo.aiedri.vo.AiedriRequestUserVo;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Map;

public class RequestFilter implements ContainerRequestFilter {
	Logger logger=Logger.getLogger(this.getClass());
	
	 
   public void filter(ContainerRequestContext context) throws IOException {
   	logger.debug("-----into request filter----------");
   	HttpServletRequest servletRequest= 
   			((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
   	//如果不是认证请求，需要验证用户名和密码
   	UriInfo uriInfo=context.getUriInfo();
    String path=uriInfo.getPath();
    logger.debug("RequestFilter path:"+path);
    if(path.equals("authentication")){
    	return;
    }
	Cookie[] cooks=servletRequest.getCookies();

    if(null==cooks || cooks.length==0){
    	returnUnauthorized(context);
    	return;
    }
	   logger.debug("RequestFilter cooks length:"+cooks.length);
    int count=0;//用于记录存放用户名的cookie的个数
	for(Cookie cook:cooks){
		if(checkCookValid(cook)){
			count++;
		}
	}
	logger.debug("RequestFilter count:"+count);
	if(count==0){
		returnUnauthorized(context);
    }
    
  }

private boolean checkCookValid(Cookie cook) {
	logger.debug("RequestFilter each cook name:"+cook.getName());
	if(cook.getName().equals("userName")){
		String name=cook.getValue();
		IAiedriRuleConfigBo bo=(IAiedriRuleConfigBo) BeanFactoryUtil.getBean("aiedriRuleConfigBo");
		logger.debug("RequestFilter IAiedriRuleConfigBo:"+bo);
		Map<String,AiedriRequestUserVo> map=bo.getRequestUserMap();
		logger.debug("RequestFilter RequestUserMap:"+map);
		if(null==map || map.isEmpty()){
			return true;
		}
		if(map.containsKey(name)){
			return true;
		}
		return false;
	}
	return false;
}

private void returnUnauthorized(ContainerRequestContext context) {
	JSONObject obj=new JSONObject();
	obj.put("status", RespStatus.unauth.statusDesc);
	logger.debug("RequestFilter returnUnauthorized:"+RespStatus.unauth.statusDesc);
	context.abortWith(Response.status(Response.Status.UNAUTHORIZED)
	        .entity(obj.toString())
	        .build());
}
}
