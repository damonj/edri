package com.asiainfo.aiedri.bo.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ainx.common.util.SequenceUtil;

import com.asiainfo.aiedri.bo.IAiedriDeviceBo;
import com.asiainfo.aiedri.dao.IAiedriDeviceDao;
import com.asiainfo.aiedri.vo.AiedriDevicePort;
import com.asiainfo.aiedri.vo.AiedriDevices;
import com.asiainfo.aiedri.vo.JqGridVo;

public class AiedriDeviceBo implements IAiedriDeviceBo{
	protected Logger logger=Logger.getLogger(this.getClass());
	private IAiedriDeviceDao aiedriDeviceDao;
	
	public void setAiedriDeviceDao(IAiedriDeviceDao aiedriDeviceDao) {
		this.aiedriDeviceDao = aiedriDeviceDao;
	}

	@Override
	public int addAiedriDevice(AiedriDevices devices) {
		Integer id=SequenceUtil.getSequenceId("aiedriDeviceId");
		if(CollectionUtils.isNotEmpty(devices.getPorts())){
			for(AiedriDevicePort p:devices.getPorts()){
				p.setDeviceId(id);
				aiedriDeviceDao.addAport(p);
			}
		}
		devices.setId(id);
		return aiedriDeviceDao.addAiedriDevice(devices);
	}

	@Override
	public int updateAiedriDevice(AiedriDevices devices) {
		if(CollectionUtils.isNotEmpty(devices.getPorts())){
			for(AiedriDevicePort p:devices.getPorts()){
				if(null!=p.getId()){
					aiedriDeviceDao.updateAport(p);
				}else{
					aiedriDeviceDao.addAport(p);
				}
			}
		}
		return aiedriDeviceDao.updateAiedriDevice(devices);
	}

	@Override
	public int deleteAiedriDeviceById(Integer deviceId) {
		return aiedriDeviceDao.deleteAiedriDeviceById(deviceId);
	}

	@Override
	public AiedriDevices findAiedriDeviceById(Integer id) {
		AiedriDevices device=aiedriDeviceDao.findAiedriDeviceById(id);
		List<AiedriDevicePort> list=aiedriDeviceDao.findAportByDeviceId(id);
		if(CollectionUtils.isNotEmpty(list)){
			device.setPorts(list);
		}
		return device;
	}

	@Override
	public List<AiedriDevices> findAllAiedriDevice() {
		return aiedriDeviceDao.findAllAiedriDevice();
	}

	@Override
	public int addAport(AiedriDevicePort port) {
		return aiedriDeviceDao.addAport(port);
	}

	@Override
	public int updateAport(AiedriDevicePort port) {
		return aiedriDeviceDao.updateAport(port);
	}

	@Override
	public int deleteAportByPortIdAndDeviceId(Integer portId,Integer deviceId) {
		return aiedriDeviceDao.deleteAportByPortIdAndDeviceId(portId, deviceId);
	}

	@Override
	public AiedriDevicePort findAportByPortIdAndDeviceId(Integer portId,Integer deviceId) {
		return aiedriDeviceDao.findAportByPortIdAndDeviceId(portId, deviceId);
	}

	@Override
	public List<AiedriDevicePort> findAportByDeviceId(Integer id) {
		return aiedriDeviceDao.findAportByDeviceId(id);
	}


	@Override
	public List<AiedriDevicePort> findAllAport() {
		return aiedriDeviceDao.findAllAport();
	}

	@Override
	public Map<Integer, List<AiedriDevicePort>> findMapOfAllAport() {
		List<AiedriDevicePort> list=aiedriDeviceDao.findAllAport();
		if(CollectionUtils.isEmpty(list)){
			return null;
		}
		Map<Integer, List<AiedriDevicePort>> map=new HashMap<Integer, List<AiedriDevicePort>>();
		List<AiedriDevicePort> temp=null;
		for(AiedriDevicePort port:list){
			if(map.isEmpty() || !map.containsKey(port.getDeviceId())){
				temp=new ArrayList<AiedriDevicePort>();
				temp.add(port);
				map.put(port.getDeviceId(), temp);
			}else{
				map.get(port.getDeviceId()).add(port);
			}
		}
		return map;
	}
	@Override
	public void getDeviceList(JqGridVo vo) {
		aiedriDeviceDao.getDeviceList(vo);
	}

}
