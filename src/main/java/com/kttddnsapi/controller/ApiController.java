package com.kttddnsapi.controller;

import static org.assertj.core.api.Assertions.allOf;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.in;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.FileLock;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.junit.internal.runners.statements.ExpectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;

import com.kttddnsapi.model.Commodity;
import com.kttddnsapi.model.ImergencyReport;
import com.kttddnsapi.model.Selfas;
import com.kttddnsapi.service.ApiService;
import com.kttddnsapi.service.ClientAccessLogService;
import com.kttddnsapi.service.CommodityService;
import com.kttddnsapi.service.DdnslogService;
import com.kttddnsapi.service.ImergencyReportService;

import com.kttddnsapi.service.P2preservedService;
import com.kttddnsapi.service.SelfasService;
import com.kttddnsapi.service.SetupService;
import com.kttddnsapi.util.Encryptions;
import com.nikhaldimann.inieditor.IniEditor;

@CrossOrigin
@Controller
@Transactional(rollbackFor = { Exception.class })
public class ApiController {
	@Autowired
	private ApiService apiService;

	@Autowired
	private P2preservedService p2preservedService;

	@Autowired
	private SelfasService selfasService;

	@Autowired
	private SetupService setupService;

	@Autowired
	private CommodityService commodityService;

	@Autowired
	private DdnslogService ddnslogService;

	@Autowired
	private ClientAccessLogService clientAccessLogService;

	@Autowired
	private ImergencyReportService imergenyReportService;
	@Autowired
	private ServletContext context;
	Logger logger = LoggerFactory.getLogger(ApiController.class);
	Logger KTTDDNS_CLIENT_ACCESSLOG_looger = LoggerFactory.getLogger("CLIENT_ACCESSLOG");
	Logger KTTDDNS_INFECTION_LOG_looger = LoggerFactory.getLogger("INFECTION_LOG");

