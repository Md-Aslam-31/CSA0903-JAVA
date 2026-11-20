# Assignment 2 - Java Collections and Design

**Course:** CSA09 - Programming in Java (CO2)  
**Student:** Md Aslam  
**Register number:** [Enter your register number before submission]  
**Repository:** https://github.com/Md-Aslam-31/CSA0903-JAVA  
**Compiler/runtime:** OnlineGDB Java; JDK 17 language features  
**Questions:** Q1-Q7 | **Date executed:** 20 November 2026

> Evidence distinction: code was actually compiled and run in OnlineGDB. The repository and PDF include readable images made from the captured console text, not browser-window screenshots. The controlled browser showed the actual results, but its screenshot could not be exported into a file. Raw console text and measured values are retained in each task's `outputs/` folder. All timings below are measurements from one online execution session, not portable performance guarantees.

## Experimental method and limits

- Time was measured with `System.nanoTime()` and reported in milliseconds. Workloads include a checksum or retained result so the computation remains observable.
- Q1 uses one warmup call for each method/workload and averages five timed rounds. Q2 averages five rounds and constructs the collection before starting each operation timer. Q4 uses five rounds for puts, 100,000 lookups and iteration, with keys/query arrays prepared outside timed sections. Q6 averages five add-and-drain runs per size. Q7 uses two warmup comparisons and seven measured rounds on the same prebuilt data for both implementations.
- These are simple `nanoTime` experiments in a shared online VM. They are suitable for checking direction and scale, but not a substitute for JMH with forks, controlled heap/GC, and confidence intervals. Some timings vary sharply with JIT compilation and garbage collection; conclusions combine measured data with collection/API complexity.
- `estimated_intermediate_result_strings` is an allocation estimate for result strings, not an object-profiler count. Modern Java compilers may allocate additional temporary buffers/arrays. Heap-delta measurement was omitted because the online shared heap and GC make a single before/after number misleading.

---

## Q1 - Building text under load: String, StringBuilder or StringBuffer

### Prediction and design

`String` is immutable. In a loop, `s += field` must create a new result string as the prefix grows. For `n` fields, the copied prefix lengths sum to `1 + 2 + ... + n`, or O(n^2) characters in the worst case. At least `n-1` intermediate result strings per ticket are estimated. A builder grows a mutable buffer and copies the final content once: O(n) appended characters and O(n) storage. `StringBuffer` has the same append strategy with synchronized methods; its coordination cost is useful only if the mutable buffer is genuinely shared.

Three intentionally different workloads were used: one short ticket (1 x 10 fields), many medium tickets (10,000 x 50), and fewer wide tickets (2,000 x 100). Each field contains a short deterministic text value. All methods generate identical output lengths and checksums.

### Measured evidence

| Workload | Tickets x fields | Method | Average ms (5 rounds) | Estimated intermediate result Strings | Checksum |
|---|---:|---|---:|---:|---:|
| Short | 1 x 10 | String `+` | 0.031 | 9 | 120 |
| Short | 1 x 10 | StringBuilder | 0.029 | 1 final | 120 |
| Short | 1 x 10 | StringBuffer | 0.024 | 1 final | 120 |
| Many medium | 10,000 x 50 | String `+` | 28.953 | 490,000 | 6,400,000 |
| Many medium | 10,000 x 50 | StringBuilder | 14.727 | 10,000 | 6,400,000 |
| Many medium | 10,000 x 50 | StringBuffer | 25.371 | 10,000 | 6,400,000 |
| Fewer wide | 2,000 x 100 | String `+` | 9.008 | 198,000 | 2,580,000 |
| Fewer wide | 2,000 x 100 | StringBuilder | 6.335 | 2,000 | 2,580,000 |
| Fewer wide | 2,000 x 100 | StringBuffer | 3.065 | 2,000 | 2,580,000 |

The short case is too small to distinguish methods reliably. For many medium tickets, StringBuilder was about 1.97x faster than `+`; for fewer wide tickets, StringBuffer happened to be fastest in this session. That reversal is a useful warning: five rounds in one shared VM are not enough to claim a universal StringBuilder-versus-StringBuffer winner. All three methods produced the same checksum, while the estimated intermediate result String count for repeated `+` grew by roughly 49 times in the 50-field workload.

### Decision and alternatives

Use a new thread-confined `StringBuilder` for each ticket: it avoids shared mutable state and has no per-append synchronization. For a shared booking log, prefer a logging queue/writer with a clear concurrency policy. `StringBuffer` can synchronize individual appends, but a sequence of appends for one log record is not automatically atomic as a group; synchronize the whole record or enqueue immutable records if interleaving matters.

