package com.kttddnsapi.util;

public class InstancePW_GEN_UTIL {
	 
	public static boolean isbUserCLib() {
		return bUserCLib;
	}
	public static void setbUserCLib(boolean bUserCLib) {
		InstancePW_GEN_UTIL.bUserCLib = bUserCLib;
	}
	private static boolean bUserCLib = true;
	public static  native String ktt_cipher_makeInstantPasswd_C(int year, int month, int day, int hour, byte[] mac ,int choose_old_new); 
	static
	{
		try {
		System.loadLibrary("cmakeInstantPasswd");
		}
		catch(Exception e)
		{
			bUserCLib = false;
		}
	}
	 public void finalize() {
	        System.gc();
	    }
	public static String ktt_cipher_makeInstantPasswd_JAVA(int year, int month, int day, int hour, byte[] mac)
	{
		int i;
		long passwd_key = 0;
		//int passwd_key = 0; ///연산의 결과는 64비트가 맞으나 기기와의 호환성을 위하여 32비트 int연산을 함)
		final char pw_symbol[] = new char [] {'_','+','-','=','?','@','~','!','&','#'};
		int len = mac.length;
		byte[] macArray = mac ;//new byte[6];
		//for(  i = 0; i < len && i < (6*2); i += 2)
		//	macArray[i/2] = (byte) (mac[i] * 256 + mac[i+1]);
		//	macArray[i / 2] = (byte)((Character.digit(apiMac.charAt(i), 16) << 4) |
		//			Character.digit(apiMac.charAt(i+1), 16));
		//System.out.println(macArray.toString());
		byte[] str = new byte[16];
 
		byte[] key = new byte[16];
		byte[] pwd = new byte[8];	
		 
		for(i = 0 ; i < 6 ; i++)
			passwd_key += (int)(0xff & mac[i]);

		passwd_key = passwd_key * year * month * day * (25-hour);
		passwd_key = passwd_key & 0xffffffff;
		String hexStr = String.format("%x", passwd_key);
		len = hexStr.length();
	 
		for(i = 0 ; i < 8 ; i++)
		{
			str[i] = (byte)((passwd_key >> ((i%len)*4)) & 0xf);
			key[i] = (byte)((mac[(11-i)/2] >> ((i%2)*4)) & 0xf);
			pwd[i] = (byte)((str[i] + key[i]) & 0xf);
		}
		
		hexStr = String.format("%x%x%x%x%x%x%x%x",pwd[7],pwd[6],pwd[5],pwd[4],pwd[3],pwd[2],pwd[1],pwd[0]);		
		
		byte[] passwd = hexStr.getBytes();
		int r = (int) (passwd_key % 10);
		if (r >= 0 && r < pw_symbol.length)
			passwd[7] = (byte) pw_symbol[r];
		else
			passwd[7] = (byte) pw_symbol[0];

		hexStr = new String(passwd);
		str = null;
		key = null;
		pwd = null;
	//	System.out.println(hexStr);
		return hexStr;
	}
}
