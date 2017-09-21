package com.asiainfo.aiedri.action;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ainx.common.spring.BeanFactoryUtil;

import com.asiainfo.aiedri.bo.IAiedriTaskManageBo;
import com.asiainfo.aiedri.util.NumberValueProcessor;
import com.asiainfo.aiedri.util.QuerySqlUtil;
import com.asiainfo.aiedri.util.RuleConstant;
import com.asiainfo.aiedri.util.TaskConstant;
import com.asiainfo.aiedri.vo.AiedriRequestVo;
import com.asiainfo.aiedri.vo.JqGridVo;
import com.asiainfo.aiedri.vo.TupleRuleVo;



public class AiedriTaskAction extends AiedriBaseAction{
    public IAiedriTaskManageBo getAiedriTaskManageBo() {
    	 return (IAiedriTaskManageBo)BeanFactoryUtil.getBean("aiedriTaskManageBo");

    }
    
    public ActionForward getTupleRulesByTaskId(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
        String ruleId=request.getParameter("ruleId");
        if(StringUtils.isEmpty(ruleId)){
        	return null;
        }
        JqGridVo vo=QuerySqlUtil.initJGridVoByRequest(request);
    	getAiedriTaskManageBo().queryTupleRulesByTaskIdPageAndCon(Integer.parseInt(ruleId),vo);
        JSONObject json = JSONObject.fromObject(vo);
        writeObjectJson(response,  json);
        return null;
    }
    public ActionForward getTupleRulesDetailByTaskId(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
        String ruleId=request.getParameter("ruleId");
        if(StringUtils.isEmpty(ruleId)){
        	return null;
        }
        JSONObject backJson = new JSONObject();
		try {
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerDefaultValueProcessor(Integer.class, new NumberValueProcessor());
			TupleRuleVo rule=getAiedriTaskManageBo().getTupleRulesDetailByTaskId(Integer.parseInt(ruleId));
			backJson.put("flag", true);
			backJson.put("dataObj", JSONObject.fromObject(rule, jsonConfig));
		} catch(Exception nfae) {
			backJson.put("flag", false);
			backJson.put("msg", nfae.getMessage());
		}
				
		writeObjectJson(response,backJson);
		return null;
    }
    
    public ActionForward getTaskTupleList(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
    	JqGridVo vo=QuerySqlUtil.initJGridVoByRequest(request);
    	TupleRuleVo task = trans2Bean(request, TupleRuleVo.class);
        getAiedriTaskManageBo().queryTaskTupleListBySearchVo(vo,task);
        JSONObject json = JSONObject.fromObject(vo);
        writeObjectJson(response,  json);
        return null;
    }
    //根据id值查找任务详情信息
    public ActionForward getTaskTupleById(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
    	JSONObject backJson = new JSONObject();
        String id = request.getParameter("id");
        if(StringUtils.isEmpty(id)){
        	return null;
        }
		try {
	    	TupleRuleVo task=getAiedriTaskManageBo().getTupleRulesDetailByTaskId(Integer.parseInt(id));
		    backJson.put("flag", true);
			backJson.put("data", task); 
	    }catch (Exception e) {
	    	backJson.put("flag", false);
			backJson.put("msg", e.getMessage()); 
	    }
	    writeObjectJson(response, backJson);
		return null;
    }
    //查看当前生效的规则个数
    public ActionForward loadCurrRuleNum(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
    	TupleRuleVo task = new TupleRuleVo();
    	task.setStatus(RuleConstant.RULE_ISSUED);//已经下发
    	task.setTaskType(TaskConstant.TASK_TYPE_ADD);//规则类型为添加规则
    	int num=getAiedriTaskManageBo().loadCurrRuleNum(task);
        JSONObject json = new JSONObject();
        json.put("num", num);
        writeObjectJson(response,  json);
        return null;
    }
    //查看当前生效的规则明细
    public ActionForward loadCurrRuleDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
    	JqGridVo vo=QuerySqlUtil.initJGridVoByRequest(request);
    	TupleRuleVo task = new TupleRuleVo();
    	task.setStatus(RuleConstant.RULE_ISSUED);//已经下发
    	task.setTaskType(TaskConstant.TASK_TYPE_ADD);//规则类型为添加规则
    	getAiedriTaskManageBo().queryTaskTupleListBySearchVo(vo,task);
        JSONObject json = JSONObject.fromObject(vo);
        writeObjectJson(response,  json);
        return null;
    }
    //查看请求记录
    public ActionForward getRequestList(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
    	JqGridVo vo=QuerySqlUtil.initJGridVoByRequest(request);
    	AiedriRequestVo task = trans2Bean(request, AiedriRequestVo.class);
    	getAiedriTaskManageBo().queryRequestListBySearchVo(vo,task);
        JSONObject json = JSONObject.fromObject(vo);
        writeObjectJson(response,  json);
        return null;
    }
    //查看当前生效规则的统计情况
    public ActionForward getCurrentValidTaskStatistics(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
    	TupleRuleVo task = new TupleRuleVo();
    	task.setStatus(RuleConstant.RULE_ISSUED);//已经下发
    	task.setTaskType(TaskConstant.TASK_TYPE_ADD);//规则类型为添加规则
    	List<AiedriRequestVo> tasks = getAiedriTaskManageBo().getCurrValidTaskStatistic(task);
    	int num=getAiedriTaskManageBo().loadCurrRuleNum(task);
    	JSONArray json = JSONArray.fromObject(tasks);
        writeObjectJson(response,  json);
        return null;
    }
}
