package com.kttddnsapi.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiDao
{
	public int insertDevice(Map<String, Object> map);

	public int updateDeviceServiceopenWhereMac(String mac);
	public int updateDeviceServicenoWhereMac(Map<String, Object> map);
	 
	public int updateDeviceServicenoWhereInMac(Map<String, Object> map);
	public int updateDeviceServiceWhereInMac(Map<String, Object> map);
	public int updateDeviceP2pWhereMac(Map<String, Object> map);
	public int updateDeviceP2pdeviceWhereMac(String mac);

	public int update_users_service_access_rule(Map<String,Object>map);
	
	public int updateUserOTP_YN(Map<String, Object> map);
	
	public int deleteDeviceWhereSysid(String sysid);
	public int deleteDeviceWhereDomain(String domain);
	public int deleteDeviceWhereMac(String mac);

	public List<Map<String, Object>> selectDeviceInSysidForDeviceinfo(Map<String, Object> map);
	public List<Map<String, Object>> selectDeviceWhereServicenoForOss(String serviceNo);

	public List<Map<String, Object>> selectDeviceInSysidForDeviceinfo2(Map<String, Object> map);
	public List<Map<String, Object>> selectDeviceWhereInServicenoForApp(Map<String, Object> map);

	public List<Map<String, Object>> selectDeviceInSysidForDeviceinfo3(Map<String, Object> map);
	public List<Map<String, Object>> selectDeviceWhereInServicenoForCs(Map<String, Object> map);

	public List<Map<String, Object>> selectDeviceWhereDomain(String domain);
	public List<Map<String, Object>> selectDeviceWhereInDomain(Map<String, Object> map);
	public List<Map<String, Object>> selectDeviceWhereMac(String mac);
	public String selectDevicePublicIpWhereMac(String mac);
	public String selectDevicePublicIpWhereMac_accessrule(String mac);

	public List<Map<String, Object>> selectDevicePublicIpWhereMacByMap(Map<String,Object> mac);
	public List<Map<String, Object>> selectDevicePublicIpWhereMacWithAccessRule(String mac);
	public List<Map<String, Object>> selectDeviceWhereSysid(Map<String, Object> map);
	public List<Map<String, Object>> selectDeviceWhereServiceno(String serviceNo);

	public List<Map<String, Object>> selectDeviceWhereSysid2(String sysid);
	public List<Map<String, Object>> selectDeviceWhereMac2(String mac);
	public List<Map<String, Object>> selectDeviceWhereMac3(String mac);
	public List<Map<String, Object>> selectDeviceWhereMac4(String mac);
	public List<Map<String, Object>> selectDeviceWhereMac5(String mac);
	public String selectServiceCuststsWhereServiceno(String serviceNo);
	public String selectServiceContractnoWhereServiceno(String serviceNo);
	public List<Map<String, Object>> selectDeviceWhereInServiceno(Map<String, Object> map);
	public List<Map<String, Object>> selectDeviceWhereInServicenoOTP(Map<String, Object> map);
	public List<Map<String, Object>> selectDeviceMAKERWhereInServiceno(Map<String, Object> map);
	
	public List<Map<String, Object>> selectDeviceWhereInMac(Map<String, Object> map);
	public List<Map<String, Object>> selectDeviceWhereInMac2(Map<String, Object> map);
	public List<Map<String, Object>> selectDeviceP2pWhereMac(String mac);
	public List<Map<String, Object>> selectDeviceP2pWhereMac2(String mac);
	public List<Map<String, Object>> selectServiceWhereInServiceno(Map<String, Object> map);
	public List<Map<String, Object>> selectDevicePhoneWhereInServiceno(Map<String, Object> map);
	public List<Map<String, Object>> selectDeviceMacWhereInServicenoPhoneOTPKttTEST(Map<String, Object> map);
	public List<Map<String, Object>> selectDeviceMacWhereInServicenoPhoneOTP1HourKttTEST(Map<String, Object> map);
	public List<Map<String, Object>> selectDeviceOTP1HourTimeWhereInServiceno(Map<String, Object> map);	
	
	public List<Map<String, Object>> selectDeviceWhereInPhone(Map<String, Object> map);
	
	public List<Map<String, Object>> selectDeviceWhereServicenoCms(String serviceNo);

	public int insertCheckIPValidation(Map<String, Object> map);
	public List<String> updateAppStaistics(Map<String, Object> map);
}
