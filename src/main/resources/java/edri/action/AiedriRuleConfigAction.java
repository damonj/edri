package com.asiainfo.aiedri.action;

import ainx.common.spring.BeanFactoryUtil;
import ainx.common.util.CryptUtil;

import com.asiainfo.aiedri.bo.IAiedriRuleConfigBo;
import com.asiainfo.aiedri.vo.AiedriRequestUserVo;
import com.asiainfo.aiedri.vo.AiedriRuleConfigVo;
import com.asiainfo.aiedri.vo.ProtectedIpsVo;

import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;

public class AiedriRuleConfigAction extends AiedriBaseAction{
    public IAiedriRuleConfigBo getAiedriRuleConfigBo() {
   	 return (IAiedriRuleConfigBo)BeanFactoryUtil.getBean("aiedriRuleConfigBo");

   }
	public ActionForward findAllProtectedIps(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject backJson = new JSONObject();
		try {
			List<ProtectedIpsVo> list=getAiedriRuleConfigBo().findAllProtectedIps();
			backJson.put("flag", true);
			backJson.put("msg", "��ѯ�ɹ�!");
			backJson.put("data", list);
		} catch (Exception nfae) {
			backJson.put("flag", false);
			backJson.put("msg", nfae.getMessage());
		}
		writeObjectJson(response, backJson);
		return null;
	}
	public ActionForward addProtectedIps(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject backJson = new JSONObject();
		ProtectedIpsVo vo = trans2Bean(request, ProtectedIpsVo.class);
		Long currTime=System.currentTimeMillis()/1000L;
		vo.setCreatTime(String.valueOf(currTime));
		boolean flag=false;
		String msg="����ʧ�ܣ�";
		try {
			int num=getAiedriRuleConfigBo().addProtectedIpsVo(vo);
			if(num>0){
				flag=true;
				msg="����ɹ���";
			}
		} catch (Exception nfae) {
			msg=nfae.getMessage();
		}
		backJson.put("flag",flag);
		backJson.put("msg", msg);
		writeObjectJson(response, backJson);
		return null;
	}
	public ActionForward findProtectedIpsById(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		String id=request.getParameter("id");
		if(StringUtils.isEmpty(id)){
			return null;
		}
		JSONObject backJson = new JSONObject();
		try {
			ProtectedIpsVo vo=getAiedriRuleConfigBo().findProtectedIpsById(Integer.parseInt(id));
			backJson.put("flag", true);
			backJson.put("msg", "��ѯ�ɹ�!");
			backJson.put("data", vo);
		} catch (Exception nfae) {
			backJson.put("flag", false);
			backJson.put("msg", nfae.getMessage());
		}
		writeObjectJson(response, backJson);
		return null;
	}

	public ActionForward editProtectedIpsById(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject backJson = new JSONObject();
		ProtectedIpsVo vo = trans2Bean(request, ProtectedIpsVo.class);
		Long currTime=System.currentTimeMillis()/1000L;
		vo.setCreatTime(String.valueOf(currTime));
		boolean flag=false;
		String msg="����ʧ�ܣ�";
		try {
			int num=getAiedriRuleConfigBo().updateProtectedIpsVo(vo);
			if(num>0){
				flag=true;
				msg="���³ɹ���";
			}
		} catch (Exception nfae) {
			msg=nfae.getMessage();
		}
		backJson.put("flag",flag);
		backJson.put("msg", msg);
		writeObjectJson(response, backJson);
		return null;
	}
	public ActionForward deleteProtectedIpsById(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		String id=request.getParameter("id");
		if(StringUtils.isEmpty(id)){
			return null;
		}
		JSONObject backJson = new JSONObject();
		boolean flag=false;
		String msg="ɾ��ʧ�ܣ�";
		try {
			int num=getAiedriRuleConfigBo().deleteProtectedIpsVo(Integer.parseInt(id));
			if(num>0){
				flag=true;
				msg="ɾ���ɹ���";
			}
		} catch (Exception nfae) {
			msg=nfae.getMessage();
		}
		backJson.put("flag",flag);
		backJson.put("msg", msg);
		writeObjectJson(response, backJson);
		return null;
	}
	
