# Q7 - Fault-tolerant processor

Compile and run from the repository root with JDK 17 or later:

~~~powershell
javac -encoding UTF-8 -d Assignment3\Task07\out Assignment3\Task07\ResultProcessor.java
java -cp Assignment3\Task07\out ResultProcessor
~~~

Processes 10,000 score-file paths with injected missing/corrupt inputs, checks a hand-built 5-file set, and records 1/2/4/8 worker timings.

Recorded output is in outputs/run-output.txt. The paired PNG is a labeled rendering of local compiler output, not a native browser screenshot. See ../REPORT.md for analysis and limitations.


Input fixtures are in the adjacent inputs/ folder. The Java programs generate the same deterministic cases internally so they run without external setup.
