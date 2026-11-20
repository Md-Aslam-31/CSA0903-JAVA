# A1 - Design a file format

## Record layouts

Delimited text (UTF-8, one RFC 4180 CSV record per line): `pass_id,reg_no,name,route_no,boarding_stop,valid_until,amount`. Quote every field; double an embedded `"` as `""`. A name containing commas is quoted, for example `P17,192311007,"Rao, Arun",12,"North, Gate",2027-06-30,4500.00`. Parse with a CSV-aware parser; never use `split(",")` for the production format. Write to a temporary file then atomically replace the data file so a power failure cannot leave a half-record at the end.

Binary `DataOutputStream` layout, big-endian and versioned: 4-byte magic `BPS1`; 4-byte format version; for each record, 4-byte payload length followed by UTF-8 strings as `writeInt(byteLength)` + exact bytes (`pass_id`, `reg_no`, `name`, `boarding_stop`), 4-byte route number, 8-byte epoch day (`valid_until`), and 8-byte amount. Length-prefixing supports variable names and detects truncation; validate lengths against configured bounds before allocating. Do not persist Java `writeUTF` modified-UTF bytes as a general UTF-8 interchange format.

## Operations: text file

```text
ALGORITHM ADD_PASS(file, newPass)
BEGIN
    VALIDATE all fields and unique pass ID
    CREATE temporary file beside file
    IF file exists THEN
        FOR EACH parsed record IN buffered reader(file)
            WRITE encoded record TO buffered writer(temporary file)
        END FOR
    END IF
    WRITE encoded newPass TO buffered writer(temporary file)
    FLUSH and CLOSE writer
    ATOMICALLY REPLACE file WITH temporary file
END

ALGORITHM FIND_PASS_BY_ID(file, targetID)
BEGIN
    IF file does not exist THEN RETURN NOT_FOUND
    OPEN BufferedReader(FileReader(file, UTF-8))
    FOR EACH line
        TRY parse exactly seven CSV fields and validate types
        IF record.pass_id = targetID THEN RETURN record
        ON parse/validation error: RECORD line number; CONTINUE
    END FOR
    RETURN NOT_FOUND
END

ALGORITHM LIST_ROUTE(file, targetRoute)
BEGIN
    OPEN BufferedReader(FileReader(file, UTF-8))
    FOR EACH line
        TRY parse and validate record
        IF record.route_no = targetRoute THEN EMIT record
        ON damaged line: RECORD diagnostic; CONTINUE
    END FOR
END
```

`BufferedReader`/`BufferedWriter` reduce per-character calls and make lines convenient; `FileReader`/`FileWriter` use the platform charset unless an explicit charset API is used. Prefer `Files.newBufferedReader/newBufferedWriter(path, UTF_8)` for deterministic encoding. `FileInputStream`/`FileOutputStream` are appropriate for raw bytes or encoded binary files, not line-oriented text.

## Damaged data and complexity

Report a missing file distinctly (first use may initialize an empty store; lookup returns not found; list returns empty). A malformed line is quarantined/reported with its line number and skipped so one corrupt record does not terminate a lookup. For writes, write a complete replacement to a same-directory temporary file, flush/close it, then `ATOMIC_MOVE`; keep/restore a backup if the file system does not support atomic moves. A journal/checksum or database is preferable when strict crash recovery is required.

The line scan for lookup/list is O(n) time and O(record length) working memory; route listing is O(n+r) including r returned rows. A fixed-length `RandomAccessFile` with ID-to-slot index gives O(1) seek after index lookup, but finding the ID without an index remains O(n). Padding wastes `recordSize - actualSize` bytes per record and imposes field bounds; 8,000 variable-size rows benefit from compact CSV unless indexed lookup latency becomes a measured bottleneck.
