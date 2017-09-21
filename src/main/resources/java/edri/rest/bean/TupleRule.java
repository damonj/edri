package com.asiainfo.aiedri.rest.bean;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TupleRule {
	private String valid_after;//ÃëÊý
	private String valid_before;
	private String valid_scope;
	private String rule_id;
	private String rule_type;
	private Quintuple rule;
	private Integer uplink = null;
	private Integer downlink = null;
	public String getValid_after() {
		return valid_after;
	}
	public void setValid_after(String valid_after) {
		this.valid_after = valid_after;
	}
	public String getValid_before() {
		return valid_before;
	}
	public void setValid_before(String valid_before) {
		this.valid_before = valid_before;
	}
	public String getValid_scope() {
		return valid_scope;
	}
	public void setValid_scope(String valid_scope) {
		this.valid_scope = valid_scope;
	}
	public String getRule_id() {
		return rule_id;
	}
	public void setRule_id(String rule_id) {
		this.rule_id = rule_id;
	}
	public String getRule_type() {
		return rule_type;
	}
	public void setRule_type(String rule_type) {
		this.rule_type = rule_type;
	}
	public Quintuple getRule() {
		return rule;
	}
	public void setRule(Quintuple rule) {
		this.rule = rule;
	}
	public Integer getUplink() {
		return uplink;
	}
	public void setUplink(Integer uplink) {
		this.uplink = uplink;
	}
	public Integer getDownlink() {
		return downlink;
	}
	public void setDownlink(Integer downlink) {
		this.downlink = downlink;
	}	
}
