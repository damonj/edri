package com.asiainfo.aiedri.dao.impl;

import ainx.common.spring.jdbc.EJB3AnnontationRowMapper;
import ainx.common.spring.jdbc.impl.JdbcPagingUtil;
import com.asiainfo.aiedri.dao.IAiedriTaskManageDAO;
import com.asiainfo.aiedri.util.QuerySqlUtil;
import com.asiainfo.aiedri.util.RuleConstant;
import com.asiainfo.aiedri.util.TaskConstant;
import com.asiainfo.aiedri.vo.AiedriRequestVo;
import com.asiainfo.aiedri.vo.JqGridVo;
import com.asiainfo.aiedri.vo.TupleRuleVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;


public class AiedriTaskManageDAO implements IAiedriTaskManageDAO {

	protected JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	private JdbcPagingUtil jdbcPagingUtil;
	public void setJdbcPagingUtil(JdbcPagingUtil jdbcPagingUtil) {
		this.jdbcPagingUtil = jdbcPagingUtil;
	}


public void queryTupleRulesByTaskIdPageAndCon(int id, JqGridVo vo) {
	StringBuilder conditionBuilder = new StringBuilder();
	conditionBuilder.append(" WHERE ID= "+id);
	if(StringUtils.isNotEmpty(vo.getSearch())){
		conditionBuilder.append(vo.getSearch());
	}
	List<Object> paramList = new ArrayList<Object>();
    String order = " ORDER BY " + " `" + QuerySqlUtil.changePropertyToSql(vo.getSidx()) + "` " + vo.getSord();
	String selectSql = "SELECT * FROM AIEDRI_TASK_TUPLE"
			+ conditionBuilder
			+ order;
	
	int allsize = queryAllTupleRuleCount(conditionBuilder, paramList.toArray());
	QuerySqlUtil.processGridVo(vo, allsize);
	@SuppressWarnings("unchecked")
	List<TupleRuleVo> list = jdbcPagingUtil.getListByPage(jdbcTemplate, selectSql, paramList.toArray(), TupleRuleVo.class, vo.getPage(), vo.getRows());
	vo.setDataList(list);
}
private int queryAllTupleRuleCount(StringBuilder conditionBuilder,
		Object[] array) {
	String selectAllCount = "SELECT COUNT(1) FROM AIEDRI_TASK_TUPLE" + conditionBuilder.toString();
	int total = jdbcTemplate.queryForInt(selectAllCount, array);
	return total;
}

@Override
public TupleRuleVo getTupleRulesDetailByTaskId(int id) {
	String selectSql = "SELECT T.*, R.CREATE_DATE,R.TASK_TYPE "
			+ "           FROM AIEDRI_REQUEST R , AIEDRI_TASK_TUPLE T "
			+ "          WHERE R.ID=T.REQUEST_ID "
			+ "            AND T.ID=?";
	Object[] obj=new Object[]{id};
	List<TupleRuleVo> list = jdbcTemplate.query(selectSql, obj, new EJB3AnnontationRowMapper(TupleRuleVo.class));
	return CollectionUtils.isEmpty(list)?null:list.get(0);
}
@Override
public void queryTaskTupleListBySearchVo(JqGridVo vo, TupleRuleVo task) {
	StringBuilder conditionBuilder = createTaskTupleConditionBuilder(task);
	Object[] paramList = new Object[]{};
    String order = " ORDER BY " + " `" + QuerySqlUtil.changePropertyToSql(vo.getSidx()) + "` " + vo.getSord();
	String selectSql = "SELECT RULE.*,Q.CREATE_DATE,Q.TASK_TYPE "
			+ "           FROM AIEDRI_TASK_TUPLE RULE "
			+ "           LEFT JOIN AIEDRI_REQUEST Q "
			+ "             ON RULE.REQUEST_ID=Q.ID "
			+ conditionBuilder
			+ order;
	
	int allsize = queryTaskTupleRuleCount(conditionBuilder, paramList);
	QuerySqlUtil.processGridVo(vo, allsize);
	@SuppressWarnings("unchecked")
	List<TupleRuleVo> list = jdbcPagingUtil.getListByPage(jdbcTemplate, selectSql, paramList, TupleRuleVo.class, vo.getPage(), vo.getRows());
	vo.setDataList(list);
}
@Override
public int loadCurrRuleNum(TupleRuleVo task) {
	StringBuilder conditionBuilder = createTaskTupleConditionBuilder(task);
	Object[] paramList = new Object[]{};
	return queryTaskTupleRuleCount(conditionBuilder, paramList);
}
private int queryTaskTupleRuleCount(StringBuilder conditionBuilder,
		Object[] array) {
	String selectAllCount = "SELECT COUNT(1) FROM AIEDRI_TASK_TUPLE RULE "
			+ "                LEFT JOIN AIEDRI_REQUEST Q "
			+ "                  ON RULE.REQUEST_ID=Q.ID " 
			+ conditionBuilder.toString();
	int total = jdbcTemplate.queryForInt(selectAllCount, array);
	return total;
}
private StringBuilder createTaskTupleConditionBuilder(TupleRuleVo task) {
	StringBuilder conditionBuilder=new StringBuilder();
	if(null==task){
		return null;
	}
	conditionBuilder.append(" WHERE 1=1 ");
	if(StringUtils.isNotEmpty(task.getStartCreateDate()) && StringUtils.isNotEmpty(task.getEndCreateDate())){
		conditionBuilder.append(" AND Q.CREATE_DATE>= "+task.getStartCreateDate())
		                .append(" AND Q.CREATE_DATE<= "+task.getEndCreateDate());
	}
	if(null!=task.getTaskType()){
		conditionBuilder.append(" AND Q.TASK_TYPE= "+task.getTaskType());
	}
	if(null!=task.getStatus()){
		conditionBuilder.append(" AND RULE.STATUS= "+task.getStatus());
	}
	if(StringUtils.isNotEmpty(task.getValidBefore()) && StringUtils.isNotEmpty(task.getValidAfter())){
		conditionBuilder.append(" AND RULE.VALID_BEFORE>="+task.getValidBefore())
		                .append(" AND RULE.VALID_AFTER>="+task.getValidAfter());
	}
	if(StringUtils.isNotEmpty(task.getValidScope())){
		conditionBuilder.append(" AND RULE.VALID_SCOPE='"+task.getValidScope()+"'");
	}
	if(StringUtils.isNotEmpty(task.getRuleId())){
		conditionBuilder.append(" AND RULE.RULE_ID='"+task.getRuleId()+"'");
	}
	if(StringUtils.isNotEmpty(task.getRuleType())){
		conditionBuilder.append(" AND RULE.RULE_TYPE='"+task.getRuleType()+"'");
	}
	if(StringUtils.isNotEmpty(task.getSip())){
		conditionBuilder.append(" AND RULE.SIP='"+task.getSip()+"'");
	}
	if(StringUtils.isNotEmpty(task.getDip())){
		conditionBuilder.append(" AND RULE.DIP='"+task.getDip()+"'");
	}
	if(StringUtils.isNotEmpty(task.getSmask())){
		conditionBuilder.append(" AND RULE.SMASK='"+task.getSmask()+"'");
	}
	if(StringUtils.isNotEmpty(task.getDmask())){
		conditionBuilder.append(" AND RULE.DMASK='"+task.getDmask()+"'");
	}
	if(null!=task.getSport()){
		conditionBuilder.append(" AND RULE.SPORT="+task.getSport());
	}
	if(null!=task.getDport()){
		conditionBuilder.append(" AND RULE.DPORT="+task.getDport());
	}
	if(StringUtils.isNotEmpty(task.getSpmask())){
		conditionBuilder.append(" AND RULE.SPMASK='"+task.getSpmask()+"'");
	}
	if(StringUtils.isNotEmpty(task.getDpmask())){
		conditionBuilder.append(" AND RULE.DPMASK='"+task.getDpmask()+"'");
	}
	if(StringUtils.isNotEmpty(task.getProto())){
		conditionBuilder.append(" AND RULE.PROTO='"+task.getProto()+"'");
	}
	if(StringUtils.isNotEmpty(task.getPmask())){
		conditionBuilder.append(" AND RULE.PMASK='"+task.getPmask()+"'");
	}
	if(null!=task.getRequestId()){
		conditionBuilder.append(" AND RULE.REQUEST_ID="+task.getRequestId());
	}
	return conditionBuilder;
}


@Override
public void queryRequestListBySearchVo(JqGridVo vo, AiedriRequestVo task) {
	StringBuilder conditionBuilder = createRequestListConditionBuilder(task);
	Object[] paramList = new Object[]{};
    String order = " ORDER BY " + " `" + QuerySqlUtil.changePropertyToSql(vo.getSidx()) + "` " + vo.getSord();
	String selectSql = "SELECT RE.*,RULE.REQUEST_ID,"
			+ "                CONCAT(RULE.S0,',',RULE.S1,',',RULE.S2,',',RULE.S3,',',RULE.S4,',',RULE.S5,',',RULE.S6) STATUS "
			+ "           FROM AIEDRI_REQUEST RE "
			+ "           LEFT JOIN ( "
			+ "                SELECT REQUEST_ID "
			+ "                       ,SUM(CASE WHEN STATUS=0 THEN 1 ELSE 0 END ) S0 "
			+ "                       ,SUM(CASE WHEN STATUS=1 THEN 1 ELSE 0 END ) S1 "
			+ "                       ,SUM(CASE WHEN STATUS=2 THEN 1 ELSE 0 END ) S2 "
			+ "                       ,SUM(CASE WHEN STATUS=3 THEN 1 ELSE 0 END ) S3 "
			+ "                       ,SUM(CASE WHEN STATUS=4 THEN 1 ELSE 0 END ) S4 "
			+ "                       ,SUM(CASE WHEN STATUS=5 THEN 1 ELSE 0 END ) S5 "
			+ "                       ,SUM(CASE WHEN STATUS=6 THEN 1 ELSE 0 END ) S6 "
			+ "                  FROM AIEDRI_TASK_TUPLE "
			+ "                 GROUP BY REQUEST_ID "
			+ "                ) RULE "
			+ "             ON RE.ID=REQUEST_ID "
			+ conditionBuilder
			+ order;
	
	int allsize = queryRequestListCount(conditionBuilder, paramList);
	QuerySqlUtil.processGridVo(vo, allsize);
	@SuppressWarnings("unchecked")
	List<AiedriRequestVo> list = jdbcPagingUtil.getListByPage(jdbcTemplate, selectSql, paramList, AiedriRequestVo.class, vo.getPage(), vo.getRows());
	//changeRequestStatus(list);
	vo.setDataList(list);
}


//private void changeRequestStatus(List<AiedriRequestVo> list) {
//	//begin to process status
//
//	if(CollectionUtils.isNotEmpty(list)){
//		for(int i=0;i<list.size();i++){
//			String statusEnd="";
//			String status=list.get(i).getStatus();
//			String[] statusArray=new String[]{};
//			Map<String,Integer> map=new HashMap<String,Integer>();
//			if(StringUtils.isNotEmpty(status)){
//				statusArray=status.split(",");
//				if(null==statusArray && statusArray.length==0){
//					continue;
//				}
//				if(map.isEmpty() || !map.containsKey(statusArray[i])){
//					map.put(statusArray[i], 1);
//				}else{
//					map.put(statusArray[i], map.get(statusArray[i])+1);
//				}
//			}
//			if(!map.isEmpty()){
//				statusEnd="";
//				for(String key:map.keySet()){
//					switch(Integer.parseInt(key)){
//						case  0:
//							statusEnd+=map.get(map)+"???????;";
//							break;
//						case  1:
//							statusEnd+=map.get(map)+"?????????;";
//							break;
//						case  2:
//							statusEnd+=map.get(map)+"????????;";
//							break;
//						case  3:
//							statusEnd+=map.get(map)+"???????;";
//							break;
//						case  4:
//							statusEnd+=map.get(map)+"?????????;";
//							break;
//						case  5:
//							statusEnd+=map.get(map)+"???????;";
//							break;
//					}
//
//				}
//			}
//			list.get(i).setStatus(statusEnd);
//		}
//	}
//	//end of process status
//}


private int queryRequestListCount(StringBuilder conditionBuilder,
		Object[] paramList) {
	String selectAllCount = "SELECT COUNT(1) FROM AIEDRI_REQUEST RE "
			+ conditionBuilder.toString();
	int total = jdbcTemplate.queryForInt(selectAllCount, paramList);
	return total;
}


private StringBuilder createRequestListConditionBuilder(AiedriRequestVo task) {
	StringBuilder conditionBuilder=new StringBuilder();
	if(null==task){
		return null;
	}
	conditionBuilder.append(" WHERE 1=1 ");
	if(StringUtils.isNotEmpty(task.getStartCreateDate()) && StringUtils.isNotEmpty(task.getEndCreateDate())){
		conditionBuilder.append(" AND RE.CREATE_DATE>= "+task.getStartCreateDate())
		                .append(" AND RE.CREATE_DATE<= "+task.getEndCreateDate());
	}
	if(null!=task.getTaskType()){
		conditionBuilder.append(" AND RE.TASK_TYPE= "+task.getTaskType());
	}
	
	if(StringUtils.isNotEmpty(task.getUserId())){
		conditionBuilder.append(" AND RE.USER_ID="+task.getUserId());
	}
	if(StringUtils.isNotEmpty(task.getFromIp())){
		conditionBuilder.append(" AND RE.FROM_IP='"+task.getFromIp()+"'");
	}
	return conditionBuilder;
}


@Override
public List<AiedriRequestVo> getCurrValidTaskStatistic(TupleRuleVo task) {
	StringBuilder conditionBuilder = createTaskTupleConditionBuilder(task);
	Object[] paramList = new Object[]{};
	return queryCurrValidTaskStatistic(conditionBuilder, paramList);
}