`String.join(delimiter, fields)` and `StringJoiner` also perform linear aggregation and are readable for delimiter-based output. `String.format` is convenient for fixed templates but has formatting/parser overhead and is not a substitute for repeated growing-prefix concatenation. Complexity by output length is O(n) for a builder/join operation and O(n^2) for naive repeated concatenation; both need O(n) final output space, but repeated concatenation creates more temporary strings and copies.

Source: `Task01/StringsUnderLoad.java`. Raw output and CSV: `Task01/outputs/`.

---

## Q2 - When is ArrayList appropriate?

### Design and test conditions

A playlist with 1,000, 5,000 or 20,000 entries was tested. Each result is the five-round average; collection construction is outside timed sections. Each round adds 10,000 elements at the end. For front operations, middle operations and k-th access, operation count is `max(20, 1,000,000 / n)` so approximate work stays bounded as `n` grows. Iteration visits the entire collection once. ArrayDeque has no indexed `get` or middle-insert operation, so the k-th comparison traverses an iterator and middle insertion is not applicable.

### Timing results

Times are milliseconds. A/L/D means ArrayList / LinkedList / ArrayDeque. `mid` is a matched insert/remove pair at the middle; `getK` uses the scaled operation count.

| Size | Ops for scaled tests | End adds 10k A/L/D | Front add+remove A/L/D | Middle pair A/L | Get k A/L/D iterator | Full iteration A/L/D |
|---:|---:|---:|---:|---:|---:|---:|
| 1,000 | 1,000 | 0.682 / 0.938 / 0.788 | 0.265 / 0.170 / 0.123 | 0.202 / 1.779 | 0.069 / 0.778 / 12.999 | 0.101 / 0.126 / 0.078 |
| 5,000 | 200 | 0.662 / 0.347 / 0.327 | 0.143 / 0.019 / 0.024 | 0.078 / 1.609 | 0.015 / 0.791 / 0.544 | 0.379 / 0.380 / 0.359 |
| 20,000 | 50 | 0.154 / 0.310 / 0.310 | 0.152 / 0.007 / 0.006 | 0.074 / 1.644 | 0.005 / 0.832 / 0.555 | 0.683 / 0.763 / 0.635 |

Some ArrayDeque/LinkedList access timings at small sizes show high noise and one outlier, which is why I use the operation contracts as well as the observations. Middle insertion by index is much slower for LinkedList in all three displayed samples because locating the index takes traversal; changing from array shifting to nodes does not remove that search. ArrayList k-th access remains far faster. End-add timing differences are small and not stable enough here to choose on speed alone.

### Recommendation and complexity

Use ArrayList when the playlist is mostly read/iterated, appends at the end, and needs random song access. Stop recommending it when frequent front insertion/removal dominates at large playlists: shifting the remaining references is O(n) each time. For the revised Up Next queue, ArrayDeque is a natural choice: add/remove at either end is amortized O(1), but it has no index-based middle operations. LinkedList adds O(1) node storage per element (plus references) but has poor locality and indexed insert/get still take O(n) to find the position.

| Operation | ArrayList | LinkedList | ArrayDeque |
|---|---:|---:|---:|
| Append | amortized O(1) | O(1) | amortized O(1) at tail |
| Add/remove front | O(n) | O(1) | amortized O(1) |
| Insert at middle by index | O(n) shift | O(n) search + O(1) link | unsupported directly |
| Get k-th | O(1) | O(n) | O(n) traversal; no `get` API |
| Iterate n items | O(n), contiguous refs | O(n), node traversal | O(n), circular array |
| Storage per item | one reference slot plus spare capacity | node object with item/prev/next references | one reference slot plus spare capacity |

A plain array can implement insert/remove by shifting the suffix, O(n), and get by direct index, O(1). It needs manual resize/capacity logic; an ArrayList already provides that machinery. For Up Next, a `Deque<Song>` backed by ArrayDeque expresses both-end use directly.

Source: `Task02/PlaylistCollections.java`. Raw averages: `Task02/outputs/measurements.csv`.

---

## Q3 - Engineer the hashCode/equals contract

### Predictions and observed tests

Hash-based lookup first uses `hashCode()` to find a bucket, then `equals()` to compare candidates in that bucket. Equal objects must have equal hash codes. Without a matching override, distinct objects commonly have different identity-based hashes, so `equals()` may never be reached for a duplicate scan. The result depends on hash collisions; it is not guaranteed to be exactly three for every possible execution.

| Test | Prediction before run | Observed size/result | Explanation |
|---|---|---|---|
| Add the same object twice | 1 | size 1 | HashSet detects the identical reference. |
| Add distinct objects with repeated registration, broken class has equals only | usually 3 | size 3 | Identity hashes placed equal registrations in different buckets in this run. |
| Correct Student with immutable `regNo`, equals and hashCode use registration | 2 | size 2 | Equal registrations have equal hashes and equals removes duplicate. |
| Mutate a key field after insertion | `contains` can fail | false, size remains 1 | The object is stored in the bucket chosen by its old hash; lookup uses its new hash. |
| TreeSet comparator compares registration only | 1 for duplicate registration | size 1 | Comparator result 0 defines sameness for TreeSet. |

