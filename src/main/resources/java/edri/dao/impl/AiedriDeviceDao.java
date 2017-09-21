package com.asiainfo.aiedri.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import ainx.common.spring.jdbc.EJB3AnnontationRowMapper;
import ainx.common.spring.jdbc.impl.JdbcPagingUtil;
import ainx.common.util.SequenceUtil;

import com.asiainfo.aiedri.dao.IAiedriDeviceDao;
import com.asiainfo.aiedri.util.QuerySqlUtil;
import com.asiainfo.aiedri.util.TaskConstant;
import com.asiainfo.aiedri.vo.AiedriDevicePort;
import com.asiainfo.aiedri.vo.AiedriDevices;
import com.asiainfo.aiedri.vo.AiedriTaskVo;
import com.asiainfo.aiedri.vo.JqGridVo;

public class AiedriDeviceDao implements IAiedriDeviceDao {
	protected JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	private JdbcPagingUtil jdbcPagingUtil;
	public void setJdbcPagingUtil(JdbcPagingUtil jdbcPagingUtil) {
		this.jdbcPagingUtil = jdbcPagingUtil;
	}
	@Override
	public int addAiedriDevice(AiedriDevices devices) {
		String sql="INSERT INTO AIEDRI_DEVICES("
				+ "        ID, NAME,IP,SNMP_PORT,SNMP_VERSION,SNMP_STRING,"
				+ "        DEVICE_MODEL,DEVICE_MANUFACT,USER_NAME,PASSWORD) "
				+ " VALUES (?,?,?,?,?,?,?,?,?,?)";
		Object[] obj=new Object[]{devices.getId(),devices.getName(),devices.getIp(),devices.getSnmpPort(),
				devices.getSnmpVersion(),devices.getSnmpString(),
				devices.getDeviceModel(),devices.getDeviceManufact(),
				devices.getUserName(),devices.getPassword()};
		return jdbcTemplate.update(sql, obj);
	}

	@Override
	public int updateAiedriDevice(AiedriDevices devices) {
		String sql="UPDATE AIEDRI_DEVICES SET NAME=?,IP=?,SNMP_PORT=?,SNMP_VERSION=?, "
				+ "SNMP_STRING=?, DEVICE_MODEL=?,DEVICE_MANUFACT=?,USER_NAME=?,"
				+ "PASSWORD=? WHERE ID=?";
		Object[] obj=new Object[]{devices.getName(),devices.getIp(),devices.getSnmpPort(),
				devices.getSnmpVersion(),devices.getSnmpString(),
				devices.getDeviceModel(),devices.getDeviceManufact(),
				devices.getUserName(),devices.getPassword(),
				devices.getId()};
		return jdbcTemplate.update(sql, obj);
	}

	@Override
	public int deleteAiedriDeviceById(Integer deviceId) {
		String sql="DELETE FROM AIEDRI_DEVICES WHERE ID=?";
		return jdbcTemplate.update(sql, new Object[]{deviceId});
	}

	@Override
	public AiedriDevices findAiedriDeviceById(Integer id) {
		String sql="SELECT * FROM AIEDRI_DEVICES WHERE ID=?";
		List<AiedriDevices> list=jdbcTemplate.query(sql, new Object[]{id}, new EJB3AnnontationRowMapper(AiedriDevices.class));
		return CollectionUtils.isEmpty(list)?null:list.get(0);
	}

	@Override
	public List<AiedriDevices> findAllAiedriDevice() {
		String sql="SELECT * FROM AIEDRI_DEVICES";
		return jdbcTemplate.query(sql, new Object[]{}, new EJB3AnnontationRowMapper(AiedriDevices.class));
	}

	@Override
	public int addAport(AiedriDevicePort port) {
		Integer id=SequenceUtil.getSequenceId("aiedriDeviceId");
		String sql="INSERT INTO AIEDRI_DEVICE_PORT(ID, NAME,DEVICE_ID,BANDWIDTH) "
				+ " VALUES (?,?,?,?)";
		Object[] obj=new Object[]{id,port.getName(),port.getDeviceId(),port.getBandwidth()};
		return jdbcTemplate.update(sql, obj);
	}

	@Override
	public int updateAport(AiedriDevicePort port) {
		String sql="UPDATE AIEDRI_DEVICE_PORT SET NAME=?,DEVICE_ID=?,BANDWIDTH=?"
				+ "  WHERE ID=?";
		Object[] obj=new Object[]{port.getName(),port.getDeviceId(),port.getBandwidth(),port.getId()};
		return jdbcTemplate.update(sql, obj);
	}

	@Override
	public int deleteAportByPortIdAndDeviceId(Integer portId, Integer deviceId) {
		String sql="DELETE FROM AIEDRI_DEVICE_PORT WHERE ID=? AND DEVICE_ID=?";
		return jdbcTemplate.update(sql, new Object[]{portId,deviceId});
	}

	@Override
	public AiedriDevicePort findAportByPortIdAndDeviceId(Integer id,
			Integer deviceId) {
		String sql="SELECT * FROM AIEDRI_DEVICE_PORT WHERE ID=? AND DEVICE_ID=?";
		List<AiedriDevicePort> list=jdbcTemplate.query(sql, new Object[]{id,deviceId}, new EJB3AnnontationRowMapper(AiedriDevicePort.class));
		return CollectionUtils.isEmpty(list)?null:list.get(0);
	}

	@Override
	public List<AiedriDevicePort> findAportByDeviceId(Integer deviceId) {
		String sql="SELECT * FROM AIEDRI_DEVICE_PORT WHERE DEVICE_ID=?";
		return jdbcTemplate.query(sql, new Object[]{deviceId}, new EJB3AnnontationRowMapper(AiedriDevicePort.class));
	}
	@Override
	public List<AiedriDevicePort> findAllAport() {
		String sql="SELECT * FROM AIEDRI_DEVICE_PORT ";
		return jdbcTemplate.query(sql, new Object[]{}, new EJB3AnnontationRowMapper(AiedriDevicePort.class));
	}
	@Override
	public void getDeviceList(JqGridVo vo) {
		StringBuilder conditionBuilder = new StringBuilder();
		conditionBuilder.append(" WHERE 1=1 ");
		if(StringUtils.isNotEmpty(vo.getSearch())){
			conditionBuilder.append(vo.getSearch());
		}
		List<Object> paramList = new ArrayList<Object>();
	    String order = " ORDER BY " + " `" + QuerySqlUtil.changePropertyToSql(vo.getSidx()) + "` " + vo.getSord();
		String selectSql = "SELECT * FROM  AIEDRI_DEVICES"
				+ conditionBuilder
				+ order;
		
		Integer record = queryAllDevicesCount(conditionBuilder, paramList.toArray());
		QuerySqlUtil.processGridVo(vo, record);
		@SuppressWarnings("unchecked")
		List<AiedriDevices> list = jdbcPagingUtil.getListByPage(jdbcTemplate, selectSql, paramList.toArray(), AiedriDevices.class, vo.getPage(), vo.getRows());
		vo.setDataList(list);
	}
	
	private int queryAllDevicesCount(StringBuilder conditionBuilder, Object[] array) {
		String selectAllCount = "SELECT COUNT(1) FROM AIEDRI_DEVICES" + conditionBuilder.toString();
		int total = jdbcTemplate.queryForInt(selectAllCount, array);
		return total;
	}
}
