package com.kttddnsapi.service;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kttddnsapi.dao.ClientAccessLogDao;

@Service
public class ClientAccessLogService {
  @Autowired
  private ClientAccessLogDao clientAceesLogDao;
  static public final int TYPE_APP_ACCESS_LOG = 0;
  static public final int TYPE_CMS_ACCESS_LOG = TYPE_APP_ACCESS_LOG+1;
  static public final int TYPE_DEVICE_ACCESS_LOG = TYPE_CMS_ACCESS_LOG+1;
  static public final int TYPE_UNKOWN_ACCESS_LOG = TYPE_DEVICE_ACCESS_LOG+1;
  public int insert_update_ClientAccessLogTbl(int accessType, String req_id, String mac_address, String api_type)
  {
    Map<String, Object> map = new HashMap<>();
   System.out.println(accessType);
    map.put("client_access_log_req_type", accessType);
    map.put("req_id", req_id);
    map.put("mac_address", mac_address);
    map.put("api_type", api_type);
    return clientAceesLogDao.insert_update_ClientAccessLogTbl(map);
  }
}
