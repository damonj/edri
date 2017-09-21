package com.asiainfo.aiedri.bo;

import com.asiainfo.aiedri.rest.bean.TupleRule;
import com.asiainfo.aiedri.vo.AiedriRequestVo;
import com.asiainfo.aiedri.vo.JqGridVo;
import com.asiainfo.aiedri.vo.TupleRuleVo;

import java.util.List;

public interface IAiedriTaskManageBo {
	
	//��������Id��ѯ���е��·��Ĺ���
	public void queryTupleRulesByTaskIdPageAndCon(int parseInt, JqGridVo vo);
    
    //��������Id��ѯ���е��·��Ĺ���
	public TupleRuleVo getTupleRulesDetailByTaskId(int parseInt);
    //����ʱ��Ͳ�ѯ������ѯ�·�����
	public void queryTaskTupleListBySearchVo(JqGridVo vo, TupleRuleVo task);
	//����ʱ��Ͳ�ѯ������ѯ�����¼
	public void queryRequestListBySearchVo(JqGridVo vo, AiedriRequestVo task);
    //�õ���ǰ��Ч����ĸ���
	public int loadCurrRuleNum(TupleRuleVo task);
	//�õ���ǰ��Ч���������ͳ��
	public List<AiedriRequestVo> getCurrValidTaskStatistic(TupleRuleVo task);
	//��ѯ��ǰ��Ч������б�
	public List<TupleRule> getCurValidTuples();
}
