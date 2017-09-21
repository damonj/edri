package com.asiainfo.aiedri.dao.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import ainx.common.spring.jdbc.EJB3AnnontationRowMapper;
import ainx.common.spring.jdbc.impl.JdbcPagingUtil;
import ainx.common.util.SequenceUtil;

import com.asiainfo.aiedri.dao.IAiedriRuleConfigDao;
import com.asiainfo.aiedri.vo.AiedriRequestUserVo;
import com.asiainfo.aiedri.vo.AiedriRuleConfigVo;
import com.asiainfo.aiedri.vo.AiedriVendorType;
import com.asiainfo.aiedri.vo.ProtectedIpsVo;


public class AiedriRuleConfigDao implements IAiedriRuleConfigDao {
	protected JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	private JdbcPagingUtil jdbcPagingUtil;
	public void setJdbcPagingUtil(JdbcPagingUtil jdbcPagingUtil) {
		this.jdbcPagingUtil = jdbcPagingUtil;
	}
	@Override
	public List<ProtectedIpsVo> findAllProtectedIps() {
		String sql="SELECT * FROM AIEDRI_PROTECTED_IPS";
		return jdbcTemplate.query(sql, new Object[]{}, new EJB3AnnontationRowMapper(ProtectedIpsVo.class));
	}
	@Override
	public ProtectedIpsVo findProtectedIpsById(Integer id) {
		String sql="SELECT * FROM AIEDRI_PROTECTED_IPS WHERE ID=? ";
		List<ProtectedIpsVo> list= jdbcTemplate.query(sql, new Object[]{id}, new EJB3AnnontationRowMapper(ProtectedIpsVo.class));
	    return CollectionUtils.isEmpty(list)?null:list.get(0);
	}
	@Override
	public int addProtectedIpsVo(ProtectedIpsVo vo) {
		Integer id=SequenceUtil.getSequenceId("aiedriConfigId");
		String sql="INSERT INTO AIEDRI_PROTECTED_IPS(ID,IP,IP_MASK,CREAT_TIME) VALUES(?,?,?,?)";
		Object[] obj=new Object[]{id,vo.getIp(),vo.getIpMask(),vo.getCreatTime()};
		return jdbcTemplate.update(sql, obj);
	}

	@Override
	public int updateProtectedIpsVo(ProtectedIpsVo vo) {
		String sql="UPDATE AIEDRI_PROTECTED_IPS SET IP=?,IP_MASK=?,CREAT_TIME=? WHERE ID=?";
		Object[] obj=new Object[]{vo.getIp(),vo.getIpMask(),vo.getCreatTime(),vo.getId()};
		return jdbcTemplate.update(sql, obj);
	}

	@Override
	public int deleteProtectedIpsVo(Integer id) {
		String sql="DELETE FROM AIEDRI_PROTECTED_IPS WHERE ID=?";
		Object[] obj=new Object[]{id};
		return jdbcTemplate.update(sql, obj);
	}

	@Override
	public AiedriRuleConfigVo findRuleConfigVoParam() {
		String sql="SELECT SUM(CASE CODE WHEN 'maxNum' THEN VALUE ELSE 0 END) MAX_NUM , "
				+ "        SUM(CASE CODE WHEN 'alertThresh' THEN VALUE ELSE 0 END) ALERT_THRESH , "
				+ "        SUM(CASE CODE WHEN 'terminatThresh' THEN VALUE ELSE 0 END) TERMINAT_THRESH , "
				+ "        SUM(CASE CODE WHEN 'minMask' THEN VALUE ELSE 0 END) MIN_MASK , "
				+ "        SUM(CASE CODE WHEN 'maxDurationTime' THEN VALUE ELSE 0 END) MAX_DURATION_TIME "
				+ "  FROM  AIEDRI_RULE_CONFIG WHERE TYPE_NAME='rule-config'";
		List<AiedriRuleConfigVo> list= jdbcTemplate.query(sql, new Object[]{}, new EJB3AnnontationRowMapper(AiedriRuleConfigVo.class));
		return CollectionUtils.isEmpty(list)?null:list.get(0);
	}

	@Override
	public int updateRuleConfigParam(AiedriRuleConfigVo vo) {
		String sql="UPDATE AIEDRI_RULE_CONFIG "
				+ "    SET VALUE = CASE CODE "
				+ "   WHEN 'maxNum' THEN ? "
				+ "   WHEN 'alertThresh' THEN ? "
				+ "   WHEN 'terminatThresh' THEN ? "
				+ "   WHEN 'minMask' THEN ? "
				+ "   WHEN 'maxDurationTime' THEN ? "
				+ "    END "
				+ "  WHERE TYPE_NAME='rule-config'";
		return jdbcTemplate.update(sql, new Object[]{vo.getMaxNum(),vo.getAlertThresh(),vo.getTerminatThresh(),vo.getMinMask(),vo.getMaxDurationTime()});
	}
	@Override
	public String findConfigParamByPName(String name) {
		String sql="SELECT VALUE FROM AIEDRI_RULE_CONFIG "
				+ "  WHERE TYPE_NAME='rule-config' "
				+ "    AND CODE=?";
		Object[] obj=new Object[]{name};
		List<String> list=jdbcTemplate.queryForList(sql, obj,String.class);
		return CollectionUtils.isEmpty(list)?null:list.get(0);
	}
	@Override
	public List<AiedriRequestUserVo> findAllRequestUser() {
		String sql="SELECT * FROM AIEDRI_REQUEST_USER";
		return jdbcTemplate.query(sql, new Object[]{}, new EJB3AnnontationRowMapper(AiedriRequestUserVo.class));
	}
	@Override
	public int updateRequestUser(AiedriRequestUserVo vo) {
		String sql="UPDATE AIEDRI_REQUEST_USER SET USER_NAME=?, USER_PASSWORD=?, IP=? WHERE ID=? ";
		Object[] obj=new Object[]{vo.getUserName(),vo.getUserPassword(),vo.getIp(),vo.getId()};
		return jdbcTemplate.update(sql, obj);
	}
	@Override
	public List<AiedriVendorType> getVendorTypeList() {
		String sql="SELECT * FROM AIEDRI_VENDOR_TYPE";
		return jdbcTemplate.query(sql, new Object[]{}, new EJB3AnnontationRowMapper(AiedriVendorType.class));
	}
	

}
