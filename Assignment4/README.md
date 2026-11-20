# Assignment 4 - Event-Driven GUI Design

**Student:** Md Aslam  
**Register number:** enter before submission (not supplied)  
**Course:** CSA09 Programming in Java, CO4  
**Java:** Eclipse Temurin JDK 17.0.21  
**Repository:** https://github.com/Md-Aslam-31/CSA0903-JAVA

This adds Assignment 4 to the existing coursework repository as requested. The handout asks for an individual repository named `CSA09-<RegisterNumber>-Assignment4`; the existing repository is used to keep the prior and future assignment folders together. Add the register number before submission.

## Task index

- **Task 1:** fee GUI diagnosis, service/gateway redesign, JDBC adapter, and service checks.
- **Task 2:** outpatient appointment event model, component tree, handler pseudocode, lifecycle comparison, and runnable prototype.
- **Task 3:** responsive library search layout using nested BorderLayout and FlowLayout panels.
- **Task 4:** corrected persistent AWT drawing pad plus intentionally broken typo case and compiler diagnostic.
- **Task 5:** four listener styles, Enter trigger, dialogs, and completed lambda exercise.
- **Task 6:** layered Swing/JDBC event registration, database schema, validation, unique-key duplicate protection, and paging.

## Compile and run

Compile each source file with JDK 17. The Task 6 demo/checks also need the bundled H2 JAR. The MySQL deployment path needs MySQL Connector/J and environment variables `EVENT_DB_URL`, `EVENT_DB_USER`, and `EVENT_DB_PASSWORD`.

```powershell
javac -encoding UTF-8 -d Assignment4/Task01/out Assignment4/Task01/FeeWindow.java Assignment4/Task01/FeeServiceChecks.java
java -cp Assignment4/Task01/out FeeServiceChecks

javac -encoding UTF-8 -d Assignment4/Task02/out Assignment4/Task02/AppointmentDesk.java
javac -encoding UTF-8 -d Assignment4/Task03/out Assignment4/Task03/LibrarySearchFrame.java
javac -encoding UTF-8 -d Assignment4/Task04/out Assignment4/Task04/DrawPad.java
javac -encoding UTF-8 -d Assignment4/Task05/out Assignment4/Task05/ConverterStyles.java Assignment4/Task05/Converter.java
javac -encoding UTF-8 -cp Assignment4/Task06/lib/h2-2.3.232.jar -d Assignment4/Task06/out Assignment4/Task06/EventRegistrationApp.java Assignment4/Task06/RegistrationServiceChecks.java
java -cp "Assignment4/Task06/out;Assignment4/Task06/lib/h2-2.3.232.jar" RegistrationServiceChecks
java -cp "Assignment4/Task06/out;Assignment4/Task06/lib/h2-2.3.232.jar" EventRegistrationApp --demo
```

## Evidence and verification

All six tasks compile with JDK 17. Task 1 service checks pass, and Task 6 checks pass against bundled H2 2.3.232 in MySQL compatibility mode. Native GUI screenshots are saved under task output folders: Task 3 default and maximized layouts, Task 4 three-click drawing state, Task 5 successful conversion dialog, and Task 6 duplicate rejection dialog. Compiler/service output is retained as text logs.

A local MySQL server and Connector/J were unavailable, so MySQL deployment was not run. The equivalent schema, insert, paging, filtering, and uniqueness behavior was executed on H2 in MySQL mode. Task 3 has three saved sizes and Task 4 includes the three-click state before minimize and the restored state. Register number was not supplied and must be filled in before submission.

See [REPORT.md](REPORT.md) for the written answers and [Solution-Report.pdf](Solution-Report.pdf) for the printable report with evidence screenshots.
