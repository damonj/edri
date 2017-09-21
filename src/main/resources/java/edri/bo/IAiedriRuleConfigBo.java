package com.asiainfo.aiedri.bo;

import java.util.List;
import java.util.Map;

import com.asiainfo.aiedri.vo.AiedriRequestUserVo;
import com.asiainfo.aiedri.vo.AiedriRuleConfigVo;
import com.asiainfo.aiedri.vo.AiedriVendorType;
import com.asiainfo.aiedri.vo.ProtectedIpsVo;





public interface IAiedriRuleConfigBo {
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
	Map<String,AiedriRequestUserVo> getRequestUserMap();
	
	//��ȡ���е��豸����
	Map<String,List<AiedriVendorType>> getVendorTypeMap();
	List<AiedriVendorType> getVendorTypeList();
}
