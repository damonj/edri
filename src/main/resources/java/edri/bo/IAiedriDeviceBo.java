package com.asiainfo.aiedri.bo;

import java.util.List;
import java.util.Map;

import com.asiainfo.aiedri.vo.AiedriDevicePort;
import com.asiainfo.aiedri.vo.AiedriDevices;
import com.asiainfo.aiedri.vo.JqGridVo;




public interface IAiedriDeviceBo {
	public int addAiedriDevice(AiedriDevices devices);
	public int updateAiedriDevice(AiedriDevices devices);
	public int deleteAiedriDeviceById(Integer deviceId);
	public AiedriDevices findAiedriDeviceById(Integer id);
	public List<AiedriDevices> findAllAiedriDevice();
	public int addAport(AiedriDevicePort port);
	public int updateAport(AiedriDevicePort port);
	public int deleteAportByPortIdAndDeviceId(Integer portId,Integer deviceId);
	public AiedriDevicePort findAportByPortIdAndDeviceId(Integer id,Integer deviceId);
	public List<AiedriDevicePort> findAportByDeviceId(Integer deviceId);
	public List<AiedriDevicePort> findAllAport();
	public Map<Integer,List<AiedriDevicePort>> findMapOfAllAport();
	public void getDeviceList(JqGridVo vo);
}
