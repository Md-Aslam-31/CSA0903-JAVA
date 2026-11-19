package parking.app;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class AdaptationDemo {
    public static void main(String[] args) {
        ParkingFacility facility = new ParkingFacility(new int[]{2, 1, 2, 1, 3});
        LocalDate day = LocalDate.of(2026, 11, 20);
        System.out.println("Campus Smart Parking — adapted (5 unequal floors)");
        Ticket eBike = facility.enter(new EBike("TN10EB1010"), Pass.NONE,
                LocalDateTime.of(day, LocalTime.of(8, 0)));
        Ticket van = facility.enter(new DeliveryVan("TN20DV2020"), new OneDayPass(day),
                LocalDateTime.of(day, LocalTime.of(10, 0)));
        System.out.println("E-bike " + eBike.slot() + " flat fee Rs "
                + facility.exit("TN10EB1010", LocalDateTime.of(day, LocalTime.of(9, 0))));
        System.out.println("Delivery van " + van.slot() + " one-day pass fee Rs "
                + facility.exit("TN20DV2020", LocalDateTime.of(day, LocalTime.of(11, 0))));
        System.out.println("Delivery van outside window Rs "
                + new DeliveryVan("TN30DV3030").fee(60,
                        LocalDateTime.of(day, LocalTime.of(18, 0))));
        System.out.println("Delivery van inside 09:00–17:00 Rs "
                + new DeliveryVan("TN40DV4040").fee(60,
                        LocalDateTime.of(day, LocalTime.of(10, 0))));
        System.out.println("Electric-car charging for 10 kWh Rs "
                + new ElectricCar("TN50EV5050").chargingFee(10));
        System.out.println("Monthly pass valid for November: "
                + new MonthlyPass(YearMonth.from(day)).covers(day));
        System.out.println("Unequal floor capacities: 2, 1, 2, 1, 3; slots total=9");
    }
}

interface PricingRule { double fee(Vehicle vehicle, int minutes, LocalDateTime entry); }
interface Chargeable { double chargingFee(int kWh); }
interface ParkingPass { boolean covers(LocalDate date); }
enum Pass implements ParkingPass { NONE { public boolean covers(LocalDate d) { return false; } } }
record MonthlyPass(YearMonth validMonth) implements ParkingPass {
    public MonthlyPass { if (validMonth == null) throw new IllegalArgumentException("Pass month required"); }
    @Override public boolean covers(LocalDate date) { return validMonth.equals(YearMonth.from(date)); }
}
record OneDayPass(LocalDate validOn) implements ParkingPass {
    public OneDayPass { if (validOn == null) throw new IllegalArgumentException("Pass date required"); }
    @Override public boolean covers(LocalDate date) { return validOn.equals(date); }
}

abstract class Vehicle {
    private final String regNo;
    private final PricingRule rule;
    protected Vehicle(String regNo, PricingRule rule) {
        if (regNo == null || regNo.isBlank()) throw new IllegalArgumentException("Registration required");
        this.regNo = regNo; this.rule = rule;
    }
    final String regNo() { return regNo; }
    final double fee(int minutes, LocalDateTime entry) {
        if (minutes < 0) throw new IllegalArgumentException("Duration cannot be negative");
        return rule.fee(this, minutes, entry);
    }
}
final class HourlyRule implements PricingRule {
    private final double rate;
    HourlyRule(double rate) { this.rate = rate; }
    public double fee(Vehicle v, int minutes, LocalDateTime entry) {
        return (minutes / 60 + (minutes % 60 == 0 ? 0 : 1)) * rate;
    }
}
final class FlatRule implements PricingRule {
    private final double amount;
    FlatRule(double amount) { this.amount = amount; }
    public double fee(Vehicle v, int minutes, LocalDateTime entry) { return amount; }
}
final class DeliveryRule implements PricingRule {
    private final double carRate;
    DeliveryRule(double carRate) { this.carRate = carRate; }
    public double fee(Vehicle v, int minutes, LocalDateTime entry) {
        int hours = minutes / 60 + (minutes % 60 == 0 ? 0 : 1);
        LocalTime t = entry.toLocalTime();
        boolean daytime = !t.isBefore(LocalTime.of(9, 0)) && t.isBefore(LocalTime.of(17, 0));
        return hours * carRate * (daytime ? 2.0 : 1.0);
    }
}
final class EBike extends Vehicle {
    EBike(String regNo) { super(regNo, new FlatRule(25.0)); }
}
final class DeliveryVan extends Vehicle {
    DeliveryVan(String regNo) { super(regNo, new DeliveryRule(30.0)); }
}
final class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) { super(regNo, new HourlyRule(10.0)); }
}
final class ElectricCar extends Vehicle implements Chargeable {
    ElectricCar(String regNo) { super(regNo, new HourlyRule(45.0)); }
    @Override public double chargingFee(int kWh) {
        if (kWh < 0) throw new IllegalArgumentException("kWh cannot be negative");
        return 12.0 * kWh;
    }
}
final class Car extends Vehicle {
    Car(String regNo) { super(regNo, new HourlyRule(30.0)); }
}

record Slot(int floor, int number) {
    @Override public String toString() { return "(" + floor + "," + number + ")"; }
}
record Ticket(long number, Vehicle vehicle, Slot slot, LocalDateTime entry, ParkingPass pass) {
    String regNo() { return vehicle.regNo(); }
    private static final AtomicLong NEXT = new AtomicLong(2000);
    static Ticket issue(Vehicle v, Slot s, LocalDateTime t, ParkingPass p) {
        return new Ticket(NEXT.getAndIncrement(), v, s, t, p);
    }
}
final class ParkingFacility {
    private final String[][] occupancy;
    private final Map<String, Ticket> active = new HashMap<>();
    ParkingFacility(int[] capacity) {
        occupancy = new String[capacity.length][];
        for (int f = 0; f < capacity.length; f++) {
            if (capacity[f] < 0) throw new IllegalArgumentException("Negative capacity");
            occupancy[f] = new String[capacity[f]];
        }
    }
    Ticket enter(Vehicle v, ParkingPass pass, LocalDateTime time) {
        if (active.containsKey(v.regNo())) throw new IllegalArgumentException("Duplicate registration");
        for (int f = 0; f < occupancy.length; f++) for (int s = 0; s < occupancy[f].length; s++) {
            if (occupancy[f][s] == null) {
                Slot slot = new Slot(f, s); occupancy[f][s] = v.regNo();
                Ticket ticket = Ticket.issue(v, slot, time, pass); active.put(v.regNo(), ticket); return ticket;
            }
        }
        throw new IllegalStateException("No free slot");
    }
    double exit(String regNo, LocalDateTime time) {
        Ticket ticket = active.remove(regNo);
        if (ticket == null) throw new IllegalArgumentException("No active ticket");
        occupancy[ticket.slot().floor()][ticket.slot().number()] = null;
        int minutes = Math.toIntExact(Math.max(0, Duration.between(ticket.entry(), time).toMinutes()));
        return ticket.pass().covers(ticket.entry().toLocalDate()) ? 0.0 : ticket.vehicle().fee(minutes, ticket.entry());
    }
}
