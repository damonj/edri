package com.asiainfo.aiedri.rest.application;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.asiainfo.aiedri.rest.filter.RequestFilter;


public class RestApplication extends ResourceConfig {
    public RestApplication(){
    	//服务类所在的包路径
    	packages("com.asiainfo.aiedri.rest.resources");
    	//注册JSON转换器
    	register(JacksonJsonProvider.class);  
    	 //打印访问日志，便于跟踪调试，正式发布可清除 
        register(LoggingFeature.class);
        //注册容器filter
        register(RequestFilter.class);
        //register(ResponseFilter.class);
    }
}
