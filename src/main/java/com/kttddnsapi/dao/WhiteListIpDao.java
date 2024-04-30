package com.kttddnsapi.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.kttddnsapi.model.WhiteListIp;

@Mapper
public interface WhiteListIpDao
{
	public List<WhiteListIp> selectWhiteListIp();
	public List<WhiteListIp> selectDeviceMacWhereWhiteListIp();
	public int insertDeviceWhiteListIp(Map<String, Object> map);	
}