The fix makes `regNo` final and implements `hashCode()` from the same identity field used by `equals()`, e.g. `Objects.hash(regNo)`. Never mutate a field that participates in a hash key while the key is stored in a HashSet/HashMap. The TreeSet alternative has O(log n) add/search and sorted iteration; `HashMap<String,Student>` keyed by registration has expected O(1) lookup and O(n) storage, while HashSet<Student> is a direct expected-O(1) unique collection.

Source: `Task03/Student.java`. The incomplete and corrected outcomes are retained in its output transcript.

---

## Q4 - Pharmacy inventory maps

### Design and measurement

Medicine codes are generated before timed sections. Each size is inserted into a fresh map in each of five rounds; the reported put value is the average. Then 100,000 lookup keys are prebuilt and lookup/iteration are each timed five times on populated maps. The report prioritizes the actual operations: repeated stock lookup, code-sorted nightly report and insertion-order report.

| Medicines | Put avg ms H/T/L | 100k get avg ms H/T/L | Iterate avg ms H/T/L | First key H/T/L |
|---:|---:|---:|---:|---|
| 1,000 | 0.188 / 0.490 / 0.393 | 3.711 / 29.561 / 2.643 | 0.195 / 0.195 / 0.166 | M522654 / M000000 / M000000 |
| 10,000 | 0.626 / 12.227 / 1.559 | 1.226 / 18.827 / 1.400 | 0.527 / 0.351 / 0.360 | M995484 / M000000 / M000000 |
| 50,000 | 3.920 / 34.692 / 21.756 | 13.531 / 41.181 / 3.304 | 0.922 / 0.455 / 0.881 | M246564 / M000000 / M000000 |

H/T/L is HashMap / TreeMap / LinkedHashMap. These timings are particular to one OnlineGDB session; the stable engineering distinctions are the contracts, not the exact rank of each row. TreeMap returns key order; LinkedHashMap preserves insertion order; HashMap does not promise either. All three map types produced the required key lookup values. In this run, 100,000 lookups remained far below the stated acceptability criterion of 1 ms per lookup even for TreeMap at 50,000 entries (41.181 ms total, about 0.412 microseconds/lookup). Remaining costs include hashing/comparison, memory/cache misses, string-key creation if done by the caller, and VM scheduling.

Use HashMap as the stock lookup store if lookup throughput is primary, TreeMap if a continuously sorted view is required, and LinkedHashMap for the insertion-order report. A wrong map can still make a report by copying and sorting keys, O(n log n), or retaining a second ordered index, which uses more memory. Hashtable is an older synchronized API; new code should choose an explicit concurrent map or external synchronization only when its concurrency requirements call for it.

A sorted array of medicine records supports `Arrays.binarySearch` in O(log n) lookup and O(n) insertion due shifting, with O(n) storage. It naturally produces code order in O(n) iteration and no hash buckets, but frequent updates are more expensive than map puts. It is reasonable for static/read-mostly stock snapshots.

Source: `Task04/PharmacyMaps.java`. Five-run raw data is in `Task04/outputs/measurements.csv`.

---

## Q5 - Generic ranking component

`Team` stores points, goal difference and name. The comparator is lexicographic: points descending, goal difference descending, then name ascending. The comparator chain uses `Comparator.comparingInt(...).reversed().thenComparing(...)`; it avoids subtraction overflow and applies each tie-break only after the prior fields compare equal. This lexicographic composition is transitive because each component comparator is transitive and the first nonzero component determines the result.

`Leaderboard<T>` accepts `Comparator<? super T>`, stores a `List<T>`, and returns a sorted copy using `List.sort`, so asking for the ranking does not mutate insertion order. `addAll(Collection<? extends T>)` uses an extends wildcard because the collection produces values assignable to T. The comparator is injected, so the type does not need to implement `Comparable`; that keeps Team, Player and College free to expose different ranking policies. If one universal natural order were intrinsic to a type, `T extends Comparable<? super T>` would be a reasonable bound. `List<? extends T>` is also useful when loading a batch of subtype items into a broader leaderboard.

OnlineGDB output:

```text
Ranking: [Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]
Generic bounded-independent list size: 4
Bad subtraction comparator sign(hi,lo): 1 (wrong; should be negative)
```

Bisons precedes Panthers because points and goal difference tie, then “Bisons” precedes “Panthers” alphabetically. The faulty subtraction comparator overflows for `Integer.MAX_VALUE` and `Integer.MIN_VALUE`, returning the wrong sign. The alternative stable-sort design applies least-significant key first: name ascending, then goal difference descending, then points descending. It requires three O(n log n) sorts (still O(n log n) asymptotically, with greater constants) and relies on stability; one comparator chain is easier to audit and reuse. A TreeSet using this comparator would collapse records whose ranking keys all compare as zero; a sorted List retains all entries.