	// Only Test Server
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
	@ResponseBody
	public ResultForm httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException ex) {
		if (logger.isDebugEnabled())
			ex.printStackTrace();
		return ResultForm.fail(ResultForm.ResultCode.NO_BODY, "파라미터가 없습니다");
	}

	public static class ResultForm<T> {
		private ResultCode resultCode;
		private String resultMsg;

		public ResultForm(ResultCode resultCode, String resultMsg) {
			this.resultCode = resultCode;
			this.resultMsg = resultMsg;
		}

		public static ResultForm success() {
			return new ResultForm(ResultCode.SUCCESS, "정상");
		}

		public static ResultForm fail(ResultCode resultCode, String resultMsg) {
			return new ResultForm(resultCode, resultMsg);
		}

		public enum ResultCode {
			SUCCESS, UNKNOWN, ERROR, INVALID_PARAMETER, NO_BODY
		}
	} // Only Test Server

	// @ControllerAdvice
	// @Order(Ordered.LOWEST_PRECEDENCE)
	// @Transactional(timeout = 30)
	// @CacheEvict(value = "hash_key", allEntries = true)
	// @Scheduled(fixedRateString = "${caching.spring.hash_keyTTL}")
	@RequestMapping("serverinfo.do")
	@ResponseBody
	public Map<String, Object> serverinfo() {
		// Sysemt.out.println(new Date() +" DDNS API SERVER INFO");
		logger.info("DDNS API SERVER INFO");
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHH");
		// Calendar calendar = Calendar.getInstance();
		// String currentTime = simpleDateFormat.format(calendar.getTime());
		String currentTime = simpleDateFormat.format(new Date());

		String hash_key = Encryptions.getSHA(currentTime, 256);

		Map<String, Object> response = new HashMap<>();
		response.put("hash_key", hash_key);
		// System.out.println(new Date() + " DDNS API RESPONSE : " +
		// response.toString());
		logger.info("DDNS API SERVER INFO {}", response.toString());
		simpleDateFormat = null;
		currentTime = null;
		return response;
	}

	@RequestMapping("kttddnsapi.do")
	@ResponseBody
	public Map<String, Object> kttddnsapi(@RequestBody Map<String, Object> request) {
		// System.out.println(new Date() +" DDNS API REQUEST : " + request.toString());
		logger.info("DDNS API REQUEST {}", request.toString());
		String result = "fail";
		String command = (String) request.get("command");
		String requestKey = "";

		Map<String, Object> response = new HashMap<>();
		response.put("command", command);

		boolean mustMsg = false;

		{
			try {
				switch (command) {
				case "KTTDDNS_SYSID_TO_DEVICEINFO":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_SYSID_TO_DEVICEINFO(request, response);
					} else {
						response.put("msg", "서버 인증에 실패하였습니다.");
						result = "nopermission";
					}
					break;
				case "KTTDDNS_SERVICENO_TO_OSS_DEVICEINFO":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_SERVICENO_TO_OSS_DEVICEINFO(request, response);
					} else {
						response.put("msg", "서버 인증에 실패하였습니다.");
						result = "nopermission";
					}
					break;
				case "KTTDDNS_SYSID_TO_DEVICEINFO2":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_SYSID_TO_DEVICEINFO2(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_SERVICENO_TO_APP_DEVICEINFO":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_SERVICENO_TO_APP_DEVICEINFO(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_SYSID_TO_DEVICEINFO3":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_SYSID_TO_DEVICEINFO3(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_SERVICENO_TO_CS_DEVICEINFO":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_SERVICENO_TO_CS_DEVICEINFO(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_DOMAIN_TO_DEVICEINFO":
					System.out.println("KTTDDNS_DOMAIN_TO_DEVICEINFO");
					result = KTTDDNS_DOMAIN_TO_DEVICEINFO(request, response);
					break;
				case "KTTDDNS_MAC_TO_DEVICEINFO":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_MAC_TO_DEVICEINFO(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_MAC_TO_MASTERKEY":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_MAC_TO_MASTERKEY(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_MAC_TO_P2PUID":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_MAC_TO_P2PUID(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_REG_P2P":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_REG_P2P(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_SYSID_TO_DEL_DOMAIN":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_SYSID_TO_DEL_DOMAIN(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_DOMAIN_TO_DEL_DOMAIN":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_DOMAIN_TO_DEL_DOMAIN(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_MAC_TO_DEL_DOMAIN":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_MAC_TO_DEL_DOMAIN(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_MAC_TO_P2P":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_MAC_TO_P2P(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_MAC_TO_INSTANTPW":

					requestKey = (String) request.get("auth");

					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_MAC_TO_INSTANTPW(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_REMOTEAS_ASCODE":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_REMOTEAS_ASCODE(request, response);
					} else {
						response.put("msg", "서버 인증에 실패하였습니다.");
						result = "nopermission";
					}
					break;
				case "KTTDDNS_REMOTEAS_CLOSE":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_REMOTEAS_CLOSE(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_CHECK_ASCODE":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_CHECK_ASCODE(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_DEVICE_CMD_URL":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_DEVICE_CMD_URL(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_CHECK_DEVKEY":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_CHECK_DEVKEY(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_DEVICE_PLAY_URL":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_DEVICE_PLAY_URL(request, response);
					} else {
						response.put("msg", "인증에 실패되었습니다.");
						result = "nopermission";
					}
					break;
				case "KTTDDNS_SERVICENO_TO_DEVICE_PLAY_URL":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_SERVICENO_TO_DEVICE_PLAY_URL(request, response);
					} else {
						response.put("msg", "인증에 실패되었습니다.");
						result = "nopermission";
					}
					break;
				case "KTTDDNS_CHECK_DEVPLAYKEY":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_CHECK_DEVPLAYKEY(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_DEVICE_RESET_PW_URL":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_DEVICE_RESET_PW_URL(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_OTP_LOG":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_OTP_LOG(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_OTP_LOG2":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_OTP_LOG2(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_MAC_TO_SERVICE":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_MAC_TO_SERVICE(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_MAC_SERVICEOPEN":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_MAC_SERVICEOPEN(request, response);
					else
						result = "nopermission";
					break;
				case "KTTDDNS_SERVICE_APP_REGISTER":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_SERVICE_APP_REGISTER(request, response);
					} else {
						response.put("msg", "인증 실패 하였습니다.");
						result = "nopermission";
					}
					break;
				case "KTTDDNS_SERVICE_REGISTER":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_SERVICE_REGISTER(request, response);
					} else {
						response.put("msg", "인증 실패 하였습니다.");
						result = "nopermission";
					}
					break;
				case "KTTDDNS_SERVICE_CHECK_OPEN":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_SERVICE_CHECK_OPEN(request, response);
					} else {
						response.put("msg", "인증 실패 하였습니다.");
						result = "nopermission";
					}
					break;
				case "KTTDDNS_SERVICE_DEVICEINFO":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_SERVICE_DEVICEINFO(request, response);
					} else {
						response.put("msg", "인증 실패 하였습니다.");
						result = "nopermission";
					}
					break;
				case "KTTDDNS_SERVICE_UNREGISTER":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_SERVICE_UNREGISTER(request, response);
					} else {
						response.put("msg", "인증 실패 하였습니다.");
						result = "nopermission";
					}
					break;
				case "KTTDDNS_SERVICE_DELETE_DEVICE":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_SERVICE_DELETE_DEVICE(request, response);
					} else {
						response.put("msg", "인증 실패 하였습니다.");
						result = "nopermission";
					}
					break;
				case "KTTDDNS_SERVICE_DELETE_MAC":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_SERVICE_DELETE_MAC(request, response);
					} else {
						response.put("msg", "인증 실패 하였습니다.");
						result = "nopermission";
					}
					break;
				case "KTTDDNS_SERVICE_SERVICEOPEN":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_SERVICE_SERVICEOPEN(request, response);
					} else {
						response.put("msg", "인증 실패 하였습니다.");
						result = "nopermission";
					}
					break;
				case "KTTDDNS_COMMODITY_LIST":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_COMMODITY_LIST(request, response);
					} else {
						response.put("msg", "인증 실패 하였습니다.");
						result = "nopermission";
					}
					break;
				case "KTTDDNS_SERVICENO_TO_DEVICE_PLAY_URL_CMS":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_SERVICENO_TO_DEVICE_PLAY_URL_CMS(request, response);
					} else {
						response.put("msg", "인증 실패 하였습니다.");
						result = "nopermission";
					}
					break;
				// HISTORY 2022-12-02 사용자 관리기능을 위한 로그 관련 기능을 추가함.
				case "KTTDDNS_CLIENT_ACCESSLOG":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = true;
						result = KTTDDNS_CLIENT_ACCESSLOG(request, response);
					} else {
						response.put("msg", "인증 실패 하였습니다.");
						result = "nopermission";
					}
					break;
				case "KTTDDNS_INFECTION_LOG": // INFECTION 발생 시 로그 처리 용도
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = false;
						result = KTTDDNS_INFECTION_LOG(request, response);
					} else {
						// response.put("msg", "인증 실패 하였습니다.");
						response.clear();
						result = "nopermission";

						// result = "fail";
					}
					break;
				// DMS SNMP 업그레이드 시 장비가 수신 한 FTP 주소에 대한 검증
				case "KTTDDNS_CHECK_IP_VALIDATION":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey)) {
						mustMsg = false;
						result = KTTDDNS_CHECK_IP_VALIDATION(request, response);
					} else {
						// response.put("msg", "인증 실패 하였습니다.");

						response.clear();
						result = "nopermission";
					}
				default:
					break;
				//case "KTTDDNS_SERVICENO_TO_OTPLIST":
					case "KTTDDNS_SERVICENO_TO_UNOTPLIST":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_SERVICENO_TO_UNOTPLIST(request, response);
					else
						result = "nopermission";
					break;
					
				case "KTTDDNS_SERVICENO_TO_UNOTPLIST_1HOUR":
					requestKey = (String) request.get("auth");
					if (Encryptions.isAvailableKey(command, requestKey))
						result = KTTDDNS_SERVICENO_TO_UNOTPLIST_1HOUR(request, response);
					else
						result = "nopermission";
					break;
				}
			} catch (Exception e) {

				if (mustMsg && response.containsKey("msg") == false) {
					response.put("msg", "서버에 오류가 발생하였습니다.");
				}
				if (logger.isDebugEnabled()) {
					e.printStackTrace();
					logger.debug(e.getLocalizedMessage());
				}
				// System.out.println(e);
			}
		}
		response.put("return", result);
		// System.out.println(new Date() + " DDNS API RESPONSE : " +
		// response.toString());
		logger.info("DDNS API RESPONSE {}", response.toString());
		return response;
	}

	public String KTTDDNS_SYSID_TO_DEVICEINFO(Map<String, Object> request, Map<String, Object> response) {
		@SuppressWarnings("unchecked")
		ArrayList<String> sysidList = (ArrayList<String>) request.get("sysidList");
		String empNo = (String) request.get("empNo");

		if (sysidList == null || empNo == null || empNo.length() == 0) {
			response.put("msg", "잘못된 파라미터입니다.");
			return "fail";
		}

		if (sysidList.size() == 0) {
			List<Map<String, Object>> deviceList = new ArrayList<>();
			response.put("deviceList", deviceList);
			return "success";
		}

		String sysidStrings = "";
		for (int i = 0; i < sysidList.size(); i++) {
			String sysid = sysidList.get(i);
			if (sysid.length() == 0)
				continue;
			if (sysidStrings.length() == 0)
				sysidStrings += ("'" + sysid + "'");
			else
				sysidStrings += (", '" + sysid + "'");
		}

		if (sysidStrings.length() == 0) {
			List<Map<String, Object>> deviceList = new ArrayList<>();
			response.put("deviceList", deviceList);
			return "success";
		}

		List<Map<String, Object>> deviceListOrg = apiService.selectDeviceInSysidForDeviceinfo(sysidStrings);
		List<Map<String, Object>> deviceList = new ArrayList<>();
		for (Map<String, Object> deviceItem : deviceListOrg) {
			if ((int) deviceItem.get("domainType") == 20 || (int) deviceItem.get("domainType") == 21)
				continue;
			deviceList.add(deviceItem);
		}

		apiService.getDeviceListP2pInfo(deviceList);

		if (deviceList.size() > 0)
			response.put("msg", "장비 조회 성공");
		else
			response.put("msg", "장비 FW를 2.0.0 이상으로 업그레이드 한 뒤 System ID 등록하고, 도메인을 telecopview로 변경해주세요.");

		response.put("deviceList", deviceList);
		return "success";
	}

	public String KTTDDNS_SERVICENO_TO_OSS_DEVICEINFO(Map<String, Object> request, Map<String, Object> response) {
		String serviceNo = (String) request.get("serviceNo");
		String empNo = (String) request.get("empNo");

		if (serviceNo == null || serviceNo.length() == 0 || empNo == null || empNo.length() == 0) {
			response.put("msg", "잘못된 파라미터입니다.");
			return "fail";
		}

		List<Map<String, Object>> deviceListOrg = apiService.selectDeviceWhereServicenoForOss(serviceNo);
		List<Map<String, Object>> deviceList = new ArrayList<>();
		for (Map<String, Object> deviceItem : deviceListOrg) {
			if ((int) deviceItem.get("domainType") == 20 || (int) deviceItem.get("domainType") == 21)
				continue;
			deviceList.add(deviceItem);
		}

		if (deviceList.size() > 0)
			response.put("msg", "장비 조회 성공");
		else
			response.put("msg", "OSS에서 장비를 등록하고, 장비 F/W를 최신버전으로 업그레이드 하신 뒤, AS Code 발급이 가능합니다.");

		response.put("deviceList", deviceList);
		return "success";
	}

	public String KTTDDNS_SYSID_TO_DEVICEINFO2(Map<String, Object> request, Map<String, Object> response) {
		@SuppressWarnings("unchecked")
		ArrayList<String> sysidList = (ArrayList<String>) request.get("sysidList");

		if (sysidList == null)
			return "fail";

		if (sysidList.size() == 0) {
			List<Map<String, Object>> deviceList = new ArrayList<>();
			response.put("deviceList", deviceList);
			return "success";
		}

		String sysidStrings = "";
		for (int i = 0; i < sysidList.size(); i++) {
			String sysid = sysidList.get(i);
			if (sysid.length() == 0)
				continue;
			if (sysidStrings.length() == 0)
				sysidStrings += ("'" + sysid + "'");
			else
				sysidStrings += (", '" + sysid + "'");
		}

		if (sysidStrings.length() == 0) {
			List<Map<String, Object>> deviceList = new ArrayList<>();
			response.put("deviceList", deviceList);
			return "success";
		}

		List<Map<String, Object>> deviceListOrg = apiService.selectDeviceInSysidForDeviceinfo2(sysidStrings);
		List<Map<String, Object>> deviceList = new ArrayList<>();
		for (Map<String, Object> deviceItem : deviceListOrg) {
			if ((int) deviceItem.get("domainType") == 20 || (int) deviceItem.get("domainType") == 21)
				continue;
			deviceList.add(deviceItem);
		}

		apiService.getDeviceListP2pInfo(deviceList);

		for (Map<String, Object> deviceItem : deviceList) {
			String noColonMac = (String) deviceItem.get("macAddress");
			deviceItem.put("masterKey", Encryptions.encryptMasterkey(Encryptions.getSHA(noColonMac + "KTT_MASTER", 1)));
		}
		response.put("deviceList", deviceList);
		return "success";
	}

	  public String KTTDDNS_SERVICENO_TO_APP_DEVICEINFO(Map<String, Object> request, Map<String, Object> response) {
		    ArrayList<String> serviceNoList = (ArrayList<String>)request.get("serviceNoList");
		    if (serviceNoList == null)
		      return "fail"; 
		    if (serviceNoList.size() == 0) {
		      List<Map<String, Object>> list = new ArrayList<>();
		      response.put("deviceList", list);
		      return "success";
		    } 
		    String serviceNoListString = "";
		    for (int i = 0; i < serviceNoList.size(); i++) {
		      String serviceNo = serviceNoList.get(i);
		      if (serviceNo.length() != 0)
		        if (serviceNoListString.length() == 0) {
		          serviceNoListString = String.valueOf(serviceNoListString) + "'" + serviceNo + "'";
		        } else {
		          serviceNoListString = String.valueOf(serviceNoListString) + ", '" + serviceNo + "'";
		        }  
		    } 
		    if (serviceNoListString.length() == 0) {
		      List<Map<String, Object>> list = new ArrayList<>();
		      response.put("deviceList", list);
		      return "success";
		    } 
		    List<Map<String, Object>> deviceListOrg = this.apiService.selectDeviceWhereInServicenoForApp(serviceNoListString);
		    List<Map<String, Object>> deviceList = new ArrayList<>();
		    for (Map<String, Object> deviceItem : deviceListOrg) {

		    	//231214 sjlee xml add otp_yn != 0  
		    	if ((((Integer)deviceItem.get("domainType")).intValue() == 20 || 
		    		  ((Integer)deviceItem.get("domainType")).intValue() == 21))
		    		continue; 
		      deviceList.add(deviceItem);
		   
		    }
		    for (Map<String, Object> deviceItem : deviceList) {
		      String noColonMac = (String)deviceItem.get("macAddress");
		      
		      deviceItem.put("masterKey", Encryptions.encryptMasterkey(Encryptions.getSHA(String.valueOf(noColonMac) + "KTT_MASTER", 1)));
		    } 
		    response.put("deviceList", deviceList);
		    return "success";
		  }
	
	
	/**
	 * 
	 * @Method Name : KTTDDNS_SERVICENO_TO_APP_DEVICEINFO
	 * @작성일 : 2022. 11. 25.
	 * @작성자 : Foryoucom
	 * @변경이력 : -2022-11-25 OTP_YN기능 추가 -- 변경 : OTP_YN 컬럼이 1 이상인 경우에만 장치 리스트를 응답한다.
	 * @Method 설명 :고객통합앱 서버에서 SytemID를 이용하여 영상장비 정보를 얻어오는 기능 (C)
	 * @param request
	 * @param response
	 * @return 류수석코드
	 */
	/**
	 * SW 주장치 연동 및 영상 시스템 고도화 개발 시방서 V3 (002) 에 따라 OTP_YN 이 1 인 경우와 register_type이
	 * 0~2 이면서 servie_open - 2이상인 장비에 대하여 장비 리스트를 응답 해당 장비 펌웨어 버전이 OTP 일괄인증을 지원하는
	 * 경우에는 해당 정보를 같이 반영한다.
	 */

	public String KTTDDNS_SERVICENO_TO_APP_DEVICEINFO_1(Map<String, Object> request, Map<String, Object> response) {
		@SuppressWarnings("unchecked")

		final ArrayList<String> serviceNoList = (ArrayList<String>) request.get("serviceNoList");
		final Boolean ignoreOTPCondition = request.containsKey("ignoreOTP")
				? Boolean.valueOf(request.get("ignoreOTP").toString())
				: Boolean.FALSE;

		// logger.info("ignoreOTP : "+ ignoreOTPCondition);

		if (serviceNoList == null) {
			return "fail";
		}
		if (serviceNoList.size() == 0) {
			List<Map<String, Object>> deviceList = new ArrayList<>();
			response.put("deviceList", deviceList);
			deviceList = null;
			return "success";
		}
		/*
		 * String serviceNoListString = ""; for (int i = 0; i < serviceNoList.size();
		 * i++) { String serviceNo = serviceNoList.get(i); if (serviceNo.length() == 0)
		 * continue; if (serviceNoListString.length() == 0) serviceNoListString += ("'"
		 * + serviceNo + "'"); else serviceNoListString += (", '" + serviceNo + "'"); }
		 * if (serviceNoListString.length() == 0) { List<Map<String, Object>> deviceList
		 * = new ArrayList<>(); response.put("deviceList", deviceList); deviceList =
		 * null; return "success"; }
		 * 
		 */
		StringBuffer serviceNoListString = new StringBuffer(1024);
		try {
			for (String serviceNo : serviceNoList) {
				if (serviceNo.length() > 7) {

					serviceNoListString.append("'");
					serviceNoListString.append(serviceNo);
					serviceNoListString.append("',");
				}

				// System.out.println(serviceNoListString);
			}
			// 마지막 ,(쉼표)제거
			serviceNoListString.delete(serviceNoListString.toString().length() - 1,
					serviceNoListString.toString().length());

			if (serviceNoListString.toString().length() == 0) {
				List<Map<String, Object>> deviceList = new ArrayList<>();
				response.put("deviceList", deviceList);
				deviceList = null;
				return "success";
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				e.printStackTrace();
		}

		// List<Map<String, Object>> deviceListOrg =
		// apiService.selectDeviceWhereInServicenoForApp(serviceNoListString);
		// System.out.println(serviceNoListString);
		/// HISTORY 추가 - selectDeviceWhereInServicenoForApp 수행 기록을 서버에 남기기 위하여 해당 필드에
		// WITH구문을 추가하여 쿼리를 다중으로 처리하도록 함.
		List<Map<String, Object>> deviceListOrg = apiService
				.selectDeviceWhereInServicenoForApp(serviceNoListString.toString());
		// 위 기능을 위하여 해당 함수도 일부 수정

		/*
		 * 이전 코드 oty_yn을 확인하지 않는 코드 final List<Map<String, Object>> deviceList = new
		 * ArrayList<>(); for(Map<String, Object> deviceItem : deviceListOrg) { int
		 * domainType = (int) deviceItem.get("domainType"); int otp_yn = (int)
		 * deviceItem.get("otp_yn"); if (domainType == 20 || (int) domainType == 21)
		 * continue; // 2022-11-30일 전에는 아래 조건 판단이 없이 리스트를 전달하였음. if (otp_yn <= 0 ||
		 * otp_yn > 3) // otp_yn : 사용안함(0), 기존사용자(1) , 관리자 변경 (2) , 인증 완료(3) //
		 * continue;
		 * 
		 * deviceList.add(deviceItem); } for (Map<String, Object> deviceItem :
		 * deviceList) { String noColonMac = (String) deviceItem.get("macAddress");
		 * deviceItem.put("masterKey",
		 * Encryptions.encryptMasterkey(Encryptions.getSHA(noColonMac + "KTT_MASTER",
		 * 1))); }
		 */
		// 변경 코드 otp_yn을 확인하는 코드
		if (deviceListOrg != null) {
			final List<Map<String, Object>> deviceList = new ArrayList<>();
			deviceListOrg.parallelStream().forEach(deviceItem -> {

				if (deviceItem != null && deviceItem.containsKey("domainType")) {
					int domainType = (int) deviceItem.get("domainType");
					if ((domainType == 22 || domainType == 23)) // 'octdvr.co.kr' 20 'octnvr.co.kr' 21
					{
						int otp_yn = deviceItem.containsKey("otp_yn") ? (int) deviceItem.get("otp_yn") : 0;
						int service_open = deviceItem.containsKey("service_open") ? (int) deviceItem.get("service_open")
								: 0;
						int register_type = deviceItem.containsKey("register_type")
								? (int) deviceItem.get("register_type")
								: -1;

						Boolean b = (otp_yn > 0 && otp_yn <= 3) || ignoreOTPCondition; // OTP YN CHECK

						b = b || (service_open == 2
								&& (register_type == 0 || register_type == 1 || register_type == 2));// resister_type
																										// check

						if (b) {
							StringBuffer noColonMac = new StringBuffer();

							if (deviceItem.containsKey("macAddress")) {
								// String noColonMac = (String) deviceItem.get("macAddress");
								noColonMac.append(String.valueOf(deviceItem.get("macAddress"))).append("KTT_MASTER");
								deviceItem.put("masterKey",
										Encryptions.encryptMasterkey(Encryptions.getSHA(noColonMac.toString(), 1)));
							} else {
								deviceItem.put("masterKey", "");
							}
							if (deviceItem.containsKey("DeviceVer")) {
								String strDeviceVer = String.valueOf(deviceItem.get("DeviceVer"));
								int tDeviceVer = apiService.verString2Int(strDeviceVer);
								if (tDeviceVer != 0 && tDeviceVer >= apiService.verString2Int("V3.2.0")) {
									deviceItem.put("otp_batch_authentication_avaiable", true);
								} else {
									// FROM DB
									int tDeviceOtpAvailableVer = deviceItem.containsKey("last_ver")
											? apiService.verString2Int(deviceItem.get("last_ver").toString())
											: -1;

									if (tDeviceVer != 0 && tDeviceOtpAvailableVer > -1) {
										deviceItem.put("otp_batch_authentication_avaiable", true);


									} else {
										deviceItem.put("otp_batch_authentication_avaiable", false);

									}
									/*
									 * String strModelName = deviceItem.containsKey("modelName") ?
									 * deviceItem.get("modelName").toString(): ""; if(tDeviceVer != 0 &&
									 * strModelName.length() > 0){IniEditor manual_notice_list = new IniEditor();
									 * RequestAttributes requestAttributes =
									 * RequestContextHolder.getRequestAttributes(); HttpServletRequest
									 * httpServletRequest = ((ServletRequestAttributes)
									 * requestAttributes).getRequest(); String OTP_B_A_INI =
									 * httpServletRequest.getServletContext().getRealPath("opt_b_a.ini");
									 * System.out.println("otp_batch_authentication_avaiable" + OTP_B_A_INI); File
									 * file = new File(OTP_B_A_INI); if(file.exists()) {try
									 * {manual_notice_list.load(file); String fwAvailbleVer =
									 * manual_notice_list.get(strModelName, "fwver"); if(fwAvailbleVer.length()>0 &&
									 * tDeviceVer >= apiService.verString2Int(fwAvailbleVer.trim()))
									 * {deviceItem.put("otp_batch_authentication_avaiable", true);} } catch
									 * (IOException e) {deviceItem.put("otp_batch_authentication_avaiable", false);}
									 * }else{deviceItem.put("otp_batch_authentication_avaiable", true);}} else
									 * {deviceItem.put("otp_batch_authentication_avaiable", false);}
									 */
								}
							}
							deviceList.add(deviceItem);
							noColonMac = null;
						}
					}
				}
			});

			response.put("deviceList", deviceList);
		} else {
			response.put("deviceList", new ArrayList<>());
		}
		// serviceNoListString = null;
		// deviceList = null;

		return "success";
	}

	public String KTTDDNS_SYSID_TO_DEVICEINFO3(Map<String, Object> request, Map<String, Object> response) {
		@SuppressWarnings("unchecked")
		ArrayList<String> sysidList = (ArrayList<String>) request.get("sysidList");

		if (sysidList == null)
			return "fail";

		if (sysidList.size() == 0) {
			List<Map<String, Object>> deviceList = new ArrayList<>();
			response.put("deviceList", deviceList);
			return "success";
		}

		String sysidStrings = "";
		for (int i = 0; i < sysidList.size(); i++) {
			String sysid = sysidList.get(i);
			if (sysid.length() == 0)
				continue;
			if (sysidStrings.length() == 0)
				sysidStrings += ("'" + sysid + "'");
			else
				sysidStrings += (", '" + sysid + "'");
		}

		if (sysidStrings.length() == 0) {
			List<Map<String, Object>> deviceList = new ArrayList<>();
			response.put("deviceList", deviceList);
			return "success";
		}

		List<Map<String, Object>> deviceList = apiService.selectDeviceInSysidForDeviceinfo3(sysidStrings);
		response.put("deviceList", deviceList);
		return "success";
	}

	public String KTTDDNS_SERVICENO_TO_CS_DEVICEINFO(Map<String, Object> request, Map<String, Object> response) {
		@SuppressWarnings("unchecked")
		ArrayList<String> serviceNoList = (ArrayList<String>) request.get("serviceNoList");

		if (serviceNoList == null)
			return "fail";

		if (serviceNoList.size() == 0) {
			List<Map<String, Object>> deviceList = new ArrayList<>();
			response.put("deviceList", deviceList);
			return "success";
		}

		String serviceNoListString = "";
		for (int i = 0; i < serviceNoList.size(); i++) {
			String serviceNo = serviceNoList.get(i);
			if (serviceNo.length() == 0)
				continue;
			if (serviceNoListString.length() == 0)
				serviceNoListString += ("'" + serviceNo + "'");
			else
				serviceNoListString += (", '" + serviceNo + "'");
		}

		if (serviceNoListString.length() == 0) {
			List<Map<String, Object>> deviceList = new ArrayList<>();
			response.put("deviceList", deviceList);
			return "success";
		}

		List<Map<String, Object>> deviceList = apiService.selectDeviceWhereInServicenoForCs(serviceNoListString);
		response.put("deviceList", deviceList);
		return "success";
	}

	@SuppressWarnings("unchecked")
	public String KTTDDNS_DOMAIN_TO_DEVICEINFO(Map<String, Object> request, Map<String, Object> response) {

		ArrayList<String> domainList = (ArrayList<String>) request.get("domain");
		// System.out.println("\n"+domainList.size());
		if (domainList.size() == 0) {
			List<Map<String, Object>> deviceList = new ArrayList<Map<String, Object>>();
			response.put("deviceList", deviceList);
			return "success";
		}

		String domainListString = "";
		for (int i = 0; i < domainList.size(); i++) {
			String domain = (domainList.get(i)).toLowerCase();
			if (domain.length() == 0)
				continue;
			if (domainListString.length() == 0)
				domainListString += ("'" + domain + "'");
			else
				domainListString += (", '" + domain + "'");
		}

		if (domainListString.length() == 0) {
			List<Map<String, Object>> deviceList = new ArrayList<Map<String, Object>>();
			response.put("deviceList", deviceList);
			return "success";
		}
		// System.out.println(domainListString);
		List<Map<String, Object>> deviceList = apiService.selectDeviceWhereInDomain(domainListString);
		response.put("deviceList", deviceList);
		return "success";
	}

	/*
	 * public String KTTDDNS_DOMAIN_TO_DEVICEINFO(Map<String, Object> request,
	 * Map<String, Object> response) {
	 * 
	 * @SuppressWarnings("unchecked") ArrayList<String> domainList =
	 * (ArrayList<String>)request.get("domain");
	 * 
	 * if(domainList == null) return "fail";
	 * 
	 * List<Map<String, Object>> deviceList = new ArrayList<>();
	 * 
	 * for(int i = 0; i < domainList.size(); i++) { Map<String, Object> device =
	 * apiService.selectDeviceWhereDomain((domainList.get(i)).toLowerCase());
	 * 
	 * if(device == null) { Map<String, Object> nDevice = new HashMap<>();
	 * nDevice.put("fullDomain", (domainList.get(i)).toLowerCase());
	 * nDevice.put("p2pUid", ""); nDevice.put("p2pPriority", 0);
	 * nDevice.put("macAddress", ""); deviceList.add(i, nDevice); } else { String
	 * noColonMac = device.get("macAddress").toString(); P2pregister p2pregister =
	 * apiService.getP2p(noColonMac); device.put("p2pUid",
	 * p2pregister.getP2p_uid()); device.put("p2pPriority",
	 * p2pregister.getP2p_priority()); deviceList.add(i, device); } }
	 * 
	 * response.put("deviceList", deviceList); return "success"; }
	 */

	public String KTTDDNS_MAC_TO_DEVICEINFO(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");

		if (macAddress == null)
			return "fail";

		String apiMac = Encryptions.remakeMac(macAddress, false);
		if (apiMac.length() != 12)
			return "fail";
		String ddnsMac = Encryptions.remakeMac(macAddress, true);

		Map<String, Object> device = apiService.selectDeviceWhereMac(ddnsMac);
		if (device == null)
			return "noregist";

		response.put("device", device);
		return "success";
	}
	/*
	 * public String KTTDDNS_MAC_TO_MASTERKEY(Map<String, Object> request,
	 * Map<String, Object> response) { String macAddress =
	 * (String)request.get("macAddress");
	 * 
	 * if(macAddress == null) return "fail";
	 * 
	 * String apiMac = Encryptions.remakeMac(macAddress, false); if(apiMac.length()
	 * != 12) return "fail"; String ddnsMac = Encryptions.remakeMac(macAddress,
	 * true);
	 * 
	 * HttpServletRequest httpServletRequest =
	 * ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).
	 * getRequest(); String clientIp = apiService.getIp(httpServletRequest); String
	 * serverIp = apiService.selectDevicePublicIpWhereMac(ddnsMac);
	 * 
	 * if(!clientIp.equals(serverIp)) return "nomatchip";
	 * 
	 * String masterKey = Encryptions.getSHA(apiMac + "KTT_MASTER", 1);
	 * response.put("masterKey", Encryptions.encryptMasterkey(masterKey)); return
	 * "success"; } /
	 */
	// selectDevicePublicIpWhereMac -> service user 가 1인 경우에 한정하여 발급

	
	public String KTTDDNS_MAC_TO_MASTERKEY(Map<String, Object> request, Map<String, Object> response) {
	    String macAddress = (String)request.get("macAddress");
	    
	    String access_rule;
	    if (macAddress == null)
	      return "fail"; 
	    String apiMac = Encryptions.remakeMac(macAddress, false);
	    if (apiMac.length() != 12)
	      return "fail"; 
	    String ddnsMac = Encryptions.remakeMac(macAddress, true);
	    logger.debug("ddnsMac" + ddnsMac);
	    HttpServletRequest httpServletRequest = (
	      (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	    String clientIp = this.apiService.getIp(httpServletRequest);
	    String serverIp = this.apiService.selectDevicePublicIpWhereMac(ddnsMac);
	     access_rule = this.apiService.selectDevicePublicIpWhereMac_accessrule(ddnsMac);

	    if (!clientIp.equals(serverIp))
	      return "nomatchip"; 
	    if(access_rule == null)
	    {
	    	access_rule = "0";
	    }
	    String masterKey = Encryptions.getSHA(String.valueOf(apiMac) + "KTT_MASTER", 1);
	    response.put("masterKey", Encryptions.encryptMasterkey(masterKey));
		response.put("access_rule", access_rule.toString());
	    return "success";
	  }
	/*20231208 류수석님혼자개발한거라 원래 상용코드로 원복 + accrule만 추가
	public String KTTDDNS_MAC_TO_MASTERKEY_1(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");
		try {
			if (macAddress == null) {
				return "fail";
			}

			String apiMac = Encryptions.remakeMac(macAddress, false);
			if (apiMac.length() != 12) {
				return "fail";
			}

			String ddnsMac = Encryptions.remakeMac(macAddress, true);
			String serverIp = "0.0.0.0";
			Integer access_rule = 0;

			List<Map<String, Object>> map = null;
			HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder
					.currentRequestAttributes()).getRequest();

			String clientIp = apiService.getIp(httpServletRequest);

			if (request.containsKey("white_ip")) {
				String ip = request.get("white_ip").toString().trim();

				Matcher m1 = this.VALID_IPV4_PATTERN.matcher(ip);
				Matcher m12 = this.VALID_IPV6_PATTERN1.matcher(ip);
				Matcher m22 = this.VALID_IPV6_PATTERN2.matcher(ip);

				if (m1.matches() == true || (m12.matches() != false || m22.matches() != false)) {
					map = apiService.selectDevicePublicIpWhereMac(ddnsMac, ip, 0);
				}
			} else {
				map = apiService.selectDevicePublicIpWhereMac(ddnsMac, "127.0.0.1", 0);
			}
			// System.out.println("service_user : "+ map.get(0).get("service_user"));
			// System.out.println("service_user : "+ map.get(0).keySet());
			


			 if (map != null && map.size() > 0 && map.get(0).get("service_user") != null
			 && map.get(0).get("service_user").toString().equals("1"))
			{
				serverIp = map.get(0).get("addr").toString();
				access_rule = Integer.valueOf(map.get(0).get("access_rule").toString());
				logger.info("access_rule 0~2 : " + access_rule);
			} else {
				return "nomatchip";
			}
			serverIp = map.get(0).get("addr").toString();

			// String serverIp = apiService.selectDevicePublicIpWhereMac(ddnsMac); //<==
			if (serverIp == null || serverIp.length() == 0) {
				response.put("access_rule", new String("-1"));
				logger.info("access_rule : " + response);
			}

			if (!clientIp.trim().equals(serverIp.trim())) {
				response.put("access_rule", new String("-2"));
				logger.info("access_rule : " + response);
			}
			
			if (!clientIp.equals(serverIp))
			{
			      return "nomatchip"; 
			}
			 	
			String masterKey = Encryptions.getSHA(apiMac + "KTT_MASTER", 1);
			access_rule = Integer.valueOf(map.get(0).get("access_rule").toString());
			response.put("masterKey", Encryptions.encryptMasterkey(masterKey));
			response.put("access_rule", access_rule.toString());
			logger.info("return response : " + response);

			return "success";
		} catch (Exception e) {
			response.clear();
			return "fail";
		}
	}
	 */
	public String KTTDDNS_MAC_TO_P2PUID(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");

		if (macAddress == null)
			return "fail";

		String apiMac = Encryptions.remakeMac(macAddress, false);
		if (apiMac.length() != 12)
			return "fail";
		String ddnsMac = Encryptions.remakeMac(macAddress, true);
		try {
			Map<String, Object> device = apiService.selectDeviceP2pWhereMac2(ddnsMac);

			if (device.get("p2pUid").toString().length() > 0 && (int) device.get("p2pDevice") == 0) {
				if (!apiService.updateDeviceP2pdeviceWhereMac(ddnsMac))
					return "fail";
			}

			response.put("p2pUid", device.get("p2pUid"));
		} catch (Exception e) {
			response.put("p2pUid", "");
		}
		return "success";
	}

	public String KTTDDNS_REG_P2P(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");
		Integer p2pPriority = (Integer) request.get("p2pPriority");

		if (macAddress == null || p2pPriority == null)
			return "fail";

		String apiMac = Encryptions.remakeMac(macAddress, false);
		if (apiMac.length() != 12)
			return "fail";
		String ddnsMac = Encryptions.remakeMac(macAddress, true);

		Map<String, Object> device = apiService.selectDeviceP2pWhereMac(ddnsMac);
		if (device == null)
			return "fail";

		if (device.get("p2pUid").toString().length() == 0) {
			String p2pUid = "";
			p2pUid = p2preservedService.selectP2preservedWhereHistorymac(ddnsMac);
			if (p2pUid == null || p2pUid.length() == 0) {
				p2pUid = p2preservedService.selectP2preservedWhereUsed0();
				if (p2pUid == null || p2pUid.length() == 0)
					return "nop2puid";
			}

			if (!apiService.updateDeviceP2pWhereMac(ddnsMac, p2pUid, p2pPriority))
				return "fail";
			if (!p2preservedService.updateP2preserved(p2pUid, ddnsMac)) {
				apiService.updateDeviceP2pWhereMac(ddnsMac, "", 0);
				return "fail";
			}
		} else {
			if (!apiService.updateDeviceP2pWhereMac(ddnsMac, device.get("p2pUid").toString(), p2pPriority))
				return "fail";
		}

		response.put("macAddress", apiMac);
		return "success";
	}

	public String KTTDDNS_SYSID_TO_DEL_DOMAIN(Map<String, Object> request, Map<String, Object> response) {
		String sysid = (String) request.get("sysid");

		if (sysid == null || sysid.length() != 13)
			return "fail";

		if (!apiService.deleteDeviceWhereSysid(sysid))
			return "fail";

		response.put("sysid", sysid);
		return "success";
	}

	public String KTTDDNS_DOMAIN_TO_DEL_DOMAIN(Map<String, Object> request, Map<String, Object> response) {
		String domain = (String) request.get("domain");

		if (domain == null)
			return "fail";

		if (!apiService.deleteDeviceWhereDomain(domain.toLowerCase()))
			return "fail";

		response.put("fullDomain", domain.toLowerCase());
		return "success";
	}

	public String KTTDDNS_MAC_TO_DEL_DOMAIN(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");

		if (macAddress == null)
			return "fail";

		String apiMac = Encryptions.remakeMac(macAddress, false);
		if (apiMac.length() != 12)
			return "fail";
		String ddnsMac = Encryptions.remakeMac(macAddress, true);

		if (!apiService.deleteDeviceWhereMac(ddnsMac))
			return "fail";

		response.put("macAddress", macAddress);
		return "success";
	}

	public String KTTDDNS_MAC_TO_P2P(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");

		if (macAddress == null)
			return "fail";

		String apiMac = Encryptions.remakeMac(macAddress, false);
		if (apiMac.length() != 12)
			return "fail";
		String ddnsMac = Encryptions.remakeMac(macAddress, true);

		response.put("p2p", apiService.selectDeviceP2pWhereMac(ddnsMac));
		return "success";
	}

	/**
	 * 
	 * @Method Name : KTTDDNS_MAC_TO_INSTANTPW
	 * @작성일 : 2022. 11. 30.
	 * @작성자 : Foryoucom
	 * @변경이력 : 2022.11.30 dB서버 내 MAC 주소가 있는지 검사 및 있는 경우 해당 장비의 버전을 확인하여 그에 따라서 OTP
	 *       연산을 수행
	 * @Method 설명 :
	 * @param request
	 * @param response
	 * @return
	 */
	public String KTTDDNS_MAC_TO_INSTANTPW(Map<String, Object> request, Map<String, Object> response) {

		if (request.containsKey("macAddress") == false || request.containsKey("staffId") == false) {
			// logger.debug("MAC : NULL, stattfId : NULL");
			return "fail";
		}
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
		String DeprecatedCheckFile = httpServletRequest.getServletContext().getRealPath("DeprecatedCheckFile");
		// System.out.println("DeprecatedCheckFile " + DeprecatedCheckFile);
		// //DeprecatedCheckFile
		// /usr/local/tomcat_ddnsApi/webapps/kttddnsapi/DeprecatedCheckFile

		File file = new File(DeprecatedCheckFile);
		String macAddress = (String) request.get("macAddress");
		String staffId = (String) request.get("staffId");
		boolean isDeprecatedOldFunction = file.exists();

		/*
		 * if (macAddress == null || staffId == null) { //
		 * logger.debug("MAC : NULL, stattfId : NULL"); return "fail"; }
		 */
		String apiMac = Encryptions.remakeMac(macAddress, false);
		if (apiMac.length() != 12) {
			// logger.debug(apiMac);
			return "fail";
		}

		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		byte[] mac_num = new byte[6];
		mac_num[0] = (byte) Integer.parseInt(apiMac.substring(0, 2), 16);
		mac_num[1] = (byte) Integer.parseInt(apiMac.substring(2, 4), 16);
		mac_num[2] = (byte) Integer.parseInt(apiMac.substring(4, 6), 16);
		mac_num[3] = (byte) Integer.parseInt(apiMac.substring(6, 8), 16);
		mac_num[4] = (byte) Integer.parseInt(apiMac.substring(8, 10), 16);
		mac_num[5] = (byte) Integer.parseInt(apiMac.substring(10, 12), 16);
		mac_num[0] &= 0xFF;
		mac_num[1] &= 0xFF;
		mac_num[2] &= 0xFF;
		mac_num[3] &= 0xFF;
		mac_num[4] &= 0xFF;
		mac_num[5] &= 0xFF;

		String instantPw = null;
		if (request.containsKey("version") == false) {
			if (isDeprecatedOldFunction == false) {
				instantPw = apiService.ktt_cipher_makeInstantPasswd(year, month, day, hour, mac_num, 1); // use new
																											// style
																											// password
			} else {
				response.put("msg", "This function is deprecated without version descrption.");
			}
		} else {

			Map<String, Object> device = null;
			device = apiService.selectDeviceWhereMac4(Encryptions.remakeMac(macAddress, true));
			if (device != null) // MAC을 기준으로 조회가 된 경우
			{
				String msg = "";
				try {

					if (device.containsKey("last_ver") == false || device.containsKey("device_ver") == false) {
						msg = "모델을 확인할 수 없습니다.";
						response.put("msg", msg);
						return "fail";
					}

					String strLast_ver = device.get("last_ver") != null ? device.get("last_ver").toString() : "";
					String strDevice_ver = device.get("device_ver") != null ? device.get("device_ver").toString() : "";
					int fw_version = apiService.verString2Int(strDevice_ver); // 현재 장비가 사용하는 펌웨어의 버전
					if (apiService.verString2Int(strLast_ver) == 0 || fw_version == 0) {
						msg = "펌웨어 버전 표시가 잘못되었습니다.";
						response.put("msg", msg);
						return "fail";
					}
					if (fw_version <= apiService.verString2Int("V1.5.0")) // 1.5.0 이하인 경우
					{
						instantPw = apiService.ktt_cipher_makeInstantPasswd(year, month, day, hour, mac_num, 0); // use
																													// old
						// style
						// password
					} else {
						instantPw = apiService.ktt_cipher_makeInstantPasswd(year, month, day, hour, mac_num, 1); // use
						// new
						// style
						// password
					}
					response.put("fw_version", fw_version);
					device = null;
				} catch (Exception e) {
					// e.printStackTrace();
					response.put("msg", e.getLocalizedMessage());
					return "fail";
				}
			} else // MAC을 기준으로 조회가 되지 않은 경우
			{
				try {
					Object o = request.get("version");
					int version;
					if (o instanceof String) {
						version = Integer.parseInt(o.toString());
					} else if (o instanceof Integer) {
						version = (Integer) o;
					} else if (o instanceof Short) {
						version = ((Short) o);
					} else {
						version = -1;
					}

					switch (version) {
					case 1: // 신형 OTP
						instantPw = apiService.ktt_cipher_makeInstantPasswd(year, month, day, hour, mac_num, 1); // use
																													// new
																													// style
																													// password
						break;
					case 2: // 구형 OTP
						instantPw = apiService.ktt_cipher_makeInstantPasswd(year, month, day, hour, mac_num, 0); // use
																													// old
																													// style
																													// password
						break;
					default:
						response.put("msg", "version을 확인해주세요");
						return "fail";

					}
				} catch (Exception e) {
					logger.debug(e.getLocalizedMessage());
					return "fail";
				}
			}
		}
		if (instantPw != null)
			response.put("instantPw", instantPw);
		mac_num = null;
		return "success";
	}

	/**
	 * 
	 * @Method Name : KTTDDNS_REMOTEAS_ASCODE
	 * @작성일 : 2023. 1. 12.
	 * @작성자 : Foryoucom
	 * @변경이력 :
	 * @Method 설명 :
	 * @param request
	 * @param response
	 * @return //userLevel //0 : 영상앱에서 사용자가 호출할 때 //1 : OSS앱에서 KTT직원이 호출할 때
	 *         //userLevel 키가 없을때는 0으로 판단한다. (영상앱에서는 userLevel을 추가하지 않을 예정) //
	 */
	public String KTTDDNS_REMOTEAS_ASCODE(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");
		Integer userLevel = (Integer) request.get("userLevel");
		String empNo = (String) request.get("empNo");

		if (macAddress == null || empNo == null || empNo.length() == 0
				|| (userLevel != null && userLevel != 0 && userLevel != 1)) {
			response.put("msg", "잘못된 파라미터입니다.");
			return "fail";
		}

		if (userLevel == null)
			userLevel = 0;

		String apiMac = Encryptions.remakeMac(macAddress, false);
		if (apiMac.length() != 12) {
			response.put("msg", "잘못된 맥주소입니다.");
			return "fail";
		}

		Selfas oldSelfas = selfasService.selectSelfasWhereMacAndUserlevel(apiMac, userLevel);
		String certnum = Integer.toString(Encryptions.generateCertnum(8));
		if (oldSelfas == null) {
			Selfas newSelfas = new Selfas();
			newSelfas.setMac(apiMac);
			newSelfas.setCertnum(certnum);
			newSelfas.setUser_level(userLevel);
			if (!selfasService.insertSelfas(newSelfas)) {
				response.put("msg", "AS Code 생성에 실패하였습니다.");
				return "fail";
			}
		} else {
			oldSelfas.setCertnum(certnum);
			if (!selfasService.updateSelfasCertnum(oldSelfas)) {
				response.put("msg", "AS Code 생성에 실패하였습니다.");
				return "fail";
			}
		}

		response.put("asCode", certnum);
		response.put("macAddress", macAddress);
		response.put("msg", "[우클릭] -> [기타] -> [DMS]에서 AS Code를 입력해주세요. 입력창이 없을 경우 펌웨어를 V2.0.0 이상으로 업그레이드 해주세요.");
		return "success";
	}

	/**
	 * 
	 * @Method Name : KTTDDNS_REMOTEAS_CLOSE
	 * @작성일 : 2023. 1. 6.
	 * @작성자 : Foryoucom
	 * @변경이력 :
	 * @Method 설명 :요청 받은 원격 AS에 대하여 맥 주소를 기준으로 Self AS TABLE에서 삭제를 수행
	 * @param request
	 * @param response
	 * @return
	 */
	public String KTTDDNS_REMOTEAS_CLOSE(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");
		String empNo = (String) request.get("empNo");

		if (macAddress == null || empNo == null || empNo.length() == 0)
			return "fail";

		String apiMac = Encryptions.remakeMac(macAddress, false);
		if (apiMac.length() != 12)
			return "fail";

		if (!selfasService.deleteSelfasWhereMac(apiMac))
			return "fail";

		return "success";
	}

	/**
	 * 
	 * @Method Name : KTTDDNS_CHECK_ASCODE
	 * @작성일 : 2023. 1. 12.
	 * @작성자 : Foryoucom
	 * @변경이력 :
	 * @Method 설명 :
	 * @param request
	 * @param response
	 * @return
	 * 
	 * 		//isPrivate 이 0 이고 self_as(user_level) 이 1 이면 실패를 리턴한다.(fail)
	 *         isPrivate --> 0 : AS Code를 Public IP에서 사용(기본값) ,1 : AS Code를 Local
	 *         GUI 또는 Private IP에서 사용 //isPrivate 이 1 이고 self_as(user_level) 이 2이면
	 *         실패를 리턴한다.(fail) //isPrivate 이 0 이고 self_as(user_level) 이 2일 때 발급 시간이
	 *         10분(OTP유효시간)을 초과한 경우 발급 정보를 DB에서 삭제하고 실패를 리턴한다.(timeout)
	 */

	public String KTTDDNS_CHECK_ASCODE(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");
		String asCode = (String) request.get("asCode");
		Integer isPrivate = (Integer) request.get("isPrivate");
		if (macAddress == null || asCode == null || (isPrivate != null && isPrivate != 0 && isPrivate != 1)
				|| (asCode.length() != 8 && asCode.length() != 64))
			return "fail";

		if (isPrivate == null)
			isPrivate = 0;

		String apiMac = Encryptions.remakeMac(macAddress, false);
		if (apiMac.length() != 12)
			return "fail";

		List<Selfas> selfasList = selfasService.selectSelfasWhereMac(apiMac);
		if (selfasList == null || selfasList.size() == 0)
			return "incorrect";

		boolean ascode_del_check = false;
		boolean notExist = true;
		for (Selfas selfasItem : selfasList) {
			String certnum = selfasItem.getCertnum();
			if (asCode.length() == 64)
				certnum = Encryptions.getSHA(selfasItem.getCertnum(), 256); /// Device 에 입력된 AS CODE 를
																			/// Device가 SHA-256 으로 변환하여 서버에 전달
			/// (...)
			if (!certnum.equals(asCode.toUpperCase()))
				continue;

			if (selfasItem.getUser_level() == 1 && isPrivate == 0)
				return "fail";

			if (LocalDateTime.now()
					.isAfter(((selfasItem.getCertnum_create_time()).toLocalDateTime()).plusMinutes(60 * 24))) {
				ascode_del_check = selfasService.deleteSelfasWhereMacUserlevel(apiMac, selfasItem.getUser_level());
				return "timeout";
			}

			if (selfasItem.getUser_level() == 2 && isPrivate == 1)
				return "fail";
			Map<String, Object> setup = setupService.selectSetup();
			final int checkAscodeTime = (int) setup.get("devplaykey_limit_min");
			// user_level == 2 일경우 ascode 발급 시간을 확인해서 현재 시간과 비교 시 현재시간이 더 작은경우 등록된 ascode
			// delete 후 fail(timeout)
			if (isPrivate == 0 && selfasItem.getUser_level() == 2 && LocalDateTime.now()
					.isAfter(((selfasItem.getCertnum_create_time()).toLocalDateTime()).plusMinutes(checkAscodeTime))) {
				ascode_del_check = selfasService.deleteSelfasWhereMacUserlevel(apiMac, 2);
				if (!ascode_del_check == true) {
					continue;
				} else {
					return "timeout";
				}
			}
			notExist = false;

		}

		if (notExist)
			return "incorrect";

		return "success";
	}

	public String KTTDDNS_DEVICE_CMD_URL(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");
		String empNo = (String) request.get("empNo");

		if (macAddress == null || empNo == null || empNo.length() == 0)
			return "fail";

		String apiMac = Encryptions.remakeMac(macAddress, false);
		if (apiMac.length() != 12)
			return "fail";
		String ddnsMac = Encryptions.remakeMac(macAddress, true);

		Map<String, Object> device = apiService.selectDeviceWhereMac(ddnsMac);
		if (device == null)
			return "noregist";

		String masterKey = Encryptions.encryptMasterkey(Encryptions.getSHA(apiMac + "KTT_MASTER", 1));

		String timeStr = String.format("%08X", (System.currentTimeMillis() / 1000) + (60 * 60 * 24));
		String devKey = Encryptions.makeDevKey(masterKey, timeStr);

		String url = "https://" + device.get("fullDomain") + ":" + device.get("webport") + "/kttapi/remotecmd?devkey="
				+ devKey;
		response.put("url", url);

		return "success";
	}

	/**
	 * 
	 * @Method Name : KTTDDNS_CHECK_DEVKEY
	 * @작성일 : 2023. 4. 4.
	 * @작성자 : Foryoucom
	 * @변경이력 :
	 * @Method 설명 :
	 * @param request
	 * @param response
	 * @return 2023-04-04 deprecated 사용 하는 곳이 없음
	 */
	public String KTTDDNS_CHECK_DEVKEY(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");
		String devKey = (String) request.get("devKey");
		// File file = new
		// File(context.getRealPath("/WEB-INF/classes/deprecated_KTTDDNS_CHECK_DEVKEY"));
		// boolean isDeprecatedOldFunction = file.exists();
		// file = null;
		/// if (isDeprecatedOldFunction) {
		// response.clear();
		// return "deprecated";
		// }

		if (macAddress == null || devKey == null)
			return "fail";

		String apiMac = Encryptions.remakeMac(macAddress, false);
		if (apiMac.length() != 12)
			return "fail";
		String ddnsMac = Encryptions.remakeMac(macAddress, true);

		Map<String, Object> device = apiService.selectDeviceWhereMac(ddnsMac);
		if (device == null)
			return "noregist";

		String masterKey = Encryptions.encryptMasterkey(Encryptions.getSHA(apiMac + "KTT_MASTER", 1));
		String timeStr = devKey.substring(40);
		String devKey2 = Encryptions.makeDevKey(masterKey, timeStr);

		if (devKey.equals(devKey2) == false)
			return "incorrect";

		long checkTime = Long.parseLong(timeStr, 16) * 1000;
		if (checkTime < System.currentTimeMillis())
			return "timeout";

		return "success";
	}

	public String KTTDDNS_DEVICE_PLAY_URL(Map<String, Object> request, Map<String, Object> response) {
		String sysid = (String) request.get("sysid");
		String eventTime = (String) request.get("eventTime");
		Integer guardStatus = (Integer) request.get("guardStatus");
		String empNo = (String) request.get("empNo");

		if (sysid == null || sysid.length() != 11 || eventTime == null || eventTime.length() != 14
				|| !StringUtils.isNumeric(eventTime) || empNo == null || empNo.length() == 0) {
			response.put("msg", "포맷 에러 등으로 실패하였습니다.");
			return "fail";
		}

		Map<String, Object> setup = setupService.selectSetup();

		try {
			Date eventTimeDate = new SimpleDateFormat("yyyyMMddHHmmss").parse(eventTime);
			Calendar limitTime = Calendar.getInstance();
			limitTime.setTime(eventTimeDate);
			limitTime.set(Calendar.HOUR, 0);
			limitTime.set(Calendar.MINUTE, 0);
			limitTime.set(Calendar.SECOND, 0);
			limitTime.add(Calendar.DATE, (int) setup.get("eventtime_limit_day"));
			if (logger.isDebugEnabled())
				logger.debug("limitTime : " + Integer.toString(limitTime.get(Calendar.YEAR)) + " - "
						+ Integer.toString(limitTime.get(Calendar.MONTH)) + " - "
						+ Integer.toString(limitTime.get(Calendar.DAY_OF_MONTH)) + " - "
						+ Integer.toString(limitTime.get(Calendar.HOUR_OF_DAY)) + " - "
						+ Integer.toString(limitTime.get(Calendar.MINUTE)) + " - "
						+ Integer.toString(limitTime.get(Calendar.SECOND)));
			Calendar now = Calendar.getInstance();
			if (logger.isDebugEnabled())
				logger.debug("now : " + Integer.toString(now.get(Calendar.YEAR)) + " - "
						+ Integer.toString(now.get(Calendar.MONTH)) + " - "
						+ Integer.toString(now.get(Calendar.DAY_OF_MONTH)) + " - "
						+ Integer.toString(now.get(Calendar.HOUR_OF_DAY)) + " - "
						+ Integer.toString(now.get(Calendar.MINUTE)) + " - "
						+ Integer.toString(now.get(Calendar.SECOND)));
			if (now.compareTo(limitTime) == 1) {
				response.put("msg", "영상관제 가능 기간(설정시간:" + (int) setup.get("eventtime_limit_day") + "일)이 초과되었습니다.");
				return "unablePlayTime";
			}
		} catch (ParseException e) {
			if (logger.isDebugEnabled())
				logger.debug(e.getLocalizedMessage());
			// e.printStackTrace();
		}

		List<Map<String, Object>> deviceListOrg = apiService.selectDeviceWhereSysid(sysid);

		if (deviceListOrg == null || deviceListOrg.size() == 0) {
			response.put("msg", sysid + " System ID로 등록된 영상장비가 없습니다.");
			return "noregist";
		}

		if (deviceListOrg.size() > 5) {
			response.put("msg", "등록된 영상장비 개수가 5대를 초과하여 영상관제가 불가능합니다.");
			return "overcount";
		}

		List<Map<String, Object>> deviceList = new ArrayList<>();

		for (Map<String, Object> deviceItem : deviceListOrg) {
			String apiMac = Encryptions.remakeMac((String) deviceItem.get("macAddress"), false);
			String masterKey = Encryptions.encryptMasterkey(Encryptions.getSHA(apiMac + "KTT_MASTER", 1));
			String timeStr = String.format("%08X",
					(System.currentTimeMillis() / 1000) + (60 * (int) setup.get("devplaykey_limit_min")));
			String devPlayKey = Encryptions.makeDevPlayKey(masterKey, timeStr, eventTime);
			String url = "https://" + deviceItem.get("fullDomain") + ":" + deviceItem.get("webport")
					+ "/kttapi/eventplaycmd?devplaykey=" + devPlayKey + "&eventtime=" + eventTime + "&guardstatus="
					+ guardStatus;

			Map<String, Object> device = new HashMap<>();
			device.put("sysid", deviceItem.get("systemid"));
			device.put("macAddress", apiMac);
			device.put("url", url);

			deviceList.add(device);
		}

		response.put("deviceList", deviceList);
		response.put("msg", "성공되었습니다.");
		return "success";
	}

	public String KTTDDNS_SERVICENO_TO_DEVICE_PLAY_URL(Map<String, Object> request, Map<String, Object> response) {
		String serviceNo = (String) request.get("serviceNo");
		String eventTime = (String) request.get("eventTime");
		Integer guardStatus = (Integer) request.get("guardStatus");
		String empNo = (String) request.get("empNo");

		if (serviceNo == null || serviceNo.length() != 8 || eventTime == null || eventTime.length() != 14
				|| !StringUtils.isNumeric(eventTime) || empNo == null || empNo.length() == 0) {
			response.put("msg", "포맷 에러 등으로 실패하였습니다.");
			return "fail";
		}

		Map<String, Object> setup = setupService.selectSetup();

		try {
			Date eventTimeDate = new SimpleDateFormat("yyyyMMddHHmmss").parse(eventTime);
			Calendar limitTime = Calendar.getInstance();
			limitTime.setTime(eventTimeDate);
			limitTime.set(Calendar.HOUR, 0);
			limitTime.set(Calendar.MINUTE, 0);
			limitTime.set(Calendar.SECOND, 0);
			limitTime.add(Calendar.DATE, (int) setup.get("eventtime_limit_day"));
			System.out.println("limitTime : " + Integer.toString(limitTime.get(Calendar.YEAR)) + " - "
					+ Integer.toString(limitTime.get(Calendar.MONTH)) + " - "
					+ Integer.toString(limitTime.get(Calendar.DAY_OF_MONTH)) + " - "
					+ Integer.toString(limitTime.get(Calendar.HOUR_OF_DAY)) + " - "
					+ Integer.toString(limitTime.get(Calendar.MINUTE)) + " - "
					+ Integer.toString(limitTime.get(Calendar.SECOND)));
			Calendar now = Calendar.getInstance();
			System.out.println("now : " + Integer.toString(now.get(Calendar.YEAR)) + " - "
					+ Integer.toString(now.get(Calendar.MONTH)) + " - "
					+ Integer.toString(now.get(Calendar.DAY_OF_MONTH)) + " - "
					+ Integer.toString(now.get(Calendar.HOUR_OF_DAY)) + " - "
					+ Integer.toString(now.get(Calendar.MINUTE)) + " - " + Integer.toString(now.get(Calendar.SECOND)));
			if (now.compareTo(limitTime) == 1) {
				response.put("msg", "영상관제 가능 기간(설정시간:" + (int) setup.get("eventtime_limit_day") + "일)이 초과되었습니다.");
				return "unablePlayTime";
			}
		} catch (ParseException e) {
			if (logger.isDebugEnabled())
				logger.debug(e.getLocalizedMessage());
			// e.printStackTrace();
		}

		List<Map<String, Object>> deviceListOrg = apiService.selectDeviceWhereServiceno(serviceNo);

		if (deviceListOrg == null || deviceListOrg.size() == 0) {
			response.put("msg", serviceNo + " Service No로 등록된 영상장비가 없습니다.");
			return "noregist";
		}

		if (deviceListOrg.size() > 5) {
			response.put("msg", "등록된 영상장비 개수가 5대를 초과하여 영상관제가 불가능합니다.");
			return "overcount";
		}

		List<Map<String, Object>> deviceList = new ArrayList<>();

		for (Map<String, Object> deviceItem : deviceListOrg) {
			String apiMac = Encryptions.remakeMac((String) deviceItem.get("macAddress"), false);
			String masterKey = Encryptions.encryptMasterkey(Encryptions.getSHA(apiMac + "KTT_MASTER", 1));
			String timeStr = String.format("%08X",
					(System.currentTimeMillis() / 1000) + (60 * (int) setup.get("devplaykey_limit_min")));
			String devPlayKey = Encryptions.makeDevPlayKey(masterKey, timeStr, eventTime);
			String url = "https://" + deviceItem.get("fullDomain") + ":" + deviceItem.get("webport")
					+ "/kttapi/eventplaycmd?devplaykey=" + devPlayKey + "&eventtime=" + eventTime + "&guardstatus="
					+ guardStatus;

			Map<String, Object> device = new HashMap<>();
			device.put("serviceNo", deviceItem.get("serviceNo"));
			device.put("macAddress", apiMac);
			device.put("url", url);

			deviceList.add(device);
		}

		response.put("deviceList", deviceList);
		response.put("msg", "성공되었습니다.");
		return "success";
	}

	public String KTTDDNS_CHECK_DEVPLAYKEY(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");
		String devPlayKey = (String) request.get("devPlayKey");
		String eventTime = (String) request.get("eventTime");

		if (macAddress == null || devPlayKey == null || eventTime == null || eventTime.length() != 14
				|| !StringUtils.isNumeric(eventTime))
			return "fail";

		String apiMac = Encryptions.remakeMac(macAddress, false);
		if (apiMac.length() != 12)
			return "fail";
		String ddnsMac = Encryptions.remakeMac(macAddress, true);

		Map<String, Object> device = apiService.selectDeviceWhereMac(ddnsMac);
		if (device == null)
			return "noregist";

		String masterKey = Encryptions.encryptMasterkey(Encryptions.getSHA(apiMac + "KTT_MASTER", 1));
		String timeStr = devPlayKey.substring(40);
		String devPlayKey2 = Encryptions.makeDevPlayKey(masterKey, timeStr, eventTime);

		if (devPlayKey.equals(devPlayKey2) == false)
			return "incorrect";

		long checkTime = Long.parseLong(timeStr, 16) * 1000;
		if (checkTime < System.currentTimeMillis())
			return "timeout";

		long limitsec = (checkTime - System.currentTimeMillis()) / 1000;
		response.put("limitsec", limitsec);

		return "success";
	}

	public String KTTDDNS_DEVICE_RESET_PW_URL(Map<String, Object> request, Map<String, Object> response) {
		String sysid = (String) request.get("sysid");
		String empNo = (String) request.get("empNo");

		if (sysid == null || sysid.length() == 0 || empNo == null || empNo.length() == 0)
			return "fail";

		List<Map<String, Object>> deviceListOrg = apiService.selectDeviceWhereSysid2(sysid);

		if (deviceListOrg == null || deviceListOrg.size() == 0)
			return "noregist";

		List<Map<String, Object>> deviceList = new ArrayList<>();

		for (Map<String, Object> deviceItem : deviceListOrg) {
			String apiMac = Encryptions.remakeMac((String) deviceItem.get("macAddress"), false);
			String masterKey = Encryptions.encryptMasterkey(Encryptions.getSHA(apiMac + "KTT_MASTER", 1));
			String timeStr = String.format("%08X", (System.currentTimeMillis() / 1000) + (60 * 60 * 24));
			String devKey = Encryptions.makeDevKey(masterKey, timeStr);
			String url = "https://" + deviceItem.get("fullDomain") + ":" + deviceItem.get("webport")
					+ "/kttapi/resetpassword?devkey=" + devKey;

			Map<String, Object> device = new HashMap<>();
			device.put("sysid", deviceItem.get("systemid"));
			device.put("macAddress", apiMac);
			device.put("url", url);

			deviceList.add(device);
		}

		response.put("deviceList", deviceList);

		return "success";
	}

	public String KTTDDNS_OTP_LOG(Map<String, Object> request, Map<String, Object> response) {
		String domain = (String) request.get("domain");
		String macAddress = (String) request.get("macAddress");
		String sysid = (String) request.get("sysid");
		String phone = (String) request.get("phone");

		if (domain == null || sysid == null || macAddress == null || phone == null)
			return "fail";

		String msg = "OTP SEND - DOMAIN : " + domain + ", MAC ADDRESS : " + macAddress + ", SYSTEM ID : " + sysid
				+ ", PHONE : " + phone;

		if (!ddnslogService.insertDdnslog(macAddress, msg))
			return "fail";

		return "success";

		/*
		 * String rtn_msg = "fail";
		 * 
		 * if(domain == null || sysid == null || macAddress == null || phone == null)
		 * return rtn_msg;
		 * 
		 * String fileNameDate = new
		 * SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis()); String
		 * logDate = new
		 * SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()); String
		 * logMsg = logDate + " | " + domain + " | " + macAddress + " | " + sysid +
		 * " | " + phone; String savePath = "/usr/local/otp_log"; File folder = new
		 * File(savePath + "/");
		 * 
		 * if(!folder.exists()) folder.mkdir();
		 * 
		 * File file = new File(savePath, fileNameDate + "_otp.log"); BufferedReader br
		 * = null; BufferedWriter bw = null; boolean isExists = file.exists();
		 * 
		 * try { if(isExists) { String line = ""; String oldStr = ""; String newStr =
		 * ""; br = new BufferedReader(new FileReader(file)); while((line =
		 * br.readLine()) != null) { oldStr += (line + "\r\n"); } newStr = oldStr +
		 * logMsg; bw = new BufferedWriter(new FileWriter(file)); bw.write(newStr); }
		 * else { bw = new BufferedWriter(new FileWriter(file)); bw.write(logMsg); }
		 * bw.flush(); } catch (IOException e) { e.printStackTrace(); } finally { if(br
		 * != null) try {br.close(); } catch (IOException e) {} if(bw != null) try
		 * {bw.close(); } catch (IOException e) {} rtn_msg = "success"; }
		 * 
		 * return rtn_msg;
		 */
	}

	/**
	 * 
	 * @Method Name : KTTDDNS_OTP_2
	 * @작성일 : 2022. 11. 25.
	 * @작성자 : Foryoucom
	 * @변경이력 :
	 * @Method 설명 : OTP_YN 기능 추가로 해당 요청이 와서 정상 처리 시 OTP_YN값이 3으로 변경 됨.
	 * @param request
	 * @param response
	 * @return
	 */
	public String KTTDDNS_OTP_LOG2(Map<String, Object> request, Map<String, Object> response) {
		/*
		 * String domain = (String) request.get("domain"); String macAddress = (String)
		 * request.get("macAddress"); String serviceNo = (String)
		 * request.get("serviceNo"); String phone = (String) request.get("phone");
		 * Integer access_rule = 0;
		 * 
		 * if (domain == null || serviceNo == null || macAddress == null || phone ==
		 * null) return "fail";
		 * 
		 * // String msg = "OTP SEND - DOMAIN : " + domain + ", MAC ADDRESS : " +
		 * macAddress + ", SERVICE // NO : " + serviceNo + ", PHONE : " + phone;
		 * StringBuffer msg = new StringBuffer(); msg.append("OTP SEND - DOMAIN : ");
		 * msg.append(domain); msg.append(", MAC ADDRESS : "); msg.append(macAddress);
		 * msg.append(", SERVICE NO : "); msg.append(serviceNo);
		 * msg.append(", PHONE : "); msg.append(phone); if
		 * (request.containsKey("CHANGE_OTP_YN_FORCE")) { Integer otp_yn =
		 * Integer.valueOf((String) request.get("CHANGE_OTP_YN_FORCE"));
		 * apiService.updateUserOTP_YN(otp_yn, macAddress); } else { if
		 * (!ddnslogService.insertDdnslog(macAddress, msg.toString())) /// OTP_YN의 경우 해당
		 * INSERT 구문이 성공 /// 시 dB서버의 트리거(함수)에 의하여 /// 자동으로 3으로 갱신이 됨. { msg = null;
		 * return "fail"; } } msg = null; response.put("access_rule",
		 * access_rule.toString()); return "success";
		 */

		// 2023-10-20 OTP LOG2 access_rule 추가(김동혁선임 nvr에 전달)
		String domain = (String) request.get("domain");
		String macAddress = (String) request.get("macAddress");
		String serviceNo = (String) request.get("serviceNo");
		String phone = (String) request.get("phone");

		try {
			if (domain == null || serviceNo == null || macAddress == null || phone == null || macAddress == null) {
				return "fail";
			}
			StringBuffer msg = new StringBuffer();
			msg.append("OTP SEND - DOMAIN : ");
			msg.append(domain);
			msg.append(", MAC ADDRESS : ");
			msg.append(macAddress);
			msg.append(", SERVICE NO : ");
			msg.append(serviceNo);
			msg.append(", PHONE : ");
			msg.append(phone);
			
			String apiMac = Encryptions.remakeMac(macAddress, false);

			if (apiMac.length() != 12) {
				return "fail";
			}

			String ddnsMac = Encryptions.remakeMac(macAddress, true);
			
			// ddnslogService.insertDdnslogPhone(phone);
			ddnslogService.insertDdnslogPhone(macAddress, phone, serviceNo, msg.toString());
			if (request.containsKey("CHANGE_OTP_YN_FORCE")) {
				Integer otp_yn = Integer.valueOf((String) request.get("CHANGE_OTP_YN_FORCE"));
				apiService.updateUserOTP_YN(otp_yn, macAddress);
			}
			else if (!ddnslogService.insertDdnslog(macAddress, msg.toString())) /// OTP_YN의 경우 해당 INSERT 구문이 성공
			                                                                     /// 시 dB서버의 트리거(함수)에 의하여
			    	  
			// else if (!ddnslogService.insertDdnslog(macAddress, msg))
			

			// OTP_YN의 경우 해당 INSERT 구문이 성공시 dB서버의 트리거(함수)에 의하여 자동으로 3으로 갱신이 됨.
			{
				msg = null;
				return "fail";
			}

			Integer access_rule = 0;
			List<Map<String, Object>> map = null;

			if (request.containsKey("white_ip")) {
				String ip = request.get("white_ip").toString().trim();

				Matcher m1 = this.VALID_IPV4_PATTERN.matcher(ip);
				Matcher m12 = this.VALID_IPV6_PATTERN1.matcher(ip);
				Matcher m22 = this.VALID_IPV6_PATTERN2.matcher(ip);

				if (m1.matches() == true || (m12.matches() != false || m22.matches() != false)) {
					map = apiService.selectDevicePublicIpWhereMac(ddnsMac, ip, 0);

				}
			} else {
				map = apiService.selectDevicePublicIpWhereMac(ddnsMac, "127.0.0.1", 0);
			}
		
			// SJ 통합버전 4.0.0 버전 업그래이드 시 개선 추가(20231128)

			Map<String, Object> device = null;

			device = apiService.selectDeviceWhereMac4(Encryptions.remakeMac(ddnsMac, true));
			int device_ver = apiService.verString2Int(device.get("device_ver").toString());
			if (device_ver >= apiService.verString2Int("V4.0.0")) {
				access_rule = 2;
				if (apiService.update_users_service_no_access_rule(ddnsMac, access_rule) == true) {
				}
			} else {
				access_rule = Integer.valueOf(map.get(0).get("access_rule").toString());

			}
			response.put("access_rule", access_rule.toString()); // access_rule = 0 --> 전체 허용(올레, 스마트아이즈, 통합앱) 1 -->
																	// 올레, 통합앱, 2 --> 통합앱
			return "success";

			

			
			/*
			if (map != null && map.size() > 0 && map.get(0).get("service_user") != null
					&& map.get(0).get("service_user").toString().equals("1")) {

				
				// SJ 통합버전 4.0.0 버전 업그래이드 시 개선 추가(20231128)

				Map<String, Object> device = null;

				device = apiService.selectDeviceWhereMac4(Encryptions.remakeMac(ddnsMac, true));
				int device_ver = apiService.verString2Int(device.get("device_ver").toString());
				logger.debug(" device_ver : " + device_ver);

				if (device_ver >= apiService.verString2Int("V4.0.0")) {
					access_rule = 2;
					if (apiService.update_users_service_no_access_rule(macAddress, access_rule) == true) {
						logger.debug(" !!!!!!!!!!!!!!!!!!!!!");
						logger.debug(" 버전높아 access_rule : " + access_rule);
					}
				} else {
					access_rule = Integer.valueOf(map.get(0).get("access_rule").toString());
					logger.debug(" ??????????????????????");
					logger.debug(" 버전낮아 access_rule : " + access_rule);

				}
				logger.debug(" 최종 access_rule  : " + access_rule);
				response.put("access_rule", access_rule.toString()); // access_rule = 0 --> 전체 허용(올레, 스마트아이즈, 통합앱) 1 -->
																		// 올레, 통합앱, 2 --> 통합앱
				// sjend
				return "success";

			} else {
				return "nomatchip";
			}*/

		} catch (Exception e) {
			response.clear();
			return "fail";
		}
	}

	public String KTTDDNS_MAC_TO_SERVICE(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");

		if (macAddress == null)
			return "fail";

		String apiMac = Encryptions.remakeMac(macAddress, false);
		if (apiMac.length() != 12)
			return "fail";
		String ddnsMac = Encryptions.remakeMac(macAddress, true);

		Map<String, Object> device = apiService.selectDeviceWhereMac2(ddnsMac);
		if (device == null)
			return "noregist";

		///////////////////////////////////////////////////////////////////////////////////
		if (device.get("service_no").toString().length() != 8 || device.get("sys_id") == null
				|| device.get("sys_id").toString().length() != 13) {
			return "noservice";
		}

		response.put("sysid", device.get("sys_id"));

		/*
		 * if(device.get("service_no").toString().length() != 8) return "noservice";
		 * 
		 * String sysid = ""; if(device.get("sys_id") != null) sysid =
		 * (String)device.get("sys_id");
		 * 
		 * response.put("sysid", sysid);
		 */
		///////////////////////////////////////////////////////////////////////////////////

		response.put("serviceNo", device.get("service_no"));

		return "success";
	}

	public String KTTDDNS_MAC_SERVICEOPEN(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");

		if (macAddress == null)
			return "fail";

		String apiMac = Encryptions.remakeMac(macAddress, false);
		if (apiMac.length() != 12)
			return "fail";
		String ddnsMac = Encryptions.remakeMac(macAddress, true);

		Map<String, Object> device = apiService.selectDeviceWhereMac2(ddnsMac);
		if (device == null)
			return "noregist";
		if (device.get("service_no").toString().length() != 8 || device.get("sys_id") == null
				|| device.get("sys_id").toString().length() != 13 || device.get("cust_sts") == null
				|| (!device.get("cust_sts").toString().equals("01") && !device.get("cust_sts").toString().equals("04")
						&& !device.get("cust_sts").toString().equals("05"))) {
			return "noservice";
		}

		if ((int) device.get("service_open") >= 1)
			return "success";

		if (!apiService.updateDeviceServiceopenWhereMac(ddnsMac))
			return "fail";

		return "success";
	}

	public String KTTDDNS_SERVICE_APP_REGISTER(Map<String, Object> request, Map<String, Object> response) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> serviceList = (List<Map<String, Object>>) request.get("serviceList");
		String appID = (String) request.get("appID");
		Integer registerType = (Integer) request.get("registerType");

		String msg = "오류가 발생하였습니다. 고객센터(1588-0112)로 연락주세요.";
		String result = "fail";

		do {
			if (serviceList == null || serviceList.size() == 0 || appID == null || registerType == null
					|| (registerType != null && registerType != 5 && registerType != 6))
				break;

			int count = 0;
			String list[][] = new String[serviceList.size()][4];

			for (Map<String, Object> service : serviceList) {
				if (service.get("serviceNo") == null || service.get("serviceNo").toString().length() != 8
						|| service.get("macAddress") == null)
					break;

				String apiMac = Encryptions.remakeMac(service.get("macAddress").toString(), false);
				if (apiMac.length() != 12)
					break;
				String ddnsMac = Encryptions.remakeMac(service.get("macAddress").toString(), true);

				Map<String, Object> device = apiService.selectDeviceWhereMac3(ddnsMac);
				if (device == null) {
					msg = "서버에 등록된 장비가 없습니다.";
					break;
				}

				String contractNo = apiService
						.selectServiceContractnoWhereServiceno(service.get("serviceNo").toString());
				if (contractNo == null) {
					msg = "서비스 번호가 존재하지 않습니다.";
					break;
				}

				String serviceNoCheck = device.get("service_no").toString().replaceAll("\\s+", ""); // \\s+ -하나 이상의 공백
																									// 문자 시퀀스
				if (serviceNoCheck.length() != 8 || serviceNoCheck.length() < 8) {
					// 요청된 registerType ==6 > break;(fail) insert
					if (registerType == 6) {
						msg = "해당 장비에 해당 서비스 번호를 등록할 수 없습니다.";
						break;
					}
					if ((int) device.get("register_type") != 7) {
						msg = "서비스 상태를 확인해주세요.";
						break;
					}
					list[count][0] = service.get("serviceNo").toString();
					list[count][1] = ddnsMac;
					list[count][2] = registerType.toString();
					list[count][3] = appID;
					count++;
				} else {
					// 220809 추가개발 필요 확인
					// register_type 0,1,2는 검증이 끝난 타입이고, 고객님이 변경할 수 있기에 employee_no 가 변경안되어도 되지만 다른
					// register_type 는 변경자가 누군지 확인이 되어야한다.
					if (!device.get("service_no").toString().equals(service.get("serviceNo").toString())) {
						if (!device.get("employee_no").equals(appID)) {
							String users_contract_no = apiService
									.selectServiceContractnoWhereServiceno(device.get("service_no").toString());
							if (users_contract_no == null || !users_contract_no.equals(contractNo)) {
								msg = "이미 다른 서비스번호에 등록되어 있는 장비입니다.";
								break;
							}
						}
					}
					int chkregisterType = (int) device.get("register_type");
					if (chkregisterType == 0 || chkregisterType == 1 || chkregisterType == 2) {
						list[count][0] = service.get("serviceNo").toString();
						list[count][1] = ddnsMac;
						list[count][2] = device.get("register_type").toString();
						list[count][3] = device.get("employee_no").toString();
						count++;
					} else if (chkregisterType > 2) {
						list[count][0] = service.get("serviceNo").toString();
						list[count][1] = ddnsMac;
						list[count][2] = registerType.toString();
						list[count][3] = appID;
						count++;
					} else {
						msg = "장비 정보 확인이 필요합니다.";
						break;
					}
				}
			}

			if (count < serviceList.size())
				break;

			for (count = 0; count < serviceList.size(); count++) {
				if (!apiService.updateDeviceServicenoWhereMac(list[count][0], list[count][1],
						Integer.parseInt(list[count][2]), list[count][3]))
					break;
				/*
				 * //DDNS Bind Server's Zone (Named) File 에 신규 등록 하는 기능 if(registerType >= 3 &&
				 * registerType <= 4) { if(device != null && device.containsKey("fullDomain")&&
				 * device.containsKey("addr")) { String domain, addr;//DDNS ZONE 파일 등록 기능 , 신규
				 * 등록이 아닌 IP-RENEWAL의 경우 DDNS Process에서 처리하도록 수정
				 * 
				 * domain=(String)device.get("fullDomain"); addr =(String)device.get("addr"); //
				 * System.out.println("domain>>"+ domain); // System.out.println("addr>>"+
				 * addr); final String cmd = "/usr/local/kttzfm/user_data_ns.sh";//shell script
				 * ( DDNS 관련 스크립트 호출) if(new File(cmd).exists()) { CommandLine cmdLine = new
				 * CommandLine(cmd); //(등록할 맥주소 또는 도메인 명칭) cmdLine.addArgument(domain,false);
				 * cmdLine.addArgument(addr,false); this.execAsync(cmdLine); } } }
				 */
			}

			if (count < serviceList.size())
				break;

			msg = "정상적으로 처리 되었습니다.";
			result = "success";
		} while (false);

		response.put("msg", msg);
		return result;
	}

	/**
	 * async방식으로 외부 명령어 실행 - apachec commons의 exec를 사용
	 * 
	 * @param command
	 *            실행 command
	 */
	public void execAsync(CommandLine command) {

		logger.info("async exec. command: " + command.toString());

		DefaultExecutor executor = new DefaultExecutor();

		try {
			executor.execute(new CommandLine(command), new DefaultExecuteResultHandler());

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		executor = null;
	}

	/**
	 * 외부 명령 실행 후 결과를 String으로 받음
	 * 
	 * @param command
	 * @return
	 * @throws Exception
	 */
	public String execAndRtnResult(CommandLine command) throws Exception {

		logger.info("execAndRtnResult. command: " + command.toString());

		String rtnStr = "";

		DefaultExecutor executor = new DefaultExecutor();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		PumpStreamHandler streamHandler = new PumpStreamHandler(baos);
		executor.setStreamHandler(streamHandler);

		/*
		 * 로컬 테스트시 사용 커멘드 CommandLine tmp = CommandLine.parse("ipconfig");
		 * tmp.addArgument("/all");
		 */

		try {

			int exitCode = executor.execute(new CommandLine(command));
			rtnStr = baos.toString();

			logger.info("exitCode : " + exitCode);
			logger.debug("outputStr : " + rtnStr);

		} catch (Exception e) {
			logger.warn(e.getMessage(), e); // error로그는 필요시 사용하는 곳 에서 catch후 사용하세요
			throw new Exception(e.getMessage(), e);
		}

		return rtnStr;

	}

	public String KTTDDNS_SERVICE_REGISTER(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");
		String serviceNo = (String) request.get("serviceNo");
		String empNo = (String) request.get("empNo");
		Integer registerType = (Integer) request.get("registerType");

		String msg = "에러가 발생하였습니다. 담당자에게 연락주시기 바랍니다.";
		String result = "fail";

		do {
			if (macAddress == null || serviceNo == null || registerType == null || empNo == null
					|| serviceNo.length() != 8 || empNo.length() == 0 || registerType > 7) {

				break;
			}

			String apiMac = Encryptions.remakeMac(macAddress, false);

			String ddnsMac = Encryptions.remakeMac(macAddress, true);
			if (apiMac.length() != 12) {
				msg = "MAC 에러가 발생하였습니다. 담당자에게 연락주시기 바랍니다.";
				break;
			}
			String cust_sts = apiService.selectServiceCuststsWhereServiceno(serviceNo);
			if (cust_sts == null || (!cust_sts.equals("01") && !cust_sts.equals("02") && !cust_sts.equals("04")
					&& !cust_sts.equals("05"))) {
				msg = "고객의 서비스 상태를 확인하세요.";
				break;
			}

			Map<String, Object> device = null;
			if (registerType >= 3 && registerType <= 4)
				device = apiService.selectDeviceWhereMac4(ddnsMac);
			else
				device = apiService.selectDeviceWhereMac3(ddnsMac);

			if (device == null) {
				if (registerType >= 0 && registerType <= 2) {
					if (!apiService.insertDevice(serviceNo, ddnsMac, registerType, empNo))
						break;

					msg = "정상적으로 처리 되었습니다.";
					result = "success";
					break;
				} else {
					msg = "장치가 존재하지 않습니다.";
					break;
				}
			}

			if (device.get("service_no").toString().length() > 0) {
				if (device.get("service_no").toString().equals(serviceNo)) {
					msg = "이미 등록된 상태입니다.";
					break;
				} else {
					msg = "이미 다른 장치가 사용중입니다.";
					break;
				}
			}
			/**
			 * CODE HISTORY : 2022-10-07 registerType 이 3인 경우에만 펌웨어 업그레이드 유무를 판단하도록 변경 (종전
			 * 3과 4인 경우)
			 */

			if (registerType == 3 || registerType == 4) {
				try {
					String strLast_ver = device.get("last_ver").toString();
					String strDevice_ver = device.get("device_ver").toString();
					if (strLast_ver == null || strDevice_ver == null) {
						msg = "모델을 확인할 수 없습니다.";
						break;
					}
					if (apiService.verString2Int(strLast_ver) == 0 || apiService.verString2Int(strDevice_ver) == 0) {
						msg = "펌웨어 버전 표시가 잘못되었습니다.";
						break;
					}
					if (registerType == 3
							&& (apiService.verString2Int(strDevice_ver) < apiService.verString2Int(strLast_ver))) {
						msg = "펌웨어 업그레이드를 진행해 주세요.";
						result = "fwup";
						break;
					}
				} catch (Exception e) {
					msg = "모델을 확인할 수 없습니다.";
					break;
				}
			}

			/*
			 * if(registerType >= 3 && registerType <= 4) { if(device.get("last_ver") ==
			 * null) { msg = "모델을 확인할 수 없습니다."; break; }
			 * 
			 * if(apiService.verString2Int(device.get("device_ver").toString()) == 0 ||
			 * apiService.verString2Int(device.get("last_ver").toString()) == 0) { msg =
			 * "펌웨어 버전 표시가 잘못되었습니다."; break; }
			 * 
			 * if(apiService.verString2Int(device.get("device_ver").toString()) <
			 * apiService.verString2Int(device.get("last_ver").toString())) { msg =
			 * "펌웨어 업그레이드를 진행해 주세요."; result = "fwup"; break; } }
			 */

			if (!apiService.updateDeviceServicenoWhereMac(serviceNo, ddnsMac, registerType, empNo))
				break;
			/*
			 * //DDNS Bind Server's Zone (Named) File 에 신규 등록 하는 기능 if(registerType >= 3 &&
			 * registerType <= 4) { if(device != null && device.containsKey("fullDomain")&&
			 * device.containsKey("addr")) { String domain, addr;//DDNS ZONE 파일 등록 기능 , 신규
			 * 등록이 아닌 IP-RENEWAL의 경우 DDNS Process에서 처리하도록 수정
			 * 
			 * domain=(String)device.get("fullDomain"); addr =(String)device.get("addr"); //
			 * System.out.println("domain>>"+ domain); // System.out.println("addr>>"+
			 * addr); final String cmd = "/usr/local/kttzfm/user_data_ns.sh";//shell script
			 * ( DDNS 관련 스크립트 호출) if(new File(cmd).exists()) { CommandLine cmdLine = new
			 * CommandLine(cmd); //(등록할 맥주소 또는 도메인 명칭) cmdLine.addArgument(domain,false);
			 * cmdLine.addArgument(addr,false); this.execAsync(cmdLine); } } }
			 */
			msg = "정상적으로 처리 되었습니다.";
			result = "success";
		} while (false);
		response.put("msg", msg);
		return result;
	}

	public String KTTDDNS_SERVICE_CHECK_OPEN(Map<String, Object> request, Map<String, Object> response) {
		@SuppressWarnings("unchecked")
		ArrayList<String> recvDeviceList = (ArrayList<String>) request.get("deviceList");
		String serviceNo = (String) request.get("serviceNo");

		boolean anyService = false;
		String msg = "에러가 발생하였습니다. 담당자에게 연락주시기 바랍니다.";
		String result = "fail";

		do {
			if (recvDeviceList == null || recvDeviceList.size() == 0 || serviceNo == null)
				break;

			if (serviceNo.equals("any"))
				anyService = true;

			List<Map<String, Object>> sendDeviceList = new ArrayList<>();
			int count = 0;
			int openCount = 0;

			for (String recvDevice : recvDeviceList) {
				String apiMac = Encryptions.remakeMac(recvDevice, false);
				if (apiMac.length() != 12)
					break;
				String ddnsMac = Encryptions.remakeMac(recvDevice, true);

				Map<String, Object> device = apiService.selectDeviceWhereMac3(ddnsMac);
				if (device == null) {
					msg = "장치가 존재하지 않습니다.";
					break;
				}

				if (anyService == false && !serviceNo.equals(device.get("service_no").toString())) {
					msg = "서비스 번호와 장치 정보가 일치하지 않습니다.";
					break;
				}

				Map<String, Object> sendDevice = new HashMap<>();
				sendDevice.put("macAddress", apiMac);
				sendDevice.put("open", (int) device.get("service_open") > 0 ? 1 : 0);
				sendDeviceList.add(sendDevice);

				if ((int) device.get("service_open") > 0)
					openCount++;
				count++;
			}

			if (count < recvDeviceList.size())
				break;

			response.put("deviceList", sendDeviceList);

			if (openCount < recvDeviceList.size()) {
				msg = "아래 장비에 대해 장비 개통 UI에서 준공처리 완료해 주세요.";
				break;
			}

			response.put("serviceNo", serviceNo);
			msg = "정상적으로 처리 되었습니다.";
			result = "success";
		} while (false);

		response.put("msg", msg);
		return result;
	}

	public String KTTDDNS_SERVICE_DEVICEINFO(Map<String, Object> request, Map<String, Object> response) {
		@SuppressWarnings("unchecked")
		ArrayList<String> serviceNoList = (ArrayList<String>) request.get("serviceNoList");

		String msg = "에러가 발생하였습니다. 담당자에게 연락주시기 바랍니다.";
		String result = "fail";
		Boolean bOtpYnError = false;

		do {
			if (serviceNoList == null || serviceNoList.size() == 0)
				break;

			ArrayList<String> tmpList = new ArrayList<>();
			for (String serviceNo : serviceNoList) {
				if (serviceNo.length() == 0 || tmpList.contains(serviceNo))
					continue;
				tmpList.add(serviceNo);
			}

			String serviceNoListString = "";
			for (int i = 0; i < tmpList.size(); i++) {
				if (serviceNoListString.length() == 0)
					serviceNoListString += ("'" + tmpList.get(i) + "'");
				else
					serviceNoListString += (", '" + tmpList.get(i) + "'");
			}

			if (serviceNoListString.length() == 0)
				break;

			List<Map<String, Object>> serviceList = apiService.selectServiceWhereInServiceno(serviceNoListString);
			if (serviceList == null || serviceList.size() != tmpList.size()) {
				logger.debug("serviceNoListString :" + serviceNoListString);
				msg = "서비스번호가 존재하지 않습니다.";
				break;
			}
			// List<Map<String, Object>> tmpDeviceList = apiService.selectDeviceWhereInServiceno(serviceNoListString);

			// SJ 통합버전 4.0.0 버전 업그래이드 시 개선 추가(20231128)
			List<Map<String, Object>> tmpDeviceList = apiService.selectDeviceWhereInServicenoOTP(serviceNoListString);
			List<Map<String, Object>> deviceList = new ArrayList<>();
			int all_device = tmpDeviceList.size();

			
			List<Map<String, Object>> tmpRifaDeviceList = apiService.selectDeviceMAKERWhereInServiceno(serviceNoListString);
			int rifa_device = tmpRifaDeviceList.size();
			int count = 0;
			boolean bNoMaker_device = true; 

			String macList = "";
			
			for (Map<String, Object> tmpRifaDevice : tmpRifaDeviceList) {
				//int otp_yn = apiService.verString2Int(tmpRifaDevice.get("otp_yn").toString());
				int otp_yn = tmpRifaDevice.containsKey("otp_yn") ? (int) tmpRifaDevice.get("otp_yn") : 0;

				
				if (tmpRifaDevice.get("mac") == null) {
					continue;
				}
				if(otp_yn != 3)
				{
					if (macList.length() == 0)
					{
						macList += ("'" + tmpRifaDevice.get("mac") + "'");
					}
					else
					{
						macList += (", '" + tmpRifaDevice.get("mac") + "'");
					}
				}
			}
			 
			
			for (Map<String, Object> tmpDevice : tmpDeviceList) {
				String maker = tmpDevice.get("maker").toString();
				String tmpMaker = "RIFAT";
				
				if(!maker.trim().equals(tmpMaker) )
				{
					bNoMaker_device = true; 

				}
				else
				{
					
					bNoMaker_device = false;
					break;
				}
			}
			
			boolean allUpgrede = true; 
			
			for (Map<String, Object> tmpDevice : tmpDeviceList) {
				int device_ver = apiService.verString2Int(tmpDevice.get("device_ver").toString());
				int last_ver = apiService.verString2Int(tmpDevice.get("last_ver").toString());
				
				if (device_ver != last_ver) 
				{
					allUpgrede = false; 
					break;
				}
			}
			
			
			for (Map<String, Object> tmpDevice : tmpDeviceList) {

				if (tmpDevice.get("mac") == null) {
					count++;
					continue;
				}
				
				Map<String, Object> device = new HashMap<>();
				device.put("serviceNo", tmpDevice.get("service_no"));
				device.put("registerType", tmpDevice.get("register_type"));
				device.put("macAddress", tmpDevice.get("mac"));
				
				
				/* KTTDDNS_SERVICE_REGISTER 로 등록한 경우 device_ver 이 존재하지 않는다. */
				if (tmpDevice.get("device_ver") == null || tmpDevice.get("device_ver").toString().length() == 0) {
					device.put("model", "");
					device.put("fwUp", 1);
				} else if (tmpDevice.get("last_ver") == null || tmpDevice.get("last_ver").toString().length() == 0) {
					msg = "모델 코드가 존재하지 않습니다.";
					break;
				} else {
					
					int device_ver = apiService.verString2Int(tmpDevice.get("device_ver").toString());
					int last_ver = apiService.verString2Int(tmpDevice.get("last_ver").toString());

					if (device_ver == 0 || last_ver == 0) {
						msg = "펌웨어 버전 표시가 잘못되었습니다.";
						break;
					}
										
					//String maker = tmpDevice.containsKey("maker") ? tmpDevice.get("maker").toString(): "";
					int otp_yn = tmpDevice.containsKey("otp_yn") ? (int) tmpDevice.get("otp_yn") : 0;
					
					String maker = tmpDevice.get("maker").toString();
					String tmpMaker = "RIFAT";
					// 2023.12.27 정보보안개선 수정사항 대료 적용
					/*
					if (allUpgrede) 
					{
						logger.debug("장비의 버전이 모두 최신버전을 경우");
						if(all_device <= 5)
						{
							logger.debug("장비의 개수가 5개 이하일 경우");
							if(bNoMaker_device)
							{
								logger.debug("5대 이하 모든 장비가 이화트론 장비가 아닌경우");
								device.put("model", tmpDevice.get("model"));
								device.put("fwUp", 0);
							
							}
							//else if(rifa_device > 0 && rifa_device <= 5)
							else
							{
								if(otp_yn == 3)
								{
									logger.debug("모든 장비(5대)가 3 OTP 인증 완료 일 경우");
									device.put("model", tmpDevice.get("model"));
									device.put("fwUp", 0);
								}
								else
								{
									logger.debug("하나라도 OTP 인증이 안했을 경우");
									response.put("deviceList", deviceList);
									msg = "장비에서 [우클릭] →[시스템]→[사용자수정]에서 OTP인증 후 비밀번호 변경을 진행해주세요. (" + macList + ")";
									result = "success";
									break;
								}
							}
							//else if(!maker.trim().equals(tmpMaker) || rifa_device > 5)
						}
						else if(all_device > 5)
						{
							logger.debug("장비의 갯수가 5개보다 많을 경우");

							device.put("model", tmpDevice.get("model"));
							device.put("fwUp", 0);
						}
						
					}
					else  
					{
						logger.debug("장비의 버전이 1개라도 낮을 경우");
						device.put("model", tmpDevice.get("model"));
						device.put("fwUp", (device_ver < last_ver) ? 1 : 0);
					}
					*/
			
					if (allUpgrede) 
					{
						if(maker.trim().equals(tmpMaker))
						{
							if(rifa_device > 0 && rifa_device <= 5)
							{
								if(otp_yn == 3)
								{
									logger.debug("모든 장비(5대)가 3 OTP 인증 완료 일 경우 : fwup = 0으로 device List를 전달한다.");
									device.put("model", tmpDevice.get("model"));
									device.put("fwUp", 0);
								}
								else 
								{
								
                					logger.debug("하나라도 OTP 인증이 안했을 경우 (0 OTP 인증안함, 1 기존통합사용자, 2 관리자변경) :  msg에 아래의 내용과 장비의 mac 을 현시하며 device List는 null로 전달한다.");
                					List<Map<String, Object>> emptyList = new ArrayList<>();
                					response.put("deviceList", emptyList);
									msg = "장비에서 [우클릭] →[시스템]→[사용자수정]에서 OTP인증 후 비밀번호 변경을 진행해주세요. (" + macList + ")";
									result = "success";
									emptyList= null;
									break;
								}
								
							}
						}
						//20240102 시나리오 확인 예정
						else if(bNoMaker_device || rifa_device > 5)
						{
							logger.debug(" 요청한 모든 장비가 이화트론 장비가 아니거나, 이화트론 장비 이지만 장비의 갯수가 5개보다 많을 경우 fwup = 0으로 device List를 전달한다. ");
							device.put("model", tmpDevice.get("model"));
							device.put("fwUp", 0);

						}
					}
					else  
					{
						logger.debug("장비의 버전이 1개라도 낮을 경우 :  모든 장비의 device List를 전달하며, 최신 버전이 아닌 장비는 fwup = 1, 최신 버전인 장비는 fwup = 0으로 전달 한다. ");
						device.put("model", tmpDevice.get("model"));
						device.put("fwUp", (device_ver < last_ver) ? 1 : 0);
					}
					
						
						// SJ add
/*
						int access_rule = tmpDevice.containsKey("access_rule") ? (int) tmpDevice.get("access_rule") : 0;
						int otp_yn = tmpDevice.containsKey("otp_yn") ? (int) tmpDevice.get("otp_yn") : 0;
						logger.debug("access_rule : " + access_rule);
						logger.debug("otp_yn :" + otp_yn);
*/
					/*
						if ((device_ver >= apiService.verString2Int("V4.0.0")) && (otp_yn == 0)) {
							// response.put("fwUp", 1);
							device.put("fwUp", 1);
							// msg = "장비에서 [우클릭] →[시스템]→[사용자수정]에서 OTP인증 후 비밀번호 변경을 진행해주세요";
							bOtpYnError = true;
							// device.put("msg device", msg);
							logger.debug(msg);
							// break;
						}*/
					//}

				}
				deviceList.add(device);
				count++;
			}

			if (count < tmpDeviceList.size())
				break;

			response.put("deviceList", deviceList);
			/*
			if (!bOtpYnError) {
				msg = "정상적으로 처리 되었습니다.";
			} else {
				msg = "장비에서 [우클릭] →[시스템]→[사용자수정]에서 OTP인증 후 비밀번호 변경을 진행해주세요. (" + macList + ")";
			}*/
		      msg = "정상적으로 처리 되었습니다.";
		      result = "success";
		} while (false);

		response.put("msg", msg);
		return result;
	}

	public String KTTDDNS_SERVICE_UNREGISTER(Map<String, Object> request, Map<String, Object> response) {
		@SuppressWarnings("unchecked")
		ArrayList<String> deviceList = (ArrayList<String>) request.get("deviceList");
		String serviceNo = (String) request.get("serviceNo");

		String msg = "에러가 발생하였습니다. 담당자에게 연락주시기 바랍니다.";
		String result = "fail";

		do {
			if (deviceList == null || deviceList.size() == 0 || serviceNo == null || serviceNo.length() != 8)
				break;

			String macListString = "";
			for (int i = 0; i < deviceList.size(); i++) {
				String apiMac = Encryptions.remakeMac(deviceList.get(i), false);
				if (apiMac.length() != 12)
					break;
				String ddnsMac = Encryptions.remakeMac(deviceList.get(i), true);

				if (macListString.length() == 0)
					macListString += ("'" + ddnsMac + "'");
				else
					macListString += (", '" + ddnsMac + "'");
			}

			if (macListString.length() == 0)
				break;

			int count = 0;
			List<Map<String, Object>> tmpDeviceList = apiService.selectDeviceWhereInMac(macListString);
			if (tmpDeviceList.size() < deviceList.size()) {
				msg = "장치가 존재하지 않습니다.";
				break;
			}

			for (Map<String, Object> tmpDevice : tmpDeviceList) {
				if (!tmpDevice.get("service_no").toString().equals(serviceNo)) {
					msg = "서비스번호와 장치정보가 일치하지 않습니다.";
					break;
				}

				if (0 <= (int) tmpDevice.get("register_type") && (int) tmpDevice.get("register_type") <= 2) {
					msg = "MAC을 공사완료단에서 등록한 장비는 삭제처리가 불가능합니다. 관리자에게 연락주세요.";
					break;
				}

				count++;
			}

			if (count < tmpDeviceList.size())
				break;

			if (!apiService.updateDeviceServicenoWhereInMac(macListString))
				break;

			msg = "정상적으로 처리 되었습니다.";
			result = "success";
		} while (false);

		response.put("msg", msg);
		return result;
	}

	public String KTTDDNS_SERVICE_DELETE_DEVICE(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");
		String serviceNo = (String) request.get("serviceNo");

		String msg = "에러가 발생하였습니다. 담당자에게 연락주시기 바랍니다.";
		String result = "fail";

		do {
			if (macAddress == null || serviceNo == null || serviceNo.length() != 8)
				break;

			String apiMac = Encryptions.remakeMac(macAddress, false);
			if (apiMac.length() != 12)
				break;
			String ddnsMac = Encryptions.remakeMac(macAddress, true);

			Map<String, Object> device = apiService.selectDeviceWhereMac3(ddnsMac);
			if (device == null) {
				msg = "장치가 존재하지 않습니다.";
				break;
			}

			if (device.get("service_no").toString().length() > 0
					&& !serviceNo.equals(device.get("service_no").toString())) {
				msg = "서비스번호와 장치가 일치하지 않습니다.";
				break;
			}

			if (!apiService.deleteDeviceWhereMac(ddnsMac))
				break;

			// String logMsg = "KTTDDNS_SERVICE_DELETE_DEVICE - MAC ADDRESS : " + macAddress
			// + ", SERVICE NO : " + serviceNo;

			if (!ddnslogService.insertDdnslog(macAddress, String.format(
					"KTTDDNS_SERVICE_DELETE_DEVICE - MAC ADDRESS : %s  SERVICE NO : %s", macAddress, serviceNo)))
				break;

			msg = "정상적으로 처리 되었습니다.";
			result = "success";
		} while (false);

		response.put("msg", msg);
		return result;
	}

	public String KTTDDNS_SERVICE_DELETE_MAC(Map<String, Object> request, Map<String, Object> response) {
		String macAddress = (String) request.get("macAddress");

		String msg = "에러가 발생하였습니다. 담당자에게 연락주시기 바랍니다.";
		String result = "fail";

		do {
			if (macAddress == null)
				break;

			String apiMac = Encryptions.remakeMac(macAddress, false);
			if (apiMac.length() != 12)
				break;
			String ddnsMac = Encryptions.remakeMac(macAddress, true);

			Map<String, Object> device = apiService.selectDeviceWhereMac3(ddnsMac);
			if (device == null) {
				msg = "장치가 존재하지 않습니다.";
				break;
			}

			if (!apiService.deleteDeviceWhereMac(ddnsMac))
				break;

			msg = "정상적으로 처리 되었습니다.";
			result = "success";
		} while (false);

		response.put("msg", msg);
		return result;
	}

	//// Dynamic Zone 파일을 이용하는 경우 , 스크립트를 수행하여 DDNS를 등록하도록 교정해야 함.
	/// kttzfm 하위 스크립트 중 nslookup 스크립트를 이용함. service_user 가 1인 경우에만 DDNS활성화가 됨
	public String KTTDDNS_SERVICE_SERVICEOPEN(Map<String, Object> request, Map<String, Object> response) {
		@SuppressWarnings("unchecked")
		ArrayList<String> deviceList = (ArrayList<String>) request.get("deviceList");

		String msg = "에러가 발생하였습니다. 담당자에게 연락주시기 바랍니다.";
		String result = "fail";

		do {
			if (deviceList == null || deviceList.size() == 0)
				break;

			String macListString = "";
			for (int i = 0; i < deviceList.size(); i++) {
				String apiMac = Encryptions.remakeMac(deviceList.get(i), false);
				if (apiMac.length() != 12)
					break;
				String ddnsMac = Encryptions.remakeMac(deviceList.get(i), true);

				if (i == 0)
					macListString += ("'" + ddnsMac + "'");
				else
					macListString += (", '" + ddnsMac + "'");
			}

			int count = 0;
			int alreadyOpen = 0;
			List<Map<String, Object>> tmpDeviceList = apiService.selectDeviceWhereInMac2(macListString);
			if (tmpDeviceList == null || tmpDeviceList.size() != deviceList.size())
				break;

			String deviceListString = "";

			for (int i = 0; i < tmpDeviceList.size(); i++) {
				Map<String, Object> tmpDevice = tmpDeviceList.get(i);
				if (tmpDevice.get("service_no").toString().length() != 8) {
					msg = "서비스번호가 존재하지 않습니다.";
					break;
				}

				if (tmpDevice.get("cust_sts") == null || (!tmpDevice.get("cust_sts").toString().equals("01")
						&& !tmpDevice.get("cust_sts").toString().equals("04")
						&& !tmpDevice.get("cust_sts").toString().equals("05"))) {
					msg = "고객의 서비스 상태를 확인하세요.";
					break;
				}

				if ((int) tmpDevice.get("service_open") < 1) {
					msg = "서비스 개통 UI에서 준공처리 완료해 주세요.";
					break;
				}

				if ((int) tmpDevice.get("service_open") >= 2) {
					alreadyOpen++;
					continue;
				}

				if (deviceListString.length() == 0)
					deviceListString += ("'" + tmpDevice.get("mac") + "'");
				else
					deviceListString += (", '" + tmpDevice.get("mac") + "'");

				count++;
			}

			if ((count + alreadyOpen) < tmpDeviceList.size())
				break;

			if (alreadyOpen < tmpDeviceList.size()) {
				// 아래 Update 시 service_user 가 1로 변경 되며, 1로 설정 될 시에 DNS서비를 진행하도록 시나리오가 구성 되어 있음.
				if (!apiService.updateDeviceServiceWhereInMac(deviceListString))
					break;
				/*
				 * //DDNS Bind Server's Zone (Named) File 에 신규 등록 하는 기능 if(registerType >= 3 &&
				 * registerType <= 4) { if(device != null && device.containsKey("fullDomain")&&
				 * device.containsKey("addr")) { String domain, addr;//DDNS ZONE 파일 등록 기능 , 신규
				 * 등록이 아닌 IP-RENEWAL의 경우 DDNS Process에서 처리하도록 수정
				 * 
				 * domain=(String)device.get("fullDomain"); addr =(String)device.get("addr"); //
				 * System.out.println("domain>>"+ domain); // System.out.println("addr>>"+
				 * addr); final String cmd = "/usr/local/kttzfm/user_data_ns.sh";//shell script
				 * ( DDNS 관련 스크립트 호출) if(new File(cmd).exists()) { CommandLine cmdLine = new
				 * CommandLine(cmd); //(등록할 맥주소 또는 도메인 명칭) cmdLine.addArgument(domain,false);
				 * cmdLine.addArgument(addr,false); this.execAsync(cmdLine); } } }
				 */
			}

			msg = "정상적으로 처리 되었습니다.";
			result = "success";
		} while (false);

		response.put("msg", msg);
		return result;
	}

	public String KTTDDNS_COMMODITY_LIST(Map<String, Object> request, Map<String, Object> response) {
		List<Commodity> commodityList = commodityService.selectCommodity();

		ArrayList<String> videoDeviceList = new ArrayList<String>();
		ArrayList<String> swDeviceList = new ArrayList<String>();

		for (int i = 0; i < commodityList.size(); i++) {
			videoDeviceList.add(commodityList.get(i).getCode());
			if (commodityList.get(i).getAuto_reg_swcontroller() == 1)
				swDeviceList.add(commodityList.get(i).getCode());
		}

		response.put("videoDeviceList", videoDeviceList);
		response.put("swDeviceList", swDeviceList);
		response.put("msg", "정상적으로 처리 되었습니다.");

		return "success";
	}

	/*
	 * public String KTTDDNS_SERVICENO_TO_DEVICE_PLAY_URL_CMS(Map<String, Object>
	 * request, Map<String, Object> response) { String serviceNo = (String)
	 * request.get("serviceNo"); String eventTime = (String)
	 * request.get("eventTime"); Integer guardStatus = (Integer)
	 * request.get("guardStatus"); String empNo = (String) request.get("empNo");
	 * boolean fwVer300UnderFlag = false; if (serviceNo == null ||
	 * serviceNo.length() != 8 || eventTime == null || eventTime.length() != 14 ||
	 * !StringUtils.isNumeric(eventTime) || empNo == null || empNo.length() == 0) {
	 * response.put("msg", "포맷 에러 등으로 실패하였습니다."); return "fail"; } List<Map<String,
	 * Object>> deviceListOrg = apiService.selectDeviceWhereServicenoCms(serviceNo);
	 * 
	 * if (deviceListOrg == null || deviceListOrg.size() == 0) { response.put("msg",
	 * serviceNo + " Service No로 등록된 영상장비가 없습니다."); return "noregist"; }
	 * 
	 * if (deviceListOrg.size() > 5) { response.put("msg",
	 * "등록된 영상장비 개수가 5대를 초과하여 영상관제가 불가능합니다."); return "overcount"; }
	 * 
	 * Map<String, Object> setup = setupService.selectSetup();
	 * 
	 * try { Date eventTimeDate = new
	 * SimpleDateFormat("yyyyMMddHHmmss").parse(eventTime); Calendar limitTime =
	 * Calendar.getInstance(); limitTime.setTime(eventTimeDate);
	 * limitTime.set(Calendar.HOUR, 0); limitTime.set(Calendar.MINUTE, 0);
	 * limitTime.set(Calendar.SECOND, 0); limitTime.add(Calendar.DATE, (int)
	 * setup.get("eventtime_limit_day")); if (logger.isDebugEnabled())
	 * logger.debug("limitTime : " + Integer.toString(limitTime.get(Calendar.YEAR))
	 * + " - " + Integer.toString(limitTime.get(Calendar.MONTH)) + " - " +
	 * Integer.toString(limitTime.get(Calendar.DAY_OF_MONTH)) + " - " +
	 * Integer.toString(limitTime.get(Calendar.HOUR_OF_DAY)) + " - " +
	 * Integer.toString(limitTime.get(Calendar.MINUTE)) + " - " +
	 * Integer.toString(limitTime.get(Calendar.SECOND))); Calendar now =
	 * Calendar.getInstance(); if (logger.isDebugEnabled()) logger.debug("now : " +
	 * Integer.toString(now.get(Calendar.YEAR)) + " - " +
	 * Integer.toString(now.get(Calendar.MONTH)) + " - " +
	 * Integer.toString(now.get(Calendar.DAY_OF_MONTH)) + " - " +
	 * Integer.toString(now.get(Calendar.HOUR_OF_DAY)) + " - " +
	 * Integer.toString(now.get(Calendar.MINUTE)) + " - " +
	 * Integer.toString(now.get(Calendar.SECOND))); if (now.compareTo(limitTime) ==
	 * 1) { response.put("msg", "영상관제 가능 기간(설정시간:" + (int)
	 * setup.get("eventtime_limit_day") + "일)이 초과되었습니다."); return "unablePlayTime";
	 * } } catch (ParseException e) { if (logger.isDebugEnabled())
	 * logger.debug(e.getLocalizedMessage()); return "unablePlayTime"; }
	 * 
	 * List<Map<String, Object>> deviceList = new ArrayList<>(); for (Map<String,
	 * Object> deviceItem : deviceListOrg) { final String apiMac =
	 * Encryptions.remakeMac((String) deviceItem.get("macAddress"), false); final
	 * String domain = (String) deviceItem.get("fullDomain"); final String port =
	 * (String) deviceItem.get("port"); final int limitTime = 60 * (int)
	 * setup.get("devplaykey_limit_min"); final String timeStr =
	 * String.format("%08X", (System.currentTimeMillis() / 1000) + limitTime); //
	 * (60 // * // (int) // setup.get("devplaykey_limit_min"))); // int limitTime =
	 * 60 * (int) setup.get("devplaykey_limit_min"); String otp1 = ""; String chkMa
	 * = "";
	 * 
	 * int device_ver = 0; try {
	 * //System.out.println(deviceItem.get("RawDeviceVer")); if(
	 * deviceItem.containsKey("RawDeviceVer")) { java.util.StringTokenizer stk = new
	 * java.util.StringTokenizer((String) deviceItem.get("RawDeviceVer"),".");
	 * if(stk.countTokens() == 3) { Pattern p = Pattern.compile("\\d+"); Matcher m =
	 * p.matcher(stk.nextToken()); //가장 앞에 펌웨어 버전 자리의 수가 3이면 3.0.0이상의기준을 만족
	 * 
	 * if(m.find()) { if(Integer.valueOf(m.group()) >=3) { fwVer300UnderFlag =
	 * false; }else { fwVer300UnderFlag = true; } device_ver =
	 * Integer.valueOf(m.group()) * 100000;
	 * 
	 * }
	 * 
	 * m= p.matcher(stk.nextToken()); if(m.find()) { device_ver +=
	 * Integer.valueOf(m.group()) * 1000;
	 * 
	 * } m= p.matcher(stk.nextToken()); if(m.find()) { device_ver +=
	 * Integer.valueOf(m.group()) * 1;
	 * 
	 * }
	 * 
	 * }else { fwVer300UnderFlag = false; }
	 * 
	 * }else { device_ver = Integer.parseInt((String) deviceItem.get("deviceVer"));
	 * if (device_ver < 300) fwVer300UnderFlag = true; else fwVer300UnderFlag =
	 * false; }
	 * 
	 * 
	 * }catch (Exception e) {
	 * 
	 * 
	 * }
	 * 
	 * if (device_ver < 3000000) { // masterkey 발급 && chkMa = 1 otp1 =
	 * Encryptions.encryptMasterkey(Encryptions.getSHA(apiMac + "KTT_MASTER", 1));
	 * chkMa = "1"; } else if(device_ver >= 3000000) {//if (device_ver >= 3.0.0) if
	 * (apiMac.length() != 12) { response.put("msg", "잘못된 맥주소입니다."); return "fail";
	 * }
	 * 
	 * Selfas oldSelfas = selfasService.selectSelfasWhereMacAndUserlevelCms(apiMac,
	 * 2); String certnum = Integer.toString(Encryptions.generateCertnum(8)); if
	 * (oldSelfas == null) { Selfas newSelfas = new Selfas();
	 * newSelfas.setMac(apiMac); newSelfas.setCertnum(certnum);
	 * newSelfas.setUser_level(2); if (!selfasService.insertSelfas(newSelfas)) {
	 * response.put("msg", "AS Code 생성에 실패하였습니다."); return "fail"; } // otp1 =
	 * newSelfas.getCertnum(); } else { oldSelfas.setCertnum(certnum);
	 * oldSelfas.setUser_level(2); if
	 * (!selfasService.updateSelfasCertnum(oldSelfas)) { response.put("msg",
	 * "AS Code 생성에 실패하였습니다."); return "fail"; } // otp1 = oldSelfas.getCertnum(); }
	 * otp1 = selfasService.selectSelfasWhereMacAndUserlevelCmsCertnum(apiMac, 2);
	 * // ascode - DB에서 // CertNum을 읽어 // 오는 부분으로 , 위 // 주석으로 변경 가능함. chkMa = "0"; }
	 * String otp = Encryptions.makeOtp(otp1, timeStr); String url =
	 * "kttcmslite64://KTT_GUARD?domain=" + domain + "&port=" + port + "&otp=" + otp
	 * + "&eventtime=" + eventTime + "&limitTime=" + limitTime + "&guardstatus=" +
	 * guardStatus + "&chkMa=" + chkMa;
	 * 
	 * StringBuffer url = new StringBuffer(512); Map<String, Object> device = new
	 * HashMap<>(); device.put("serviceNo", deviceItem.get("serviceNo"));
	 * device.put("macAddress", apiMac); try (Formatter fm = new Formatter(url)) {
	 * fm.format(
	 * "kttcmslite64://KTT_GUARD?domain=%s&port=%s&otp=%s&eventtime=%s&limitTime=%d&guardstatus=%d&chkMa=%s",
	 * domain, port, otp, eventTime, limitTime, guardStatus, chkMa);
	 * device.put("url", url.toString());
	 * 
	 * } catch (Exception e) { //response.put("msg", "AS Code 생성에 실패하였습니다."+(
	 * logger.isDebugEnabled() ? e.getLocalizedMessage() :"")); // return "fail";
	 * device.put("url","kttcmslite64://"); } deviceList.add(device); url = null;
	 * device = null; }
	 * 
	 * response.put("deviceList", deviceList); response.put("msg", "성공되었습니다.");
	 * return "success"; }
	 */
	public String KTTDDNS_SERVICENO_TO_DEVICE_PLAY_URL_CMS(Map<String, Object> request, Map<String, Object> response) {
		String serviceNo = (String) request.get("serviceNo");
		String eventTime = (String) request.get("eventTime");
		Integer guardStatus = (Integer) request.get("guardStatus");
		String empNo = (String) request.get("empNo");
		StringBuffer msg = new StringBuffer(512);
		if (serviceNo == null || serviceNo.length() != 8 || eventTime == null || eventTime.length() != 14
				|| !StringUtils.isNumeric(eventTime) || empNo == null || empNo.length() == 0) {
			response.put("msg", "포맷 에러 등으로 실패하였습니다.");
			return "fail";
		}
		List<Map<String, Object>> deviceListOrg = apiService.selectDeviceWhereServicenoCms(serviceNo);

		if (deviceListOrg == null || deviceListOrg.size() == 0) {
			response.put("msg", serviceNo + " Service No로 등록된 영상장비가 없습니다.");
			return "noregist";
		}

		if (deviceListOrg.size() > 5) {
			response.put("msg", "등록된 영상장비 개수가 5대를 초과하여 영상관제가 불가능합니다.");
			return "overcount";
		}

		Map<String, Object> setup = setupService.selectSetup();

		try {
			Date eventTimeDate = new SimpleDateFormat("yyyyMMddHHmmss").parse(eventTime);
			Calendar limitTime = Calendar.getInstance();
			limitTime.setTime(eventTimeDate);
			limitTime.set(Calendar.HOUR, 0);
			limitTime.set(Calendar.MINUTE, 0);
			limitTime.set(Calendar.SECOND, 0);
			limitTime.add(Calendar.DATE, (int) setup.get("eventtime_limit_day"));
			if (logger.isDebugEnabled())
				logger.debug("limitTime : " + Integer.toString(limitTime.get(Calendar.YEAR)) + " - "
						+ Integer.toString(limitTime.get(Calendar.MONTH)) + " - "
						+ Integer.toString(limitTime.get(Calendar.DAY_OF_MONTH)) + " - "
						+ Integer.toString(limitTime.get(Calendar.HOUR_OF_DAY)) + " - "
						+ Integer.toString(limitTime.get(Calendar.MINUTE)) + " - "
						+ Integer.toString(limitTime.get(Calendar.SECOND)));
			Calendar now = Calendar.getInstance();
			if (logger.isDebugEnabled())
				logger.debug("now : " + Integer.toString(now.get(Calendar.YEAR)) + " - "
						+ Integer.toString(now.get(Calendar.MONTH)) + " - "
						+ Integer.toString(now.get(Calendar.DAY_OF_MONTH)) + " - "
						+ Integer.toString(now.get(Calendar.HOUR_OF_DAY)) + " - "
						+ Integer.toString(now.get(Calendar.MINUTE)) + " - "
						+ Integer.toString(now.get(Calendar.SECOND)));
			if (now.compareTo(limitTime) == 1) {
				response.put("msg", "영상관제 가능 기간(설정시간:" + (int) setup.get("eventtime_limit_day") + "일)이 초과되었습니다.");
				return "unablePlayTime";
			}
		} catch (ParseException e) {
			if (logger.isDebugEnabled())
				logger.debug(e.getLocalizedMessage());
			return "unablePlayTime";
		}
		final int limitTime = 60 * (int) setup.get("devplaykey_limit_min");
		final String timeStr = String.format("%08X", (System.currentTimeMillis() / 1000) + limitTime); // (60
		List<Map<String, Object>> deviceList = new ArrayList<>();
		deviceListOrg.parallelStream().forEach(deviceItem -> {
			Map<String, Object> device = new HashMap<>();
			final String apiMac = deviceItem.containsKey("macAddress")
					? Encryptions.remakeMac((String) deviceItem.get("macAddress"), false)
					: "";
			final String domain = deviceItem.containsKey("fullDomain") ? (String) deviceItem.get("fullDomain") : "";
			final String port = deviceItem.containsKey("port") ? (String) deviceItem.get("port") : "";
			String otp1 = "";
			String chkMa = "";
			int device_ver = 0;
			if (apiMac.length() == 12) {
				try {
					if (deviceItem.containsKey("RawDeviceVer")) {
						final java.util.StringTokenizer stk = new java.util.StringTokenizer(
								(String) deviceItem.get("RawDeviceVer"), ".");
						Pattern p = Pattern.compile("\\d+");
						Matcher m = p.matcher(stk.nextToken()); // 가장 앞에 펌웨어 버전 자리의 수가 3이면 3.0.0이상의기준을 만족
						if (m.find()) {
							if (Integer.valueOf(m.group()) >= 3) {
								Selfas oldSelfas = selfasService.selectSelfasWhereMacAndUserlevelCms(apiMac, 2);
								String certnum = Integer.toString(Encryptions.generateCertnum(8));
								if (oldSelfas == null) {
									Selfas newSelfas = new Selfas();
									newSelfas.setMac(apiMac);
									newSelfas.setCertnum(certnum);
									newSelfas.setUser_level(2);
									if (!selfasService.insertSelfas(newSelfas)) {
										msg.append(apiMac).append("FAIL :  AS Code 생성에 실패하였습니다. HINT [DB]");
									} else {
										otp1 = selfasService.selectSelfasWhereMacAndUserlevelCmsCertnum(apiMac, 2); // ascode
																													// -
																													// DB에서
									}
									// otp1 = newSelfas.getCertnum();
								} else {
									oldSelfas.setCertnum(certnum);
									oldSelfas.setUser_level(2);
									if (!selfasService.updateSelfasCertnum(oldSelfas)) {
										msg.append(apiMac).append("FAIL :  AS Code 생성에 실패하였습니다.HINT [DB]");
									} else {
										otp1 = selfasService.selectSelfasWhereMacAndUserlevelCmsCertnum(apiMac, 2); // ascode
																													// -
																													// DB에서
									}
									// otp1 = oldSelfas.getCertnum();
								}
								// CertNum을 읽어 오는 부분으로 , 위 주석으로 변경 가능함.
								chkMa = "0";
							} else {
								otp1 = Encryptions.encryptMasterkey(Encryptions.getSHA(apiMac + "KTT_MASTER", 1));
								chkMa = "1";
							}
							device_ver = Integer.valueOf(m.group()) * 1000000;
						}
						m = p.matcher(stk.nextToken());
						if (m.find()) {
							device_ver += Integer.valueOf(m.group()) * 1000;
						}
						m = p.matcher(stk.nextToken());
						if (m.find()) {
							device_ver += Integer.valueOf(m.group()) * 1;
						}
					}
				} catch (Exception e) {
					device_ver = 0;
				}
				StringBuffer url = new StringBuffer(512);

				if (logger.isDebugEnabled() && deviceItem.containsKey("serviceNo") == false)
					device.put("serviceNo", "");
				else
					device.put("serviceNo", deviceItem.get("serviceNo"));

				if (logger.isDebugEnabled()) {
					if (deviceItem.containsKey("addr") == false)
						device.put("IP", "");
					else
						device.put("IP", deviceItem.get("addr"));

				}
				device.put("macAddress", apiMac);
				try (Formatter fm = new Formatter(url)) {
					fm.format(
							"kttcmslite64://KTT_GUARD?domain=%s&port=%s&otp=%s&eventtime=%s&limitTime=%d&guardstatus=%d&chkMa=%s",
							domain, port, Encryptions.makeOtp(otp1, timeStr), eventTime, limitTime, guardStatus, chkMa);
					device.put("url", url.toString());

				} catch (Exception e) {
					if (logger.isDebugEnabled()) {
						msg.append("\n").append(e.getLocalizedMessage());
					}
					device.put("url", "kttcmslite64://");
				}
				url = null;
			} else {
				msg.append(apiMac).append("FAIL : 잘못된 맥주소입니다.");
			}

			device.put("serviceNo", deviceItem.get("serviceNo"));
			device.put("macAddress", apiMac);
			deviceList.add(device);
			device = null;
		});

		response.put("deviceList", deviceList);

		if (msg.toString().length() != 0 || deviceList.size() == 0) {
			response.put("msg", msg.toString());
		} else
			response.put("msg", "성공되었습니다.");

		return deviceList.size() == 0 ? "fail" : "success";

	}

	public String KTTDDNS_CLIENT_ACCESSLOG(Map<String, Object> request, Map<String, Object> response) {
		String result = "success";
		java.sql.Timestamp date;

		try {
			if (request.containsKey("date")) {
				Object o = request.get("date");
				try {
					if (o instanceof java.sql.Timestamp) {
						date = (java.sql.Timestamp) o;
					} else if (o instanceof Long) {
						date = new java.sql.Timestamp((Long) o);
					} else if (o instanceof Integer) {
						date = new java.sql.Timestamp((Integer) o);
					} else if (o instanceof String) {
						date = java.sql.Timestamp.valueOf((String) o);
					} else {
						date = new Timestamp(System.currentTimeMillis());
					}
				} catch (Exception e) {
					date = new Timestamp(System.currentTimeMillis());
				}
			}
			if (request.containsKey("onlyReport")) // KTTDDNS_CLIENT_ACCESSLOG(통계 기록 모드)
				if (request.containsKey("mac_list")) {
					ArrayList<String> list = (ArrayList<String>) (request.get("mac_list"));
					String app_name = String.valueOf(request.get("app_name"));
					Integer mobile_type = Integer.valueOf(request.get("mobile_type").toString());
					List<String> mlist = (List<String>) apiService.updateAppStaistics(list, app_name, mobile_type);
					// System.out.println(mlist.toString());
					if (mlist != null) {
						response.put("filtered_mac_address", mlist);
					}
					if (Boolean.valueOf(request.get("onlyReport").toString()) == true) {
						return "success";
					}
				}
		} catch (Exception e) {
			e.printStackTrace();
			if (Boolean.valueOf(request.get("onlyReport").toString()) == true) {
				return "fail";
			}
		}

		if (request.containsKey("mac")) {
			Object o;
			String mac = (String) request.get("mac");
			response.put("mac", mac);

			try {
				int intType = -1;
				boolean isCLientAPI = false;
				if (request.containsKey("client_type") && request.containsKey("register_type")) {
					// 사용자 관리 기능 3안
					KTTDDNS_CLIENT_ACCESSLOG_looger.info("mac {} \t client_type {} \t register_type {}",
							request.get("mac"), request.get("client_type"), request.containsKey("register_type"));

					response.put("client_type", request.get("client_type"));
					o = request.get("client_type");
					if (o instanceof String) {
						String strType = (String) o;
						if (strType.matches("-?\\d+(\\.\\d+)?")) // match a number with optional '-' and
						// decimal.
						{
							intType = Integer.parseInt(strType);

							isCLientAPI = true;
						}
					} else if (o instanceof Integer) {
						intType = (Integer) o;
						isCLientAPI = true;
					} else if (o instanceof Short) {
						intType = ((Integer) o).shortValue();
						isCLientAPI = true;
					}
					if (isCLientAPI && request.containsKey("id")) { // CLINET 용 (주로 : 모바일단말기)

						switch (intType) {
						case 0: // 통합 앱 app_date 컬럼과 app_access_id컬럼값을 갱신한다.
							result = clientAccessLogService.insert_update_ClientAccessLogTbl(
									ClientAccessLogService.TYPE_APP_ACCESS_LOG, (String) request.get("id"),
									Encryptions.remakeMac(mac, true), String.valueOf(request.get("register_type"))) > 0
											? "success"
											: "fail";
							break;
						case 1: // cms_date 컬럼과 cms_access_id컬럼값을 갱신한다.,,RESERVED
							result = clientAccessLogService.insert_update_ClientAccessLogTbl(
									ClientAccessLogService.TYPE_CMS_ACCESS_LOG, (String) request.get("id"),
									Encryptions.remakeMac(mac, true), String.valueOf(request.get("register_type"))) > 0
											? "success"
											: "fail";
							break;
						case 2:
							result = clientAccessLogService.insert_update_ClientAccessLogTbl(
									ClientAccessLogService.TYPE_DEVICE_ACCESS_LOG, String.valueOf(o),
									Encryptions.remakeMac(mac, true), "fromDevice") > 0 ? "success" : "fail";
							break;
						default:
							response.clear();
							result = "fail";
							break;
						}
					}
				} else if (request.containsKey("type")) { // 장비가 호출
					// 사용자 관리 기능 1안
					o = request.get("type");
					String strType = String.valueOf(request.get("type"));
					if (o instanceof String) {

						if (strType.matches("-?\\d+(\\.\\d+)?")) // match a number with optional '-' and
						// decimal.
						{
							intType = Integer.parseInt(strType);

							isCLientAPI = true;
						}
					} else if (o instanceof Integer) {
						intType = (Integer) o;
						isCLientAPI = true;
					} else if (o instanceof Short) {
						intType = ((Integer) o).shortValue();
						isCLientAPI = true;
					}
					// System.out.println("\n"+ intType);
					if (isCLientAPI && request.containsKey("protocol_type")) { // CLINET 용 (주로 : 모바일단말기)
						switch (intType) {
						/*
						 * case 0: // 장비 UI에서 호출 된 경우 result =
						 * clientAccessLogService.insert_update_ClientAccessLogTbl(
						 * ClientAccessLogService.TYPE_APP_ACCESS_LOG, "🖲 VideoSurveillanceDevice",
						 * Encryptions.remakeMac(mac, true), request.get("protocol_type").toString())> 0
						 * ? "success" : "fail"; break; case 1: //장비 웹 뷰어에서 호출 된 경우. result =
						 * clientAccessLogService.insert_update_ClientAccessLogTbl(
						 * ClientAccessLogService.TYPE_CMS_ACCESS_LOG, "🌐 WEB-VIEWER",
						 * Encryptions.remakeMac(mac, true), request.get("protocol_type").toString())> 0
						 * ? "success" : "fail";
						 */
						case 0:
						case 1:
							result = clientAccessLogService.insert_update_ClientAccessLogTbl(
									ClientAccessLogService.TYPE_DEVICE_ACCESS_LOG, strType,
									Encryptions.remakeMac(mac, true), request.get("protocol_type").toString()) > 0
											? "success"
											: "fail";
							break;
						default:
							result = "fail";
							break;
						}
					} else { // Device API (장비에서 호출 되는 API)
						// device_protocol_date 컬럼과 device_protocol_type컬럼을 갱신한다.
						// String strType = (String) request.get("type");
						result = clientAccessLogService.insert_update_ClientAccessLogTbl(
								ClientAccessLogService.TYPE_UNKOWN_ACCESS_LOG, strType,
								Encryptions.remakeMac(mac, true), "fromDevice") > 0 ? "success" : "fail";

					}
				}

				return result;
			} catch (Exception e) {
				// System.out.print("\n\nE");
				// e.printStackTrace();
				logger.debug(e.getLocalizedMessage());
			}
		}

		response.put("msg", "포맷 에러 등으로 실패하였습니다.");
		return "fail";
	}

	/*
	 * public String KTTDDNS_SERVICENO_TO_OTPLIST(Map<String, Object> request,
	 * Map<String, Object> response) {
	 * 
	 * @SuppressWarnings("unchecked") String phone =
	 * String.valueOf(request.get("phone")) ; ArrayList<String> serviceNoList =
	 * (ArrayList<String>) request.get("serviceNoList");
	 * 
	 * 
	 * String msg = ""; String result = "fail";
	 * 
	 * do { if (serviceNoList == null || phone == null || serviceNoList.size() == 0)
	 * break;
	 * 
	 * ArrayList<String> tmpList = new ArrayList<>(); for (String serviceNo :
	 * serviceNoList) { if (serviceNo.length() == 0 || tmpList.contains(serviceNo))
	 * continue; tmpList.add(serviceNo); }
	 * 
	 * String serviceNoListString = ""; for (int i = 0; i < tmpList.size(); i++) {
	 * if (serviceNoListString.length() == 0) serviceNoListString += ("'" +
	 * tmpList.get(i) + "'"); else serviceNoListString += (", '" + tmpList.get(i) +
	 * "'"); }
	 * 
	 * if (serviceNoListString.length() == 0) break;
	 * 
	 * List<Map<String, Object>> serviceList =
	 * apiService.selectDevicePhoneWhereInServiceno(serviceNoListString);
	 * logger.debug("serviceList :" + serviceList); if (serviceList == null ||
	 * serviceList.size() != tmpList.size()) { msg = "서비스번호가 존재하지 않습니다."; break; }
	 * 
	 * List<Map<String, Object>> tmpDeviceList =
	 * apiService.selectDevicePhoneWhereInServiceno(serviceNoListString);
	 * List<Map<String, Object>> deviceList = new ArrayList<>();
	 * 
	 * int count = 0;
	 * 
	 * for (Map<String, Object> tmpDevice : tmpDeviceList) { if
	 * (tmpDevice.get("mac") == null) { count++; continue; }
	 * 
	 * Map<String, Object> device = new HashMap<>(); device.put("macaddress",
	 * tmpDevice.get("mac")); device.put("phone", tmpDevice.get("phone"));
	 * 
	 * int macaddress =
	 * apiService.verString2Int(tmpDevice.get("macaddress").toString()); //int phone
	 * = apiService.verString2Int(tmpDevice.get("phone").toString());
	 * 
	 * logger.debug("macaddress :" + macaddress); logger.debug("phone :" + phone);
	 * int otp_yn = tmpDevice.containsKey("otp_yn") ? (int)
	 * tmpDevice.get("otp_yn"):0;
	 * 
	 * if( otp_yn > 0 && otp_yn <= 3 ) { logger.debug("????????????????????????"); }
	 * else { logger.debug("!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	 * 
	 * }
	 * 
	 * deviceList.add(device); count++; }
	 * 
	 * if (count < tmpDeviceList.size()) break;
	 * 
	 * response.put("deviceList", deviceList); msg = "정상적으로 처리 되었습니다."; result =
	 * "success"; } while (false);
	 * 
	 * response.put("msg", msg); return result; }
	 */

	//SJ OTP 일괄인증 기능 미인증 단말 요청 API요청20231129 / 추가20231210
	public String KTTDDNS_SERVICENO_TO_UNOTPLIST(Map<String, Object> request, Map<String, Object> response) {
		@SuppressWarnings("unchecked")

		ArrayList<String> serviceNoList = (ArrayList<String>) request.get("serviceNoList");
		String phone = String.valueOf(request.get("phone"));

		 logger.debug("phone :" + phone);
		if (serviceNoList == null || phone == null) {
			return "fail";
		}
		if (serviceNoList.size() == 0) {
			List<Map<String, Object>> deviceList = new ArrayList<>();
			response.put("deviceList", deviceList);
			deviceList = null;
			return "success";
		}

		StringBuffer serviceNoListString = new StringBuffer(1024);
		try {
			for (String serviceNo : serviceNoList) {
				if (serviceNo.length() > 7) {
					serviceNoListString.append("'");
					serviceNoListString.append(serviceNo);
					serviceNoListString.append("',");
				}
			}
			// 마지막 ,(쉼표)제거
			serviceNoListString.delete(serviceNoListString.toString().length() - 1,
					serviceNoListString.toString().length());

			if (serviceNoListString.toString().length() == 0) {
				List<Map<String, Object>> deviceList = new ArrayList<>();
				response.put("macList", deviceList);
				deviceList = null;
				return "success";
			}
		} catch (Exception e) {
			if (logger.isDebugEnabled())
				e.printStackTrace();
		}
		

		//ktt test server용
		List<Map<String, Object>> deviceListOrg = apiService.selectDeviceMacWhereInServicenoPhoneOTPKttTEST(serviceNoListString.toString(), phone);

		//ktt 상용 서버 용
		//List<Map<String, Object>> deviceListOrg = apiService.selectDevicePhoneWhereInServiceno(serviceNoListString.toString(), phone);
		
		
		
		if (deviceListOrg != null) {
			final List<Map<String, Object>> deviceList = new ArrayList<>();
			deviceListOrg.parallelStream().forEach(deviceItem -> {
				
				/*
				if (deviceItem != null && deviceItem.containsKey("domainType")) {
					int domainType = (int) deviceItem.get("domainType");
					
					String maker = deviceItem.get("maker").toString();
					String tmpMaker = "dahua";
					logger.debug("domainType : "+domainType);
					if ((domainType == 22 || domainType == 23)) // 'octdvr.co.kr' 20 'octnvr.co.kr' 21
					{
						logger.debug("telecopview 도메인 사용 장비");
						if(!maker.trim().equals(tmpMaker))
						{
							logger.debug("제조사가 dahua는 제외");
							//deviceList.add(deviceItem);
						}
					}
				}*/
				
				
				deviceList.add(deviceItem);
			});

			response.put("macList", deviceList);
		} else {
			response.put("macList", new ArrayList<>());
		}
		return "success";
	}

	//SJ OTP 일괄인증 기능1시간 이내 미인증 단말 요청 API요청20231129 / 추가20231211
		public String KTTDDNS_SERVICENO_TO_UNOTPLIST_1HOUR(Map<String, Object> request, Map<String, Object> response) {
			@SuppressWarnings("unchecked")

			ArrayList<String> serviceNoList = (ArrayList<String>) request.get("serviceNoList");
			String phone = String.valueOf(request.get("phone"));

			 logger.debug("phone :" + phone);
			if (serviceNoList == null || phone == null) {
				return "fail";
			}
			if (serviceNoList.size() == 0) {
				List<Map<String, Object>> deviceList = new ArrayList<>();
				response.put("deviceList", deviceList);
				deviceList = null;
				return "success";
			}

			StringBuffer serviceNoListString = new StringBuffer(1024);
			try {
				for (String serviceNo : serviceNoList) {
					if (serviceNo.length() > 7) {
						serviceNoListString.append("'");
						serviceNoListString.append(serviceNo);
						serviceNoListString.append("',");
					}
				}
				// 마지막 ,(쉼표)제거
				serviceNoListString.delete(serviceNoListString.toString().length() - 1,
						serviceNoListString.toString().length());

				if (serviceNoListString.toString().length() == 0) {
					List<Map<String, Object>> deviceList = new ArrayList<>();
					response.put("macList", deviceList);
					deviceList = null;
					return "success";
				}
			} catch (Exception e) {
				if (logger.isDebugEnabled())
					e.printStackTrace();
			}
			//ktt test server용
			List<Map<String, Object>> deviceListOrg = apiService.selectDeviceMacWhereInServicenoPhoneOTP1HourKttTEST(serviceNoListString.toString(), phone);

			//ktt 상용 서버 용
			//List<Map<String, Object>> deviceListOrg = apiService.selectDeviceOTP1HourTimeWhereInServiceno(serviceNoListString.toString(), phone);

			
			if (deviceListOrg != null) {
				final List<Map<String, Object>> deviceList = new ArrayList<>();
				deviceListOrg.parallelStream().forEach(deviceItem -> {
					
					deviceList.add(deviceItem);
				});

				response.put("macList", deviceList);
			} else {
				response.put("macList", new ArrayList<>());
			}
			return "success";
		}
		
	/**
	 * 
	 * @Method Name : KTTDDNS_INFECTION_LOG
	 * @작성일 : 2023. 2. 22.
	 * @작성자 : Foryoucom
	 * @변경이력 :
	 * @Method 설명 :녹화기 등 장비에서 보내는 로그로 장비가 외부 공격 등에 의하여 실행파일의 변조 등을 리포트 시 이를 특정 파일에
	 *         기록 등을 실행함.
	 * @param request
	 *            DDNS API 에 따른 파라미터
	 * @param response
	 *            success 등 전송해야 할 파라미터
	 * @return
	 */
	private Lock ER_Report_lock = new ReentrantLock();

	public String KTTDDNS_INFECTION_LOG(Map<String, Object> request, Map<String, Object> response) {
		String result = "success";
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		// HttpServletRequest httpServletRequest =
		// ((ServletRequestAttributes) requestAttributes).getRequest();
		final String fd_loc = "/usr/local/monitoring_logs/monitoring.log";
		final String filter_fd_loc = context.getRealPath("/WEB-INF/classes/EMG_IF_Filter_KeyWord"); // httpServletRequest.getServletContext().getRealPath("EMG_IF_Filter_KeyWord");

		// logger.debug(request.toString());
		// response.clear();

		if (request.containsKey("macAddress") && request.containsKey("infection_type")) {

			try (ImergencyReport imergencyReport = new ImergencyReport();) {
				Boolean bWriteMonitoringLog = true;
				StringBuffer infection_log_msg = new StringBuffer();
				String domain = request.containsKey("domain") ? request.get("domain").toString() : "";
				String macAddress = request.get("macAddress").toString();
				Integer infection_type = Integer.valueOf(request.get("infection_type").toString());
				String apiMac = Encryptions.remakeMac(macAddress, true);
				String log_data = request.containsKey("log_data") ? String.valueOf(request.get("log_data")) : "";
				infection_log_msg.append("INFECTION MSG ➡️ ").append("MAC : ").append(apiMac).append(" Type : ")
						.append(infection_type).append(" DOMAIN: ").append(domain);
				if (request.containsKey("is_recovery_infection")) {
					KTTDDNS_INFECTION_LOG_looger.info("infection : is_recovery_infection {} infection : Log {} ",
							request.get("is_recovery_infection"), log_data);
					infection_log_msg.append(" is_recovery_infection ").append(request.get("is_recovery_infection"));
				} else {
					KTTDDNS_INFECTION_LOG_looger.info("infection : Log {}", log_data);
				}
				imergencyReport.setLog_dump(log_data);
				imergencyReport.setMac(apiMac);
				imergencyReport.setIm_evt_body(infection_log_msg.toString());
				imergencyReport.setIm_evt_type(infection_type.shortValue());

				File file = new File(filter_fd_loc);
				if (log_data.length() > 0 && file.exists()) {
					// StringBuilder ftp_filter_keyword = new StringBuilder();
					String line;
					FileReader reader = new FileReader(file);
					BufferedReader breader = new BufferedReader(reader);
					final String[] log_data_split = log_data.split("\\s+");
					while (bWriteMonitoringLog && (line = breader.readLine()) != null) {
						if (line.length() > 0 /* log_data.matches(".*"+ftp_filter_keyword+".*") */ ) {
							// ftp_filter_keyword.append(".*").append(line).append(".*");
							for (String S : log_data_split) {
								// if( S.matches(ftp_filter_keyword.toString())==true )
								if (line.contains(S)) {
									bWriteMonitoringLog = false;
									break;
								}
							}
						}
						// ftp_filter_keyword.setLength(0);
					}
					breader.close();
				}
				logger.debug("infection : Domain {} MacAddress {} Type {} ", domain, macAddress, infection_type);
				logger.debug("log_data {}", log_data);
				if (log_data.length() > 0 && bWriteMonitoringLog) {
					final String d = LocalDateTime.now(ZoneId.of("GMT+09:00"))
							.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

					imergenyReportService.insertImergenyReport(imergencyReport);

					for (int i = 0; i < 3; i++) {
						if (ER_Report_lock.tryLock(20, TimeUnit.SECONDS)) {
							// 해당 로그를 이용하는 다른 프로세스 있으므로 3회 쓰기를 시도 하여 기록 하도록 함...
							try {
								FileOutputStream _fStream = new FileOutputStream(fd_loc, true); // location , append
																								// flag
								OutputStreamWriter _oWriter = new OutputStreamWriter(_fStream, "UTF-8");
								BufferedWriter _fWriter = new BufferedWriter(_oWriter);
								FileLock f1;
								f1 = _fStream.getChannel().tryLock();

								if (f1 != null) {

									_fWriter.write(d);
									_fWriter.write(" EMG_IF ");
									_fWriter.write(apiMac);
									_fWriter.write(" ");
									switch (infection_type) {
									case 1: // File
									case 2: // Process
									case 3: // File & Process
									case 4: // malignant IP
										_fWriter.write(infection_type.toString());
										break;
									default:
										_fWriter.write("Unkwon");

										break;
									}
									_fWriter.write(System.lineSeparator());

									_fWriter.flush();
									f1.release();
									_fWriter.close();
									ER_Report_lock.unlock();
								}
								// response.put("action", "");//Reserved
								// response.put("msg", ""); //Reserved
								infection_log_msg = null;
								break;
							} catch (FileNotFoundException e) {
								try {
									Thread.sleep(100);
								} catch (InterruptedException ie) {
									Thread.currentThread().interrupt();
									ER_Report_lock.unlock();
								}
							} catch (IOException e) {
								try {
									Thread.sleep(200);
								} catch (InterruptedException ie) {
									Thread.currentThread().interrupt();
									ER_Report_lock.unlock();
								}
							} catch (Exception e) {
								ER_Report_lock.unlock();
							}
						}
					}
				} else {
					logger.debug("Filtering Word \n");
				}
			} catch (Exception e) {
				// e.getStackTrace();
				result = "fail";
				// logger.debug(e.toString());
				response.clear();
				logger.debug(e.getLocalizedMessage());
			}
		} else if (request.containsKey("macAddress") && request.containsKey("show_report")) {
			// RequestAttributes requestAttributes =
			// RequestContextHolder.getRequestAttributes();
			HttpServletResponse httpServelResponse = ((ServletRequestAttributes) requestAttributes).getResponse();
			try {
				final CSVPrinter csvPrinter = new CSVPrinter(httpServelResponse.getWriter(), CSVFormat.EXCEL);
				httpServelResponse.setCharacterEncoding("UTF-8");
				httpServelResponse.setContentType("text/csv; charset=UTF-8");

				String exportFileName = "🚨_IMG_IF_Report" + LocalDate.now().toString() + ".csv";
				httpServelResponse.setHeader("Content-disposition", "attachment;filename=" + exportFileName);
				csvPrinter.print("DATE");
				csvPrinter.print("MAC");
				csvPrinter.print("Event");
				csvPrinter.print("Dump");
				csvPrinter.println();

				imergenyReportService.selectImergencyRepory(new ResultHandler<ImergencyReport>() {

					@Override
					public void handleResult(ResultContext<? extends ImergencyReport> context) {
						if (csvPrinter != null && context.getResultObject() != null) {
							try {
								// csvPrinter.printRecord(new
								// ArrayList<>(resultContext.getResultObject().values()));
								// csvPrinter.printRecord(resultContext);
								ImergencyReport report = context.getResultObject();
								{
									csvPrinter.print(report.getDate());
									csvPrinter.print(report.getMac_macaddr());
									csvPrinter.print(report.getIm_evt_body());
									csvPrinter.print(report.getLog_dump());
									csvPrinter.println();
								}

							} catch (IOException e) {
								// TODO Auto-generated catch block
								StringWriter error = new StringWriter();
								e.printStackTrace(new PrintWriter(error));
								logger.debug("Exception {}", error.toString());
							}
						}
					}
				}, request.containsKey("search_day") ? request.get("search_day").toString() : "");
				try {
					csvPrinter.flush();
					csvPrinter.close();
				} catch (IOException e) {

					StringWriter error = new StringWriter();
					e.printStackTrace(new PrintWriter(error));
					logger.debug("Exception {}", error.toString());
				}
			} catch (IOException e) {
				result = "fail";
				// logger.debug(e.toString());
				StringWriter error = new StringWriter();
				e.printStackTrace(new PrintWriter(error));
				logger.debug("Exception {}", error.toString());
			}
		} else {
			logger.warn("KTTDDNS_INFECTION_LOG REQUEST {}", request.toString());
		}
		return result;
	}

	/**
	 * 
	 * @Method Name : KTTDDNS_CHECK_IP_VALIDATION
	 * @작성일 : 2023. 3. 16.
	 * @작성자 : Foryoucom
	 * @변경이력 :
	 * @Method 설명 :
	 * @param request
	 * @param response
	 */
	private static Pattern VALID_IPV4_PATTERN = null;
	private static Pattern VALID_IPV6_PATTERN1 = null;
	private static Pattern VALID_IPV6_PATTERN2 = null;
	private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
	private static final String ipv6Pattern1 = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";
	private static final String ipv6Pattern2 = "^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)$";

	static {
		try {
			VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
			VALID_IPV6_PATTERN1 = Pattern.compile(ipv6Pattern1, Pattern.CASE_INSENSITIVE);
			VALID_IPV6_PATTERN2 = Pattern.compile(ipv6Pattern2, Pattern.CASE_INSENSITIVE);
		} catch (PatternSyntaxException e) {
			System.out.println("Neither");
		}
	}
	private final static int TIME_OUT = 10000;
	private final static int PORT = 53;
	private final static int BUF_SIZE = 8192;

	private static void query(String dnsServerIP, String domainName, List<String> ipList)
			throws SocketTimeoutException, IOException {
		DatagramSocket socket = new DatagramSocket(0);
		socket.setSoTimeout(TIME_OUT);

		ByteArrayOutputStream outBuf = new ByteArrayOutputStream(BUF_SIZE);
		DataOutputStream output = new DataOutputStream(outBuf);
		encodeDNSMessage(output, domainName);

		InetAddress host = InetAddress.getByName(dnsServerIP);
		DatagramPacket request = new DatagramPacket(outBuf.toByteArray(), outBuf.size(), host, PORT);

		socket.send(request);

		byte[] inBuf = new byte[BUF_SIZE];
		ByteArrayInputStream inBufArray = new ByteArrayInputStream(inBuf);
		DataInputStream input = new DataInputStream(inBufArray);
		DatagramPacket response = new DatagramPacket(inBuf, inBuf.length);

		socket.receive(response);

		decodeDNSMessage(input, ipList);

		socket.close();
	}

	private static void encodeDNSMessage(DataOutputStream output, String domainName) throws IOException {
		// transaction id
		output.writeShort(1);
		// flags
		output.writeShort(0x100);
		// number of queries
		output.writeShort(1);
		// answer, auth, other
		output.writeShort(0);
		output.writeShort(0);
		output.writeShort(0);

		encodeDomainName(output, domainName);

		// query type
		output.writeShort(1);
		// query class
		output.writeShort(1);

		output.flush();
	}

	private static void encodeDomainName(DataOutputStream output, String domainName) throws IOException {
		for (String label : StringUtils.split(domainName, '.')) {
			output.writeByte((byte) label.length());
			output.write(label.getBytes());
		}
		output.writeByte(0);
	}

	private static void decodeDNSMessage(DataInputStream input, List<String> ipList) throws IOException {
		// header
		// transaction id
		input.skip(2);
		// flags
		input.skip(2);
		// number of queries
		input.skip(2);
		// answer, auth, other
		short numberOfAnswer = input.readShort();
		input.skip(2);
		input.skip(2);

		// question record
		skipDomainName(input);
		// query type
		input.skip(2);
		// query class
		input.skip(2);

		// answer records
		for (int i = 0; i < numberOfAnswer; i++) {
			input.mark(1);
			byte ahead = input.readByte();
			input.reset();
			if ((ahead & 0xc0) == 0xc0) {
				// compressed name
				input.skip(2);
			} else {
				skipDomainName(input);
			}

			// query type
			short type = input.readShort();
			// query class
			input.skip(2);
			// ttl
			input.skip(4);
			short addrLen = input.readShort();
			if (type == 1 && addrLen == 4) {
				int addr = input.readInt();
				ipList.add(longToIp(addr));
			} else {
				input.skip(addrLen);
			}
		}
	}

	private static void skipDomainName(DataInputStream input) throws IOException {
		byte labelLength = 0;
		do {
			labelLength = input.readByte();
			input.skip(labelLength);
		} while (labelLength != 0);
	}

	private static String longToIp(long ip) {
		return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
	}

	public String KTTDDNS_CHECK_IP_VALIDATION(Map<String, Object> request, Map<String, Object> response) {
		String result = "fail";
		String ip, macAddress = "";
		StringBuilder log = new StringBuilder();
		final String fd_loc = "/usr/local/monitoring_logs/monitoring.log";
		List<String> ipList = null;
		if (request.containsKey("domain")) {
			ipList = new ArrayList<>();
			try {
				query("1.1.1.1", request.get("domain").toString(), ipList);
			} catch (SocketTimeoutException e) {
				ipList.clear();
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				ipList.clear();
				// e.printStackTrace();
			}
		}
		if (request.containsKey("ip")) {
			ip = String.valueOf(request.get("ip"));
			Matcher m1 = this.VALID_IPV4_PATTERN.matcher(ip);
			Matcher m12 = this.VALID_IPV6_PATTERN1.matcher(ip);
			Matcher m22 = this.VALID_IPV6_PATTERN2.matcher(ip);
			if (m1.matches() == false && (m12.matches() == false && m22.matches() == false)) {
				response.put("ip", ip);
				result = "fail";
			} else {
				if (request.containsKey("macAddress")) {
					macAddress = String.valueOf(request.get("macAddress"));
					macAddress = Encryptions.remakeMac(macAddress.trim(), true);
					try {
						RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
						HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes)
								.getRequest();
						String ftp_url_file = httpServletRequest.getServletContext().getRealPath("ftp_url");
						log.append("MAC_ADDRESS : ").append(macAddress).append(" IP : ").append(ip);
						logger.debug(log.toString());
						Boolean bIP_ValidCheck = apiService.insertCheckIPValidation(macAddress, ip, log.toString());
						if (bIP_ValidCheck == true) {
							if (ipList != null && ipList.size() > 0) {
								AtomicInteger ordinal = new AtomicInteger(0);
								ipList.forEach(s -> {
									if (s.equals(ip))
										ordinal.incrementAndGet();
								});
								if (ordinal.intValue() > 0) {
									result = "success";
								}
							} else
								result = "success";
						} else {
							File file = new File(ftp_url_file);
							if (file.exists()) {
								String site_url_loc;
								FileReader reader = new FileReader(file);
								BufferedReader breader = new BufferedReader(reader);
								result = "fail";
								while ((site_url_loc = breader.readLine()) != null) {
									// SubnetUtils subnetUtils = new SubnetUtils(site_url_loc.trim());
									if (site_url_loc.length() > 0) {
										if (site_url_loc.trim().contains(ip)) {
											result = "success";
											break;
										}
									}
								}
								breader.close();
							}
						}
					} catch (Exception e) {
						result = "fail";
						StringWriter error = new StringWriter();
						e.printStackTrace(new PrintWriter(error));
						logger.debug("Exception {}", error.toString());
					}
				}
			}
			logger.debug(request.toString());
			logger.debug(response.toString());
			if (result.equals("fail")) {
				HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder
						.currentRequestAttributes()).getRequest();
				String clientIp = apiService.getIp(httpServletRequest);
				for (int i = 0; i < 3; i++) {
					try {
						if (ER_Report_lock.tryLock(20, TimeUnit.SECONDS)) {
							// 해당 로그를 이용하는 다른 프로세스 있으므로 3회 쓰기를 시도 하여 기록 하도록 함...
							try {
								FileOutputStream _fStream = new FileOutputStream(fd_loc, true); // location , append
																								// flag
								OutputStreamWriter _oWriter = new OutputStreamWriter(_fStream, "UTF-8");
								BufferedWriter _fWriter = new BufferedWriter(_oWriter);
								FileLock f1;
								f1 = _fStream.getChannel().tryLock();
								KTTDDNS_INFECTION_LOG_looger.info(
										" KTTDDNS_CHECK_IP_VALIDATION[FAIL -clientIp {}] : ASK IP {} MacAddress {} ",
										clientIp, ip, macAddress);
								if (f1 != null) {
									String d = LocalDateTime.now(ZoneId.of("GMT+09:00"))
											.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
									_fWriter.write(d);
									_fWriter.write(" CHECK_IP_VALIDATION ");
									_fWriter.write(log.toString());
									_fWriter.write(" ");

									_fWriter.write(System.lineSeparator());

									_fWriter.flush();
									f1.release();
									_fWriter.close();
									ER_Report_lock.unlock();
								}
								break;
							} catch (FileNotFoundException e) {
								try {
									Thread.sleep(100);
								} catch (InterruptedException ie) {
									Thread.currentThread().interrupt();
									ER_Report_lock.unlock();
								}
							} catch (IOException e) {
								try {
									Thread.sleep(200);
								} catch (InterruptedException ie) {
									Thread.currentThread().interrupt();
									ER_Report_lock.unlock();
								}
							} catch (Exception e) {
								ER_Report_lock.unlock();
							}
						}
					} catch (InterruptedException e) {
						ER_Report_lock.unlock();
						StringWriter error = new StringWriter();
						e.printStackTrace(new PrintWriter(error));
						logger.debug("Exception {}", error.toString());
					}
				}

			}
		}

		return result;
	}

}
