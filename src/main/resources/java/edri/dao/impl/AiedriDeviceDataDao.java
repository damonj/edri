package com.asiainfo.aiedri.dao.impl;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import ainx.common.spring.jdbc.EJB3AnnontationRowMapper;
import ainx.common.spring.jdbc.impl.JdbcPagingUtil;

import com.asiainfo.aiedri.dao.IAiedriDeviceDataDao;
import com.asiainfo.aiedri.vo.AiedriPfIfData;

public class AiedriDeviceDataDao implements IAiedriDeviceDataDao {
	protected JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	private JdbcPagingUtil jdbcPagingUtil;
	public void setJdbcPagingUtil(JdbcPagingUtil jdbcPagingUtil) {
		this.jdbcPagingUtil = jdbcPagingUtil;
	}
	@Override
	public List<AiedriPfIfData> getIfPfDataByIfidAndDeviceid(Integer ifId,
			Integer deviceId,String startTime,String endTime) {
		String sql="SELECT D.ID DEVICE_ID, D.NAME DEVICE_NAME, "
				+ "        P.ID IF_ID, P.NAME IF_NAME, "
				+ "        PDATA.COLDATE,PDATA.IP, "
				+ "        PDATA.IF_DESC, "
				+ "        ROUND(PDATA.INRATE/1000,2) INRATE, "
				+ "        ROUND(PDATA.OUTRATE/1000,2) OUTRATE "
				+ "   FROM AIEDRI_DEVICES D, AIEDRI_DEVICE_PORT P, "
				+ "        AIEDRI_PF_IF_DATA PDATA "
				+ "  WHERE P.DEVICE_ID=D.ID "
				+ "    AND PDATA.IP=D.IP "
				+"     AND PDATA.IF_DESC=P.NAME "
				+ "    AND D.ID=? "
				+ "    AND P.ID=? "
				+ "    AND PDATA.COLDATE>=? "
				+ "    AND PDATA.COLDATE<=?"
				+ "  ORDER BY PDATA.COLDATE ASC";
		Object[] obj=new Object[]{deviceId,ifId,startTime,endTime};
		return jdbcTemplate.query(sql, obj, new EJB3AnnontationRowMapper(AiedriPfIfData.class));
	}
	@Override
	public List<AiedriPfIfData> getIfAndDeviceInfo(String startTime,
			String endTime) {
		String sql="SELECT D.ID DEVICE_ID, D.NAME DEVICE_NAME, "
				+ "        P.ID IF_ID, P.NAME IF_NAME "
				+ "   FROM AIEDRI_DEVICES D, AIEDRI_DEVICE_PORT P, "
				+ "        AIEDRI_PF_IF_DATA PDATA "
				+ "  WHERE P.DEVICE_ID=D.ID "
				+ "    AND PDATA.IP=D.IP "
				+ "    AND PDATA.COLDATE>=? "
				+ "    AND PDATA.COLDATE<=?"
				+ "  GROUP BY DEVICE_ID,IF_ID ";
		Object[] obj=new Object[]{startTime,endTime};
		return jdbcTemplate.query(sql, obj, new EJB3AnnontationRowMapper(AiedriPfIfData.class));
	}
	@Override
	public List<AiedriPfIfData> getLatestIfData(int deviceId, int ifId,
			String coldate) {
		String sql="SELECT D.ID DEVICE_ID, D.NAME DEVICE_NAME, "
				+ "        P.ID IF_ID, P.NAME IF_NAME, "
				+ "        PDATA.* "
				+ "   FROM AIEDRI_DEVICES D, AIEDRI_DEVICE_PORT P, "
				+ "        AIEDRI_PF_IF_DATA PDATA "
				+ "  WHERE P.DEVICE_ID=D.ID "
				+ "    AND PDATA.IP=D.IP "
				+ "    AND D.ID=? "
				+ "    AND P.ID=? "
				+ "    AND PDATA.COLDATE>? "
				+ "  ORDER BY PDATA.COLDATE DESC";
		Object[] obj=new Object[]{deviceId,ifId,coldate};
		return jdbcTemplate.query(sql, obj, new EJB3AnnontationRowMapper(AiedriPfIfData.class));
	}

}
