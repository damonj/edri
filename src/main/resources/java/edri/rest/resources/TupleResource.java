package com.asiainfo.aiedri.rest.resources;

import ainx.common.spring.BeanFactoryUtil;
import com.asiainfo.aiedri.bo.IAiedriRuleConfigBo;
import com.asiainfo.aiedri.bo.IAiedriTaskBo;
import com.asiainfo.aiedri.bo.IAiedriTaskManageBo;
import com.asiainfo.aiedri.rest.bean.*;
import com.asiainfo.aiedri.rest.constant.RespStatus;
import com.asiainfo.aiedri.vo.ProtectedIpsVo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/tuple")
public class TupleResource {
	@Context   
	private HttpServletRequest servletRequest;
	IAiedriRuleConfigBo ruleConfigBo = (IAiedriRuleConfigBo) BeanFactoryUtil.getBean("aiedriRuleConfigBo");
	public IAiedriTaskBo getAiedriTaskBO() {
   	 	return (IAiedriTaskBo)BeanFactoryUtil.getBean("aiedriLoginBO");
    }

	public IAiedriTaskManageBo getAiedriTaskManageBo(){
		return (IAiedriTaskManageBo)BeanFactoryUtil.getBean("aiedriTaskManageBo");
	}
	Logger logger=Logger.getLogger(TupleResource.class);
	@POST  
    @Path("/add.json")
	@Consumes(MediaType.APPLICATION_JSON)  
	@Produces(MediaType.APPLICATION_JSON)  
    public String addJson(String str)  
    {  
		logger.debug("---into TupleResource addJson at server.---");
		logger.debug("addJson param is:"+str);
		List<StatusBean> resp=new ArrayList<StatusBean>();
		try{
			JSONObject obj = JSONObject.fromObject(str);
			JSONArray array=(JSONArray) obj.get("request");
			List<TupleRule> list=(List<TupleRule>) JSONArray.toCollection(array,TupleRule.class);
			logger.debug("addJson param list size:"+list.size());
			if(CollectionUtils.isEmpty(list)){
				setErrorStatusToList(resp,RespStatus.ruleError.statusDesc);
				logger.error("addJson rule content is empty");
			}else{
				resp=getAiedriTaskBO().addJsonAddRequestAndTuple(list, servletRequest);
			}
		}catch(Exception e){
			setErrorStatusToList(resp,RespStatus.other.statusDesc);
			logger.error("there is error in addJson.detail is :"+ e);
		}
		return capsulateListResponse(resp);
    }
 
	@POST  
    @Path("/delete.json")
	@Consumes(MediaType.APPLICATION_JSON)  
	@Produces(MediaType.APPLICATION_JSON)  
    public String deleteJson(String str)  {
		logger.debug("---into TupleResource deleteJson at server.---");
		logger.debug("deleteJson param is:"+str);
		List<StatusBean> resp=new ArrayList<StatusBean>();
		try{
			JSONObject obj = JSONObject.fromObject(str);
			JSONArray array=(JSONArray) obj.get("request");
			List<TupleRule> list=(List<TupleRule>) JSONArray.toCollection(array,TupleRule.class);
			logger.debug("deleteJson param list size:"+list.size());
			if(CollectionUtils.isEmpty(list)){
				setErrorStatusToList(resp,RespStatus.ruleError.statusDesc);
				logger.error("deleteJson rule content is empty");
			}else{
				resp=getAiedriTaskBO().addJsonDeleteRequestAndTuple(list, servletRequest);
			}
		}catch(Exception e){
			setErrorStatusToList(resp, RespStatus.other.statusDesc);
			logger.error("there is error in deleteJson detail is :"+ e);
		}
		return capsulateListResponse(resp); 
	}
	@POST  
    @Path("/search.json")
	@Consumes(MediaType.APPLICATION_JSON)  
	@Produces(MediaType.APPLICATION_JSON)  
    public String searchJson(RequestParamBean requestBean)  {
		logger.debug("---into searchJson at server.---");
		JSONArray resp=new JSONArray();
		try{
			List<TupleRule> list=requestBean.getRequest();
			if(CollectionUtils.isEmpty(list)){
				setErrorStatusToJsonArray(resp, RespStatus.ruleError.statusDesc);
			}else{
				resp=getAiedriTaskBO().addJsonSearchRequestAndTuple(list, servletRequest);
			}
		}catch(Exception e){
			setErrorStatusToJsonArray(resp, RespStatus.other.statusDesc);
			logger.error("there is error in searchJson detail is :"+ e);
		}
		return capsulateJsonArrayResponse(resp);
	}

