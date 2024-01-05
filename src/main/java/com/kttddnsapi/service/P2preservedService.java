package com.kttddnsapi.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kttddnsapi.dao.P2preservedDao;

@Service
public class P2preservedService
{
	@Autowired
	private P2preservedDao p2preservedDao;

	public boolean updateP2preserved(String p2pUid, String historyMac)
	{
		Map<String, Object> map = new HashMap<>();
		map.put("p2pUid", p2pUid);
		map.put("historyMac", historyMac);

		if(p2preservedDao.updateP2preserved(map) > 0)
			return true;
		else
			return false;
	}

	public String selectP2preservedWhereHistorymac(String historyMac)
	{
		return p2preservedDao.selectP2preservedWhereHistorymac(historyMac);
	}

	public String selectP2preservedWhereUsed0()
	{
		return p2preservedDao.selectP2preservedWhereUsed0();
	}
}
