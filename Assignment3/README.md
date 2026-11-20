# Assignment 3 - Exceptions and Concurrency

**Student:** Md Aslam  
**Register number:** [Enter your register number before submission]  
**Partner:** [Enter partner name and register number; complete the contribution table honestly]  
**Course:** CSA09 - Programming in Java, CO3  
**Java:** JDK 17.0.21  
**Repository:** https://github.com/Md-Aslam-31/CSA0903-JAVA

This folder contains implementations and analysis for Q1-Q7. The assignment sheet specifies pair work and a new repository named CSA09-<RegisterNumber>-Assignment3. The student requested the prior repository for this series, so these files are in Assignment3/ beside Assignments 1 and 2. Confirm that repository choice with the course submission rules.

## Compile and run

From the repository root in PowerShell, use JDK 17 or later:

~~~powershell
javac -encoding UTF-8 -d Assignment3\Task01\out Assignment3\Task01\ATMDemo.java
java -cp Assignment3\Task01\out ATMDemo

javac -encoding UTF-8 -d Assignment3\Task02\out Assignment3\Task02\StorageDemo.java
java -cp Assignment3\Task02\out StorageDemo

javac -encoding UTF-8 -d Assignment3\Task03\out Assignment3\Task03\ResourceSafety.java
java -cp Assignment3\Task03\out ResourceSafety

javac -encoding UTF-8 -d Assignment3\Task04\out Assignment3\Task04\ThreadLifecycleDemo.java
java -cp Assignment3\Task04\out ThreadLifecycleDemo

javac -encoding UTF-8 -d Assignment3\Task05\out Assignment3\Task05\SeatBooking.java
java -cp Assignment3\Task05\out SeatBooking

javac -encoding UTF-8 -d Assignment3\Task06\out Assignment3\Task06\ConcurrencyCoordination.java
java -cp Assignment3\Task06\out ConcurrencyCoordination

javac -encoding UTF-8 -d Assignment3\Task07\out Assignment3\Task07\ResultProcessor.java
java -cp Assignment3\Task07\out ResultProcessor
~~~

The compiler used for the recorded local runs was Eclipse Temurin OpenJDK 17.0.21. The OnlineGDB desktop browser was detecting user input during this task, so the recorded evidence comes from local JDK output. The PNGs are labeled console transcript renderings, not native screenshots.

## Task results

- **Task01 / Q1:** Valid withdrawal succeeds; invalid input, negative amount, insufficient balance, and missing card-reader file follow separate exception paths. The finally message appears for every attempted transaction.
- **Task02 / Q2:** The storage layer wraps IOException in a checked SubmissionStorageException; the controller handles checked and unchecked domain failures.
- **Task03 / Q3:** The deliberately faulty manual-close run leaks one reader; finally and try-with-resources close it. Primary and suppressed close failures are shown.
- **Task04 / Q4:** Ten runs record varied station completion orders; join() waits for completion. Thread and Runnable implementations are compared.
- **Task05 / Q5:** The barrier-controlled unsafe implementation demonstrates duplicate bookings; synchronized method/block versions sell only 10 unique seats.
- **Task06 / Q6:** Reverse lock order deadlocks in a daemon demonstration; consistent lock ordering completes safely. Bounded token counter returns the expected sum and stays within 0..5.
- **Task07 / Q7:** 10,000 score-file references include injected missing/corrupt records; pool sizes 1, 2, 4, and 8 produce complete success/failure counts.

See REPORT.md for the hierarchy diagrams, test tables, run evidence, timings, complexity analysis, design discussion, and contribution-record placeholders. Enter the register number and partner details before submission.
