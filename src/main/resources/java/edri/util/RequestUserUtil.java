package com.asiainfo.aiedri.util;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import ainx.common.spring.BeanFactoryUtil;

import com.asiainfo.aiedri.dao.IAiedriRuleConfigDao;
import com.asiainfo.aiedri.vo.AiedriRequestUserVo;

public class RequestUserUtil {
		private static RequestUserUtil comInstanceRequestUserUtil = new RequestUserUtil();

		private static HashMap<String, AiedriRequestUserVo> userMap;
		private List<AiedriRequestUserVo> userList;
		private static boolean initCom = false;

		private RequestUserUtil() {
		}

		public static RequestUserUtil getInstance() {
			if (!initCom) {
				RequestUserUtil.initUtil();
			}
			return comInstanceRequestUserUtil;
		}

		private static synchronized void initUtil() {
			if (initCom) {
				return;
			}
			comInstanceRequestUserUtil.init();
			RequestUserUtil.initCom = true;
		}

		private void init() {
			this.userMap = getUserMap();
			this.userList = getUserList();
		}

		public void reload() {
			HashMap<String, AiedriRequestUserVo> userMap=getUserMap();
			List<AiedriRequestUserVo> userList=getUserList();

			this.userMap = userMap;
			this.userList = userList;
		}

		
		private HashMap<String, AiedriRequestUserVo> getUserMap() {
			List<AiedriRequestUserVo> list=getDao().findAllRequestUser();
			HashMap<String, AiedriRequestUserVo> map=new HashMap<String, AiedriRequestUserVo>();
			if(CollectionUtils.isNotEmpty(list)){
				for(AiedriRequestUserVo vo:list){
					map.put(vo.getUserName(), vo);
				}
			}
			return map;
		}

		private List<AiedriRequestUserVo> getUserList() {
			return getDao().findAllRequestUser();
		}

		private IAiedriRuleConfigDao getDao() {
			return (IAiedriRuleConfigDao) BeanFactoryUtil.getBean("aiedriRuleConfigDao");
		}


		public static void main(String[] args) {

		}

	}
