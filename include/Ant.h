/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class Ant */

#ifndef _Included_Ant
#define _Included_Ant
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     Ant
 * Method:    conn
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)LAnt;
 */
JNIEXPORT jobject JNICALL Java_Ant_conn
  (JNIEnv *, jobject, jstring, jstring, jstring, jstring, jstring);

/*
 * Class:     Ant
 * Method:    query
 * Signature: (Ljava/lang/String;)Ljava/util/List;
 */
JNIEXPORT jobject JNICALL Java_Ant_query
  (JNIEnv *, jobject, jstring);

/*
 * Class:     Ant
 * Method:    disconnect
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_Ant_disconnect
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
