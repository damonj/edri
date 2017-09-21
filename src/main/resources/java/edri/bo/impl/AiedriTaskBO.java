package com.asiainfo.aiedri.bo.impl;

import ainx.common.util.SequenceUtil;
import com.asiainfo.aiedri.bo.IAiedriTaskBo;
import com.asiainfo.aiedri.dao.IAiedriTaskDAO;
import com.asiainfo.aiedri.rest.bean.StatusBean;
import com.asiainfo.aiedri.rest.bean.TupleRule;
import com.asiainfo.aiedri.util.RuleConstant;
import com.asiainfo.aiedri.util.TaskConstant;
import com.asiainfo.aiedri.vo.AiedriRequestVo;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class AiedriTaskBO implements IAiedriTaskBo{
	protected Logger logger=Logger.getLogger(this.getClass());
	private IAiedriTaskDAO aiedriTaskDAO;
	public void setAiedriTaskDAO(IAiedriTaskDAO aiedriTaskDAO) {
		this.aiedriTaskDAO = aiedriTaskDAO;
	}
	@Override
	public List<StatusBean> addJsonAddRequestAndTuple(List<TupleRule> list,HttpServletRequest servletRequest) {
		logger.debug("--- into bo addJsonAddRequestAndTuple ---");
		List<StatusBean> resp=new ArrayList<StatusBean>();
		//记录接口的请求日志信息,返回requestId
		Integer id=SequenceUtil.getSequenceId("aiedriRequestId");
		createRequestVo(id,servletRequest,list.size(),TaskConstant.TASK_TYPE_ADD);
		logger.debug("has addAiedriRequest to database.");
		StatusBean bean=null;
		for(TupleRule rule:list){
			bean=new StatusBean();
			bean=addTupleRule(rule,id,RuleConstant.RULE_WAIT_CHECK);
			resp.add(bean);
		}
		logger.debug("has addTupleRule to database.");
		return resp;
	}

	@Override
	public List<StatusBean> addJsonDeleteRequestAndTuple(List<TupleRule> list,HttpServletRequest servletRequest) {
		logger.debug("--- into addJsonDeleteRequestAndTuple bo --");
		List<StatusBean> resp=new ArrayList<StatusBean>();
		//记录接口的请求日志信息,返回requestId
		Integer id=SequenceUtil.getSequenceId("aiedriRequestId");
		createRequestVo(id,servletRequest,list.size(),TaskConstant.TASK_TYPE_DELETE);
		logger.debug("has addAiedriRequest to database.");
		StatusBean bean=null;
		for(TupleRule rule:list){
			bean=new StatusBean();
			bean=addDeleteTupleRule(rule, id, RuleConstant.RULE_WAIT_CHECK);
			resp.add(bean);
		}
		logger.debug("has addDeleteTupleRule to database.");
		return resp;
	}
	
	@Override
	public JSONArray addJsonSearchRequestAndTuple(List<TupleRule> list,
			HttpServletRequest servletRequest) {
		//记录接口的请求日志信息,返回requestId
		Integer id=SequenceUtil.getSequenceId("aiedriRequestId");
		createRequestVo(id, servletRequest, list.size(), TaskConstant.TASK_TYPE_SEARCH);
		return null;
	}

	public JSONArray addJsonSearchRequestAndTuple(
												  HttpServletRequest servletRequest) {
		//记录接口的请求日志信息,返回requestId
		Integer id=SequenceUtil.getSequenceId("aiedriRequestId");
		createRequestVo(id,servletRequest,0,TaskConstant.TASK_TYPE_SEARCH);
		return null;
	}
	public void createRequestVo(Integer id, HttpServletRequest servletRequest,int size, Integer taskType) {
		String fromIp=getClientIpAddr(servletRequest);
		String userName=getClientUserName(servletRequest);
		AiedriRequestVo vo=new AiedriRequestVo();
		vo.setId(id);
		Long currTime=System.currentTimeMillis()/1000L;
		vo.setCreateDate(String.valueOf(currTime));
		vo.setFromIp(fromIp);
		vo.setUserId(userName);
		vo.setTaskNum(size);
		vo.setTaskType(taskType);
		aiedriTaskDAO.addAiedriRequest(vo);
	}
	@Override
	public StatusBean addDeleteTupleRule(TupleRule rule,Integer requestId,Integer ruleStatus) {
		return aiedriTaskDAO.addDeleteTupleRule(rule,requestId,ruleStatus);
	}
	@Override
	public StatusBean addTupleRule(TupleRule rule,Integer requestId,Integer ruleStatus) {
		return aiedriTaskDAO.addTupleRule(rule,requestId,ruleStatus);
	}
	//头文件的Cookie存储格式为：userName-liulx:$Version=1;userName-liulx=123456667
	private  String getClientUserName(HttpServletRequest request)  {
		Cookie[] cookies=request.getCookies();
		if(null==cookies ||  cookies.length==0){
			return "";
		}
		for(Cookie cook:cookies){
			if(cook.getName().startsWith("userName-")){
				return cook.getName().substring(9);
			}
		}
        return  "";
   }
	//获取客户端的请求IP地址
	private String getClientIpAddr(HttpServletRequest request) {
		String ip  =  request.getHeader( " x-forwarded-for " );
        if (ip  ==   null   ||  ip.length()  ==   0   ||   " unknown " .equalsIgnoreCase(ip))  {
            ip  =  request.getHeader( " Proxy-Client-IP " );
        }
        if (ip  ==   null   ||  ip.length()  ==   0   ||   " unknown " .equalsIgnoreCase(ip))  {
            ip  =  request.getHeader( " WL-Proxy-Client-IP " );
        }
         if (ip  ==   null   ||  ip.length()  ==   0   ||   " unknown " .equalsIgnoreCase(ip))  {
           ip  =  request.getRemoteAddr();
       }
        return  ip;
	}	
}
