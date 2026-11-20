# Task 6 - Campus event registration

## Run

Compile with Java 17+: `javac -encoding UTF-8 -cp Assignment4/Task06/lib/h2-2.3.232.jar -d Assignment4/Task06/out Assignment4/Task06/EventRegistrationApp.java Assignment4/Task06/RegistrationServiceChecks.java`. Run the checks with `java -cp "Assignment4/Task06/out;Assignment4/Task06/lib/h2-2.3.232.jar" RegistrationServiceChecks`. For an immediately runnable local JDBC demonstration use `java -cp "Assignment4/Task06/out;Assignment4/Task06/lib/h2-2.3.232.jar" EventRegistrationApp --demo`. The demo database starts in memory with Asha Rao already registered for Cultural Night; it supports normal registration, validation errors, duplicate rejection, paging and event filters. H2 2.3.232 is included for reproducible demonstration.

For MySQL, execute `sql/schema.sql`, put MySQL Connector/J on the runtime classpath, and set `EVENT_DB_URL=jdbc:mysql://localhost:3306/campus_events`, `EVENT_DB_USER`, and `EVENT_DB_PASSWORD`. Run `EventRegistrationApp mysql`. Credentials are read from environment and are not stored in source control. The demo runtime and deployment runtime share the same DAO/service SQL and unique constraint behavior.

## Architecture

```text
EventRegistrationApp (JFrame/JPanel/JTextField/JComboBox/JTable/JMenuBar)
           | Register / Filter / Page / Export commands
           v
RegistrationService (validates; registration, paging policies)
           |
           v
Database / JDBC (Connection + PreparedStatement + ResultSet)
           |
           v
MySQL campus_events.registrations (SQL schema + unique student/event key)
```

`SwingWorker` performs register and page-fetch database work off the event-dispatch thread. Field validation is synchronous and cheap, before starting the worker. A unique `(student_id,event_name)` index is the final race-safe duplicate guard; the service uses a prepared insert and the UI maps SQL integrity errors to a useful message. The Filter action resets offset and requests a page with `LIMIT`/`OFFSET`. Export streams all rows matching the selected event filter to CSV in a SwingWorker.

## Acceptance checks

| Case | Input / setup | Expected |
|---|---|---|
| Valid | `STU200`, `Mira Shah`, Cultural Night | Row persists and success dialog appears. |
| Invalid | Blank/short student ID or blank name | Validation dialog; no SQL write. |
| Duplicate | `STU100`, Asha Rao, Cultural Night (seeded row) | Unique constraint rejects insert; duplicate message appears. |
| Filter | Select Hackathon | Only matching registrations are shown. |
| Responsiveness | Page/load/register action | Database work executes on SwingWorker, UI event thread remains available. |

## Load-all alternative and complexity

For 100,000 rows, loading all registrations requires O(N) database transfer and table-model construction, O(N) client memory for row objects/strings plus database buffers, and can block the event-dispatch thread if implemented naively. With a page of p rows, each request transfers/builds O(p) client rows and uses O(p) model memory, while the database scan/index work depends on the query plan (roughly O(log N + p) with a suitable indexed predicate/order, potentially O(N) for a non-covering sort/filter). Navigation repeats network round trips and OFFSET cost can grow with offset; keyset pagination is preferable for deep traversal. `SwingWorker` moves I/O/row conversion off the EDT, so the window remains responsive in both designs, but a huge table can still cause long pauses during EDT model insertion. Use bounded pages and batch UI updates.
