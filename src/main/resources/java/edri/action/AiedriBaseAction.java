package com.asiainfo.aiedri.action;

import ainx.common.struts.AinxAction;
import com.asiainfo.aiedri.vo.JqGridVo;
import flex.messaging.util.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class AiedriBaseAction extends AinxAction{
	private Logger logger=Logger.getLogger(this.getClass());
	/**
	 * 每页多少条数据
	 */
	protected Integer rows = Integer.valueOf(30);
	/**
	 * 当前是第几页
	 */
	protected Integer page = Integer.valueOf(0);
	/**
	 * 总共多少条数据
	 */
	protected Integer record = Integer.valueOf(0);
	/**
	 * 总共页数
	 */
	protected Integer total = Integer.valueOf(0);
	/**
	 * 排序类型
	 */
	protected String sord;
	/**
	 * 排序对象
	 */
	protected String sidx;
	
	/**
	 * JSON输出对象
	 */
	protected void writeObjectJson(HttpServletResponse response, JSONObject json) {
		try {
			response.setHeader("Charset", "UTF-8");
			response.setContentType("text;charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(json);
			response.getWriter().flush();
		} catch (IOException e) {
			logger.error("BaseAction_writeObjectJson is error!");
		}
	}
	/**
	 * JSON输出Array对象
	 */
	protected void writeObjectJson(HttpServletResponse response, JSONArray json) {
		try {
			response.setHeader("Charset", "UTF-8");
			response.setContentType("text;charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().print(json);
			response.getWriter().flush();
		} catch (IOException e) {
			logger.error("BaseAction_writeObjectJson is error!");
		}
	}

	/**
	 * 初始化用于分页查询的JGridVo对象
	 */
	protected JqGridVo initJGridVo(HttpServletRequest request, long totalCustomerCount) {
		JqGridVo vo=new JqGridVo();
		vo.setSidx(request.getParameter("sidx"));
		vo.setSord(request.getParameter("sord"));
		if(!StringUtils.isEmpty(request.getParameter("rows"))){
			rows = Integer.parseInt(request.getParameter("rows"));
		}
		if(!StringUtils.isEmpty(request.getParameter("page"))){
			page = Integer.parseInt(request.getParameter("page"));
		}
		vo.setRows(rows);
		vo.setPage(page);		
		record = (int)totalCustomerCount;
		int param=0;
		if(this.record.intValue()%this.rows.intValue()!=0){
			param=1;
		}
        total = Integer.valueOf(this.record.intValue() / this.rows.intValue())+param;
        vo.setTotal(total);
		vo.setRecord(record);
		return vo;
	}
	
	/**
	 * 字符串数组转换为integer类型的数组
	 */
	protected Integer[] changeStrArrToIntArr(String[] ids) {
   		if(ArrayUtils.isEmpty(ids)){
   			return null;
   		}
   		Integer[] id=new Integer[ids.length];
   		for(int i=0;i<id.length;i++){
   			id[i]=Integer.parseInt(ids[i]);
   		}
   		return id;
   	}
	/**
     * 操作结果	装入json
     * @param doType
     * @param isSuccess
     * @param response
     */
    protected void returnResult(String doType, int isSuccess, HttpServletResponse response) {
        JSONObject jsonObject = new JSONObject();
        if (isSuccess == 1) {
            jsonObject.put("flag", "success");
            jsonObject.put("message", doType + "成功!");
        } else {
            jsonObject.put("flag", "error");
            jsonObject.put("message", doType + "失败!");
        }
        PrintWriter out;
        try {
            response.setCharacterEncoding("UTF-8");
            out = response.getWriter();
            out.print(jsonObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	protected <T> T trans2Bean(HttpServletRequest request, Class<T> clazz) {
		T t = null;
		try {
			t = clazz.newInstance();
			Field[] fs = clazz.getDeclaredFields();
			for(Field f:fs) {
				String tempValue = request.getParameter(f.getName());
				try {
					if(null != tempValue) {
						Method method = clazz.getDeclaredMethod("set" + f.getName().substring(0, 1).toUpperCase() + f.getName().substring(1), f.getType());
						try {
							Object param = null;
							if(f.getType() == Integer.class) {
								param = "".equals(tempValue)? null: Integer.valueOf(tempValue);
							} else if(f.getType() == Long.class) {
								param = "".equals(tempValue)? null: Long.valueOf(tempValue);
							} else {
								param = tempValue;
							}
							if(null != param) {
								method.invoke(t, param);															
							}
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						}						
					}
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return t;
	}
	protected <T> String createSearchSql(HttpServletRequest request, Class<T> clazz) {
		String sql="";
		try {
			Field[] fs = clazz.getDeclaredFields();
			for(Field f:fs) {
				String tempValue = request.getParameter(f.getName());
				if(null != tempValue) {
					if(f.getName().toLowerCase().contains("name")){
						sql+=" AND "+changePropertyToSql(f.getName())+" LIKE '%"+tempValue+"%'";
					}else if(f.getType() == String.class) {
						sql+=" AND "+changePropertyToSql(f.getName())+"='"+tempValue+"'";
					}else{
						sql+=" AND "+changePropertyToSql(f.getName())+"="+tempValue;
					}
					
				} 
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return sql;
	}
	//把bean的属性转为数据库的字段
	protected String changePropertyToSql(String prop){
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
	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getRecord() {
		return record;
	}

	public void setRecord(Integer record) {
		this.record = record;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public String getSord() {
		return sord;
	}

	public void setSord(String sord) {
		this.sord = sord;
	}

	public String getSidx() {
		return sidx;
	}

	public void setSidx(String sidx) {
		this.sidx = sidx;
	}
}
