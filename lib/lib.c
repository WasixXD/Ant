#include <stdio.h>
// #include <libpq-fe.h>
#include <jni.h>

JNIEXPORT void JNICALL Java_Ant_hello(JNIEnv *env, jobject obj) {
    printf("Hello, World! From C!\n");
}


// int main() {
//     // PGconn *conn = PQconnectdb("host=localhost user=postgres dbname=fastdialer port=5432 password=postgres");
//     PGconn *conn = PQconnectdb("postgresql://postgres:postgres@localhost:5432/fastdialer");

//     if (PQstatus(conn) == CONNECTION_BAD) {
//         fprintf(stderr, "Connection to database failed: %s\n", PQerrorMessage(conn));
//         PQfinish(conn);
//         return 1;
//     }

//     PGresult *res = PQexec(conn, "SELECT version();");

//     if (PQresultStatus(res) != PGRES_TUPLES_OK) {
//         fprintf(stderr, "SELECT failed: %s\n", PQerrorMessage(conn));
//         PQclear(res);
//         PQfinish(conn);
//         return 1;
//     }

//     printf("PostgreSQL version: %s\n", PQgetvalue(res, 0, 0));

//     PQclear(res);
//     PQfinish(conn);
//     return 0;
// }
