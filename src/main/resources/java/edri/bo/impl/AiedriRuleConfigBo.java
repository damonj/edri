package com.asiainfo.aiedri.bo.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.asiainfo.aiedri.bo.IAiedriRuleConfigBo;
import com.asiainfo.aiedri.dao.IAiedriRuleConfigDao;
import com.asiainfo.aiedri.vo.AiedriRequestUserVo;
import com.asiainfo.aiedri.vo.AiedriRuleConfigVo;
import com.asiainfo.aiedri.vo.AiedriVendorType;
import com.asiainfo.aiedri.vo.ProtectedIpsVo;

public class AiedriRuleConfigBo implements IAiedriRuleConfigBo {
	protected Logger logger=Logger.getLogger(this.getClass());
	private IAiedriRuleConfigDao aiedriRuleConfigDao;
	public void setAiedriRuleConfigDao(IAiedriRuleConfigDao aiedriRuleConfigDao) {
		this.aiedriRuleConfigDao = aiedriRuleConfigDao;
	}

	@Override
	public List<ProtectedIpsVo> findAllProtectedIps() {
		return aiedriRuleConfigDao.findAllProtectedIps();
	}
	@Override
	public ProtectedIpsVo findProtectedIpsById(Integer id) {
		return aiedriRuleConfigDao.findProtectedIpsById(id);
	}
	@Override
	public int addProtectedIpsVo(ProtectedIpsVo vo) {
		return aiedriRuleConfigDao.addProtectedIpsVo(vo);
	}

	@Override
	public int updateProtectedIpsVo(ProtectedIpsVo vo) {
		return aiedriRuleConfigDao.updateProtectedIpsVo(vo);
	}

	@Override
	public int deleteProtectedIpsVo(Integer id) {
		return aiedriRuleConfigDao.deleteProtectedIpsVo(id);
	}

	@Override
	public AiedriRuleConfigVo findRuleConfigVoParam() {
		return aiedriRuleConfigDao.findRuleConfigVoParam();
	}

	@Override
	public int updateRuleConfigParam(AiedriRuleConfigVo vo) {
		return aiedriRuleConfigDao.updateRuleConfigParam(vo);
	}

	@Override
	public String findConfigParamByPName(String name) {
		return aiedriRuleConfigDao.findConfigParamByPName(name);
	}

	@Override
	public List<AiedriRequestUserVo> findAllRequestUser() {
		return aiedriRuleConfigDao.findAllRequestUser();
	}

	@Override
	public int updateRequestUser(AiedriRequestUserVo vo) {
		return aiedriRuleConfigDao.updateRequestUser(vo);
	}

	@Override
	public Map<String, AiedriRequestUserVo> getRequestUserMap() {
		logger.debug("--into AiedriRuleConfigBo getRequestUserMap() ---");
		List<AiedriRequestUserVo> list= findAllRequestUser();
		logger.debug("AiedriRuleConfigBo findAllRequestUser list size:"+list.size());
		Map<String, AiedriRequestUserVo> map=new HashMap<String, AiedriRequestUserVo>();
		if(CollectionUtils.isEmpty(list)){
			return null;
		}
		for(AiedriRequestUserVo vo:list){
			map.put(vo.getUserName(), vo);
		}
		return map;
	}

	@Override
	public Map<String, List<AiedriVendorType>> getVendorTypeMap() {
		List<AiedriVendorType> list=getVendorTypeList();
		if(CollectionUtils.isEmpty(list)){
			return null;
		}
		Map<String, List<AiedriVendorType>> map=new HashMap<String, List<AiedriVendorType>>();
		List<AiedriVendorType> tempList=null;
		for(AiedriVendorType type:list){
			if(map.isEmpty() || !map.containsKey(type.getVendorCode())){
				tempList=new ArrayList<AiedriVendorType>();
				tempList.add(type);
				map.put(type.getVendorCode(), tempList);
			}else{
				map.get(type.getVendorCode()).add(type);
			}
		}
		return map;
	}

	@Override
	public List<AiedriVendorType> getVendorTypeList() {
		return aiedriRuleConfigDao.getVendorTypeList();
	}



}
