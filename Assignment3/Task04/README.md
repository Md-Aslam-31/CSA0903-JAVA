# Q4 - Thread life cycle

Compile and run from the repository root with JDK 17 or later:

~~~powershell
javac -encoding UTF-8 -d Assignment3\Task04\out Assignment3\Task04\ThreadLifecycleDemo.java
java -cp Assignment3\Task04\out ThreadLifecycleDemo
~~~

Runs four stations in ten trials using Thread and Runnable; records order, priority samples, run() behavior, join(), and daemon behavior.

Recorded output is in outputs/run-output.txt. The paired PNG is a labeled rendering of local compiler output, not a native browser screenshot. See ../REPORT.md for analysis and limitations.


Input fixtures are in the adjacent inputs/ folder. The Java programs generate the same deterministic cases internally so they run without external setup.
