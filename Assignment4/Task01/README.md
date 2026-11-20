# Task 1 - GUI design review

## Diagnosis

| Defect | User impact or risk |
|---|---|
| Frame owns validation, JDBC and payment policy | UI changes and payment logic become coupled; service rules are difficult to test without opening a window. |
| `==`/malformed action-command comparison is used for text | Strings require `.equals`; identity comparison can miss a matching command. |
| No empty, numeric, positive or range validation | Empty or malformed amount can throw on the event thread; invalid payments may reach storage. |
| JDBC call and `Thread.sleep(3000)` run in `actionPerformed` | The Event Dispatch Thread is blocked; the whole window freezes and may be marked unresponsive. |
| Receipt falls through to Clear | The user loses entered data and can never obtain a receipt. |
| No outcome is shown in the window | A console print is invisible to many GUI users, and failures have no recovery path. |
| SQL is concatenated | Quotes can break the query and attacker-controlled values can cause SQL injection. |
| Connection, Statement and window lifecycle are unmanaged | Connections leak; close events may leave resources or the process alive. |
| Checked SQL/interruption failures are ignored | The code does not compile as shown or loses actionable error information if exceptions are swallowed. |

## Repaired structure

`FeeWindow` owns controls, parsing, status and event wiring only. `FeeService` validates business input and invokes the `FeeGateway` port. `DemoFeeGateway` is a deterministic executable demonstration; production wiring can provide a JDBC gateway using a prepared statement and try-with-resources. The Pay callback creates a `SwingWorker`: service and database work run in `doInBackground`, and status is updated in `done` on the event thread. Pay, Clear and Receipt have separate callbacks and close uses a `WindowAdapter`.

```text
FeeWindow -> FeeService -> FeeGateway
                         -> DemoFeeGateway (demonstration)
                         -> JdbcFeeGateway (deployment adapter)
```

`JdbcFeeGateway` is the production adapter and uses a prepared statement with an automatically closed connection. `Task01/sql/schema.sql` gives its table definition. `FeeWindow` uses the demo gateway by default so the UI can run without credentials; production wiring can construct the service with `JdbcFeeGateway` and externally supplied connection settings.

## Design defence (under 100 words)

The window and service can be tested separately. FeeService accepts ordinary values and a replaceable gateway, so validation tests do not create a frame or open a database. FeeWindow owns input collection and feedback, while a worker keeps slow payment work off Swing's event-dispatch thread. Prepared-statement SQL belongs in a JDBC adapter and can be changed without rewriting controls. Each command has a named responsibility, so Receipt cannot accidentally execute Clear behavior. Closing and resource ownership are explicit, making failures easier to diagnose and future UI changes less risky.

## Alternative and complexity

With a shared listener and an `if/else` chain, dispatch compares the action against commands until one matches: worst case O(k) string comparisons for k buttons, and O(1) listener storage. Giving each button one listener object (or a shared Swing `Action`) makes dispatch direct, O(1) after the toolkit routes the event, but uses O(k) listener objects if each is distinct. An `Action` shared by multiple controls can reduce duplication. UI event dispatch itself is O(1) with either approach; the comparison describes application-level command selection, not JVM event-queue internals.