	public ActionForward findRuleConfig(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject backJson = new JSONObject();
		try {
			AiedriRuleConfigVo vo=getAiedriRuleConfigBo().findRuleConfigVoParam();
			backJson.put("flag", true);
			backJson.put("msg", "��ѯ�ɹ�!");
			backJson.put("data", vo);
		} catch (Exception nfae) {
			backJson.put("flag", false);
			backJson.put("msg", nfae.getMessage());
		}
		writeObjectJson(response, backJson);
		return null;
	}
	public ActionForward updateRuleConfig(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		JSONObject backJson = new JSONObject();
		String maxNum=request.getParameter("maxNum");
		String alertThresh=request.getParameter("alertThresh");
		String terminatThresh=request.getParameter("terminatThresh");
		String minMask=request.getParameter("minMask");
		String maxDurationTime=request.getParameter("maxDurationTime");
		AiedriRuleConfigVo vo=new AiedriRuleConfigVo();
		if(StringUtils.isNotEmpty(maxNum)){
			vo.setMaxNum(Integer.parseInt(maxNum));
		}
		if(StringUtils.isNotEmpty(alertThresh)){
			vo.setAlertThresh(Float.parseFloat(alertThresh));
		}
		if(StringUtils.isNotEmpty(terminatThresh)){
			vo.setTerminatThresh(Float.parseFloat(terminatThresh));
		}
		if(null!=minMask){
			vo.setMinMask(Integer.parseInt(minMask));
		}
		if(null!=maxDurationTime){
			vo.setMaxDurationTime(Integer.parseInt(maxDurationTime));
		}
		boolean flag=false;
		String msg="����ʧ�ܣ�";
		try {
			int num=getAiedriRuleConfigBo().updateRuleConfigParam(vo);
			if(num>0){
				flag=true;
				msg="���³ɹ���";
			}
		} catch (Exception nfae) {
			msg=nfae.getMessage();
		}
		backJson.put("flag",flag);
		backJson.put("msg", msg);
		writeObjectJson(response, backJson);
		return null;
	}
	//��ѯ���еĽӿ������û�������Ϣ
		public ActionForward findRequestUserConfig(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response) throws Exception {
			JSONObject backJson = new JSONObject();
			try {
				List<AiedriRequestUserVo> list=getAiedriRuleConfigBo().findAllRequestUser();
				AiedriRequestUserVo vo=CollectionUtils.isEmpty(list)?null:list.get(0);
				backJson.put("flag", true);
				backJson.put("msg", "��ѯ�ɹ�!");
				backJson.put("data", vo);
			} catch (Exception nfae) {
				backJson.put("flag", false);
				backJson.put("msg", nfae.getMessage());
			}
			writeObjectJson(response, backJson);
			return null;
		}
		//���½ӿ������û�������Ϣ
		public ActionForward updateRequestUserConfig(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response) throws Exception {
			JSONObject backJson = new JSONObject();
			AiedriRequestUserVo vo = trans2Bean(request, AiedriRequestUserVo.class);
			boolean flag=false;
			String msg="����ʧ�ܣ�";
			try {
				if(StringUtils.isNotEmpty(vo.getUserPassword())){
					String pass=CryptUtil.getDigest(vo.getUserPassword());
					vo.setUserPassword(pass);
				}
				int num=getAiedriRuleConfigBo().updateRequestUser(vo);
				if(num>0){
					flag=true;
					msg="���³ɹ���";
				}
			} catch (Exception nfae) {
				msg=nfae.getMessage();
			}
			backJson.put("flag",flag);
			backJson.put("msg", msg);
			writeObjectJson(response, backJson);
			return null;
		}
}
