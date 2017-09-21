package com.asiainfo.aiedri.dao;

import java.util.List;

import com.asiainfo.aiedri.vo.AiedriPfIfData;

public interface IAiedriDeviceDataDao {
    public List<AiedriPfIfData> getIfPfDataByIfidAndDeviceid(Integer ifId,Integer deviceId,String startTime,String endTime);
    //得到被监测端口的信息
  	public List<AiedriPfIfData> getIfAndDeviceInfo(String startTime,
  			String endTime);
  //得到端口的最近一条数据
  	public List<AiedriPfIfData> getLatestIfData(int deviceId, int ifId,
  			String coldate);
}
