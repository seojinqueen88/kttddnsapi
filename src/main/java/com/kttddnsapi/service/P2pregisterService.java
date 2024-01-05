package com.kttddnsapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kttddnsapi.dao.P2pregisterDao;
import com.kttddnsapi.model.P2pregister;

@Service
public class P2pregisterService
{
	@Autowired
	private P2pregisterDao p2pregisterDao;

	public boolean insertP2pregister(P2pregister p2pregister)
	{
		if(p2pregisterDao.insertP2pregister(p2pregister) > 0)
			return true;
		else
			return false;
	}

	public boolean updateP2pregister(P2pregister p2pregister)
	{
		if(p2pregisterDao.updateP2pregister(p2pregister) > 0)
			return true;
		else
			return false;
	}

	public P2pregister selectP2pregisterWhereMac(String mac)
	{
		return p2pregisterDao.selectP2pregisterWhereMac(mac);
	}
}
