package com.kttddnsapi.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kttddnsapi.model.Commodity;

@Mapper
public interface CommodityDao
{
	public List<Commodity> selectCommodity();
}
