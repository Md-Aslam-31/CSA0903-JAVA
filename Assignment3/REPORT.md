# Assignment 3 Solution Report - Exception Handling and Concurrency

**Student:** Md Aslam  
**Register number:** [Enter before submission]  
**Partner:** [Enter partner name and register number before submission]  
**Course:** CSA09 Programming in Java, CO3  
**Compiler used for recorded runs:** Eclipse Temurin JDK 17.0.21 (local)  
**Repository:** https://github.com/Md-Aslam-31/CSA0903-JAVA  
**Tasks:** Q1-Q7 | **Total marks:** 50

## Submission and evidence notes

The assignment brief describes pair work, asks for each Student A and Student B section to be attributable, requires a separately named CSA09-<RegisterNumber>-Assignment3 repository, and requests browser screenshots. The student asked to continue using the existing CSA0903-JAVA repository. The register number and partner identity were not supplied. This report therefore leaves those fields and the contribution record for the students to complete honestly. Do not claim that the other student completed work they did not do.

The desktop browser changed state while OnlineGDB was being set up and the compiler run could not be verified there. All recorded Java outputs in this submission are from local Eclipse Temurin JDK 17.0.21. The PNG evidence panels are labeled renderings of captured console output text; they are not native browser screenshots. The raw stdout and measurement CSV files are kept in each task folder.

The experiments use small, reproducible synthetic inputs to demonstrate exception and concurrency behavior. Timings are single-environment observations, not general performance guarantees.

# Q1 - Handling Failure in a Bank ATM Simulation

## Student A section - exception design draft

The program reads an account number and amount, validates the values, checks the simulated card-reader file and either dispenses cash or reports a distinct failure. Its exception hierarchy is:

~~~text
Object
└── Throwable
    ├── Error
    │   └── serious VM/resource failures (not caught by this ATM)
    └── Exception
        ├── IOException
        │   └── FileSystemException
        │       └── NoSuchFileException
        ├── InsufficientFundsException (checked, user-defined)
        └── RuntimeException
            ├── IllegalArgumentException
            ├── NoSuchElementException
            │   └── InputMismatchException
            └── SecurityException
~~~

| Failure point | Exception | Checked? | Handler |
|---|---|---|---|
| Scanner reads letters as an integer | InputMismatchException | No | Multi-catch with SecurityException |
| Account is zero or negative | IllegalArgumentException | No | Invalid-value catch |
| Withdrawal is negative | IllegalArgumentException | No | Invalid-value catch |
| Amount exceeds balance | InsufficientFundsException | Yes | Business-rule catch |
| Card reader path is absent | NoSuchFileException (an IOException) | Yes | IOException catch |
| JVM cannot continue, such as an OutOfMemoryError | Error subclass | No | Not caught as an ordinary transaction failure |

The catch blocks distinguish malformed input/access failure, the checked business rule, bad numeric values, and I/O failures. The program catches narrow cases and does not place a general Exception handler first. A catch(Exception e) before these branches would make the more specific catches unreachable at compile time. A single broad catch can be useful only at a final process boundary for logging and safe shutdown; inside transaction logic it loses the classification needed to decide whether the user can retry, correct input, or contact support.

The finally block prints a cleanup marker after a successful or failed withdrawal attempt. The Scanner is also managed by try-with-resources. A finally block normally runs as control leaves the try, including exceptional exits, but it cannot run after abrupt JVM termination or a forced process kill. It does not replace explicit resource management.

## Student B section - test and trace draft

Five distinct input paths were run. The cleanup line appears in all five cases.

| Input | Observed result | Catch path | Finally |
|---|---|---|---|
| 1001 100 | Cash dispensed | None | Yes |
| 1001 abc | InputMismatchException | InputMismatchException multi-catch | Yes |
| 1001 -20 | Withdrawal cannot be negative | IllegalArgumentException | Yes |
| 1001 600 | Exceeds balance | InsufficientFundsException | Yes |
| 1001 20 with missing reader | NoSuchFileException | IOException | Yes |

The program also launches a worker that throws an unexpected IllegalStateException. Its uncaught-exception handler prints the stack trace:

~~~text
java.lang.IllegalStateException: unexpected ATM fault
    at ATMDemo.lambda$main$0(ATMDemo.java:31)
    at java.base/java.lang.Thread.run(Thread.java:840)
~~~

The first line identifies the runtime exception and its message. The lambda frame points to the throw expression in main. Thread.run is the JVM entry point dispatching the worker's run method. The worker has no local catch for this unexpected fault, so the thread's uncaught-exception handler receives it.

