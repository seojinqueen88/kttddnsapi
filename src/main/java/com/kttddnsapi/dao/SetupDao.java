package com.kttddnsapi.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kttddnsapi.model.Setup;

@Mapper
public interface SetupDao
{
	public List<Setup> selectSetup();
}
