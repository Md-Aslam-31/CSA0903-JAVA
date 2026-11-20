# Q2 - Playlist collection choice

Compile `PlaylistCollections.java` and run it. It compares end addition, front add/remove, middle insert/remove, k-th access and iteration for ArrayList, LinkedList and ArrayDeque at 1,000, 5,000 and 20,000 songs. Each result is a 5-round average; setup is outside the timed block. End addition uses 10,000 operations per round; other operation counts scale with playlist size. Raw values are in `outputs/measurements.csv`.

Use ArrayList for random access, iteration and ordinary append-heavy playlists. Prefer ArrayDeque for a real double-ended Up Next queue. LinkedList only fits when the caller already holds node iterators/references or mostly manipulates ends; index-based middle access still requires traversal.
