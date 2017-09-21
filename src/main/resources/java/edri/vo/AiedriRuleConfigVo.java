package com.asiainfo.aiedri.vo;

public class AiedriRuleConfigVo {
	private Integer maxNum;
	private Float alertThresh;
	private Float terminatThresh;
	private Integer minMask;
	private Integer maxDurationTime;
	public Integer getMaxNum() {
		return maxNum;
	}
	public void setMaxNum(Integer maxNum) {
		this.maxNum = maxNum;
	}
	public Float getAlertThresh() {
		return alertThresh;
	}
	public void setAlertThresh(Float alertThresh) {
		this.alertThresh = alertThresh;
	}
	public Float getTerminatThresh() {
		return terminatThresh;
	}
	public void setTerminatThresh(Float terminatThresh) {
		this.terminatThresh = terminatThresh;
	}
	public Integer getMinMask() {
		return minMask;
	}
	public void setMinMask(Integer minMask) {
		this.minMask = minMask;
	}
	public Integer getMaxDurationTime() {
		return maxDurationTime;
	}
	public void setMaxDurationTime(Integer maxDurationTime) {
		this.maxDurationTime = maxDurationTime;
	}
}
