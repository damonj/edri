package com.asiainfo.aiedri.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.asiainfo.aiedri.vo.JqGridVo;
/*
 * initJGridVoByRequest()和processGridVo()两个方法组合替代了aiedriBaseAction.initJGridVo()方法
 * */
public class QuerySqlUtil {
	//把bean的属性转为数据库的字段
	public static String changePropertyToSql(String prop){
		if(StringUtils.isEmpty(prop)){
			return "";
		}
		StringBuilder sb=new StringBuilder();
		int prePos=0,pos=0;
		for(int i=0;i<prop.length();i++){
			if(Character.isUpperCase(prop.charAt(i))){
				pos=prePos;
				prePos=i;
				sb.append(prop.subSequence(pos, prePos));
				sb.append("_");
			}
		}
		sb.append(prop.subSequence(prePos, prop.length()));
		return sb.toString().toUpperCase();
	}
	/**
	 * 根据请求初始化用于分页查询的JGridVo对象
	 */
	public static JqGridVo initJGridVoByRequest(HttpServletRequest request) {
		JqGridVo vo=new JqGridVo();
		vo.setSidx(request.getParameter("sidx"));
		vo.setSord(request.getParameter("sord"));
		Integer rows=0;Integer page=0;
		if(!StringUtils.isEmpty(request.getParameter("rows"))){
			rows = Integer.parseInt(request.getParameter("rows"));
		}
		if(!StringUtils.isEmpty(request.getParameter("page"))){
			page = Integer.parseInt(request.getParameter("page"));
		}
		vo.setRows(rows);
		vo.setPage(page);		
		return vo;
	}
	//根据list的总个数，计算页码和页数
	public static void processGridVo(JqGridVo vo, Integer record) {
		int param=0;
		if(record%vo.getRows().intValue()!=0){
			param=1;
		}
		Integer total = Integer.valueOf(record / vo.getRows().intValue())+param;
        vo.setTotal(total);
		vo.setRecord(record);
	}
}
