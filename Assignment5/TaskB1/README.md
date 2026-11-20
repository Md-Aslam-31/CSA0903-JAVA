# B1 - File store and instrumented I/O

`PassStore.java` completes the supplied TODO. It scans with BufferedReader in try-with-resources, ignores malformed lines, returns the five fields on match and returns null otherwise. `FileBenchmarks.java` generates exact-size attendance fixtures and compares one-byte FileInputStream reads with 64 KiB bulk reads. `PassGrowthBenchmark.java` measures a last-record scan at 1,000, 10,000 and 100,000 passes. Raw timing data is saved under `outputs/`.

Tests cover successful add/find, absent pass ID, and a malformed line before a later valid row. B2 adds nonnumeric amount and empty-line adversarial cases. The benchmark is one local run on the recorded JDK/machine; OS cache, filesystem and JIT warm-up affect absolute times, so the trend and call-count rationale matter more than a universal speed claim.