## Joint design and alternative

Catching only Exception hides whether the failure was bad user input, a domain rejection, or an infrastructure problem. It can be acceptable at an application boundary if the program logs the full cause and shows a generic safe message. It is a poor substitute for meaningful handling inside the withdrawal flow.

An exception-free version would call Scanner.hasNextInt() before each nextInt(), validate positive account and amount values, check the balance, and check Files.exists/readability before opening the card file. Each failure would return a named status such as INVALID_INPUT, NEGATIVE_AMOUNT, INSUFFICIENT_FUNDS, or CARD_READER_ERROR. For a fixed two-number transaction, validation and status return are O(1), excluding file I/O. Throwing an exception also has O(1) work before construction, but collecting a stack trace costs time and memory proportional to stack depth d, O(d). Status codes avoid that cost for routine expected failures, but require repeated checks and explicit result interpretation. Exceptions keep propagation concise for uncommon failures.

# Q2 - User-Defined Exceptions and Propagation

## Student A section - hierarchy and propagation draft

The hierarchy used by the program is:

~~~text
Throwable
├── Error
└── Exception
    └── ExamException (checked)
        ├── StudentNotRegisteredException (checked)
        └── SubmissionStorageException (checked, retains cause)
RuntimeException
└── ExamClosedException (unchecked)
~~~

StudentNotRegisteredException and SubmissionStorageException are checked because the calling service/controller is expected to handle normal submission rejection and storage failure. ExamClosedException is unchecked in this design because the controller detects a request made outside the valid operation window before proceeding. A different API could make it checked; the key is that callers must understand the contract.

The storage failure propagates as Storage.save -> SubmissionService.submit -> controllerSubmit. Storage converts the low-level IOException into SubmissionStorageException with the original exception as its cause. The controller handles ExamException and displays a meaningful message while retaining diagnostic details.

## Student B section - scenarios and chaining draft

| Scenario | Injected condition | Result at controller |
|---|---|---|
| Accepted submission | 120 KB, registered, exam open | Saved; submission accepted |
| Storage limit exceeded | 800 KB causes IOException | Could not store answer02.pdf |
| Student not registered | registered=false | Student is not registered |
| Exam closed | examOpen=false | Exam window has closed |

For the large file, getCause() returns IOException with message Disk quota exceeded. The saved run output includes the chained trace: the SubmissionStorageException frames lead through Storage.save, SubmissionService.submit, and StorageDemo.controller; the Caused by section identifies the original IOException and its storage frame. If the wrapper is constructed without passing the cause, getCause() is null, the low-level message and origin frame are lost, and the report prints Cause when omitted: null.

A meaningful SubmissionStorageException gives the upper layer a stable, user-facing domain message without leaking filesystem details. The original IOException remains available for logs and debugging. Stack-trace capture takes O(d) time and memory in stack depth d for each exception. A cause chain retains the wrapped exception and its trace, so memory is approximately the sum of the wrapper and cause traces, O(d1+d2+...). This is appropriate for exceptional failures, not high-volume control flow.

## Joint conclusion

Checked versus unchecked affects throws clauses and compile-time handling. Making ExamException unchecked removes the need for throws declarations and mandatory catches, but it also lets a caller ignore common submission failures accidentally. The checked design makes the service contract visible. The checked exception chain gives the controller an actionable message and preserves diagnostic cause information.

# Q3 - Resource Safety, Debugging and Robust Code

## Student A section - debugger and measurement draft

The test creates three temporary store files. North and South are valid; West contains a valid line followed by CORRUPT. The reader throws while reading that second West line. The debugger stopped in StoreReader.readLine; after stepping, local s was CORRUPT. The call stack was:

~~~text
[1] ResourceSafety$StoreReader.readLine (ResourceSafety.java:11)
[2] ResourceSafety.manual (ResourceSafety.java:20)
[3] ResourceSafety.main (ResourceSafety.java:47)
~~~

Stepping again reached the catch block at ResourceSafety.manual line 21. The local loop index was 2, the reader variable was non-null, and ResourceSafety.open was 1. The faulty version logs the IOException and adds the reader to a leak list; it never reaches its close call after the read loop. The recorded debugger notes are in Task03/outputs/debug-session.txt.

| Version | Open readers immediately after the failing store | Report |
|---|---:|---|
| Faulty manual close after successful read only | 1 | North and South only |
| Manual close in finally | 0 | North and South only |
| try-with-resources | 0 | North and South only |

