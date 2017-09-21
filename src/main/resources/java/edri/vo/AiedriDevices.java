package com.asiainfo.aiedri.vo;

import java.util.List;

public class AiedriDevices {
	private Integer id;
	private String name;
	private String ip;
	private String snmpPort="161";
	private String snmpVersion="2";
	private String snmpString;
	private String deviceModel;
	private String deviceManufact;
	private String userName;
	private String password;
	private List<AiedriDevicePort> ports;
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
	public String getSnmpPort() {
		return snmpPort;
	}
	public void setSnmpPort(String snmpPort) {
		this.snmpPort = snmpPort;
	}
	public String getSnmpVersion() {
		return snmpVersion;
	}
	public void setSnmpVersion(String snmpVersion) {
		this.snmpVersion = snmpVersion;
	}
	public String getSnmpString() {
		return snmpString;
	}
	public void setSnmpString(String snmpString) {
		this.snmpString = snmpString;
	}
	public String getDeviceModel() {
		return deviceModel;
	}
	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}
	public String getDeviceManufact() {
		return deviceManufact;
	}
	public void setDeviceManufact(String deviceManufact) {
		this.deviceManufact = deviceManufact;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public List<AiedriDevicePort> getPorts() {
		return ports;
	}
	public void setPorts(List<AiedriDevicePort> ports) {
		this.ports = ports;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}

}
