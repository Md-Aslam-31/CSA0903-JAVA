# Q4 - Pharmacy inventory maps

Compile `PharmacyMaps.java` and run it. It benchmarks put, 100,000 prebuilt-key lookups and full iteration for 1,000, 10,000 and 50,000 codes. Put/get/iteration times are 5-run averages; key generation is outside the timed region. See `outputs/measurements.csv` and `outputs/run-output.txt`.

The ordering rules come from the collection contracts: TreeMap is code-sorted, LinkedHashMap retains insertion order, and HashMap promises neither. The measured first keys illustrate the difference for these inputs.
