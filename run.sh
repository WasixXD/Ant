#!/bin/bash
clear
javac -d ./include/ src/Ant.java 
javah -d ./include/ -classpath ./include/ Ant
gcc -shared -fPIC -o libant.so -I./include -I"${JAVING}/include" -I"${JAVING}/include/linux" -I/usr/include/postgresql -L/usr/lib/x86_64-linux-gnu -lpq ./lib/lib.c
javac -d ./bin/ src/*.java
LD_PRELOAD=/usr/lib/x86_64-linux-gnu/libpq.so java -Djava.library.path=. -cp ./bin/ Main 
# LD_PRELOAD=/usr/lib/x86_64-linux-gnu/libpq.so java -Djava.library.path=. -cp ./bin/ -Xlog:gc* -Xlog:gc*:gc.log Main
# rm libant.so