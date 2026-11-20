# A4 - Serialization design

The sample graph is `Hostel -> List<Room> -> List<Resident>`. Keep durable domain fields only. `password` and a live JDBC `Connection` are `transient`: credentials should not be persisted in plaintext and a connection is tied to a process and session. `serialVersionUID` is explicit, but class evolution still needs a compatibility policy, versioned migration, and tests for old snapshots. Deserialization of untrusted data is unsafe.

The executable example writes and reads a hostel snapshot and shows the durable resident data while transient fields reload as null. CSV is interoperable for flat records but needs escaping. JSON naturally represents nested data and is widely interoperable, but needs a library and schema/version discipline. Java serialization is convenient for Java-only object graphs but language-specific and sensitive to class changes.
