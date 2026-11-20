# Assignment 4 - Solution Report

**Student:** Md Aslam  
**Register number:** Not provided (fill in before submission)  
**Course:** CSA09 Programming in Java, CO4  
**Compiler/runtime:** Eclipse Temurin JDK 17.0.21  
**Repository:** https://github.com/Md-Aslam-31/CSA0903-JAVA  
**Date:** 20 November 2026

This report answers the six tasks in `ASSIGNMENT4.pdf`. The handout requests an individual repository named `CSA09-<RegisterNumber>-Assignment4`; this work is placed in the existing coursework repository at the student's request, so later assignments can remain in separate folders. The register number remains a student-supplied field.

## Implementation and verification overview

The submission includes runnable examples for all six tasks, task-specific explanations, SQL schemas, service checks and real UI screenshots. Java sources compile with Java 17. Task 1 service checks pass. Task 6 normal, invalid, duplicate and filtered registration behaviors pass against bundled H2 2.3.232 in MySQL compatibility mode. MySQL Connector/J and a MySQL server were unavailable, so deployment-mode MySQL execution was not verified.

## Task 1 - Fee collection GUI

The original design couples the frame, validation, JDBC and payment policy. It compares strings with identity/malformed commands, lacks input validation, blocks the Swing event thread with JDBC and sleep, falls through from Receipt to Clear, prints user feedback only to the console, concatenates SQL, leaks resources, and ignores exceptions. The repair separates `FeeWindow`, `FeeService`, and a `FeeGateway` interface with demo and JDBC adapters. The JDBC adapter uses a prepared statement and try-with-resources. A `SwingWorker` performs slow work outside the event dispatch thread; callbacks separately handle Pay, Clear and Receipt. Four service checks pass, covering valid payment and invalid ID, zero amount, and excess decimal places.

Alternative complexity: a shared listener's if/else dispatch is O(k) comparisons for k commands and O(1) listener storage. Per-button listeners or shared Swing Actions route commands directly in O(1) application dispatch; separate listeners require O(k) listener storage.

## Task 2 - Outpatient appointment desk

The event model maps department changes to `ItemListener`/`ActionListener` doctor-list refresh, Book to an `ActionListener`, optional Enter to the same booking command, slot changes to selection updates, and frame close to `WindowAdapter.windowClosing`. The component tree uses a BorderLayout frame, a four-row/two-column center form, and a south FlowLayout booking button. Booking trims and validates a patient ID and doctor selection, disables duplicate submission while a background request runs, then restores state and shows success or error.

The standalone frame flow is `main -> invokeLater -> constructor/build -> listeners -> pack/show -> event queue -> windowClosing -> dispose`; applets instead use container callbacks `init/start/paint/stop/destroy`. A local doctor map costs O(d+m) references and O(d_j) to populate the chosen department; querying the database on every department change repeats network/SQL latency L plus O(d_j) result work. The map is faster per change but requires refresh/invalidation when data changes.

## Task 3 - Library search layout

The runnable Swing screen uses a two-row north header, a search panel, a scrollable center result area, and a south navigation/status region. Nested BorderLayouts give remaining width and height to the results area; FlowLayout keeps controls at preferred sizes. Three real native screenshots show the default, compact restored, and maximized layouts. A GridBagLayout is a viable alternative with more constraints; absolute positioning requires O(c) manual geometry updates for c controls on every resize and risks clipping. Normal nested layout traversal is O(c).

## Task 4 - AWT drawing pad

The typo `mouseclicked` does not override `mouseClicked`; `@Override` makes the compiler reject it. Even after fixing the spelling, drawing directly via `getGraphics()` is transient and disappears when the OS repaints. The repaired program stores each point, updates the click count, calls `repaint()`, and redraws the saved points in `paint`. The exact compiler diagnostic is saved in `Task04/outputs/broken-compile.txt`. Native screenshots show the three-click state before minimizing and the same count/dots after restoring. Repainting n stored points costs O(n) time and memory. A cached BufferedImage uses O(w*h) pixels and reduces repaint work to image transfer, at the cost of memory and resizing logic.

## Task 5 - Listener styles and converter

Runnable variants demonstrate a frame implementing `ActionListener`, a named inner class, an anonymous listener, and a lambda. Each registers the same command on the Convert button and Enter action and displays a result dialog. Invalid amounts are reported to the user. The supplied lambda exercise is completed in `Converter.java`; its screenshot shows 830 INR producing 9.96 USD using the provided 0.012 factor. The change-effort CSV records one Enter registration and one shared conversion/dialog logic change for each style. A shared `AbstractAction` or method reference is preferable when a command is reused across buttons, menus or keyboard shortcuts. One action with k registrations costs O(1) action storage and O(k) bindings.

## Task 6 - Event registration system

The layered Swing/JDBC application separates UI, validation/service policy, and database access. `PreparedStatement` avoids SQL concatenation; a unique `(student_id,event_name)` constraint prevents duplicate entries even under concurrent submissions. Register, page-fetch and CSV export work run in `SwingWorker`; the table uses bounded `LIMIT/OFFSET` pages and event filters. SQL schema and MySQL environment configuration are included. Actual tests used H2 2.3.232 in MySQL compatibility mode because MySQL server/Connector-J were unavailable. Normal insert, blank-ID validation, duplicate rejection and event filtering all pass. A native screenshot shows the duplicate message and database-backed rows.

For N rows and page size p, loading all rows transfers/builds O(N) client objects and requires O(N) memory; a page transfers and retains O(p), with index-dependent query work approximately O(log N+p) and possible O(N) sorting/scanning. Deep OFFSET paging can grow more expensive; keyset paging is better for deep traversals. Keep database work off the event thread in either design.

## Evidence and submission notes

Native UI screenshots are saved in task output folders: Task 3 default/compact/maximized, Task 4 before/after minimize and restore, Task 5 conversion result, and Task 6 duplicate rejection. Compiler and service output is stored as text logs. Enter the correct register number before submitting. The MySQL runtime is documented but only H2 was executed.
