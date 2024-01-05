package com.kttddnsapi.dao;

import java.util.Map;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ClientAccessLogDao {
 public int  insert_update_ClientAccessLogTbl(Map<String, Object> map);
}
