#!/bin/bash
clear
javac -d ./include/ src/Ant.java 
javah -d ./include/ -classpath ./include/ Ant
gcc -shared -o libant.so -I"${JAVING}/include" -I"${JAVING}/include/linux" ./lib/lib.c
javac -d ./bin/ src/*.java
java -Djava.library.path=. -cp ./bin/ Main