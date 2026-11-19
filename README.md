# CSA0903 Java — Assignment 1

**Assignment:** Campus Smart-Parking System (Tasks 1–12)  
**Student:** Md Aslam  
**Register number:** `[Enter your register number]`  
**Java:** OpenJDK 17 (Temurin 17.0.21+9)

This repository contains the design report, runnable task demonstrations, the two completed programs from the assignment, and output evidence. The course sheet's example repository naming and faculty-sharing directions are assignment instructions; this work is committed to the repository you specified.

## Repository map

- [REPORT.md](REPORT.md) — full responses, algorithms, UML, complexity discussion and evaluation for Tasks 1–12.
- `Task01`–`Task12` — one folder per task with its response/demo and run evidence.
- [Task03/ParkingFacility.java](Task03/ParkingFacility.java) — completed source with original `main()` preserved.
- [Task08/FeeDemo.java](Task08/FeeDemo.java) — completed vehicle hierarchy with original `main()` preserved.
- `Task11` — runnable original parking model and scenario validation.
- `Task12` — revised model with e-bikes, delivery vans, day passes and five unequal floors.
- Runnable tasks contain raw stdout logs and PNG evidence images rendered directly from those exact logs; the plain-text logs are retained for direct verification. These are output captures, not photographs of a terminal window.

## Compile and run

Use JDK 17 or newer. From the repository root in PowerShell:

```powershell
javac -d Task03/out Task03/ParkingFacility.java
java -cp Task03/out ParkingFacility

javac -d Task08/out Task08/FeeDemo.java
java -cp Task08/out FeeDemo

$files = Get-ChildItem Task11/src -Recurse -Filter *.java | ForEach-Object FullName
javac -d Task11/out $files
java -cp Task11/out parking.app.ParkingDemo

$files = Get-ChildItem Task12/src -Recurse -Filter *.java | ForEach-Object FullName
javac -d Task12/out $files
java -cp Task12/out parking.app.AdaptationDemo
```

Task 1 and Task 2 are analytical tasks; their folder notes point to the report's worked examples and data choices.

## Results

- Task 3 allocates the first free jagged-array position and rejects invalid or repeated release.
- Task 8 uses ceiling-hour billing, dynamic dispatch and the supplied EV charging interface.
- Task 11 checks baseline vehicle, pass, fee, capacity, duplicate-registration and boundary scenarios.
- Task 12 adds the changed categories and pass/rate rules with executable test cases.

Rates in the worked examples are stated assumptions because the assignment does not prescribe a complete tariff table.
