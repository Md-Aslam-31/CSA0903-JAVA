# B3 - JDBC insertion strategies

The completed `PassLoader` binds five values, batches at 1,000 rows, commits once, rolls back every batch on SQL/runtime failure, restores autoCommit, and returns the row count. `JdbcBenchmarks` inserts 10, 1,000, 10,000 and 100,000 synthetic passes with Statement, PreparedStatement and PreparedStatement batches, verifies counts, inspects column metadata, compares transaction timings and forces a bad amount at row 5,001 with autoCommit on/off.

The task specifies a MySQL table. This environment has no MySQL server or Connector/J, so the repeatable local execution uses bundled H2 2.3.232 in MySQL compatibility mode. Results demonstrate JDBC API, batching and transaction behavior on H2 only and must not be interpreted as MySQL throughput. The SQL schema is compatible in spirit; the report records the deviation and recommendation for a MySQL rerun if server credentials become available.
