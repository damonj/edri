package com.asiainfo.aiedri.vo;

public class AiedriDevicePort {
	private Integer id;
	private String name;
	private Integer deviceId;
	private Double bandwidth;
	public AiedriDevicePort(){}
	public AiedriDevicePort(String name,Integer dId){
		this.name=name;
		this.deviceId=dId;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}
	public Double getBandwidth() {
		return bandwidth;
	}
	public void setBandwidth(Double bandwidth) {
		this.bandwidth = bandwidth;
	}
}
