package com.asiainfo.aiedri.rest.bean;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RequestTupleBean {
	private List<Quintuple> request;

	public List<Quintuple> getRequest() {
		return request;
	}

	public void setRequest(List<Quintuple> request) {
		this.request = request;
	}
}
