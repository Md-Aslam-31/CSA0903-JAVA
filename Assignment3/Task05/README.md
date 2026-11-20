# Q5 - Seat booking

Compile and run from the repository root with JDK 17 or later:

~~~powershell
javac -encoding UTF-8 -d Assignment3\Task05\out Assignment3\Task05\SeatBooking.java
java -cp Assignment3\Task05\out SeatBooking
~~~

Ten barrier-controlled unsynchronized runs reproduce duplicate booking; both synchronized variants sell ten seats; five rounds measure 10,000 bookings.

Recorded output is in outputs/run-output.txt. The paired PNG is a labeled rendering of local compiler output, not a native browser screenshot. See ../REPORT.md for analysis and limitations.


Input fixtures are in the adjacent inputs/ folder. The Java programs generate the same deterministic cases internally so they run without external setup.
