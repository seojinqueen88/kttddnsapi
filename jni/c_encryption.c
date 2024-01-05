#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "com_kttddnsapi_util_InstancePW_GEN_UTIL.h"
#ifdef __cplusplus
extern “C” {
#endif
int user_data_set_instant_password(int year, int month,int day, int hour , unsigned char *mac_address, char *new_password, char *old_password) {
	int i;
	unsigned int passwd_key = 0;
	unsigned int old_passwd_key = 0;
	unsigned int new_passwd_key = 0;
	char str[16];
	size_t len;
	char key[16];
	char pwd[8];
	int y, m, d, h;
	 
	unsigned char mac[8]= {0,0,0,0,0,0,0,0};
	const char  pw_symbol[] = {'_','+','-','=','?','@','~','!','&','#'};
   
	y =  (year);
	m =  (month);
	d =  (day);
	h =  (hour);
	memcpy(mac , mac_address , 6);
    for(i = 0 ; i < 6 ; i++)
	{
		passwd_key = passwd_key + mac[i];
	}
	//////////////////// OLD PW MAKE ////////////////////
	old_passwd_key = passwd_key * y * m * d * (h == 0? 24:h);
	sprintf(str, "%x", old_passwd_key);
	len = strlen(str);
	memset(str, 0, sizeof(str));
	for(i = 0 ; i < 8 ; i++)
	{
		str[i] = (unsigned char)(old_passwd_key >> ((i%len) * 4)) & 0xf;
		key[i] = (unsigned char)(mac[(11-i)/2] >> ((i%2)*4)) & 0xf;
		pwd[i] = (unsigned char)(str[i] + key[i]) & 0xf;
	}
	memset(old_password,0,sizeof(pwd));
	sprintf(old_password,"%x%x%x%x%x%x%x%x",pwd[7],pwd[6],pwd[5],pwd[4],pwd[3],pwd[2],pwd[1],pwd[0]);
	old_password[8] = 0;

	//////////////////// NEW PW MAKE ////////////////////
	new_passwd_key = passwd_key * y * m * d * (25 - h);
	sprintf(str, "%x", new_passwd_key);
	len = strlen(str);
	memset(str, 0, sizeof(str));
	for(i = 0 ; i < 8 ; i++)
	{
		str[i] = (unsigned char)(new_passwd_key >> ((i%len) * 4)) & 0xf;
		key[i] = (unsigned char)(mac[(11-i)/2] >> ((i%2)*4)) & 0xf;
		pwd[i] = (unsigned char) (str[i] + key[i]) & 0xf;
	}
	memset(new_password,0,sizeof(pwd));
	sprintf(new_password,"%x%x%x%x%x%x%x%x",pwd[7],pwd[6],pwd[5],pwd[4],pwd[3],pwd[2],pwd[1],pwd[0]);
	new_password[7] = pw_symbol[new_passwd_key % 10];
	new_password[8] = 0;

	return 0;
}
JNIEXPORT jstring JNICALL Java_com_kttddnsapi_util_InstancePW_1GEN_1UTIL_ktt_1cipher_1makeInstantPasswd_1C
  (JNIEnv * env , jclass c , jint year, jint month, jint day ,  jint hour, jbyteArray mac, jint choose_old_new){
	jstring jstr;
    char new_k[32];
    char old_k[32];
    size_t len = (*env)->GetArrayLength(env, mac);
    unsigned char * _mac;
    if(len == 0 ) return NULL;
    _mac = (unsigned char*)(*env)->GetByteArrayElements(env, mac,0);
			user_data_set_instant_password(year, month, day, hour , _mac ,new_k ,old_k);
	
	switch(choose_old_new)
	{
		case 0:
		jstr = (*env)->NewStringUTF(env, old_k);
			break;
		case 1:
		jstr = (*env)->NewStringUTF(env, new_k);
    	break;
		default :
		jstr = (*env)->NewStringUTF(env, "Not Ready To Style");
    	break;
	}
    (*env)->ReleaseByteArrayElements(env, mac, _mac, 0 );
	return jstr;
}
#ifdef __cplusplus
}
#endif
 