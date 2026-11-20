# Assignment 5 - Solution Report

**Programming in Java | CO5 | Data Persistence Engineering**  
**Student:** Mohamed (as named in the existing repository)  
**Register number:** Not supplied; enter before submission.  
**GitHub:** https://github.com/Md-Aslam-31/CSA0903-JAVA/tree/main/Assignment5  
**Runtime:** Eclipse Temurin JDK 17.0.21; H2 2.3.232 for local JDBC tests.

## Execution and scope

This report covers A1-A5 and B1-B4 from the supplied handout. Sources, SQL, input files, logs, and measurements are arranged by task. Executable sources compile under JDK 17. The representative PassStore example was also run on OnlineGDB. Benchmarks/JDBC outputs are local JDK/H2 runs. The assignment specifies MySQL, but no local MySQL server or Connector/J was available; H2 in MySQL compatibility mode was used and is stated wherever relevant. No H2 timing is represented as MySQL performance.

The handout asks for a new `CSA09-<RegisterNumber>-Assignment5` repository. The user selected the existing `CSA0903-JAVA` repository. The register number was not provided, so that cover field must be filled in before submission.

# Part A - Persistence design

## A1. File format and operations

Use UTF-8 RFC 4180 CSV in this order: pass ID, register number, name, route number, boarding stop, validity date, amount. Quote fields and double embedded quotes, for example `P17,192311007,"Rao, Arun",12,"North, Gate",2027-06-30,4500.00`. A CSV-aware parser is required; `split(",")` is insufficient for quoted commas. A binary `DataOutputStream` format starts with a magic/version header and encodes variable strings as UTF-8 byte lengths plus bytes, followed by route integer, epoch-day long, and amount double. Validate lengths to detect truncation and avoid oversized allocation.

For add, validate, write existing records plus the new record to a same-directory temporary file, flush/close, then atomically replace the original. For find-by-ID and list-by-route, scan with `Files.newBufferedReader(path, UTF_8)` and parse each row. Buffered character streams suit line text; byte streams suit raw/binary data. Report malformed lines with line number and continue. Treat a missing file distinctly from a missing ID. Temporary-file replacement protects against a partly written replacement; keep a backup or use a journal/checksum where atomic moves are unsupported.

Sequential find/list are O(n) time and O(record length) working memory. A fixed-length `RandomAccessFile` can seek to record i in O(1) once its slot/index is known, but finding an ID without an index remains O(n). Fixed fields waste space equal to their padding and impose field bounds; an ID-to-slot index costs O(n) additional storage and must be kept consistent.

## A2. Buffered I/O

A 50 MiB file is 52,428,800 bytes. A one-byte loop issues about 52,428,800 reads plus the EOF read: 52,428,801 Java read calls. A 64 KiB buffer needs about 800 data reads plus EOF. Both are O(N) in bytes, but buffering uses O(B) memory for buffer size B and reduces calls. It helps sequential file reads; it helps little if data is already in memory or synchronous network round trips dominate. `Files.readAllLines` is O(N) time and O(N) memory plus String/list overhead, while streaming buffered I/O uses O(B). Read-all is useful for small files needing repeated in-memory access.

## A3. DAO design

StudentDB, CourseDB and MarksDB repeat connection acquisition, SQL execution, ResultSet mapping, exception handling, and resource closure. Entity-specific pieces are table/columns, key type, parameters, and mapper. A generic `Dao<T,K>` defines insert, findById, findAll, update, and delete; `StudentDao` binds Student fields using `PreparedStatement` and maps rows using try-with-resources. See `TaskA3/StudentDao.java`.

DAO centralizes the SQL/object-table contract. It does not change query asymptotics: indexed lookup is typically O(log n), full listing O(n); the extra method dispatch is O(1).

## A4. Serialization

The example graph is Hostel → Rooms → Residents. Resident has an explicit `serialVersionUID`; password and active JDBC Connection are `transient`, so they reload as null. Reachable serializable objects are traversed; graph changes require compatibility/migration strategy. CSV is simple and interoperable for flat records but needs escaping; JSON naturally represents nested structures but needs a library and schema/version discipline. Serialization is Java-specific and must not deserialize untrusted data.

## A5. Transactions and injection

For a ₹500 transfer, disable auto-commit, debit only if source funds suffice, credit the destination, verify both update counts, commit only after both updates, and roll back on any failure; restore the prior auto-commit. Separate auto-commit statements can leave a persisted debit without its credit. Transactions add logging/locking/commit cost, while two indexed row updates remain constant-count operations in this simplified model.

