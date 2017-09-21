package com.asiainfo.aiedri.vo;

import java.util.Collections;
import java.util.List;

public class JqGridVo {
	@SuppressWarnings("rawtypes")
	protected List dataList = Collections.emptyList();
	protected Integer rows = Integer.valueOf(0);
	protected Integer page = Integer.valueOf(0);
	protected Integer total = Integer.valueOf(1);
	protected Integer record = Integer.valueOf(0);
	protected String sord;
	protected String sidx;
	protected String search;
	@SuppressWarnings("rawtypes")
	public List getDataList() {
		return dataList;
	}
	@SuppressWarnings("rawtypes")
	public void setDataList(List dataList) {
		this.dataList = dataList;
	}
	public Integer getRows() {
		return rows;
	}
	public void setRows(Integer rows) {
		this.rows = rows;
	}
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
	}
	public Integer getRecord() {
		return record;
	}
	public void setRecord(Integer record) {
		this.record = record;
	}
	public String getSord() {
		return sord;
	}
	public void setSord(String sord) {
		this.sord = sord;
	}
	public String getSidx() {
		return sidx;
	}
	public void setSidx(String sidx) {
		this.sidx = sidx;
	}
	public String getSearch() {
		return search;
	}
	public void setSearch(String search) {
		this.search = search;
	}
}
