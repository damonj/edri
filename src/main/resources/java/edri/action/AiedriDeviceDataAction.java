package com.asiainfo.aiedri.action;
import ainx.common.spring.BeanFactoryUtil;

import com.asiainfo.aiedri.bo.IAiedriDeviceDataBo;
import com.asiainfo.aiedri.vo.AiedriPfIfData;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;



public class AiedriDeviceDataAction extends AiedriBaseAction{
    public IAiedriDeviceDataBo getAiedriDeviceDataBo() {
    	 return (IAiedriDeviceDataBo)BeanFactoryUtil.getBean("aiedriDeviceDataBo");

    }
    private static final SimpleDateFormat sft=new SimpleDateFormat("yyyyMMddHHmm");
    //获取端口的流量信息
    public ActionForward getIfRateDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
		String ifId=request.getParameter("ifId");
		String deviceId=request.getParameter("deviceId");
		if(StringUtils.isEmpty(ifId) || StringUtils.isEmpty(deviceId)){
			return null;
		}
		
		long curr=System.currentTimeMillis();
		long yest=curr-24*3600*1000;
		String endTime=sft.format(new Date());
		String startTime=sft.format(new Date(yest));
		List<AiedriPfIfData>  list=getAiedriDeviceDataBo().getIfPfDataByIfidAndDeviceid(Integer.valueOf(ifId), Integer.valueOf(deviceId), startTime, endTime);
		JSONArray obj=JSONArray.fromObject(list);
		writeObjectJson(response,obj);
		return null;
    }
    //获取端口信息
    public ActionForward getIfInfoDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
		long curr=System.currentTimeMillis();
		long yest=curr-24*3600*1000;
		String endTime=sft.format(new Date());
		String startTime=sft.format(new Date(yest));
		List<AiedriPfIfData>  list=getAiedriDeviceDataBo().getIfAndDeviceInfo(startTime, endTime);
		JSONArray obj=JSONArray.fromObject(list);
		writeObjectJson(response,obj);
		return null;
    }
    //获取端口的最新一条数据
    public ActionForward getLatestIfData(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
		String deviceId=request.getParameter("deviceId");
		String ifId=request.getParameter("ifId");
		String coldate=request.getParameter("coldate");
		if(StringUtils.isEmpty(deviceId) || StringUtils.isEmpty(ifId) || StringUtils.isEmpty(coldate)){
			return null;
		}
    	List<AiedriPfIfData>  list=getAiedriDeviceDataBo().getLatestIfData(Integer.parseInt(deviceId), Integer.parseInt(ifId),coldate);
    	AiedriPfIfData vo=null;
    	if(CollectionUtils.isNotEmpty(list)){
    		vo=list.get(0);
    	}
    			
    	JSONObject obj=JSONObject.fromObject(vo);
		writeObjectJson(response,obj);
		return null;
    }
}
