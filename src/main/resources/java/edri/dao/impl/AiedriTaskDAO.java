package com.asiainfo.aiedri.dao.impl;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import ainx.common.spring.jdbc.impl.JdbcPagingUtil;
import ainx.common.util.SequenceUtil;

import com.asiainfo.aiedri.dao.IAiedriTaskDAO;
import com.asiainfo.aiedri.rest.bean.Quintuple;
import com.asiainfo.aiedri.rest.bean.StatusBean;
import com.asiainfo.aiedri.rest.bean.TupleRule;
import com.asiainfo.aiedri.rest.constant.RespStatus;
import com.asiainfo.aiedri.vo.AiedriRequestVo;


public class AiedriTaskDAO implements IAiedriTaskDAO {
    protected Logger logger=Logger.getLogger(this.getClass());
	protected JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	private JdbcPagingUtil jdbcPagingUtil;
	public void setJdbcPagingUtil(JdbcPagingUtil jdbcPagingUtil) {
		this.jdbcPagingUtil = jdbcPagingUtil;
	}
	@Override
	public int addAiedriRequest(AiedriRequestVo vo) {
		String sql="INSERT INTO `aiedri_request`(ID,CREATE_DATE,USER_ID,FROM_IP,TASK_NUM,TASK_TYPE) "
				+ " VALUES (?,?,?,?,?,?)";
		Object[] obj=new Object[]{vo.getId(),vo.getCreateDate(),vo.getUserId(),vo.getFromIp(),vo.getTaskNum(),vo.getTaskType()};
		logger.debug("addAiedriRequest sql:"+sql);
		return jdbcTemplate.update(sql, obj);
	}
	@Override
	public StatusBean addTupleRule(TupleRule rule,Integer requestId,Integer ruleStatus) {
		StatusBean bean=new StatusBean(rule.getRule_id(),RespStatus.ok.statusDesc);
		int num=0;
		try{
			String[] subSqls={};
			Quintuple tuple=rule.getRule();
			if(null!=tuple){
				subSqls=compTupleSql(rule.getRule());
			}
			Integer id=SequenceUtil.getSequenceId("aiedriRuleId");
			String sql=" INSERT INTO AIEDRI_TASK_TUPLE(ID,VALID_AFTER,VALID_BEFORE,VALID_SCOPE,"
					+ "  RULE_ID, RULE_TYPE,"
					+ subSqls[0]
					+ "  UPLINK,DOWNLINK,"
					+ "  REQUEST_ID,STATUS "
					+ "  ) VALUES( "
					+ "  ?,?,?,?,?,?,"
					+ subSqls[1]
					+ " ?,?,?,?)";
			Object[] params=new Object[]{id,rule.getValid_after(),rule.getValid_before(),
					rule.getValid_scope(),rule.getRule_id(),rule.getRule_type(),
					rule.getUplink(),rule.getDownlink(),
					requestId,ruleStatus};
			logger.debug("addTupleRule sql:"+sql);
			num= jdbcTemplate.update(sql, params);
		}catch(Exception e){
			bean.setStatus(RespStatus.other.statusDesc);
			logger.error(e.getMessage());
		}
		if(num<=0){
			bean.setStatus(RespStatus.other.statusDesc);
		}
		return bean;
	}
	@Override
	public StatusBean addDeleteTupleRule(TupleRule rule,Integer requestId,Integer ruleStatus) {
		StatusBean bean=new StatusBean(rule.getRule_id(),RespStatus.ok.statusDesc);
		int num=0;
		try{
			Integer id=SequenceUtil.getSequenceId("aiedriRuleId");
			String sql=" INSERT INTO AIEDRI_TASK_TUPLE(ID, RULE_ID, "
					+ "  REQUEST_ID,STATUS "
					+ "  ) VALUES( "
					+ "  ?,?,?,?)";
			Object[] params=new Object[]{id,rule.getRule_id(),
					requestId,ruleStatus};
			logger.debug("addDeleteTupleRule sql:"+sql);
			num= jdbcTemplate.update(sql, params);
		}catch(Exception e){
			bean.setStatus(RespStatus.other.statusDesc);
			logger.error(e.getMessage());
		}
		if(num<=0){
			bean.setStatus(RespStatus.other.statusDesc);
		}
		return bean;
	}

	private String[] compTupleSql(Quintuple tuple){
		String[] array=new String[2];
		StringBuffer subSqlProp=new StringBuffer();
		StringBuffer subSqlValue=new StringBuffer();
		if(null!=tuple.getSip() && !"".equals(tuple.getSip())){
			subSqlProp.append(" SIP,");
			subSqlValue.append(" '"+tuple.getSip()+"',");
		}
		if(null!=tuple.getDip() && !"".equals(tuple.getDip())){
			subSqlProp.append(" DIP,");
			subSqlValue.append(" '"+tuple.getDip()+"',");
		}
		if(null!=tuple.getSmask() && !"".equals(tuple.getSmask())){
			subSqlProp.append(" SMASK,");
			subSqlValue.append(" '"+tuple.getSmask()+"',");
		}
		if(null!=tuple.getDmask() && !"".equals(tuple.getDmask())){
			subSqlProp.append(" DMASK,");
			subSqlValue.append(" '"+tuple.getDmask()+"',");
		}
		if(null!=tuple.getSport()){
			subSqlProp.append(" SPORT,");
			subSqlValue.append(" '"+tuple.getSport()+"',");
		}
		if(null!=tuple.getDport()){
			subSqlProp.append(" DPORT,");
			subSqlValue.append(" '"+tuple.getDport()+"',");
		}
		if(null!=tuple.getSpmask() && !"".equals(tuple.getSpmask())){
			subSqlProp.append(" SPMASK,");
			subSqlValue.append(" '"+tuple.getSpmask()+"',");
		}
		if(null!=tuple.getDpmask() && !"".equals(tuple.getDpmask())){
			subSqlProp.append(" DPMASK,");
			subSqlValue.append(" '"+tuple.getDpmask()+"',");
		}
		if(null!=tuple.getProto() && !"".equals(tuple.getProto())){
			subSqlProp.append(" PROTO,");
			subSqlValue.append(" '"+tuple.getProto()+"',");
		}
		if(null!=tuple.getPmask() && !"".equals(tuple.getPmask())){
			subSqlProp.append(" PMASK");
			subSqlValue.append(" '"+tuple.getPmask()+"'");
		}
		logger.debug("compTupleSql subSqlProp:"+subSqlProp.toString());
		logger.debug("compTupleSql subSqlValue:"+subSqlValue.toString());
		array[0]=subSqlProp.toString();
		array[1]=subSqlValue.toString();
		return array;
	}
}
