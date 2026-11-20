# A2 - Buffered I/O

## Cost prediction

For 50 MiB (52,428,800 bytes), a byte-at-a-time loop calls `FileInputStream.read()` approximately 52,428,801 times, including the final EOF read. Each Java call traverses the stream/native boundary; the operating system may cache disk blocks, but the program still pays per-byte Java/native call overhead.

## Buffered redesign

Use `BufferedInputStream` or a 64 KiB `byte[]` and call `read(buffer)` until EOF, count `\n` bytes in each chunk, and optionally count a final unterminated line. At 50 MiB and 64 KiB chunks this is about 800 bulk reads instead of 52,428,801 single-byte calls. Time stays O(N) in bytes; memory is O(B) for buffer size B. Sequential access and fewer calls drive the speedup.

```text
ALGORITHM COUNT_LINES(path)
BEGIN
    lineCount <- 0; byteCount <- 0; lastByte <- NONE
    OPEN buffered input stream with byte buffer of size B
    WHILE read(buffer) returns count > 0
        FOR EACH byte among first count bytes
            byteCount <- byteCount + 1
            lastByte <- byte
            IF byte = LINE_FEED THEN lineCount <- lineCount + 1
        END FOR
    END WHILE
    IF byteCount > 0 AND lastByte != LINE_FEED THEN lineCount <- lineCount + 1
    RETURN lineCount
END
```

Buffering gives little benefit when the data already comes from a fast in-memory source or when each record requires a synchronous network round trip/flush; the I/O wait or per-record work dominates. For a 50 MiB `Files.readAllLines`, time is O(N) but memory is O(N) plus String/object overhead. A buffered scanner uses O(B) memory and is appropriate for large files/streaming; read-all is convenient when the file is small and random access to all lines is needed.

The runnable benchmark in `TaskB1/FileBenchmarks.java` counts generated 1 MiB, 10 MiB and 50 MiB files using the two strategies and records elapsed milliseconds. Measurements and hardware/JDK context are in the root report and CSV output.
