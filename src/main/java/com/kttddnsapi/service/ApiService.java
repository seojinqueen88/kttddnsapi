package com.kttddnsapi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kttddnsapi.dao.ApiDao;
import com.kttddnsapi.dao.P2pregisterDao;
import com.kttddnsapi.model.P2pregister;
import com.kttddnsapi.util.InstancePW_GEN_UTIL;

import ch.qos.logback.classic.Logger;

@Service
public class ApiService {
	private final String PAST_P2P_MIN_MACADDRESS = "000C2806FB18";
	private final String PAST_P2P_MAX_MACADDRESS = "000C28091EA0";
	@Autowired
	private ApiDao apiDao;

	@Autowired
	private P2pregisterDao p2pregisterDao;

	public boolean insertDevice(String serviceNo, String mac, int registerType,
			String employeeNo) {
		Map<String, Object> map = new HashMap<>();
		map.put("serviceNo", serviceNo);
		map.put("mac", mac);
		map.put("registerType", registerType);
		map.put("employeeNo", employeeNo);

		if (apiDao.insertDevice(map) > 0)
			return true;
		else
			return false;
	}

	public boolean updateDeviceServiceopenWhereMac(String mac) {
		if (apiDao.updateDeviceServiceopenWhereMac(mac) > 0)
			return true;
		else
			return false;
	}

	public boolean updateDeviceServicenoWhereMac(String serviceNo, String mac,
			int registerType, String employeeNo) {
		Map<String, Object> map = new HashMap<>();
		map.put("serviceNo", serviceNo);
		map.put("mac", mac);
		map.put("registerType", registerType);
		map.put("employeeNo", employeeNo);
	
		if (apiDao.updateDeviceServicenoWhereMac(map) > 0)
			return true;
		else
			return false;
	}

	public boolean updateDeviceServicenoWhereInMac(String macListString) {
		Map<String, Object> map = new HashMap<>();
		map.put("macListString", macListString);

		if (apiDao.updateDeviceServicenoWhereInMac(map) > 0)
			return true;
		else
			return false;
	}

	public boolean updateDeviceServiceWhereInMac(String macListString) {
		Map<String, Object> map = new HashMap<>();
		map.put("macListString", macListString);

		if (apiDao.updateDeviceServiceWhereInMac(map) > 0)
			return true;
		else
			return false;
	}

	public boolean updateDeviceP2pWhereMac(String mac, String p2pUid,
			int p2pPriority) {
		Map<String, Object> map = new HashMap<>();
		map.put("mac", mac);
		map.put("p2pUid", p2pUid);
		map.put("p2pPriority", p2pPriority);

		if (apiDao.updateDeviceP2pWhereMac(map) > 0)
			return true;
		else
			return false;
	}

	public boolean updateDeviceP2pdeviceWhereMac(String mac) {
		if (apiDao.updateDeviceP2pdeviceWhereMac(mac) > 0)
			return true;
		else
			return false;
	}

	public boolean updateUserOTP_YN(Integer opt_yn , String mac)
	{
	    Map<String, Object> map = new HashMap<>();
        map.put("mac", mac);
        map.put("opt_yn", opt_yn);
        if (apiDao.updateUserOTP_YN(map) > 0)
            return true;
        else
            return false;
	}
	
