# Assignment 2 - Java Collections and Design

**Student:** Md Aslam  
**Register number:** [Enter your register number before submission]  
**Course:** CSA09 - Programming in Java, CO2  
**Java:** Java 17 (OnlineGDB Java compiler used for executions)  
**Repository:** https://github.com/Md-Aslam-31/CSA0903-JAVA

This assignment is in its own `Assignment2/` folder beside `Assignment1/`, as requested. It contains Q1-Q7 source code, five-run timing data where applicable, raw output transcripts, a solution report, and evidence images.

> Evidence clarity: Java code was compiled and run in OnlineGDB. The PNGs in `outputs/` are readable renderings of the actual compiler console text saved in the adjacent `.txt` files. They are labeled as transcript renderings rather than browser screenshots. OnlineGDB screenshots were visible during the work but could not be exported to files from the controlled browser.

## Compile and run

Run these commands in PowerShell from the repository root. Use JDK 17 or later.

```powershell
javac -d Assignment2/Task01/out Assignment2/Task01/StringsUnderLoad.java
java -cp Assignment2/Task01/out StringsUnderLoad

javac -d Assignment2/Task02/out Assignment2/Task02/PlaylistCollections.java
java -cp Assignment2/Task02/out PlaylistCollections

javac -d Assignment2/Task03/out Assignment2/Task03/Student.java
java -cp Assignment2/Task03/out Student

javac -d Assignment2/Task04/out Assignment2/Task04/PharmacyMaps.java
java -cp Assignment2/Task04/out PharmacyMaps

javac -d Assignment2/Task05/out Assignment2/Task05/LeaderboardDemo.java
java -cp Assignment2/Task05/out LeaderboardDemo

javac -d Assignment2/Task06/out Assignment2/Task06/Triage.java
java -cp Assignment2/Task06/out Triage

javac -d Assignment2/Task07/out Assignment2/Task07/RedesignComparison.java
java -cp Assignment2/Task07/out RedesignComparison
```

## Task index

- `Task01` Q1: String, StringBuilder and StringBuffer workloads.
- `Task02` Q2: ArrayList, LinkedList and ArrayDeque operations.
- `Task03` Q3: equals/hashCode contract, broken and corrected Student designs.
- `Task04` Q4: HashMap, TreeMap and LinkedHashMap pharmacy inventory.
- `Task05` Q5: generic leaderboard and safe comparator chain.
- `Task06` Q6: severity and FIFO tie-break priority queue.
- `Task07` Q7: measured redesign of repeated string concatenation.

See [REPORT.md](REPORT.md) for predictions, benchmark conditions, results, complexity, alternative designs and limitations. Replace the register-number placeholder before submission. The assignment sheet asks for a new repo named `CSA09-<RegisterNumber>-Assignment2`; this work is placed in the same existing repository because the user requested the Assignment 1 process be repeated there.
