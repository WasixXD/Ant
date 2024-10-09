# Ant
## Tiny Wrapper of libpq for Java


# Brief 📖

This project i made because i didn't want to install packages to connect java to postgresql, in the end it became more of a proof of concept so i could parse millions rows of a csv

# Challenges 🐢
- Using JNI <br>
- Exchange data between C and Java <br>
- Parse 20 million lines of csv <br>
- Performance <br>
- Database connections <br>

# Goals 🏆
- [ x ] 20 million lines in 20 seconds with the lib <br>


# How it works? 💼

I made about 5 implementation that make something different, the last one use multithreading, concurrent queues and streams to get most of it.

