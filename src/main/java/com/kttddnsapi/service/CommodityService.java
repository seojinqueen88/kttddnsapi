package com.kttddnsapi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kttddnsapi.dao.CommodityDao;
import com.kttddnsapi.model.Commodity;

@Service
public class CommodityService
{
	@Autowired
	private CommodityDao commodityDao;

	public List<Commodity> selectCommodity()
	{
		return commodityDao.selectCommodity();
	}
}
