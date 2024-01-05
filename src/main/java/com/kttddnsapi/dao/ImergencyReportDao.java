package com.kttddnsapi.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.ResultHandler;
import com.kttddnsapi.model.ImergencyReport;

@Mapper
public interface ImergencyReportDao {
  public int insertImergenyReport(ImergencyReport report);
  public ImergencyReport selectImergencyRepory(Map<String, Object> map);
  public void selectImergencyRepory(Map<String, Object> map,ResultHandler<ImergencyReport> handler);
}
