package com.asiainfo.aiedri.rest.bean;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RequestParamBean {
	private List<TupleRule> request;

	public List<TupleRule> getRequest() {
		return request;
	}

	public void setRequest(List<TupleRule> request) {
		this.request = request;
	}
}
