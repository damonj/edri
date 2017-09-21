package com.asiainfo.aiedri.bo;

import com.asiainfo.aiedri.rest.bean.TupleRule;
import com.asiainfo.aiedri.vo.AiedriRequestVo;
import com.asiainfo.aiedri.vo.JqGridVo;
import com.asiainfo.aiedri.vo.TupleRuleVo;

import java.util.List;

public interface IAiedriTaskManageBo {
	
	//根据任务Id查询所有的下发的规则
	public void queryTupleRulesByTaskIdPageAndCon(int parseInt, JqGridVo vo);
    
    //根据任务Id查询所有的下发的规则
	public TupleRuleVo getTupleRulesDetailByTaskId(int parseInt);
    //根据时间和查询条件查询下发规则
	public void queryTaskTupleListBySearchVo(JqGridVo vo, TupleRuleVo task);
	//根据时间和查询条件查询请求记录
	public void queryRequestListBySearchVo(JqGridVo vo, AiedriRequestVo task);
    //得到当前生效规则的个数
	public int loadCurrRuleNum(TupleRuleVo task);
	//得到当前生效规则的请求统计
	public List<AiedriRequestVo> getCurrValidTaskStatistic(TupleRuleVo task);
	//查询当前生效规则的列表
	public List<TupleRule> getCurValidTuples();
}
