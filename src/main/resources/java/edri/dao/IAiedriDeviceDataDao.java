package com.asiainfo.aiedri.dao;

import java.util.List;

import com.asiainfo.aiedri.vo.AiedriPfIfData;

public interface IAiedriDeviceDataDao {
    public List<AiedriPfIfData> getIfPfDataByIfidAndDeviceid(Integer ifId,Integer deviceId,String startTime,String endTime);
    //�õ������˿ڵ���Ϣ
  	public List<AiedriPfIfData> getIfAndDeviceInfo(String startTime,
  			String endTime);
  //�õ��˿ڵ����һ������
  	public List<AiedriPfIfData> getLatestIfData(int deviceId, int ifId,
  			String coldate);
}
