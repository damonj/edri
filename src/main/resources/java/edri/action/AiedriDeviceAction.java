package com.asiainfo.aiedri.action;
import ainx.common.spring.BeanFactoryUtil;
import com.asiainfo.aiedri.bo.IAiedriDeviceBo;
import com.asiainfo.aiedri.util.NumberValueProcessor;
import com.asiainfo.aiedri.util.QuerySqlUtil;
import com.asiainfo.aiedri.vo.AiedriDevicePort;
import com.asiainfo.aiedri.vo.AiedriDevices;
import com.asiainfo.aiedri.vo.JqGridVo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;



public class AiedriDeviceAction extends AiedriBaseAction{
    public IAiedriDeviceBo getAiedriDeviceBo() {
    	 return (IAiedriDeviceBo)BeanFactoryUtil.getBean("aiedriDeviceBo");

    }
    public ActionForward getDeviceList(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
        JqGridVo vo=QuerySqlUtil.initJGridVoByRequest(request);
        String key=request.getParameter("key");
        if(!key.equals("all")){
        	String searchSql = createSearchSql(request, AiedriDevices.class);
            if(StringUtils.isNotEmpty(searchSql)){
            	vo.setSearch(searchSql);
            }
        }
        getAiedriDeviceBo().getDeviceList(vo);
        List<AiedriDevices> list=vo.getDataList();
        Map<Integer, List<AiedriDevicePort>>  map=getAiedriDeviceBo().findMapOfAllAport();
        
        if(CollectionUtils.isEmpty(list) || map.isEmpty()){
        	JSONObject json = JSONObject.fromObject(vo);
            writeObjectJson(response,  json);
            return null;
        }
        for(AiedriDevices d:list){
        	if(map.containsKey(d.getId())){
        		d.setPorts(map.get(d.getId()));
        	}
        }
        JSONObject json = JSONObject.fromObject(vo);
        writeObjectJson(response,  json);
        return null;
    }
    
	public ActionForward createDevice(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
    	AiedriDevices device = trans2Bean(request, AiedriDevices.class);
    	String portsNames=request.getParameter("portsNames");
    	if(StringUtils.isNotEmpty(portsNames)){
    		List<AiedriDevicePort> list=(List<AiedriDevicePort>)JSONArray.toCollection(JSONArray.fromObject(portsNames), AiedriDevicePort.class);
    		device.setPorts(list);
    	}
		JSONObject backJson = new JSONObject();
		try {
			int flag=getAiedriDeviceBo().addAiedriDevice(device);
			boolean backFlag = false;
			String msg = "保存失败!";
			if(flag > 0) {
				backFlag = true;
				msg = "保存成功!";
			}
			backJson.put("flag", backFlag);
			backJson.put("msg", msg);
		} catch (Exception nfae) {
			backJson.put("flag", false);
			backJson.put("msg", nfae.getMessage());
		}

		writeObjectJson(response,backJson);
		return null;
    }

    public ActionForward editDevice(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
    	AiedriDevices device = trans2Bean(request, AiedriDevices.class);
    	String portsNames=request.getParameter("portsNames");
    	if(StringUtils.isNotEmpty(portsNames)){
    		List<AiedriDevicePort> list=(List<AiedriDevicePort>)JSONArray.toCollection(JSONArray.fromObject(portsNames), AiedriDevicePort.class);
    		device.setPorts(list);
    	}
    	String deleteIds=request.getParameter("deleteIds");
    	List<String> dplist=(List<String>)JSONArray.toCollection(JSONArray.fromObject(deleteIds), String.class);
    	JSONObject backJson = new JSONObject();
		try {
			int flag=getAiedriDeviceBo().updateAiedriDevice(device);
			if(CollectionUtils.isNotEmpty(dplist)){
				for(int i=0;i<dplist.size();i++){
					getAiedriDeviceBo().deleteAportByPortIdAndDeviceId(Integer.parseInt(dplist.get(i)), device.getId());
				}
			}
			boolean backFlag = false;
			String msg = "更新失败!";
			if(flag > 0) {
				backFlag = true;
				msg = "更新成功!";
			}
			backJson.put("flag", backFlag);
			backJson.put("msg", msg);
		} catch (Exception nfae) {
			backJson.put("flag", false);
			backJson.put("msg", nfae.getMessage());
		}
		
		writeObjectJson(response,backJson);
		return null;
    }
    public ActionForward queryDeviceById(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
    	String id=request.getParameter("id");
    	if(StringUtils.isEmpty(id)){
    		return null;
    	}
    	JSONObject backJson = new JSONObject();
		try {
			JsonConfig jsonConfig = new JsonConfig();
			jsonConfig.registerDefaultValueProcessor(Integer.class, new NumberValueProcessor());
			AiedriDevices device=getAiedriDeviceBo().findAiedriDeviceById(Integer.parseInt(id));
			backJson.put("flag", true);
			backJson.put("dataObj", JSONObject.fromObject(device, jsonConfig));
		} catch(Exception nfae) {
			backJson.put("flag", false);
			backJson.put("msg", nfae.getMessage());
		}
				
		writeObjectJson(response,backJson);
		return null;
    }
    public ActionForward deleteDevice(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response){
    	String id=request.getParameter("id");
    	if(StringUtils.isEmpty(id)){
    		return null;
    	}
		JSONObject backJson = new JSONObject();
		try {
			int flag=getAiedriDeviceBo().deleteAiedriDeviceById(Integer.parseInt(id));
			boolean backFlag = false;
			String msg = "删除失败!";
			if(flag > 0) {
				backFlag = true;
				msg = "删除成功!";
			}
			backJson.put("flag", backFlag);
			backJson.put("msg", msg);
		} catch (Exception nfae) {
			backJson.put("flag", false);
			backJson.put("msg", nfae.getMessage());
		}
		
		writeObjectJson(response,backJson);
		return null;
    }
}
