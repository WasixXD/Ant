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

typedef struct Map {
    Pair *values;
    size_t size;
} Map;

static Map globalMap;

void init_global_map(size_t capacity) {
    globalMap.values = (Pair *)malloc(capacity * sizeof(Pair));
    globalMap.size = capacity;
}

PGconn* get_conn_pointer(jlong key) {
    // not so good implementation
    for(int i = 0; i < globalMap.size; i++) {
        if(globalMap.values[i].key == key) {
            return globalMap.values[i].c;
        }
    }

    return NULL;
}


JNIEXPORT jobject JNICALL Java_Ant_conn(JNIEnv *env, jobject obj, jstring host, jstring user, jstring pass, jstring port, jstring dbname) {
    srand(time(NULL));
    // ten(?)
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
        printf("Error while getting field ID");
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

JNIEXPORT void JNICALL Java_Ant_query(JNIEnv *env, jobject obj, jstring query) {
    // get key
    jclass clazz = (*env)->GetObjectClass(env, obj);
    jfieldID key_field = (*env)->GetFieldID(env, clazz, "key", "J");
    if(key_field == NULL) {
        printf("Error while getting field id\n");
        return;
    }
    jlong key = (*env)->GetLongField(env, obj, key_field);

    // get connection pointer
    PGconn *connection = get_conn_pointer(key);
    if(connection == NULL) {
        printf("Couldn't get map values\n");
        return;
    }

    const char *query_str = (*env)->GetStringUTFChars(env, query, 0);
    PGresult *result = PQexec(connection, query_str);
    if(PQresultStatus(result) != PGRES_TUPLES_OK) {
        printf("Error while querying the database: %s\n", PQresultErrorMessage(result));
        return;
    }

    int tuples = PQntuples(result);
    int fields = PQnfields(result);
    char *value = PQgetvalue(result, 0, 6);
    printf("Tabela: %dx%d\nValor: %s\n", tuples, fields, value);

    (*env)->ReleaseStringUTFChars(env, query, query_str);
    PQclear(result);
    return;
}