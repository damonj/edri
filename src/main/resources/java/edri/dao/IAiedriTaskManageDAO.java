package com.asiainfo.aiedri.dao;



import com.asiainfo.aiedri.vo.AiedriRequestVo;
import com.asiainfo.aiedri.vo.JqGridVo;
import com.asiainfo.aiedri.vo.TupleRuleVo;

import java.util.List;

public interface IAiedriTaskManageDAO {
	//根据任务Id查询所有的下发的规则
	public TupleRuleVo getTupleRulesDetailByTaskId(int parseInt);
	//根据任务Id查询所有的下发的规则
	public void queryTupleRulesByTaskIdPageAndCon(int parseInt, JqGridVo vo);
    //根据时间和查询条件查询下发规则
	void queryTaskTupleListBySearchVo(JqGridVo vo, TupleRuleVo task);
	//根据时间和查询条件查询请求记录
	public void queryRequestListBySearchVo(JqGridVo vo, AiedriRequestVo task);
	//得到当前生效规则的个数
    public int loadCurrRuleNum(TupleRuleVo task);
    //得到当前生效规则的请求统计
  	public List<AiedriRequestVo> getCurrValidTaskStatistic(TupleRuleVo task);

	List<TupleRuleVo> getCurValidTupleList();

	Integer updateRuleStatus();

	Integer moveNotExistRules();

	Integer deleteNotExistRules();
}
