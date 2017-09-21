package com.asiainfo.aiedri.rest.bean;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Quintuple {
	private String sip;
	private String dip;
	private String smask;
	private String dmask;
	private Integer sport=null;
	private Integer dport=null;
	private String spmask;
	private String dpmask;
	private String proto;
	private String pmask;
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
	
}
