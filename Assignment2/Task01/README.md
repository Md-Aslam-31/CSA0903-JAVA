# Q1 - Building text under load

Compile `StringsUnderLoad.java` and run `StringsUnderLoad`. It compares repeated String `+`, a thread-confined StringBuilder and a StringBuffer for 3 ticket/field workloads. Each reported average uses 5 timed rounds after a warmup. Results and input definitions are in `outputs/measurements.csv`; console output is in `outputs/run-output.txt`.

Observed larger-workload results varied by workload: StringBuilder led for 10,000 x 50 fields, while StringBuffer happened to lead for 2,000 x 100. Treat the online timing as a small single-session microbenchmark, not a universal ranking.
