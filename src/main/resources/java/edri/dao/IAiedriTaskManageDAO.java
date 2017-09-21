package com.asiainfo.aiedri.dao;



import com.asiainfo.aiedri.vo.AiedriRequestVo;
import com.asiainfo.aiedri.vo.JqGridVo;
import com.asiainfo.aiedri.vo.TupleRuleVo;

import java.util.List;

public interface IAiedriTaskManageDAO {
	//��������Id��ѯ���е��·��Ĺ���
	public TupleRuleVo getTupleRulesDetailByTaskId(int parseInt);
	//��������Id��ѯ���е��·��Ĺ���
	public void queryTupleRulesByTaskIdPageAndCon(int parseInt, JqGridVo vo);
    //����ʱ��Ͳ�ѯ������ѯ�·�����
	void queryTaskTupleListBySearchVo(JqGridVo vo, TupleRuleVo task);
	//����ʱ��Ͳ�ѯ������ѯ�����¼
	public void queryRequestListBySearchVo(JqGridVo vo, AiedriRequestVo task);
	//�õ���ǰ��Ч����ĸ���
    public int loadCurrRuleNum(TupleRuleVo task);
    //�õ���ǰ��Ч���������ͳ��
  	public List<AiedriRequestVo> getCurrValidTaskStatistic(TupleRuleVo task);

	List<TupleRuleVo> getCurValidTupleList();

	Integer updateRuleStatus();

	Integer moveNotExistRules();

	Integer deleteNotExistRules();
}