Source: `Task05/LeaderboardDemo.java`.

---

## Q6 - Priority, order and fairness in triage

The comparator sorts severity ascending (1 is critical) and then arrival sequence ascending. The arrival counter gives a total FIFO order among equal severities. The eight-case hand result is:

```text
Meena(S1) Kumar(S1) Lata(S2) Zoya(S2) Ravi(S3) John(S3) Dev(S3) Asha(S5)
```

The program displays a copied list sorted by the comparator; printing `PriorityQueue` directly exposes its heap layout, not a fully ordered traversal. Adding and polling are O(log n) each, peek is O(1), and space is O(n).

| Queue size | Average time for adding all and polling all, 5 rounds (ms) |
|---:|---:|
| 1,000 | 3.568 |
| 10,000 | 5.347 |
| 50,000 | 25.178 |

The workload was short and ran in one shared online VM, so it confirms executable behavior and a rough scale, not a service-level capacity benchmark. A PriorityQueue is reasonable for 5,000 daily patients in an in-memory process; production must also account for concurrent admissions, durable records, cancellation, multiple doctors and database/service overhead.

Strict severity priority can starve severity-5 patients if higher severity cases continue indefinitely. One fair policy is aging: every fixed wait interval, improve a patient's effective priority by one severity level (bounded at critical), preserve arrival sequence within the new level, and reinsert/rekey the patient. This bounds the wait at the cost of allowing older less-severe cases eventually to outrank newly arrived urgent cases. Another design is five FIFO queues plus periodic promotion. A sorted LinkedList has O(n) admission and O(1) removal from the head; five FIFO queues give O(1) admission/removal plus a scan of at most five heads (O(1) for fixed five levels), but the policy for aging and within-level arrival order must still be implemented.

Source: `Task06/Triage.java`; raw measurements are in `Task06/outputs/measurements.csv`.

---

## Q7 - Diagnose, redesign and validate

### A-C. Diagnosis, objective and redesign

The original Q1 strategy repeatedly concatenates an immutable String. Each next append copies the current prefix, so building a ticket with n fields copies O(n^2) characters and produces many intermediate result strings. In the selected many-ticket workload, the Q1 run estimated 490,000 result strings from 10,000 x 50 fields. The redesign objective is to create the same text and checksum while reducing the result String estimate to one final String per ticket and the work to O(n) appended characters per ticket.

Pseudocode:

```text
for each ticket:
    builder = StringBuilder(estimatedCapacity)
    for field in ticket:
        builder.append(field).append(delimiter)
    ticketText = builder.toString()
```

This adds one mutable builder per ticket and a capacity estimate. It preserves each ticket as independent thread-local work and does not add global synchronization.

### D-E. Prediction and validation

Prediction: for 10,000 tickets with 50 already-created fields each, StringBuilder should be faster and use fewer intermediate result Strings. Both implementations should produce equal lengths/checksums. The comparison prebuilds the identical input, performs two warmups, then averages seven timed rounds.

| Same Q7 workload | Average ms (7 rounds) |
|---|---:|
| Original repeated String `+` | 21.578 |
| Pre-sized StringBuilder | 3.262 |
| Measured speedup | 6.61x |

The checksum was 6,400,000 for both. This supports the prediction for this workload. Q1's separate 10,000 x 50 test yielded 28.953 ms vs 14.727 ms; it is not directly interchangeable because Q1 constructs field text inside each timed ticket build, while Q7 deliberately prebuilds identical field arrays before timing to isolate ticket assembly. Both experiments point the same way, but their different setup explains the different speedup.

### F. Final design defence

StringBuilder achieved the defined objective on this test: same output checksum, about 6.61x faster observed time and one estimated final result String per ticket instead of 49 intermediate result Strings per ticket. The residual costs are the builder buffer, the final String copy, and choosing an initial capacity. For one tiny ticket, the measured methods were effectively tied and simple concatenation can be clearer. For this repeated e-ticket workload, the builder is a defensible default. A formal claim about memory or broad JVM performance would require an allocation profiler and a proper JMH benchmark across forks/VMs.

Source: `Task07/RedesignComparison.java`; raw output is in `Task07/outputs/run-output.txt`.

---

## Overall result and submission checklist

The source, result tables, output transcripts and this report cover Q1-Q7, including the three supplied incomplete programs (Q3, Q5 and Q6). The current repo is the one the user designated; the assignment-specific repository naming example was not used. Before submission, replace the register-number placeholder. The assignment sheet also requires sharing the repo with faculty; no faculty account was supplied, so no collaborator was added.
