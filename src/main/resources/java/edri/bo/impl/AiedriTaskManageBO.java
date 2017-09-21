package com.asiainfo.aiedri.bo.impl;

import com.asiainfo.aiedri.bo.IAiedriTaskManageBo;
import com.asiainfo.aiedri.dao.IAiedriTaskManageDAO;
import com.asiainfo.aiedri.rest.bean.Quintuple;
import com.asiainfo.aiedri.rest.bean.TupleRule;
import com.asiainfo.aiedri.vo.AiedriRequestVo;
import com.asiainfo.aiedri.vo.JqGridVo;
import com.asiainfo.aiedri.vo.TupleRuleVo;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class AiedriTaskManageBO implements IAiedriTaskManageBo{
	protected Logger logger=Logger.getLogger(this.getClass());
	private IAiedriTaskManageDAO aiedriTaskManageDAO;
	public void setAiedriTaskManageDAO(IAiedriTaskManageDAO aiedriTaskManageDAO) {
		this.aiedriTaskManageDAO = aiedriTaskManageDAO;
	}
	
	public void queryTupleRulesByTaskIdPageAndCon(int parseInt, JqGridVo vo) {
		aiedriTaskManageDAO.queryTupleRulesByTaskIdPageAndCon(parseInt, vo);
	}
	
	@Override
	public TupleRuleVo getTupleRulesDetailByTaskId(int parseInt) {
		return aiedriTaskManageDAO.getTupleRulesDetailByTaskId(parseInt);
	}
	@Override
	public void queryTaskTupleListBySearchVo(JqGridVo vo, TupleRuleVo task) {
		aiedriTaskManageDAO.queryTaskTupleListBySearchVo( vo, task);
	}

	@Override
	public void queryRequestListBySearchVo(JqGridVo vo, AiedriRequestVo task) {
		aiedriTaskManageDAO.queryRequestListBySearchVo(vo, task);
	}

	@Override
	public int loadCurrRuleNum(TupleRuleVo task) {
		return aiedriTaskManageDAO.loadCurrRuleNum(task);
	}

	@Override
	public List<AiedriRequestVo> getCurrValidTaskStatistic(TupleRuleVo task) {
		return aiedriTaskManageDAO.getCurrValidTaskStatistic(task);
	}


	@Override
	public List<TupleRule> getCurValidTuples() {
		List<TupleRuleVo> tupleRuleVos = aiedriTaskManageDAO.getCurValidTupleList();

		List<TupleRule> tupleRules = new ArrayList<TupleRule>();

		for (TupleRuleVo vo : tupleRuleVos) {
			TupleRule bean = new TupleRule();
			bean.setValid_after(vo.getValidAfter());
			bean.setValid_before(vo.getValidBefore());
			bean.setValid_scope(vo.getValidScope());
			bean.setDownlink(vo.getDownlink());
			bean.setUplink(vo.getUplink());
			bean.setRule_id(vo.getRuleId());
			bean.setRule_type(vo.getRuleType());
			Quintuple qt = new Quintuple();
			qt.setDip(vo.getDip());
			qt.setDmask(vo.getDmask());
			qt.setSip(vo.getSip());
			qt.setSmask(vo.getSmask());
			qt.setProto(vo.getProto());
			qt.setDport(vo.getDport());
			qt.setSport(vo.getSport());
			bean.setRule(qt);
			tupleRules.add(bean);
		}
		return tupleRules;
	}

}
