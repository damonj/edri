package com.asiainfo.aiedri.dao;

import java.util.List;
import java.util.Map;

import com.asiainfo.aiedri.vo.AiedriRequestUserVo;
import com.asiainfo.aiedri.vo.AiedriRuleConfigVo;
import com.asiainfo.aiedri.vo.AiedriVendorType;
import com.asiainfo.aiedri.vo.ProtectedIpsVo;





public interface IAiedriRuleConfigDao {
	List<ProtectedIpsVo> findAllProtectedIps();
	ProtectedIpsVo findProtectedIpsById(Integer id);
	int addProtectedIpsVo(ProtectedIpsVo vo);
	int updateProtectedIpsVo(ProtectedIpsVo vo);
	int deleteProtectedIpsVo(Integer id);
	
	AiedriRuleConfigVo findRuleConfigVoParam();
	int updateRuleConfigParam(AiedriRuleConfigVo vo);
	//���������name����AiedriRuleConfigVo�е����Լ��ɡ�
	String findConfigParamByPName(String name);
	
	List<AiedriRequestUserVo> findAllRequestUser();
	int updateRequestUser(AiedriRequestUserVo vo);
	
	List<AiedriVendorType> getVendorTypeList();
}