Concatenated login SQL lets input alter grammar. `admin' OR '1'='1' -- ` can make the predicate succeed and comment out the password test. Bind ID and password hash in a PreparedStatement. Parameter values cannot become SQL syntax. Regex/quote escaping is fragile across encoding, types, collation, comments, and dialects. Validation/escaping and binding each process O(m) input characters; binding is the robust grammar boundary. Production passwords must be salted hashes.

# Part B - Implementation and experiments

## B1. File store and measurements

`PassStore.find` uses `BufferedReader` in try-with-resources, skips wrong field counts and nonnumeric data, returns matching five fields or null, and continues after damage. Tests cover add/find, missing ID, malformed shape, invalid number, blank line, and later valid data. The supplied exercise uses a simple comma format; the production A1 design specifies a CSV-aware parser.

| File bytes | Lines | One-byte ms | Buffered 64 KiB ms |
|---:|---:|---:|---:|
| 1,048,576 | 13,105 | 1,798 | 4 |
| 10,485,760 | 131,041 | 17,777 | 11 |
| 52,428,800 | 655,201 | 88,431 | 26 |

One local run; raw values are in `TaskB1/outputs/buffering-measurements.csv`. Counts agree. Times vary with machine, filesystem cache, and JIT; the 50 MiB byte-at-a-time run took about 88 seconds while buffered reading took 26 ms.

| Records | Last-record lookup ms |
|---:|---:|
| 1,000 | 1.217 |
| 10,000 | 3.577 |
| 100,000 | 22.024 |

Lookup is O(n) time, O(1) working memory beyond the current line. A HashMap can provide expected O(1) lookup at O(n) extra memory, with index maintenance/rebuild costs. Measurements are a single run and noisy.

## B2. Adversarial inputs

In the local database demo, invalid ID `admin' OR '1'='1' -- ` with a wrong password returns true in the unsafe concatenated query and false with PreparedStatement. A damaged file tests wrong field count, nonnumeric amount, and blank line before a later good row. Diagnostics are emitted and the valid record is still found. See `TaskB2/outputs/`.

## B3. JDBC strategy and atomicity

`PassLoader.loadAll` binds five parameters, batches every 1,000 rows, commits once, rolls back on SQL/runtime failure, restores auto-commit, and returns row count. The benchmark verifies row counts at 10, 1,000, 10,000, and 100,000 and prints JDBC metadata.

| Rows | Statement transaction ms | Prepared transaction ms | Prepared batch transaction ms | Prepared batch auto-commit ms |
|---:|---:|---:|---:|---:|
| 1,000 | 97.182 | 25.406 | 17.299 | 12.010 |
| 10,000 | 185.724 | 139.753 | 126.763 | 109.480 |
| 100,000 | 776.356 | 518.636 | 423.937 | 459.944 |

These are single H2 2.3.232 run times, not MySQL performance. Repeated trials/warm-up and median/variance would be needed for strong timing conclusions. Batch client parameter memory is O(b) for batch size b (plus driver internals); total insert work remains O(n). Multi-row INSERT can use O(n) statement/parameter memory unless chunked. LOAD DATA can reduce per-row protocol work but is database-specific and requires secure file handling.

An invalid amount at row 5,001 of 10,000 left 5,000 rows with auto-commit enabled and zero rows with a transaction. This directly demonstrates rollback's value. MySQL rerun is needed for MySQL-specific behavior/performance.

## B4. Migration and engineering decision

Migration streams legacy rows, validates fields, reports rejected line numbers/reasons, batches inserts of 1,000 in one transaction, and prints DatabaseMetaData product/driver/table/column information. Sample result: 3 accepted, 2 rejected, 3 committed.

At 12 campuses and about 100× the current data with concurrent counters, use a shared relational database with unique constraints, indexes, connection pooling, transactions, and access controls. Text files suit exchange/archive but require scans, locking, crash recovery and duplicate coordination. Serialization is Java-specific and needs version management. Twelve independent serialized files require aggregate O(n) search, O(n) total storage, and cross-campus conflict handling; counters need file locks plus reconciliation or a shared coordination service.

## Evidence note

Raw stdout and benchmark CSVs are committed under each task's `outputs/`. The PNG evidence pages in the PDF are explicitly labeled readable transcript renderings from those stdout files; they are not claimed as OnlineGDB interface screenshots. The actual OnlineGDB run displayed successful output; it is noted in the repository README.
