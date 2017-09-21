package com.asiainfo.aiedri.action;

import ainx.common.spring.BeanFactoryUtil;

import com.asiainfo.aiedri.bo.IAiedriRuleConfigBo;
import com.asiainfo.aiedri.vo.AiedriVendorType;
import com.asiainfo.ainx.auth.manage.bo.IComUserBO;
import com.asiainfo.ainx.auth.manage.pojo.ComUser;
import com.asiainfo.ainx.auth.session.AuthSession;
import com.asiainfo.ainx.common.paras.util.ParaUtil;
import com.asiainfo.ainx.common.paras.vo.ParaAnsVO;

import net.sf.json.JSONObject;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class AiedriCommonAction extends AiedriBaseAction{
	public IAiedriRuleConfigBo getAiedriRuleConfigBo() {
	   	 return (IAiedriRuleConfigBo)BeanFactoryUtil.getBean("aiedriRuleConfigBo");

	   }
    private IComUserBO getComUserBO() {
        return ((IComUserBO)BeanFactoryUtil.getBean("comUserBO"));
      }
    public ActionForward editCurrUserPass(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) {
        String loginpwd=request.getParameter("loginpwd");
        AuthSession authSession = (AuthSession) request.getSession().getAttribute("authSession");
        ComUser user = authSession.getUser();
        if(null==user){
            return null;
        }
        Integer userId=user.getUserId();
        IComUserBO comUserBO = getComUserBO();
        int num=getComUserBO().editLoginPwd(userId, loginpwd);
        JSONObject backJson=new JSONObject();
        String msg="",type="";
        if(num>0) {
            msg = "�޸�����ɹ�!";
            type="success";
        } else {
            msg = "�޸�����ʧ��!";
            type="error";
        }
        backJson.put("type", type);
        backJson.put("msg", msg);
        
        writeObjectJson(response, backJson);
        return null;
    }
	public ActionForward doShowParaList(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		String paraName = request.getParameter("paraName");
		JSONObject backJson = new JSONObject();
	    ArrayList<ParaAnsVO> resList = null;
	    try {
		    if ((paraName != null) && (!(paraName.equals("")))) {
		        ParaUtil.getInstance(); 
		        resList=ParaUtil.getResAssInfmForList(paraName);
		    }
		    //��ȡ���е��豸�ͺ�
		    Map<String, List<AiedriVendorType>> map=getAiedriRuleConfigBo().getVendorTypeMap();
	        backJson.put("flag", true);
			backJson.put("data", resList); 
			backJson.put("map", map); 
	    }catch (Exception e) {
	    	backJson.put("flag", false);
			backJson.put("msg", e.getMessage()); 
	    }
	    writeObjectJson(response, backJson);
		return null;
	}
}