	@Override
	public List<TupleRuleVo> getCurValidTupleList() {
		//task type is add, rule status is succed ,so rule is exist!
		String sql = "SELECT a.* FROM AIEDRI_TASK_TUPLE a , aiedri_request B WHERE a.REQUEST_ID = b.ID AND a. STATUS = ? AND b.TASK_TYPE = ?";

		return jdbcTemplate.query(sql,new Object[]{RuleConstant.RULE_ISSUED , TaskConstant.TASK_TYPE_ADD},new EJB3AnnontationRowMapper(TupleRuleVo.class));
	}

	@Override
	public Integer updateRuleStatus() {
		String sql = "update aiedri_task_tuple f , (select a.rule_id , a.REQUEST_ID from aiedri_task_tuple a , aiedri_request b , (select c.rule_id , d.CREATE_DATE del_date from aiedri_task_tuple c , aiedri_request d where c.REQUEST_ID = d.id and c.STATUS = ? and d.TASK_TYPE = ?) e where a.REQUEST_ID = b.id and a.STATUS = ? and b.TASK_TYPE = ? and a.rule_id = e.rule_id and b.CREATE_DATE < e.del_date group by a.rule_id, a.REQUEST_ID) z  set status = ? where f.rule_id = z.rule_id and f.REQUEST_ID = z.request_id";

		return jdbcTemplate.update(sql , new Object[]{RuleConstant.RULE_ISSUED , TaskConstant.TASK_TYPE_DELETE,RuleConstant.RULE_ISSUED,TaskConstant.TASK_TYPE_ADD,RuleConstant.RULE_DELETED});
	}

	@Override
	public Integer moveNotExistRules() {
		String sql = "insert into aiedri_task_tuple_history (select * from aiedri_task_tuple where status = ? or status = ?)";
		return jdbcTemplate.update(sql , new Object[]{RuleConstant.RULE_DELETED,RuleConstant.RULE_MERGED});
	}

	@Override
	public Integer deleteNotExistRules() {
		String sql = "delete from aiedri_task_tuple where status = ? or status = ?";
		return jdbcTemplate.update(sql , new Object[]{RuleConstant.RULE_DELETED,RuleConstant.RULE_MERGED});
	}


	private List<AiedriRequestVo> queryCurrValidTaskStatistic(
		StringBuilder conditionBuilder, Object[] paramList) {
	String sql = "SELECT * FROM AIEDRI_REQUEST WHERE ID IN ( "
			+ " SELECT DISTINCT(Q.ID) FROM AIEDRI_TASK_TUPLE RULE "
			+ "   LEFT JOIN AIEDRI_REQUEST Q "
			+ "     ON RULE.REQUEST_ID=Q.ID "
			+ conditionBuilder.toString()
			+ ") ";
    return jdbcTemplate.query(sql, paramList, new EJB3AnnontationRowMapper(AiedriRequestVo.class));
    }

}
