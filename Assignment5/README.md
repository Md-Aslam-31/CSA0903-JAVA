# Assignment 5 - Data Persistence Engineering

**Course:** Programming in Java (CO5)  
**Student:** Mohamed (as listed in the existing repository)  
**Register number:** Not supplied; fill this in before submission.  
**Repository:** https://github.com/Md-Aslam-31/CSA0903-JAVA/tree/main/Assignment5  
**Java:** Eclipse Temurin 17.0.21  
**Database test engine:** H2 2.3.232 in MySQL compatibility mode, not a MySQL server.

This folder has separate `TaskA1`–`TaskA5` design answers and `TaskB1`–`TaskB4` implementation and experiment work, with sources, inputs, raw outputs, and the solution report. The PDF asks for a register-number repository, but this assignment is being kept in the existing repository as requested. The register number remains blank until supplied.

## Run

From the repository root in PowerShell, point `$jdk` to JDK 17 and use `$jar = 'Assignment5/lib/h2-2.3.232.jar'`. Compile each task source into its `out` folder. Example:

```powershell
$jdk = 'C:\path\to\jdk-17'
$jar = 'Assignment5/lib/h2-2.3.232.jar'
javac -d Assignment5/TaskA4/out Assignment5/TaskA4/Resident.java Assignment5/TaskA4/HostelSnapshotDemo.java
java -cp Assignment5/TaskA4/out HostelSnapshotDemo
javac -d Assignment5/TaskB1/out Assignment5/TaskB1/PassStore.java Assignment5/TaskB1/PassStoreChecks.java
java -cp Assignment5/TaskB1/out PassStoreChecks
java -cp Assignment5/TaskB1/out PassStore
javac -cp $jar -d Assignment5/TaskB2/out Assignment5/TaskB1/PassStore.java Assignment5/TaskB2/LoginSecurityDemo.java Assignment5/TaskB2/AdversarialPassStoreDemo.java
java -cp "Assignment5/TaskB2/out;$jar" LoginSecurityDemo
java -cp "Assignment5/TaskB2/out;$jar" AdversarialPassStoreDemo
javac -cp $jar -d Assignment5/TaskB3/out Assignment5/TaskB3/PassLoader.java Assignment5/TaskB3/JdbcBenchmarks.java
java -cp "Assignment5/TaskB3/out;$jar" JdbcBenchmarks
javac -cp $jar -d Assignment5/TaskB4/out Assignment5/TaskB4/BusPassMigration.java
java -cp "Assignment5/TaskB4/out;$jar" BusPassMigration Assignment5/TaskB4/inputs/migration-sample.csv
```

All executable sources compiled with JDK 17. The representative file-store example was also run in OnlineGDB. Benchmark and JDBC measurements are local JDK/H2 results. The full 1/10/50 MiB byte-at-a-time benchmark takes about 90 seconds on this machine.

## Results

- B1: identical line counts from both readers; byte-at-a-time vs 64 KiB buffered times were 1,798/4 ms, 17,777/11 ms, and 88,431/26 ms for 1/10/50 MiB. Last-record scans rose from 1.217 ms at 1,000 rows to 22.024 ms at 100,000 rows.
- B2: injected login succeeded only in the unsafe concatenated query; PreparedStatement rejected it. Malformed rows were diagnosed and later valid data remained findable.
- B3: Statement, PreparedStatement, and batch insert counts were checked at 10, 1k, 10k, and 100k. A forced failure left 5,000 rows under auto-commit and zero under a transaction.
- B4: three valid rows migrated, two invalid rows were reported, and database/driver metadata was printed.

H2 was used because no MySQL server or Connector/J was available. These measurements are not MySQL performance claims. See [REPORT.md](REPORT.md) and [Solution-Report.pdf](Solution-Report.pdf) for answers, data, and caveats.
