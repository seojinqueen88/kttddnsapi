package com.kttddnsapi.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.kttddnsapi.model.P2pregister;

@Mapper
public interface P2pregisterDao
{
	public int insertP2pregister(P2pregister p2pregister);
	public int updateP2pregister(P2pregister p2pregister);
	public P2pregister selectP2pregisterWhereMac(String mac);
	public List<P2pregister> selectP2pregisterInMac(Map<String, Object> map);
}
