 
#include "com_rf_chiper_wrapper_jni_rf_chiper_wrapper.h"
#include "rf_chiper.h"
#define AES256_SIZE	128
/* Header for class com_rf_chiper_wrapper_jni_rf_chiper_wrapper */

#ifndef _Included_com_rf_chiper_wrapper_jni_rf_chiper_wrapper
#define _Included_com_rf_chiper_wrapper_jni_rf_chiper_wrapper
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_getRFAes256Key
 * Signature: ([B[B)V
 */
JNIEXPORT jstring JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1getRFAes256Key
  (JNIEnv * env , jclass c)
  {
    char key[32+16];
    char iv = &key[32];
    jstring jstr;
    rf_cipher_getRFAes256Key(key,iv);
    jstr = (*env)->NewStringUTF(env, key);
    return jstr;
  }

/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_dec_aes256
 * Signature: ([BI)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1dec_1aes256
  (JNIEnv * env , jclass c, jint size)
  {
     if(size >0){
    jbyteArray retArr = (*env)->NewByteArray(env, size);
    char * data =  (char*) malloc(size);
    int r =  rf_cipher_dec_aes256(data , size);
     (*env)->SetByteArrayRegion(env, retArr, 0, size, data);
    free(data);
    return retArr;
     }else
     return NULL;
  }
/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_enc_aes256_withsize
 * Signature: ([BI)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1enc_1aes256_1withsize
  (JNIEnv * env , jclass c , jint size)
  {
     if(size >0){
    jbyteArray retArr = (*env)->NewByteArray(env, size);
    char * data =  (char*) malloc(size);
    int r =  rf_cipher_enc_aes256_withsize(data , size);
     (*env)->SetByteArrayRegion(env, retArr, 0, size, data);
    free(data);
    return retArr;
     }else
     return NULL;
  }

/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_enc_aes256
 * Signature: ([B)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1enc_1aes256
  (JNIEnv * env , jclass c  )
  {
  
    jbyteArray retArr = (*env)->NewByteArray(env, AES256_SIZE);
    char * data =  (char*) malloc(AES256_SIZE);
    int r = rf_cipher_enc_aes256(data);
     (*env)->SetByteArrayRegion(env, retArr, 0, AES256_SIZE, data);
    free(data);
    return retArr;
   
  }

/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_dec_aes256ctrPadding
 * Signature: ([BI)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1dec_1aes256ctrPadding
  (JNIEnv * env , jclass c  , jint size)
  {

     if(size >0){
    jbyteArray retArr = (*env)->NewByteArray(env, size);
    char * data =  (char*) malloc(size);
    int r =  rf_cipher_dec_aes256ctrPadding(data , size);
     (*env)->SetByteArrayRegion(env, retArr, 0, size, data);
    free(data);
    return retArr;
     }else
     return NULL;

  
  }

/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_enc_aes256ctrPadding
 * Signature: ([BI)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1enc_1aes256ctrPadding
  (JNIEnv * env, jclass c   , jint size){
 
     if(size >0){
    jbyteArray retArr = (*env)->NewByteArray(env, size);
    char * data =  (char*) malloc(size);
    int r =  rf_cipher_enc_aes256ctrPadding(data , size);
     (*env)->SetByteArrayRegion(env, retArr, 0, size, data);
    free(data);
    return retArr;
     }else
     return NULL;

  }

/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_str2sha256HexStr
 * Signature: ([B[B)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1str2sha256HexStr
  (JNIEnv * env, jclass c ,   jbyteArray str  )
  {

   char * _str  =   (unsigned char*)(*env)->GetByteArrayElements(env, str,0);
    jbyteArray retArr = (*env)->NewByteArray(env, 32);
    char p_dst[32]={'\0'};
    rf_cipher_str2sha256HexStr(p_dst,_str);
   (*env)->ReleaseByteArrayElements(env, str, _str, 0 );
   (*env)->SetByteArrayRegion(env, retArr, 0, 32, p_dst);
 return retArr;
  }

/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_str2sha256
 * Signature: ([B[B)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1str2sha256
  (JNIEnv * env , jclass c ,  jbyteArray str)
  {
  char * _str  =   (unsigned char*)(*env)->GetByteArrayElements(env, str,0);
    jbyteArray retArr = (*env)->NewByteArray(env, 32);
    char p_dst[32]={'\0'};
    rf_cipher_str2sha256(p_dst,_str);
   (*env)->ReleaseByteArrayElements(env, str, _str, 0 );
   (*env)->SetByteArrayRegion(env, retArr, 0, 32, p_dst);
    
 return retArr;
  }

/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_str2sha1
 * Signature: ([B[B)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1str2sha1
  (JNIEnv * env , jclass c,   jbyteArray str)
  {
      char * _str  =   (unsigned char*)(*env)->GetByteArrayElements(env, str,0);
    jbyteArray retArr = (*env)->NewByteArray(env, 20);
    char p_dst[20]={'\0'};
    rf_cipher_str2sha256(p_dst,_str);
   (*env)->ReleaseByteArrayElements(env, str, _str, 0 );
   (*env)->SetByteArrayRegion(env, retArr, 0, 20, p_dst);
    
 return retArr;
  }

/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_sha1
 * Signature: ([B[BI)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1sha1
  (JNIEnv * env, jclass c ,   jbyteArray str, jint size )
  {
    char * _str  =   (unsigned char*)(*env)->GetByteArrayElements(env, str,0);
    jbyteArray retArr = (*env)->NewByteArray(env, 20);
    char p_dst[20]={'\0'};
    rf_cipher_sha1(p_dst,_str , size);
   (*env)->ReleaseByteArrayElements(env, str, _str, 0 );
   (*env)->SetByteArrayRegion(env, retArr, 0, 20, p_dst);
    
 return retArr;
  }

/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_sha256
 * Signature: ([B[BI)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1sha256
  (JNIEnv * env , jclass c ,   jbyteArray p_src, jint size )
  {
char * _str  =   (unsigned char*)(*env)->GetByteArrayElements(env, str,0);
    jbyteArray retArr = (*env)->NewByteArray(env, 32);
    char p_dst[32]={'\0'};
    rf_cipher_sha256(p_dst,p_src , size);
   (*env)->ReleaseByteArrayElements(env, str, _str, 0 );
   (*env)->SetByteArrayRegion(env, retArr, 0, 32, p_dst);
    
 return retArr;
  }

/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_aes256_keyenc
 * Signature: ([BI[B[B)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1aes256_1keyenc
  (JNIEnv * env , jclass c ,  jbyteArray data , jint size , jbyteArray key , jbyteArray iv)
  {
    char * p_data  =   (unsigned char*)(*env)->GetByteArrayElements(env, data,0);
    char * p_key  =   (unsigned char*)(*env)->GetByteArrayElements(env, key,0);
    char * p_iv  =   (unsigned char*)(*env)->GetByteArrayElements(env, iv,0);
    jbyteArray retArr = (*env)->NewByteArray(env, size);
    char *  p_dst = (char *) malloc(size);
    memcpy(p_dst, p_data, size);
    rf_cipher_aes256_keyenc(p_dst,size,p_key,p_iv);
     (*env)->ReleaseByteArrayElements(env, key, p_key, 0 );
     (*env)->ReleaseByteArrayElements(env, iv, p_iv, 0 );
     (*env)->ReleaseByteArrayElements(env, data, p_data, 0 );
     (*env)->SetByteArrayRegion(env, retArr, 0, size, p_dst);
     free(p_dst);
      return retArr;
  }

/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_aes256_keydec
 * Signature: ([BI[B[B)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1aes256_1keydec
  (JNIEnv * env , jclass c ,  jbyteArray data , jint size , jbyteArray key , jbyteArray iv)
  {
      char * p_data  =   (unsigned char*)(*env)->GetByteArrayElements(env, data,0);
    char * p_key  =   (unsigned char*)(*env)->GetByteArrayElements(env, key,0);
    char * p_iv  =   (unsigned char*)(*env)->GetByteArrayElements(env, iv,0);
    jbyteArray retArr = (*env)->NewByteArray(env, size);
    char *  p_dst = (char *) malloc(size);
    memcpy(p_dst, p_data, size);
    rf_cipher_aes256_keydec(p_dst,size,p_key,p_iv);
     (*env)->ReleaseByteArrayElements(env, key, p_key, 0 );
     (*env)->ReleaseByteArrayElements(env, iv, p_iv, 0 );
     (*env)->ReleaseByteArrayElements(env, data, p_data, 0 );
     (*env)->SetByteArrayRegion(env, retArr, 0, size, p_dst);
     free(p_dst);
      return retArr;
  }

/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_sha256cmpStr
 * Signature: ([B[B)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1sha256cmpStr
  (JNIEnv * env , jclass c ,   jbyteArray sha256 , jbyteArray str)
  {
return NULL;
  }

/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_aes128ctr_keyenc
 * Signature: ([BI[B[B)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1aes128ctr_1keyenc
  (JNIEnv * env , jclass c , jbyteArray data,  jint size , jbyteArray key, jbyteArray iv)
  {
   char * p_data  =   (unsigned char*)(*env)->GetByteArrayElements(env, data,0);
    char * p_key  =   (unsigned char*)(*env)->GetByteArrayElements(env, key,0);
    char * p_iv  =   (unsigned char*)(*env)->GetByteArrayElements(env, iv,0);
    jbyteArray retArr = (*env)->NewByteArray(env, size);
    char *  p_dst = (char *) malloc(size);
    memcpy(p_dst, p_data, size);
    rf_cipher_aes128ctr_keyenc(p_dst,size,p_key,p_iv);
     (*env)->ReleaseByteArrayElements(env, key, p_key, 0 );
     (*env)->ReleaseByteArrayElements(env, iv, p_iv, 0 );
     (*env)->ReleaseByteArrayElements(env, data, p_data, 0 );
     (*env)->SetByteArrayRegion(env, retArr, 0, size, p_dst);
     free(p_dst);
      return retArr;
  }

/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_aes128ctr_keydec
 * Signature: ([BI[B[B)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1aes128ctr_1keydec
  (JNIEnv * env , jclass c , jbyteArray data,  jint size , jbyteArray key, jbyteArray iv)
{
       char * p_data  =   (unsigned char*)(*env)->GetByteArrayElements(env, data,0);
    char * p_key  =   (unsigned char*)(*env)->GetByteArrayElements(env, key,0);
    char * p_iv  =   (unsigned char*)(*env)->GetByteArrayElements(env, iv,0);
    jbyteArray retArr = (*env)->NewByteArray(env, size);
    char *  p_dst = (char *) malloc(size);
    memcpy(p_dst, p_data, size);
    rf_cipher_aes128ctr_keydec(p_dst,size,p_key,p_iv);
     (*env)->ReleaseByteArrayElements(env, key, p_key, 0 );
     (*env)->ReleaseByteArrayElements(env, iv, p_iv, 0 );
     (*env)->ReleaseByteArrayElements(env, data, p_data, 0 );
     (*env)->SetByteArrayRegion(env, retArr, 0, size, p_dst);
     free(p_dst);
      return retArr;
}
/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_aes256ctrPadding_keyenc
 * Signature: ([BI[B[B)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1aes256ctrPadding_1keyenc
 (JNIEnv * env , jclass c , jbyteArray data,  jint size , jbyteArray key, jbyteArray iv)
 {
           char * p_data  =   (unsigned char*)(*env)->GetByteArrayElements(env, data,0);
    char * p_key  =   (unsigned char*)(*env)->GetByteArrayElements(env, key,0);
    char * p_iv  =   (unsigned char*)(*env)->GetByteArrayElements(env, iv,0);
    jbyteArray retArr = (*env)->NewByteArray(env, size);
    char *  p_dst = (char *) malloc(size);
    memcpy(p_dst, p_data, size);
    rf_cipher_aes256ctrPadding_keyenc(p_dst,size,p_key,p_iv);
     (*env)->ReleaseByteArrayElements(env, key, p_key, 0 );
     (*env)->ReleaseByteArrayElements(env, iv, p_iv, 0 );
     (*env)->ReleaseByteArrayElements(env, data, p_data, 0 );
     (*env)->SetByteArrayRegion(env, retArr, 0, size, p_dst);
     free(p_dst);
      return retArr;
 }

/*
 * Class:     com_rf_chiper_wrapper_jni_rf_chiper_wrapper
 * Method:    rf_cipher_aes256ctrPadding_keydec
 * Signature: ([BI[B[B)I
 */
JNIEXPORT jbyteArray JNICALL Java_com_rf_1chiper_1wrapper_jni_rf_1chiper_1wrapper_rf_1cipher_1aes256ctrPadding_1keydec
 (JNIEnv * env , jclass c , jbyteArray data,  jint size , jbyteArray key, jbyteArray iv)
 {
      char * p_data  =   (unsigned char*)(*env)->GetByteArrayElements(env, data,0);
    char * p_key  =   (unsigned char*)(*env)->GetByteArrayElements(env, key,0);
    char * p_iv  =   (unsigned char*)(*env)->GetByteArrayElements(env, iv,0);
    jbyteArray retArr = (*env)->NewByteArray(env, size);
    char *  p_dst = (char *) malloc(size);
    memcpy(p_dst, p_data, size);
    rf_cipher_aes256ctrPadding_keydec(p_dst,size,p_key,p_iv);
     (*env)->ReleaseByteArrayElements(env, key, p_key, 0 );
     (*env)->ReleaseByteArrayElements(env, iv, p_iv, 0 );
     (*env)->ReleaseByteArrayElements(env, data, p_data, 0 );
     (*env)->SetByteArrayRegion(env, retArr, 0, size, p_dst);
     free(p_dst);
      return retArr;
 }

#ifdef __cplusplus
}
#endif
#endif
