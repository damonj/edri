package com.asiainfo.aiedri.rest.application;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.asiainfo.aiedri.rest.filter.RequestFilter;


public class RestApplication extends ResourceConfig {
    public RestApplication(){
    	//���������ڵİ�·��
    	packages("com.asiainfo.aiedri.rest.resources");
    	//ע��JSONת����
    	register(JacksonJsonProvider.class);  
    	 //��ӡ������־�����ڸ��ٵ��ԣ���ʽ��������� 
        register(LoggingFeature.class);
        //ע������filter
        register(RequestFilter.class);
        //register(ResponseFilter.class);
    }
}
