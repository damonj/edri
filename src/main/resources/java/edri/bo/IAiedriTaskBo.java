package com.asiainfo.aiedri.bo;

import com.asiainfo.aiedri.rest.bean.StatusBean;
import com.asiainfo.aiedri.rest.bean.TupleRule;
import net.sf.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface IAiedriTaskBo {
	//�ӿڵ�����������
	public List<StatusBean> addJsonAddRequestAndTuple(List<TupleRule> list,HttpServletRequest servletRequest);
	public List<StatusBean> addJsonDeleteRequestAndTuple(List<TupleRule> list,HttpServletRequest servletRequest);
	public JSONArray addJsonSearchRequestAndTuple(List<TupleRule> list,HttpServletRequest servletRequest);
	public JSONArray addJsonSearchRequestAndTuple(HttpServletRequest servletRequest);

	//�����Ԫ�����
	public StatusBean addDeleteTupleRule(TupleRule rule,Integer requestId,Integer ruleStatus);
	public StatusBean addTupleRule(TupleRule rule,Integer requestId,Integer ruleStatus);
	/*�������ӿ���־��Ϣ*/
	public void createRequestVo(Integer id, HttpServletRequest servletRequest,int size, Integer taskType);
	
}
