package com.kttddnsapi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kttddnsapi.dao.WhiteListIpDao;
import com.kttddnsapi.model.WhiteListIp;

@Service
public class WhiteListIpService
{
	@Autowired
	private WhiteListIpDao whiteListIpDao;

	public List<WhiteListIp> selectWhiteListIp()
	{
		return whiteListIpDao.selectWhiteListIp();
	}
	
	public List<WhiteListIp> selectDeviceMacWhereWhiteListIp()
	{
		return whiteListIpDao.selectDeviceMacWhereWhiteListIp();
	}
	
	public boolean insertDeviceWhiteListIp(String ddnsMac,  String ip_address)
	{
		Map<String, Object> map = new HashMap<>();
		
		map.put("mac_Address", ddnsMac);
		map.put("ip_address", ip_address);
		if(whiteListIpDao.insertDeviceWhiteListIp(map) > 0)
			return true;
		else
			return false;
	}
	
}

