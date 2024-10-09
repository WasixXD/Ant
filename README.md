# Ant
## Tiny Wrapper of libpq for Java


# Brief 📖

This project was made because i didn't want to install packages to connect java to postgresql, in the end it became more of a proof of concept so i could parse millions rows of a csv

# Challenges 🐢
- Using JNI <br>
- Exchange data between C and Java <br>
- Parse 20 million lines of csv <br>
- Performance <br>
- Database connections <br>

# Goals 🏆
- [ x ] 20 million lines in 20 seconds with the lib <br>


# Performance ⚡️

The code for the csv are in the `dados.py` file.
All the tests were made in a i5-11400 with 12 threads.

| Implementations  | 209712 lines | 20971521 lines |
|----------|----------|----------|
| Naive  | 9 minutes   | Not Tested   |
| Concurrency | 4 minutes   | Locked on 1.200.000 lines   |
| Batch + StringBuilder   | 0,4 seconds   | 24 seconds  |
| All the above + Threadpool   | 0,2 seconds   | 22-20 seconds  |