All versions omit the incomplete West report section by buffering each store's lines until that store finishes. The output is therefore consistent while the faulty manual implementation still leaks a reader. Logging names West.txt as the failure source.

## Student B section - resource correctness draft

The input reader is the resource that must be closed. The manual finally version closes it whether reading succeeds or throws. The try-with-resources version closes it automatically at the end of the try scope. If multiple resources are declared in a single try-with-resources statement, Java closes them in reverse declaration order.

The demonstration also makes both use() and close() fail. The caught exception is read failed; close failed appears in getSuppressed(). The read failure remains primary while the close failure is attached as a suppressed exception.

try-with-resources is not enough if the program still commits a partial report, fails to validate corrupt data, ignores close/write errors, or cannot recover from disk exhaustion. The report should be staged per store and committed only after validation; this is why the implementation buffers one store before appending it.

For f store files and total input size B, reading and validating takes O(B) time. Correctly closing each reader before opening the next uses O(1) open file handles. A faulty version that skips close on every failed file can accumulate O(f) handles before the OS refuses another open. Not closing any file also grows handle use to O(f). The report string itself may use O(B) memory; streaming to a temporary output file would reduce working memory.

# Q4 - Thread Creation and Life Cycle

## Student A section - thread design draft

Two stations extend Thread and two implement Runnable. Extending Thread is direct for a simple worker but uses up Java's single inheritance slot. Runnable separates the work from the thread object and is easier to submit to an executor or compose with another superclass.

~~~text
NEW --start()--> RUNNABLE --scheduler selects--> RUNNING
RUNNING --sleep()--> TIMED_WAITING --timeout--> RUNNABLE
RUNNING --join() waiting for another thread--> WAITING
RUNNING --monitor unavailable--> BLOCKED
RUNNING --run returns or throws--> TERMINATED
~~~

Java groups RUNNING within the RUNNABLE state in Thread.State. The workers sleep briefly, which produces TIMED_WAITING. Calling run() directly executes on the current main thread; it does not request a new thread. The output confirms run() executes on main. Calling start() transitions each station into concurrent execution.

After starting workers, isAlive() observed true. After join() returned, isAlive() was false for every station, and the total was 100 scripts. join() blocks the caller until that worker terminates, so a final summary after all joins includes each station.

## Student B section - observed behavior draft

The run prints ten completion-order trials. Each station reports 25 scripts; all ten totals are 100. The orders differ because the OS/JVM scheduler, sleep timing and thread readiness vary. The MIN and MAX priority samples also produced different orders in this run, but priority is only a scheduling hint and does not promise a specific sequence. Correct totals depend on joining workers and collecting all results, never on a particular finish order.

| Trial | Station finish order |
|---:|---|
| 1 | S2, S1, S4, S3 |
| 2 | S1, S4, S3, S2 |
| 3 | S1, S2, S4, S3 |
| 4 | S4, S1, S2, S3 |
| 5 | S1, S3, S2, S4 |
| 6 | S4, S3, S2, S1 |
| 7 | S2, S1, S3, S4 |
| 8 | S3, S1, S2, S4 |
| 9 | S3, S4, S1, S2 |
| 10 | S1, S3, S4, S2 |

Observed priority samples:
- MIN: S2, S4, S1, S3
- MAX: S1, S3, S2, S4

The daemon worker signals that it started and then sleeps. Main does not join it. The observed process printed main ends without joining daemon and exited without the daemon completion line. A daemon thread does not keep the JVM alive after all non-daemon threads finish.

## Joint conclusion and alternative

For n scripts and p workers, ideal completion time is about O(n/p) batches when work is balanced, plus scheduling and startup overhead; total work remains O(n). Thread stacks use O(p*S) memory for stack size S, plus per-worker state. An ExecutorService with a fixed pool reuses p workers to run Runnable or Callable tasks and returns Future objects; it bounds worker count and simplifies shutdown/error collection. Creating one thread per script instead would use O(n*S) stack memory. The single run demonstrates scheduler variability, not a stable performance benchmark.

# Q5 - Race Conditions in Cinema Seat Booking

## Student A section - race and test draft

The unsafe variant uses a CyclicBarrier for ten users per seat. Every contender first reads the seat as free; the barrier releases the group only after all ten have passed that check. The unsynchronized check-then-mark sequence then records all ten attempted sales for the same seat. An AtomicInteger keeps the demonstration counter accurate, isolating the race in the boolean seat state.

| Run | Unsynchronized sales counted | Unique seats |
|---:|---:|---:|
| 1-10 | 100 on each run | 10 |

