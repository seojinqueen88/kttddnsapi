package com.kttddnsapi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.session.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kttddnsapi.dao.ImergencyReportDao;
import com.kttddnsapi.model.ImergencyReport;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode 
@ToString
@Slf4j
@Service
public class ImergencyReportService {
  @Autowired
  private ImergencyReportDao imgerncyReport;
  
  //Logger log = LoggerFactory.getLogger("imgerncyReport");
  
  public int insertImergenyReport(ImergencyReport report)
  {
   return imgerncyReport.insertImergenyReport(report);
  }
  @Transactional(readOnly = true)
  public ImergencyReport selectImergencyRepory()
  {
    Map<String, Object> map = new HashMap<>();
    map.put("all_search_day", "all_search_day");
    //log.debug("all_search_day");
    return imgerncyReport.selectImergencyRepory(map);
  }
  @Transactional(readOnly = true)
  public void selectImergencyRepory(ResultHandler<ImergencyReport> handler, String search_day)
  {
    Map<String, Object> map = new HashMap<>();
    map.put("search_day ", search_day);
    //log.debug("search_day {}",search_day);
    imgerncyReport.selectImergencyRepory(map, handler);
  }
}
