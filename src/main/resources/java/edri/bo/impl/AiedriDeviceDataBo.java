package com.asiainfo.aiedri.bo.impl;

import java.util.List;

import org.apache.log4j.Logger;

import com.asiainfo.aiedri.bo.IAiedriDeviceDataBo;
import com.asiainfo.aiedri.dao.IAiedriDeviceDataDao;
import com.asiainfo.aiedri.vo.AiedriPfIfData;

public class AiedriDeviceDataBo implements IAiedriDeviceDataBo {
	
	protected Logger logger=Logger.getLogger(this.getClass());
	private IAiedriDeviceDataDao aiedriDeviceDataDao;
	public void setAiedriDeviceDataDao(IAiedriDeviceDataDao aiedriDeviceDataDao) {
		this.aiedriDeviceDataDao = aiedriDeviceDataDao;
	}

	@Override
	public List<AiedriPfIfData> getIfPfDataByIfidAndDeviceid(Integer ifId,
			Integer deviceId, String startTime, String endTime) {
		return aiedriDeviceDataDao.getIfPfDataByIfidAndDeviceid(ifId, deviceId, startTime, endTime);
	}

	@Override
	public List<AiedriPfIfData> getIfAndDeviceInfo(String startTime,
			String endTime) {
		return aiedriDeviceDataDao.getIfAndDeviceInfo(startTime, endTime);
	}

	@Override
	public List<AiedriPfIfData> getLatestIfData(int deviceId, int ifId,
			String coldate) {
		return aiedriDeviceDataDao.getLatestIfData(deviceId, ifId, coldate);
	}

}
