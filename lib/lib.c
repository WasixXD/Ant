#include <stdio.h>
#include <stdlib.h>
#include <malloc.h>
#include <libpq-fe.h>
#include <jni.h>
#include <time.h>
#include <limits.h>
#include "Ant.h"

typedef struct Pair {
    jlong key;
    PGconn *c;
} Pair;

typedef struct Hash {
    Pair *values;
    size_t size;
} Hash;

static Hash globalMap;
void init_global_map(size_t capacity) {
    globalMap.values = (Pair *)malloc(capacity * sizeof(Pair));
    globalMap.size = capacity;
}


JNIEXPORT jobject JNICALL Java_Ant_conn(JNIEnv *env, jobject obj, jstring host, jstring user, jstring pass, jstring port, jstring dbname) {
    srand(time(NULL));
    init_global_map(10);

    const char *host_str   = (*env)->GetStringUTFChars(env, host, 0);
    const char *user_str   = (*env)->GetStringUTFChars(env, user, 0);
    const char *pass_str   = (*env)->GetStringUTFChars(env, pass, 0);
    const char *port_str   = (*env)->GetStringUTFChars(env, port, 0);
    const char *dbname_str = (*env)->GetStringUTFChars(env, dbname, 0);

    // Safe method
    char conn_str[256];
    snprintf(conn_str, sizeof(conn_str), "postgres://%s:%s@%s:%s/%s", user_str, pass_str, host_str, port_str, dbname_str);

    PGconn *connection = PQconnectdb(conn_str);
    if(PQstatus(connection) != CONNECTION_OK) {
        printf("Error while connecting to the database: %s\n", PQerrorMessage(connection));
        goto release_strings;
    }

    jclass clazz = (*env)->GetObjectClass(env, obj);
    jfieldID key = (*env)->GetFieldID(env, clazz, "key", "J");
    if(key == NULL) {
        printf("Erro while getting field ID");
        goto release_strings;
    }

    jlong genKey = rand() % LONG_MAX;
    (*env)->SetLongField(env, obj, key, genKey);
    Pair p = { .c = connection, .key = genKey };
    globalMap.values[0] = p;

    return obj;


release_strings: 
    (*env)->ReleaseStringUTFChars(env, host, host_str);
    (*env)->ReleaseStringUTFChars(env, user, user_str);
    (*env)->ReleaseStringUTFChars(env, pass, pass_str);
    (*env)->ReleaseStringUTFChars(env, port, port_str);
    (*env)->ReleaseStringUTFChars(env, dbname, dbname_str);
    PQfinish(connection);
    return obj;
}