This means 90 duplicate sales across the 100 attempts, or nine duplicates per seat. The critical section is checking booked[seat], setting it true, and recording the successful booking. Without one atomic region, two threads can both observe false and then both set true.

The completed synchronized method and synchronized block each sold 10 unique seats in the required 100-user test.

## Student B section - synchronization measurements draft

Eight worker threads attempted 10,000 distinct-seat bookings. Five rounds were measured; times include worker-thread startup and use millisecond resolution.

| Round | Synchronized method (ms) | Synchronized block (ms) |
|---:|---:|---:|
| 1 | 4 | 2 |
| 2 | 1 | 1 |
| 3 | 1 | 1 |
| 4 | 1 | 0 |
| 5 | 1 | 0 |
| Mean | 1.6 | 0.8 |

The synchronized method locks the whole instance method on this. The block also locks this, but only around the critical section. In this short test, both scopes protect the same operations and the observed difference is within coarse timing noise; it is not evidence that one form is generally faster. Static synchronization would use the Class object's lock and is needed only if static shared state must be coordinated across all screen instances.

For 20 screens, use a per-screen lock (or an AtomicBoolean per seat) so independent screens can progress concurrently. A global static lock would serialize unrelated screens. Under contention, synchronized acquisition can queue; AtomicBoolean.compareAndSet offers a direct per-seat atomic claim, ReentrantLock provides timed/interruptible acquisition, and ConcurrentHashMap.putIfAbsent atomically inserts a seat key. These approaches use O(s) state for s seats; operations are expected O(1) per attempt, though CAS retries/lock waits increase latency under contention.

# Q6 - Deadlock and Inter-Thread Communication

## Student A section - producer-consumer draft

The bounded token counter uses a five-element circular buffer. put waits while count equals capacity; take waits while count is zero. Both methods are synchronized, update the ring index and count under one monitor, and call notifyAll after changing the state. Across the run, the observed count range was 0..5 and the sum of tokens 1..20 was 210.

wait() must occur while holding the object's monitor; it releases that monitor while waiting and reacquires it before returning. The condition belongs in a while loop because a thread can wake spuriously or another waiter may consume the state change first. sleep() pauses a thread for a time but does not release a monitor it holds; wait() releases the monitor so another producer/consumer can make progress.

The deliberately broken if variant was run five times. Each time, two consumers collected the same token value 42, totaling 84 from one produced token. In the producer case, two blocked producers woke after one item was removed and raised count to 2 in a capacity-1 buffer. The while condition prevents both failures by checking the state again after wake-up.

## Student B section - deadlock draft

The unsafe transfers lock the source account, then the destination. Two daemon threads transfer in opposite directions and synchronize after acquiring their first account lock. The observed result was Reverse-order transfer deadlocked: true. The four Coffman conditions are present:

- Mutual exclusion: an account monitor is held by one thread at a time.
- Hold and wait: each thread keeps one account lock while waiting for the other.
- No preemption: another thread cannot forcibly remove an intrinsic lock.
- Circular wait: thread one holds A and waits for B while thread two holds B and waits for A.

The safe version orders the two account locks by account id before acquiring either. That removes circular wait: every transfer acquires locks in the same global order. Opposing concurrent transfers completed and conserved the combined balance.

## Joint conclusion and alternative

Both problems require coordination around shared state. Deadlock prevention imposes a consistent lock order so transfers can finish. The producer-consumer monitor releases its lock during wait and notifies after a state change so the other role can proceed.

An ArrayBlockingQueue<Integer>(5) replaces the buffer, indices, monitor and wait/notify code with blocking put and take operations. Each is O(1) per item and the bounded queue uses O(5) space. A ReentrantLock.tryLock(timeout) alternative can abandon or retry an account transfer instead of waiting indefinitely, but both locks must be acquired or released safely before retrying. Lock ordering is simpler for guaranteed completion; timed locking is useful when the caller can report a timeout.

# Q7 - Concurrent Fault-Tolerant Result Processor

## Stage 1 - joint requirements

The program generates 10,000 score-file paths. Each task independently reads and validates one file, so file processing is parallel. Shared state is limited to the concurrent result queue and the final counts. Missing files and malformed/out-of-range scores are recorded as failures; one such result must not prevent other tasks from running.

Measurable requirements:
- Exactly 10,000 futures are submitted and collected.
- Success count plus failure count equals 10,000.
- The hand-built five-file set yields 2 successes and 3 failures.
- Missing and corrupt records are included.
- Timings are recorded for fixed pools of 1, 2, 4 and 8 workers.

## Stage 2 - Student A design draft

