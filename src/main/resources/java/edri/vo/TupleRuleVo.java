package com.asiainfo.aiedri.vo;

import org.apache.commons.lang.StringUtils;

import com.asiainfo.aiedri.rest.bean.Quintuple;
import com.asiainfo.aiedri.rest.bean.TupleRule;

public class TupleRuleVo {
	private Integer id;
	private Integer requestId;
	private Integer status;
	private String statusDesc;
	private String validAfter;
	private String validBefore;
	private String validScope;
	private String ruleId;
	private String ruleType;
	private Quintuple rule;
	private String sip;
	private String dip;
	private String smask;
	private String dmask;
	private Integer sport;
	private Integer dport;
	private String spmask;
	private String dpmask;
	private String proto;
	private String pmask;
	private Integer uplink;
	private Integer downlink;

	private String createDate;
	private Integer taskType;
	private String startCreateDate;
	private String endCreateDate;
	public Integer getRequestId() {
		return requestId;
	}
	public void setRequestId(Integer requestId) {
		this.requestId = requestId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getStatusDesc() {
		return statusDesc;
	}
	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getValidAfter() {
		return validAfter;
	}
	public void setValidAfter(String validAfter) {
		this.validAfter = validAfter;
	}
	public String getValidBefore() {
		return validBefore;
	}
	public void setValidBefore(String validBefore) {
		this.validBefore = validBefore;
	}
	public String getValidScope() {
		return validScope;
	}
	public void setValidScope(String validScope) {
		this.validScope = validScope;
	}
	public String getRuleId() {
		return ruleId;
	}
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}
	public String getRuleType() {
		return ruleType;
	}
	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}
	public String getSip() {
		return sip;
	}
	public void setSip(String sip) {
		this.sip = sip;
	}
	public String getDip() {
		return dip;
	}
	public void setDip(String dip) {
		this.dip = dip;
	}
	public String getSmask() {
		return smask;
	}
	public void setSmask(String smask) {
		this.smask = smask;
	}
	public String getDmask() {
		return dmask;
	}
	public void setDmask(String dmask) {
		this.dmask = dmask;
	}
	public Integer getSport() {
		return sport;
	}
	public void setSport(Integer sport) {
		this.sport = sport;
	}
	public Integer getDport() {
		return dport;
	}
	public void setDport(Integer dport) {
		this.dport = dport;
	}
	public String getSpmask() {
		return spmask;
	}
	public void setSpmask(String spmask) {
		this.spmask = spmask;
	}
	public String getDpmask() {
		return dpmask;
	}
	public void setDpmask(String dpmask) {
		this.dpmask = dpmask;
	}
	public String getProto() {
		return proto;
	}
	public void setProto(String proto) {
		this.proto = proto;
	}
	public String getPmask() {
		return pmask;
	}
	public void setPmask(String pmask) {
		this.pmask = pmask;
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
	public Quintuple getRule() {
		return rule;
	}
	public void setRule(Quintuple rule) {
		this.rule = rule;
	}
	public Quintuple BiuldRestQuintuple(){
		Quintuple tuple=new Quintuple();
		if(StringUtils.isNotEmpty(this.sip)){
			tuple.setSip(this.sip);
		}
		if(StringUtils.isNotEmpty(this.dip)){
			tuple.setDip(this.dip);
		}
		if(StringUtils.isNotEmpty(this.smask)){
			tuple.setSmask(this.smask);
		}
		if(StringUtils.isNotEmpty(this.dmask)){
			tuple.setDmask(this.dmask);
		}
		if(null!=this.sport){
			tuple.setSport(this.sport);
		}
		if(null!=this.dport){
			tuple.setDport(this.dport);
		}
		if(StringUtils.isNotEmpty(this.spmask)){
			tuple.setSpmask(this.spmask);
		}
		if(StringUtils.isNotEmpty(this.dpmask)){
			tuple.setDpmask(this.dpmask);
		}
		if(StringUtils.isNotEmpty(this.proto)){
			tuple.setProto(this.proto);
		}
		if(StringUtils.isNotEmpty(this.pmask)){
			tuple.setPmask(this.pmask);
		}
		return tuple;
	}
	public TupleRule BiuldRestTuple(){
		TupleRule rule=new TupleRule();
		if(StringUtils.isNotEmpty(this.validAfter)){
			rule.setValid_after(this.validAfter);
		}
		if(StringUtils.isNotEmpty(this.validBefore)){
			rule.setValid_before(this.validBefore);
		}
		if(StringUtils.isNotEmpty(this.validScope)){
			rule.setValid_scope(this.validScope);
		}
		if(StringUtils.isNotEmpty(this.ruleId)){
			rule.setRule_id(this.ruleId);
		}
		if(StringUtils.isNotEmpty(this.ruleType)){
			rule.setRule_type(this.ruleType);
		}
		if(null!=this.uplink){
			rule.setUplink(this.uplink);
		}
		if(null!=this.downlink){
			rule.setDownlink(this.downlink);
		}
		Quintuple tuple=BiuldRestQuintuple();
		if(null!=tuple){
			rule.setRule(tuple);
		}
		return rule;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public Integer getTaskType() {
		return taskType;
	}
	public void setTaskType(Integer taskType) {
		this.taskType = taskType;
	}
	public String getStartCreateDate() {
		return startCreateDate;
	}
	public void setStartCreateDate(String startCreateDate) {
		this.startCreateDate = startCreateDate;
	}
	public String getEndCreateDate() {
		return endCreateDate;
	}
	public void setEndCreateDate(String endCreateDate) {
		this.endCreateDate = endCreateDate;
	}
	
}
