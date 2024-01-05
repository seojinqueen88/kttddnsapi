package com.kttddnsapi.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.kttddnsapi.service.ApiService;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class Encryptions
{
	static org.slf4j.Logger logger =  LoggerFactory.getLogger(Encryptions.class.getSimpleName());
	/**
	 * 
	* @Method Name : encryptMasterkey
	* @작성일 : 2023. 4. 6. 
	* @작성자 : Foryoucom
	* @변경이력 :
	* @Method 설명 :
	* @param sha1str
	* @return
	 */
	public static String encryptMasterkey(String sha1str)//동작확인 필요...
	{
	    final int startNum = '0';
		final int numCount = 10;
		final int startLower = 'a';
		final int lowerCount = 'z' - 'a' + 1;
		final int startUpper = 'A';
		final int upperCount = 'Z' - 'A' + 1;
		final int totalCount = numCount + lowerCount + upperCount;
		final int e =  sha1str.length() / 2;
		final int limit_cond_1 =numCount;
		final int limit_cond_2 =numCount + lowerCount;
	    final int limit_cond_3 =startLower - limit_cond_1;
	    final int limit_cond_4 =startUpper - limit_cond_2;
	        
		String result = "";

		for(int i = 0; i < e && i < 20; i++)
		{
			char char1 = sha1str.charAt(i * 2);
			char char2 = sha1str.charAt(i * 2 + 1);
			int tmpBin = ((Character.digit(char1, 16) << 4) | Character.digit(char2, 16));
			int pos = tmpBin % totalCount;

			//if(pos < numCount)
			if(pos < limit_cond_1)
			{
				result += String.valueOf((char)(startNum + pos - (0)));
			}
			//else if(pos < (numCount + lowerCount))
			else if(pos < limit_cond_2)
			{
				//result += String.valueOf((char)(startLower + pos - (numCount)));
			  result += String.valueOf((char)( pos + limit_cond_3));
			}
			else
			{
			//	result += String.valueOf((char)(startUpper + pos - (numCount + lowerCount)));
			   result += String.valueOf((char)(pos + (limit_cond_4)));
			}
		}

		return result;
	}

	public static String getSHA(String input, int type)
	{
		String result = "";
		MessageDigest messageDigest = null;
		try
		{
			
			switch(type)
			{
			case 1:
				messageDigest = MessageDigest.getInstance("SHA-1");//sha-1의 출력값 길이는 160비트
				messageDigest.reset();
				messageDigest.update(input.getBytes("utf8"));
				//result = (String.format("%040x", new BigInteger(1, messageDigest.digest()))).toUpperCase();
				result = String.format("%040x", new Object[] { new BigInteger(1, messageDigest.digest()) }).toUpperCase();
				break;
			case 256:
			default:
				messageDigest = MessageDigest.getInstance("SHA-256");//sha-256의 출력값 길이는 256비트
				messageDigest.reset();
				messageDigest.update(input.getBytes("utf8"));
				//result = (String.format("%064x", new BigInteger(1, messageDigest.digest()))).toUpperCase();
				//logger.info("result : " +  result);
				result = String.format("%064x", new Object[] { new BigInteger(1, messageDigest.digest()) }).toUpperCase();
				
				break;
			}
		}
		catch(Exception e)
		{
		  StringWriter error = new StringWriter();
	      e.printStackTrace(new PrintWriter(error));
	      //log.debug("Exception {}", error.toString());
	      //e.printStackTrace();
		}
		messageDigest = null;
		return result;
	}
	
	// 202207 관제 -> 녹화기 접속 URL 생성 신규 암호화 
	public static String makeOtp(String masterKey, String timeStr)
	{
		String encString ="";
		try {
		byte[] masterKeyData = masterKey.getBytes();
		byte[] iv = new byte[16];
		byte[] key = new byte[32];
		String strKey = Encryptions.getSHA(timeStr, 256);
		String strIv = Encryptions.getSHA(timeStr, 1);
		byte[] tempKey = strKey.getBytes();
		byte[] tempIv = strIv.getBytes();
		System.arraycopy(tempKey, 0, key, 0, 32);
		System.arraycopy(tempIv, 0, iv, 0, 16);
		byte[] encString1 = Encryptions.AES256encrypt(masterKeyData, key, iv);
		encString = Base64.getEncoder().encodeToString(encString1);
		masterKeyData = null;
		iv = null;
		key = null;
		tempKey = null;
		tempIv = null;
		encString1 = null;
		} catch (Exception e) {
			//e.printStackTrace();
			if(logger.isDebugEnabled())
			{
				logger.error(e.getLocalizedMessage());

			}
				//log.error(e.getLocalizedMessage());
				encString = "";
		}

		return encString + timeStr;
	}
    
	public static byte[] AES256encrypt(byte[] text, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivParamSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);

        byte[] encrypted1 = cipher.doFinal(text);
		return encrypted1;
	}	
	public static void encAES256(byte data[], int size, byte key[], byte iv[])
	{
		Cipher cipher;

		try
		{
			SecretKey secureKey = new SecretKeySpec(key, "AES");
			cipher = Cipher.getInstance("AES/CTR/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(iv));
			byte[] srcData = new byte[size];
			System.arraycopy(data, 0, srcData, 0, size);
			byte[] encryptedData = cipher.doFinal(srcData);
			System.arraycopy(encryptedData, 0, data, 0, size);
		}
		catch (Exception e)
		{
			if(logger.isDebugEnabled())
			{
				logger.error("encAES256 ERROR = {}", e.getLocalizedMessage());

			}
				//log.error("encAES256 ERROR = {}", e.getLocalizedMessage());
			//System.out.println("encAES256 ERROR = " + e.toString());
		}
	}

	public static void decAES256(byte data[], int size, byte key[], byte iv[])
	{
		Cipher cipher;

		try
		{
			SecretKey secureKey = new SecretKeySpec(key, "AES");
			cipher = Cipher.getInstance("AES/CTR/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(iv));
			byte[] srcData = new byte[size];
			System.arraycopy(data, 0, srcData, 0, size);
			byte[] decryptedData = cipher.doFinal(srcData);
			System.arraycopy(decryptedData, 0, data, 0, size);
			srcData = null;
			decryptedData = null;
		}
		catch (Exception e)
		{
			if(logger.isDebugEnabled())
			{
				logger.error("encAES256 ERROR = {}", e.getLocalizedMessage());
			}
			//log.error("encAES256 ERROR = {}", e.getLocalizedMessage());
			//System.out.println("decAES256 ERROR = " + e.toString());
		}
	}

	public static String makeDevKey(String masterKey, String timeStr)
	{
		byte[] masterKeyData = masterKey.getBytes();
		byte[] iv = new byte[16];
		byte[] key = new byte[16];
		String strKey = Encryptions.getSHA(timeStr, 256);
		String strIv = Encryptions.getSHA(timeStr, 1);
		byte[] tempKey = strKey.getBytes();
		byte[] tempIv = strIv.getBytes();
		System.arraycopy(tempKey, 0, key, 0, 16);
		System.arraycopy(tempIv, 0, iv, 0, 16);
		Encryptions.encAES256(masterKeyData, masterKeyData.length, key, iv);

		//String encString = "";
		StringBuilder encString = new StringBuilder(1024);
	
		for(int i = 0; i < masterKeyData.length; i++)
		//	encString += String.format("%02X", masterKeyData[i]);
			encString.append(String.format("%02X", masterKeyData[i]));
		encString.append(timeStr);	
		//return encString + timeStr;
		return encString.toString();
	}

	public static String makeDevPlayKey(String masterKey, String timeStr, String eventTime)
	{
		byte[] masterKeyData = masterKey.getBytes();
		byte[] iv = new byte[16];
		byte[] key = new byte[16];
		String strKey = Encryptions.getSHA(timeStr + eventTime, 256);
		String strIv = Encryptions.getSHA(timeStr + eventTime, 1);
		byte[] tempKey = strKey.getBytes();
		byte[] tempIv = strIv.getBytes();
		System.arraycopy(tempKey, 0, key, 0, 16);
		System.arraycopy(tempIv, 0, iv, 0, 16);
		Encryptions.encAES256(masterKeyData, masterKeyData.length, key, iv);

		/*String encString = "";
		for(int i = 0; i < masterKeyData.length; i++)
			encString += String.format("%02X", masterKeyData[i]);

		return encString + timeStr;
		*/
		//String encString = "";
		StringBuilder encString  = new StringBuilder(1024);;
	
		for(int i = 0; i < masterKeyData.length; i++)
		//	encString += String.format("%02X", masterKeyData[i]);
			encString.append(String.format("%02X", masterKeyData[i]));
		encString.append(timeStr);	
		//return encString + timeStr;
		iv = null;
		key = null;
		
		return encString.toString();
	}

	public static String remakeMac(String mac, boolean isColon)
	{
		String noColonMac = (mac.replace(":", "")).toUpperCase();
		if(isColon == false)
			return noColonMac;

		String[] splitStr = noColonMac.split("(?<=\\G.{" + 2 + "})");
		String colonMac = StringUtils.join(splitStr, ":");

		return colonMac;
	}

	public static boolean isAvailableKey(String command, String requestKey)
	{
		if(requestKey == null || requestKey.equals("")) return false;

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHH");
		Calendar calendar = Calendar.getInstance();
		String currentTime = simpleDateFormat.format(calendar.getTime());
		String currentKey = getSHA((getSHA(currentTime, 256) + command), 256);

		Date currentDate = new Date();
		try
		{
			currentDate = simpleDateFormat.parse(currentTime);
			long previousTime = currentDate.getTime() - (60*60);
			Date previousDate = new Date(previousTime);
			String previousKey = getSHA((getSHA(simpleDateFormat.format(previousDate), 256) + command), 256);

			if(requestKey.equals(currentKey) || requestKey.equals(previousKey))
				return true;
			else
				return false;
		}
		catch(ParseException pe)
		{
		//	pe.printStackTrace();
			if(logger.isDebugEnabled())
			{
				logger.error("encAES256 ERROR = {}", pe.getLocalizedMessage());

			}
				//log.error("encAES256 ERROR = {}", pe.getLocalizedMessage());
			return false;
		}
	}

	public static int generateCertnum(int length)
	{
	//	String numStr = "1";
		StringBuilder numStr = new StringBuilder(1024);
	//	String plusNumStr = "1";
		StringBuilder plusNumStr = new StringBuilder(1024);
		plusNumStr.append("1");
		numStr.append("1");
		for(int i = 0; i < length; i++)
		{
		//	numStr += "0";
			numStr.append("0");
			if(i != length - 1)
			{
				//plusNumStr += "0";
				plusNumStr.append("0");
			}
		}

		Random random = new Random();
		int result = random.nextInt(Integer.parseInt(numStr.toString())) + Integer.parseInt(plusNumStr.toString());

		if(result > Integer.parseInt(numStr.toString()))
		{
			result = result - Integer.parseInt(plusNumStr.toString());
		}
		random = null;
		numStr = null;
		plusNumStr = null;
		return result;
	}
}
