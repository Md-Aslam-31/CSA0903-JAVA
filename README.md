# CSA0903 Java - Repository Guide

## Assignment folders

- `Assignment1/` contains the complete Tasks 1-12 response, runnable Java examples, raw output logs and output evidence images.
- `Assignment1/REPORT.md` is the full written solution.
- `Assignment1/solution-report.pdf` is the printable solution report with output evidence pages.
- `Assignment1/Task01` through `Task12` hold task-by-task material.

## Student details

- Student: Md Aslam
- Register number: enter the correct register number before submission; it was not provided in the request.
- Java: JDK 17 or newer

## Run the Java demonstrations

From the repository root in PowerShell:

```powershell
javac -d Assignment1/Task03/out Assignment1/Task03/ParkingFacility.java
java -cp Assignment1/Task03/out ParkingFacility

javac -d Assignment1/Task08/out Assignment1/Task08/FeeDemo.java
java -cp Assignment1/Task08/out FeeDemo

$files = Get-ChildItem Assignment1/Task11/src -Recurse -Filter *.java | ForEach-Object FullName
javac -d Assignment1/Task11/out $files
java -cp Assignment1/Task11/out parking.app.ParkingDemo

$files = Get-ChildItem Assignment1/Task12/src -Recurse -Filter *.java | ForEach-Object FullName
javac -d Assignment1/Task12/out $files
java -cp Assignment1/Task12/out parking.app.AdaptationDemo
```

The PNGs in each runnable task's `outputs/` folder are output transcript renderings created from the corresponding raw stdout text. They are labeled as such. Task 3 was also run in OnlineGDB's Java compiler during this workflow and completed with exit code 0.