	public boolean update_users_service_no_access_rule(String mac , Integer access_rule)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("mac", mac);
        map.put("access_rule", access_rule);
        return apiDao.update_users_service_access_rule(map) > 0 ? true : false;
    }
	
	
	
	public boolean deleteDeviceWhereSysid(String sysid) {
		if (apiDao.deleteDeviceWhereSysid(sysid) > 0)
			return true;
		else
			return false;
	}

	public boolean deleteDeviceWhereDomain(String domain) {
		if (apiDao.deleteDeviceWhereDomain(domain) > 0)
			return true;
		else
			return false;
	}

	public boolean deleteDeviceWhereMac(String mac) {
		if (apiDao.deleteDeviceWhereMac(mac) > 0)
			return true;
		else
			return false;
	}

	public List<Map<String, Object>> selectDeviceInSysidForDeviceinfo(
			String sysidStrings) {
		Map<String, Object> map = new HashMap<>();
		map.put("sysidStrings", sysidStrings);
		return apiDao.selectDeviceInSysidForDeviceinfo(map);
	}
	public List<Map<String, Object>> selectDeviceWhereServicenoForOss(
			String serviceNo) {
		return apiDao.selectDeviceWhereServicenoForOss(serviceNo);
	}

	public List<Map<String, Object>> selectDeviceInSysidForDeviceinfo2(
			String sysidStrings) {
		Map<String, Object> map = new HashMap<>();
		map.put("sysidStrings", sysidStrings);
		return apiDao.selectDeviceInSysidForDeviceinfo2(map);
	}
	
	
	public List<Map<String, Object>> selectDeviceWhereInServicenoForApp(
			String serviceNoListString) {
		Map<String, Object> map = new HashMap<>();
		 
		map.put("serviceNoListString", serviceNoListString);
		///// selectDeviceWhereInServicenoForApp_new_version true로 설정 시 select 한 시점이 dB서버에 기록 됨.
		///// false로 할 경우 이전과 같은 형태로 조회 됨.
	 map.put("selectDeviceWhereInServicenoForApp_new_version",false);
	  /// map.put("selectDeviceWhereInServicenoForApp_new_version",false);
		/////
		///System.out.println("selectDeviceWhereInServicenoForApp");
		return apiDao.selectDeviceWhereInServicenoForApp(map);
	}

	public List<String> updateAppStaistics(ArrayList <String> list,String app_name,Integer app_type)
	{
	  Map<String, Object> map = new HashMap<>();
	  map.put("list", list);
	  map.put("app_name", app_name);
	  map.put("app_type", app_type);
      return  apiDao.updateAppStaistics(map);

	}
	public List<Map<String, Object>> selectDeviceInSysidForDeviceinfo3(
			String sysidStrings) {
		Map<String, Object> map = new HashMap<>();
		map.put("sysidStrings", sysidStrings);
		return apiDao.selectDeviceInSysidForDeviceinfo3(map);
	}
	public List<Map<String, Object>> selectDeviceWhereInServicenoForCs(
			String serviceNoListString) {
		Map<String, Object> map = new HashMap<>();
		map.put("serviceNoListString", serviceNoListString);
		return apiDao.selectDeviceWhereInServicenoForCs(map);
	}

	public Map<String, Object> selectDeviceWhereDomain(String domain) {
		List<Map<String, Object>> list = apiDao.selectDeviceWhereDomain(domain);
		if (list.size() == 0)
			return null;
		return list.get(0);
	}

	public List<Map<String, Object>> selectDeviceWhereInDomain(
			String domainListString) {
		Map<String, Object> map = new HashMap<>();
		map.put("domainListString", domainListString);
		return apiDao.selectDeviceWhereInDomain(map);
	}

	public Map<String, Object> selectDeviceWhereMac(String mac) {
		List<Map<String, Object>> list = apiDao.selectDeviceWhereMac(mac);
		if (list.size() == 0)
			return null;
		return list.get(0);
	}

	public String selectDevicePublicIpWhereMac(String mac) {
		return apiDao.selectDevicePublicIpWhereMac(mac);
	}

	public String selectDevicePublicIpWhereMac_accessrule(String mac) {
		return apiDao.selectDevicePublicIpWhereMac_accessrule(mac);
	}
	
	public List<Map<String, Object>> selectDevicePublicIpWhereMacWithAccessRule(String mac) {
      return apiDao.selectDevicePublicIpWhereMacWithAccessRule(mac);
    }
	public List<Map<String, Object>> selectDevicePublicIpWhereMac(String mac,String white_ip,Integer service_open) {
	  Map<String, Object> map = new HashMap<>();
      map.put("mac", mac);
      map.put("white_ip", white_ip);
      map.put("check_service_open_nm", service_open);
      return apiDao.selectDevicePublicIpWhereMacByMap(map);
  }
  
	public List<Map<String, Object>> selectDeviceWhereSysid(String sysid) {
		Map<String, Object> map = new HashMap<>();
		map.put("sysid", sysid);
		return apiDao.selectDeviceWhereSysid(map);
	}
	public List<Map<String, Object>> selectDeviceWhereServiceno(
			String serviceNo) {
		return apiDao.selectDeviceWhereServiceno(serviceNo);
	}

	public List<Map<String, Object>> selectDeviceWhereSysid2(String sysid) {
		return apiDao.selectDeviceWhereSysid2(sysid);
	}

	public Map<String, Object> selectDeviceWhereMac2(String mac) {
		List<Map<String, Object>> list = apiDao.selectDeviceWhereMac2(mac);
		if (list.size() == 0)
			return null;
		return list.get(0);
	}

	public Map<String, Object> selectDeviceWhereMac3(String mac) {
		List<Map<String, Object>> list = apiDao.selectDeviceWhereMac3(mac);
		if (list.size() == 0)
			return null;
		return list.get(0);
	}

	public Map<String, Object> selectDeviceWhereMac4(String mac) {
		List<Map<String, Object>> list = apiDao.selectDeviceWhereMac4(mac);
		if (list.size() == 0)
			return null;
		return list.get(0);
	}

	public Map<String, Object> selectDeviceWhereMac5(String mac) {
		List<Map<String, Object>> list = apiDao.selectDeviceWhereMac4(mac);
		if (list.size() == 0)
			return null;
		return list.get(0);
	}
	

	public String selectServiceCuststsWhereServiceno(String serviceNo) {
		return apiDao.selectServiceCuststsWhereServiceno(serviceNo);
	}

	public String selectServiceContractnoWhereServiceno(String serviceNo) {
		return apiDao.selectServiceContractnoWhereServiceno(serviceNo);
	}

	public List<Map<String, Object>> selectDeviceWhereInServiceno(
			String serviceNoListString) {
		Map<String, Object> map = new HashMap<>();
		map.put("serviceNoListString", serviceNoListString);
		return apiDao.selectDeviceWhereInServiceno(map);
	}

	public List<Map<String, Object>> selectDeviceWhereInServicenoOTP(
			String serviceNoListString) {
		Map<String, Object> map = new HashMap<>();
		map.put("serviceNoListString", serviceNoListString);
		return apiDao.selectDeviceWhereInServicenoOTP(map);
	}
	
	public List<Map<String, Object>> selectDeviceMAKERWhereInServiceno(
			String serviceNoListString) {
		Map<String, Object> map = new HashMap<>();
		map.put("serviceNoListString", serviceNoListString);
		return apiDao.selectDeviceMAKERWhereInServiceno(map);
	}

	
	public List<Map<String, Object>> selectDeviceWhereInMac(
			String macListString) {
		Map<String, Object> map = new HashMap<>();
		map.put("macListString", macListString);
		return apiDao.selectDeviceWhereInMac(map);
	}

	public List<Map<String, Object>> selectDeviceWhereInMac2(
			String macListString) {
		Map<String, Object> map = new HashMap<>();
		map.put("macListString", macListString);
		return apiDao.selectDeviceWhereInMac2(map);
	}

	public List<Map<String, Object>> selectDeviceWhereInPhone(
			String serviceNoListString) {
		Map<String, Object> map = new HashMap<>();
		map.put("serviceNoListString", serviceNoListString);
		return apiDao.selectDeviceWhereInPhone(map);
	}
	
	
	
	public Map<String, Object> selectDeviceP2pWhereMac(String mac) {
		List<Map<String, Object>> list = apiDao.selectDeviceP2pWhereMac(mac);
		if (list.size() == 0)
			return null;
		return list.get(0);
	}

	public Map<String, Object> selectDeviceP2pWhereMac2(String mac) {
		List<Map<String, Object>> list = apiDao.selectDeviceP2pWhereMac2(mac);
		if (list.size() == 0)
			return null;
		return list.get(0);
	}

	public List<Map<String, Object>> selectServiceWhereInServiceno(
			String serviceNoListString) {
		Map<String, Object> map = new HashMap<>();
		map.put("serviceNoListString", serviceNoListString);
		return apiDao.selectServiceWhereInServiceno(map);
	}
	

	
	public List<Map<String, Object>> selectDeviceMacWhereInServicenoPhoneOTP(
			String serviceNoListString, String phone) {
		Map<String, Object> map = new HashMap<>();
		map.put("serviceNoListString", serviceNoListString);
		map.put("phone", phone);

		return apiDao.selectDeviceMacWhereInServicenoPhoneOTP(map);
	}
	
	public List<Map<String, Object>> selectDeviceMacWhereInServicenoPhoneOTP1Hour(
			String serviceNoListString, String phone) {
		Map<String, Object> map = new HashMap<>();
		map.put("serviceNoListString", serviceNoListString);
		map.put("phone", phone);

		return apiDao.selectDeviceMacWhereInServicenoPhoneOTP1Hour(map);
	}
	
	public List<Map<String, Object>> selectDeviceMacWhereInServicenoPhoneOTPKttTEST(
			String serviceNoListString, String phone) {
		Map<String, Object> map = new HashMap<>();
		map.put("serviceNoListString", serviceNoListString);
		map.put("phone", phone);

		return apiDao.selectDeviceMacWhereInServicenoPhoneOTPKttTEST(map);
	}
	
	public List<Map<String, Object>> selectDeviceMacWhereInServicenoPhoneOTP1HourKttTEST(
			String serviceNoListString, String phone) {
		Map<String, Object> map = new HashMap<>();
		map.put("serviceNoListString", serviceNoListString);
		map.put("phone", phone);

		return apiDao.selectDeviceMacWhereInServicenoPhoneOTP1HourKttTEST(map);
	}

	public P2pregister getOldP2p(String mac) {
		P2pregister nP2pregister = new P2pregister();
		nP2pregister.setP2p_uid("");
		nP2pregister.setP2p_priority(0);

		do {
			if (mac.length() <= 0)
				break;

			if (mac.length() == 12
					&& 0 <= mac.compareTo(PAST_P2P_MIN_MACADDRESS)
					&& mac.compareTo(PAST_P2P_MAX_MACADDRESS) <= 0) {
				nP2pregister.setP2p_uid(mac);
				nP2pregister.setP2p_priority(1);
				break;
			}
			return null;
		} while (false);

		return nP2pregister;
	}

	public P2pregister getP2p(String mac) {
		P2pregister nP2pregister = getOldP2p(mac);
		if (nP2pregister != null)
			return nP2pregister;

		nP2pregister = p2pregisterDao.selectP2pregisterWhereMac(mac);
		if (nP2pregister != null)
			return nP2pregister;

		nP2pregister = new P2pregister();
		nP2pregister.setP2p_uid("");
		nP2pregister.setP2p_priority(0);

		return nP2pregister;
	}

	public void getDeviceListP2pInfo(List<Map<String, Object>> deviceList) {
		Map<String, Object> p2pMap = new HashMap<>();
		String macStrings = "";

		for (Map<String, Object> device : deviceList) {
			String macAddress = (String) device.get("macAddress");
			P2pregister p2pregister = getOldP2p(macAddress);
			if (p2pregister != null) {
				device.put("p2pUid", p2pregister.getP2p_uid());
				device.put("p2pPriority", p2pregister.getP2p_priority());
			} else {
				if (p2pMap.size() == 0)
					macStrings += ("'" + macAddress + "'");
				else
					macStrings += (", '" + macAddress + "'");
				p2pMap.put(macAddress, device);
			}
		}

		if (p2pMap.size() == 0)
			return;
		Map<String, Object> map = new HashMap<>();
		map.put("macStrings", macStrings);
		List<P2pregister> p2pList = p2pregisterDao.selectP2pregisterInMac(map);
		if (p2pList != null) {
			for (P2pregister p2pregister : p2pList) {
				String key = (String) p2pregister.getMac();

				@SuppressWarnings("unchecked")
				Map<String, Object> device = (Map<String, Object>) p2pMap
						.get(key);

				if (device != null) {
					device.put("p2pUid", p2pregister.getP2p_uid());
					device.put("p2pPriority", p2pregister.getP2p_priority());
					p2pMap.remove((String) p2pregister.getMac());
				}
			}
		}

		for (String key : p2pMap.keySet()) {
			@SuppressWarnings("unchecked")
			Map<String, Object> device = (Map<String, Object>) p2pMap.get(key);

			if (device != null) {
				device.put("p2pUid", "");
				device.put("p2pPriority", 0);
			}
		}
	}
	/**
	 * 
	 * @Method Name : ktt_cipher_makeInstantPasswd
	 * @작성일 : 2022. 9. 6.
	 * @작성자 : Foryoucom
	 * @변경이력 :
	 * @Method 설명 : 현 녹화장비 및 운영서버(마스터 페이지 기준)으로 32비트 연산을 하므로 64비트(long)을 32비트로
	 *         연산함
	 * @param year
	 * @param month
	 * @param day
	 * @param hour
	 * @param mac
	 * @return
	 */

	public String ktt_cipher_makeInstantPasswd(int year, int month, int day,
			int hour, byte[] mac , int choose_old_new) {
		// System.out.println(mac.toString());
		if (InstancePW_GEN_UTIL.isbUserCLib())
			return InstancePW_GEN_UTIL.ktt_cipher_makeInstantPasswd_C(year,
					month, day, hour, mac , choose_old_new);
		else
			return InstancePW_GEN_UTIL.ktt_cipher_makeInstantPasswd_JAVA(year,
					month, day, hour, mac);
	}

	public String getIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");

		if (ip == null)
			ip = request.getHeader("Proxy-Client-IP");

		if (ip == null)
			ip = request.getHeader("WL-Proxy-Client-IP");

		if (ip == null)
			ip = request.getHeader("HTTP_CLIENT_IP");

		if (ip == null)
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");

		if (ip == null)
			ip = request.getRemoteAddr();

		return ip;
	}

	public int verString2Int(String verStr) {
		int intVer = 0;
		try {
		final int intArray[] = {1000000, 1000, 1};
		String strArray[] = verStr.split("\\.");
		if (strArray.length != 3)
			return 0;
		for (int i = 0; i < strArray.length; i++) 
		{
			String str = strArray[i].replaceAll("[^0-9]", "");
			if (str.length() == 0)
				return 0;
			int num = Integer.valueOf(str);
			intVer += num * intArray[i];
		}
			return intVer;
		}catch(Exception e)
		{
		   // e.printStackTrace();
		    
			return 0;
		}
	}

	public List<Map<String, Object>> selectDeviceWhereServicenoCms(
			String serviceNo) {
		return apiDao.selectDeviceWhereServicenoCms(serviceNo);
	}
	public Boolean insertCheckIPValidation(String mac, String ask_ip, String log)
	{
	  Map<String, Object> map = new HashMap<>();
      map.put("mac", mac);
      map.put("log", log);
      map.put("ask_ip", ask_ip);
     apiDao.insertCheckIPValidation(map);
	 System.out.println(map.keySet());
	 return map.containsKey("index") && (Integer.valueOf(map.get("index").toString()) !=0);
	}
	public Boolean insertCheckIPValidation(String mac, String log)
	{
	  Map<String, Object> map = new HashMap<>();
      map.put("mac", mac);
      map.put("log", log);
     apiDao.insertCheckIPValidation(map);
	 System.out.println(map.keySet());
	 return map.containsKey("index") && (Integer.valueOf(map.get("index").toString()) !=0);
	}
}
