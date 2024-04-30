package com.kttddnsapi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kttddnsapi.dao.DdnslogDao;
import com.kttddnsapi.model.P2pregister;

@Service
public class DdnslogService
{
	@Autowired
	private DdnslogDao ddnslogDao;

	public boolean insertDdnslog(String mac, String msg)
	{
		Map<String, Object> map = new HashMap<>();
		map.put("mac", mac);
		map.put("message", msg);
		if(ddnslogDao.insertDdnslog(map) > 0)
			return true;
		else
			return false;
	}
	

	public boolean insertDdnslogPhone(String mac, String phone, String service_no, String msg)
	{
		Map<String, Object> map = new HashMap<>();
		
		map.put("mac", mac);
		map.put("phone", phone);
		map.put("service_no", service_no);
		map.put("message", msg);
		if(ddnslogDao.insertDdnslogPhone(map) > 0)
			return true;
		else
			return false;
	}

	

	public boolean insertDdnslogOtpType(String mac, String phone, String service_no,  int otp_batch_all, String msg)
	{
		Map<String, Object> map = new HashMap<>();
		
		map.put("mac", mac);
		map.put("phone", phone);
		map.put("service_no", service_no);
		map.put("otp_batch_all", otp_batch_all);
		map.put("message", msg);
		if(ddnslogDao.insertDdnslogOtpType(map) > 0)
			return true;
		else
			return false;
	}

}
