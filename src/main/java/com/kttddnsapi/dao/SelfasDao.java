package com.kttddnsapi.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.kttddnsapi.model.Selfas;

@Mapper
public interface SelfasDao
{
	public int insertSelfas(Selfas selfas);
	public int updateSelfasCertnum(Selfas selfas);
	public int deleteSelfasWhereMac(String mac);
	public Selfas selectSelfasWhereMacAndUserlevel(Map<String, Object> map);
	public Selfas selectSelfasWhereMacAndUserlevelCms(Map<String, Object> map);
	public List<Selfas> selectSelfasWhereMac(String mac);
	public int deleteSelfasWhereMacUserlevel(Map<String, Object> map);
	public String selectSelfasWhereMacAndUserlevelCmsCertnum(Map<String, Object> map);
}