	/**
	 * ?????งน????
	 *
	 * @return
	 */
	@POST  
    @Path("/all")
	@Consumes(MediaType.APPLICATION_JSON)  
	@Produces(MediaType.APPLICATION_JSON)  
    public String searchAllJson()  {
		logger.debug("into searchAllJson at server.");
		JSONArray resp=new JSONArray();
		List<TupleRule> list = new ArrayList<TupleRule>();
		try{
			getAiedriTaskBO().addJsonSearchRequestAndTuple(servletRequest);

			list = getAiedriTaskManageBo().getCurValidTuples();


		}catch(Exception e){
			setErrorStatusToJsonArray(resp,RespStatus.other.statusDesc);
			logger.error("there is error in searchJson detail is :"+ e);
		}
		return capsulateAllResponse(list);
	}


	@POST
	@Path("/whitelist")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String getWhiteListJson()  {
		logger.debug("into getWhiteListJson at server.");
		JSONArray resp=new JSONArray();
		List<TupleRule> list = new ArrayList<TupleRule>();
		try{
			getAiedriTaskBO().addJsonSearchRequestAndTuple(servletRequest);

			List<ProtectedIpsVo> protectedIpsVoList = ruleConfigBo.findAllProtectedIps();

			for (ProtectedIpsVo protectedIpsVo : protectedIpsVoList) {
				TupleRule rule = new TupleRule();
				rule.setRule_id(protectedIpsVo.getId() + "");
				rule.setRule_type("sip+smask");
				Quintuple tuple = new Quintuple();
				tuple.setSip(protectedIpsVo.getIp());
				tuple.setSmask(protectedIpsVo.getIpMask());
				rule.setRule(tuple);
				list.add(rule);
			}


		}catch(Exception e){
			setErrorStatusToJsonArray(resp,RespStatus.other.statusDesc);
			logger.error("there is error in getWhiteListJson detail is :"+ e);
		}
		return capsulateAllResponse(list);
	}


	@POST  
    @Path("/fuzzy_search.json")
	@Consumes(MediaType.APPLICATION_JSON)  
	@Produces(MediaType.APPLICATION_JSON)  
    public String fuzzySearchJson(RequestTupleBean requestBean)  {
		logger.debug("into fuzzySearchJson at server.");
		return null;
	}
	private String capsulateListResponse(List<StatusBean> resp) {
		JSONObject obj=new JSONObject();
		obj.put("response", resp);
		logger.debug("capsulateListResponse:"+obj.toString());
		return obj.toString();
	}
	private String capsulateAllResponse(List<TupleRule> resp) {
		JSONObject obj=new JSONObject();
		JSONArray array = new JSONArray();


		JsonConfig jsonConfig = new JsonConfig();
		PropertyFilter filter = new PropertyFilter() {
			public boolean apply(Object object, String fieldName, Object fieldValue) {

				return null == fieldValue;
			}
		};


		jsonConfig.setJsonPropertyFilter(filter);

		obj.put("response", array.fromObject(resp, jsonConfig));
		String returnStr = obj.toString();
		logger.debug("capsulateListResponse:"+returnStr);
		return returnStr;
	}
	private String capsulateJsonArrayResponse(JSONArray resp) {
		JSONObject obj=new JSONObject();
		obj.put("response", resp);
		logger.debug("capsulateJsonArrayResponse:"+obj.toString());
		return obj.toString();
	}
	private void setErrorStatusToList(List<StatusBean> resp,String statusDesc) {
		StatusBean bean=new StatusBean();
		bean.setStatus(statusDesc);
		resp.add(bean);
	}
	private void setErrorStatusToJsonArray(JSONArray resp,String statusDesc) {
		StatusBean bean=new StatusBean();
		bean.setStatus(statusDesc);
		resp.add(bean);
	}

}
