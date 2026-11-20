# Q6 - Deadlock and token counter

Compile and run from the repository root with JDK 17 or later:

~~~powershell
javac -encoding UTF-8 -d Assignment3\Task06\out Assignment3\Task06\ConcurrencyCoordination.java
java -cp Assignment3\Task06\out ConcurrencyCoordination
~~~

Shows a reliable reverse-order deadlock, ordered-lock completion, correct 210 token sum, and observed buffer bounds 0..5.

Recorded output is in outputs/run-output.txt. The paired PNG is a labeled rendering of local compiler output, not a native browser screenshot. See ../REPORT.md for analysis and limitations.


Input fixtures are in the adjacent inputs/ folder. The Java programs generate the same deterministic cases internally so they run without external setup.
