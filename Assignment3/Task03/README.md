# Q3 - Resource safety

Compile and run from the repository root with JDK 17 or later:

~~~powershell
javac -encoding UTF-8 -d Assignment3\Task03\out Assignment3\Task03\ResourceSafety.java
java -cp Assignment3\Task03\out ResourceSafety
~~~

Compares a faulty manual close path, a manual finally path, and try-with-resources on the same injected corrupt record; outputs match and safe versions leave zero readers open.

Recorded output is in outputs/run-output.txt. The paired PNG is a labeled rendering of local compiler output, not a native browser screenshot. See ../REPORT.md for analysis and limitations.


Input fixtures are in the adjacent inputs/ folder. The Java programs generate the same deterministic cases internally so they run without external setup.
