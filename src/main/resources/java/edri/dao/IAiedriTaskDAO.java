package com.asiainfo.aiedri.dao;



import com.asiainfo.aiedri.rest.bean.StatusBean;
import com.asiainfo.aiedri.rest.bean.TupleRule;
import com.asiainfo.aiedri.vo.AiedriRequestVo;

public interface IAiedriTaskDAO {
	/*添加请求接口日志信息*/
	int addAiedriRequest(AiedriRequestVo vo);
	StatusBean addTupleRule(TupleRule rule,Integer requestId,Integer ruleStatus);
	StatusBean addDeleteTupleRule(TupleRule rule,Integer requestId,Integer ruleStatus);
}
