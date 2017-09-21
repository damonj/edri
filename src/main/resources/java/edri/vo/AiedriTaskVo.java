package com.asiainfo.aiedri.vo;

import com.asiainfo.aiedri.util.TaskConstant;


public class AiedriTaskVo {

	private Integer taskId;
	private Integer status;
	private Integer taskType;
	private String userId;
	private String createDate;
	private String fromIp;
	private String dealDate;
	private String descr;
	private String name;
	private String exeResult;
    public AiedriTaskVo() {
		super();
	}
	public AiedriTaskVo(Integer taskId,String userId,String fromIp,Integer taskType) {
		this.taskId=taskId;
		Long curr=System.currentTimeMillis()/1000L;
		this.createDate=String.valueOf(curr);
		this.userId=userId;
		this.status=TaskConstant.TASK_STATUS_CREATE;
		this.fromIp=fromIp;
		this.name=TaskConstant.RULE_TYPE_TUPLE+"_"+curr;
		this.taskType=taskType;
	}
	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getFromIp() {
		return fromIp;
	}
	public void setFromIp(String fromIp) {
		this.fromIp = fromIp;
	}
	public String getDealDate() {
		return dealDate;
	}
	public void setDealDate(String dealDate) {
		this.dealDate = dealDate;
	}
	public String getDescr() {
		return descr;
	}
	public void setDescr(String descr) {
		this.descr = descr;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Integer getTaskType() {
		return taskType;
	}
	public void setTaskType(Integer taskType) {
		this.taskType = taskType;
	}
	public String getExeResult() {
		return exeResult;
	}
	public void setExeResult(String exeResult) {
		this.exeResult = exeResult;
	}
	public Integer getTaskId() {
		return taskId;
	}
	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}
	
}