Each Callable reads and validates one path and returns an Outcome. ExecutorCompletionService lets the controller collect completed Future objects without waiting in file order. The task catches IOException and RuntimeException and returns a failure outcome; successful and failed outcomes are added to a ConcurrentLinkedQueue. The controller waits on each Future.get(), then counts every recorded outcome. This queue was chosen because multiple workers can safely append without a shared external lock.

~~~text
Controller
  -> fixed ExecutorService
      -> Callable<Path, Outcome> x 10,000
          -> read / parse / validate one score file
          -> ConcurrentLinkedQueue<Outcome>
  -> ExecutorCompletionService<Outcome>
  -> Future.get() for every submitted task
  -> count successes and failures
~~~

Pseudocode:

~~~text
create fixed pool(p), completion service, concurrent results queue
for each path:
    submit callable:
        try read, parse, range-check
            add success outcome
        catch I/O or validation exception
            add failure outcome
        return outcome
repeat n times:
    completionService.take().get()
count queue outcomes
shutdown pool
assert successes + failures == n
~~~

## Stage 3 - Student B implementation and measurements draft

The program creates 10,000 files in a temporary directory. Indexes divisible by 97 are omitted (missing files). Remaining indexes divisible by 89 contain CORRUPT; other files hold a valid id and a score from 0 through 100. The 5-file hand check returned two successes and three failures.

For the generated set, 104 paths are missing. Of the 113 indexes divisible by 89, two overlap the missing set, leaving 111 corrupt files. Thus the measured expected totals are 9,785 successful and 215 failed records.

| Workers | Elapsed time (ms) | Success | Failure | Total |
|---:|---:|---:|---:|---:|
| 1 | 119,845 | 9,785 | 215 | 10,000 |
| 2 | 291 | 9,785 | 215 | 10,000 |
| 4 | 159 | 9,785 | 215 | 10,000 |
| 8 | 106 | 9,785 | 215 | 10,000 |

The one-worker result is an extreme outlier relative to the other pools. This single measurement should not be treated as a stable ratio. The measured summary counts were consistent for every pool. Repeat the benchmark on the target machine before making a capacity decision. No unhandled failure appeared in this run.

## Stage 4 - joint evaluation and alternatives

For this I/O-heavy local experiment, eight workers had the lowest observed time, but the one-worker outlier and lack of repeated trials make that recommendation provisional. More threads can stop helping when storage bandwidth is saturated, work is CPU-bound, or scheduling and context-switch costs exceed useful parallel work. A balanced pool near the number of available cores is a reasonable starting point, then benchmark with production-sized inputs.

The summary is complete because one Future is submitted per path, every Future is taken and get() is called, each expected file/validation exception becomes an outcome, and the final assertion checks success + failure = input count. The result queue is thread-safe. A remaining limitation is that fatal Errors such as OutOfMemoryError are not converted into outcomes; the process may still fail. Temporary files are deleted after the run.

For n files of average size b, total useful work is O(n*b), with ideal parallel time about O(n*b/p) plus task and disk overhead. The generated path list and concurrent outcome queue use O(n) memory; the bounded worker pool uses O(p*S) thread-stack space. A parallel stream is shorter to write and uses the common pool, but uncaught exceptions can fail the terminal operation unless each file catches and records its own errors. Creating one thread per file could use O(n*S) thread stack memory and severe scheduling overhead. The fixed pool bounds concurrency and makes Future-based completion and error inspection explicit.

# Contribution record - complete with the partner

The following roles are taken from the assignment sheet. Enter the actual Student A and Student B names and record who performed each part. This report's draft sections are not evidence of a partner's contribution.

| Question | Student A primary responsibility | Student B primary responsibility | Joint |
|---|---|---|---|
| Q1 | Exception hierarchy, classification, catch order, finally | Tests, trace, broader-catch behavior | Design decision and alternatives |
| Q2 | Hierarchy and propagation diagram | Scenarios, chaining trace, lost-cause test | Checked vs unchecked conclusion |
| Q3 | Debugging and experiment | Resource ownership and correctness | Robustness decision |
| Q4 | Thread creation and lifecycle | Ten-run observations, priorities, daemon | Scheduler and executor alternative |
| Q5 | Reproduce race and explain critical section | Timing and synchronization comparison | Deployment choice |
| Q6 | Producer-consumer counter | Deadlock analysis and ordering | Progress and queue alternative |
| Q7 | Design | Implementation and measurements | Defense and limitations |

**Student A name/signature:** ______________________________  
**Student B name/signature:** ______________________________  
**Submission date:** __________________  

