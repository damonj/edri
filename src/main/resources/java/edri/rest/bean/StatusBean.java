package com.asiainfo.aiedri.rest.bean;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StatusBean{
	private String rule_id;
	private String status;
	public StatusBean(){}
	public StatusBean(String ruleId,String status){
		this.rule_id=ruleId;
		this.status=status;
	}
	public String getRule_id() {
		return rule_id;
	}
	public void setRule_id(String rule_id) {
		this.rule_id = rule_id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
