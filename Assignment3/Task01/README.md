# Q1 - ATM exception handling

Compile and run from the repository root with JDK 17 or later:

~~~powershell
javac -encoding UTF-8 -d Assignment3\Task01\out Assignment3\Task01\ATMDemo.java
java -cp Assignment3\Task01\out ATMDemo
~~~

Five transaction cases exercise valid, malformed, negative, over-balance, and missing-file paths. finally runs on every attempted withdrawal; an uncaught worker exception prints a trace through its handler.

Recorded output is in outputs/run-output.txt. The paired PNG is a labeled rendering of local compiler output, not a native browser screenshot. See ../REPORT.md for analysis and limitations.


Input fixtures are in the adjacent inputs/ folder. The Java programs generate the same deterministic cases internally so they run without external setup.
