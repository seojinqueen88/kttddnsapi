package com.kttddnsapi.dao;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface P2preservedDao
{
	public int updateP2preserved(Map<String, Object> map);
	public String selectP2preservedWhereHistorymac(String historyMac);
	public String selectP2preservedWhereUsed0();
}
