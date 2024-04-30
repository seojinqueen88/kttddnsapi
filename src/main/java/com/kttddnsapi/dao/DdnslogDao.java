package com.kttddnsapi.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DdnslogDao
{
	public int insertDdnslog(Map<String, Object> map);
	public int insertDdnslogPhone(Map<String, Object> map);	
	public int insertDdnslogOtpType(Map<String, Object> map);	

}
