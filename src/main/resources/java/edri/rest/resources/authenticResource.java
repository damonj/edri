package com.asiainfo.aiedri.rest.resources;

import ainx.common.spring.BeanFactoryUtil;
import ainx.common.util.CryptUtil;
import com.asiainfo.aiedri.bo.IAiedriRuleConfigBo;
import com.asiainfo.aiedri.rest.constant.RespStatus;
import com.asiainfo.aiedri.vo.AiedriRequestUserVo;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/authentication")
public class authenticResource {
	@Context   
	private HttpServletRequest servletRequest; 
	@Context  
	private HttpServletResponse servletResponse; 
	Logger logger=Logger.getLogger(authenticResource.class);
	
	@POST 
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.APPLICATION_JSON)  
    public String authentication(@FormParam("username") String username,
    		@FormParam("password") String password)  
    {  
		logger.debug("---into authentication at server---");
		JSONObject obj=new JSONObject();
		logger.debug("authentication param username:"+username);
		logger.debug("authentication param password:"+password);
		try{
			//String key=CryptUtil.getDigest(password);
			if(!checkUserValid(username,password)){
				logger.debug("authentication checkUser unValid");
				obj.put("status", RespStatus.unauth.statusDesc);
			}else{
				logger.debug("authentication checkUser Valid and set cook");
				Cookie cook=new Cookie("userName",username);
				cook.setSecure(true);
				cook.setMaxAge(2*3600);//2小时
				cook.setVersion(1);
				servletResponse.addCookie(cook);
				obj.put("status", RespStatus.ok.statusDesc);
			}
		}catch(Exception e){
			obj.put("status", RespStatus.other.statusDesc);
			logger.error("there is error in authentication!"+e);
		}
		logger.debug("authentication return obj:"+obj.toString());
		return obj.toString();
	}

	private boolean checkUserValid(String username, String password) {
		if(StringUtils.isEmpty(username)|| StringUtils.isEmpty(password)){
			return false;
		}
		logger.debug("---into authentication checkUserValid---");
		IAiedriRuleConfigBo bo=(IAiedriRuleConfigBo) BeanFactoryUtil.getBean("aiedriRuleConfigBo");
		Map<String,AiedriRequestUserVo> map=bo.getRequestUserMap();
		if(null==map || map.isEmpty()){
			return true;
		}
		logger.debug("authentication checkUserValid RequestUserMap:"+map);
		if(map.containsKey(username)){
			String pass=map.get(username).getUserPassword();
			String key=CryptUtil.getDigest(password);
			logger.debug("authentication checkUserValid pass01:"+pass);
			logger.debug("authentication checkUserValid pass02:"+key);
			if(StringUtils.isNotEmpty(pass) && !key.equals(pass)){
				return false;
			}
			//判断IP地址的有效性
			String fromIp=getClientIpAddr(servletRequest);
			String ip=map.get(username).getIp();
			logger.debug("authentication checkUserValid fromIp01:"+fromIp);
			logger.debug("authentication checkUserValid fromIp02:"+ip);
			if(StringUtils.isNotEmpty(ip) && !fromIp.equals(ip)){
				return false;
			}
			return true;
		}else{
			return false;
		}
	}
	//获取客户端的请求IP地址
	private String getClientIpAddr(HttpServletRequest request) {
		String ip  =  request.getHeader( " x-forwarded-for " );
        if (ip  ==   null   ||  ip.length()  ==   0   ||   " unknown " .equalsIgnoreCase(ip))  {
            ip  =  request.getHeader( " Proxy-Client-IP " );
        }
        if (ip  ==   null   ||  ip.length()  ==   0   ||   " unknown " .equalsIgnoreCase(ip))  {
            ip  =  request.getHeader( " WL-Proxy-Client-IP " );
        }
         if (ip  ==   null   ||  ip.length()  ==   0   ||   " unknown " .equalsIgnoreCase(ip))  {
           ip  =  request.getRemoteAddr();
       }
        return  ip;
	}	
}
