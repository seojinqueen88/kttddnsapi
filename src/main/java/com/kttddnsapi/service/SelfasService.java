package com.kttddnsapi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kttddnsapi.dao.SelfasDao;
import com.kttddnsapi.model.Selfas;

@Service
public class SelfasService
{
	@Autowired
	private SelfasDao selfasDao;

	public boolean insertSelfas(Selfas selfas)
	{
		if(selfasDao.insertSelfas(selfas) > 0)
			return true;
		else
			return false;
	}

	public boolean updateSelfasCertnum(Selfas selfas)
	{
		if(selfasDao.updateSelfasCertnum(selfas) > 0)
			return true;
		else
			return false;
	}

	public boolean deleteSelfasWhereMac(String mac)
	{
		if(selfasDao.deleteSelfasWhereMac(mac) > 0)
			return true;
		else
			return false;
	}
	
	public boolean deleteSelfasWhereMacUserlevel(String mac, int user_level)
	{
		Map<String, Object> map = new HashMap<>();
		map.put("mac", mac);
		map.put("user_level", user_level);
		if(selfasDao.deleteSelfasWhereMacUserlevel(map) > 0)
			return true;
		else
			return false;
	}
	public Selfas selectSelfasWhereMacAndUserlevel(String mac, int user_level)
	{
		Map<String, Object> map = new HashMap<>();
		map.put("mac", mac);
		map.put("user_level", user_level);

		return selfasDao.selectSelfasWhereMacAndUserlevel(map);
	}
	public Selfas selectSelfasWhereMacAndUserlevelCms(String mac, int user_level) 
	{		
		Map<String, Object> map = new HashMap<>();
		map.put("mac", mac);
		map.put("user_level", user_level);

		return selfasDao.selectSelfasWhereMacAndUserlevelCms(map);
	}
	public String selectSelfasWhereMacAndUserlevelCmsCertnum(String mac, int user_level) 
	{		
		Map<String, Object> map = new HashMap<>();
		map.put("mac", mac);
		map.put("user_level", user_level);

		return selfasDao.selectSelfasWhereMacAndUserlevelCmsCertnum(map);
	}
	public List<Selfas> selectSelfasWhereMac(String mac)
	{
		return selfasDao.selectSelfasWhereMac(mac);
	}
}
