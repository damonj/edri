package com.asiainfo.aiedri.rest.constant;

public enum RespStatus {
	ok(0,"0 OK"),noFound(1,"1 Not Found"),ruleError(2,"2 Rule Error"),
	unauth(3,"3 Unauthorized"),
	hasExist(4,"4 Rule Existed"),cFull(5,"5 Capacity Full"),
	protect(6,"6 Protected List Hit"),
	typeError(7,"7 Rule Type Unsupported"),timeError(8,"8 Valid Time Error"),
	upError(9,"9 Uplink Error"),downError(10,"10 Downlink Error"),
	rangError(11,"11 Forbidden£¬Range Error"),confilict(12,"12 Rule Conflicted"),
	ipError(100,"100 IP Error"),maskError(101,"101 IP Mask"),
	ipMaskError(102,"102 Prefix IP Mask Error"),methodError(200,"200 HTTP Method Error"),
	urlError(201,"201 URL Too Long"),domainError(300,"300 Domain Too Long"),
	other(9999,"9999 Other Error");
	
	public final int statusCode;
	public final String statusDesc;
	private RespStatus(int statusCode,String statusDesc){
		this.statusCode = statusCode;
		this.statusDesc=statusDesc;
	}
}
