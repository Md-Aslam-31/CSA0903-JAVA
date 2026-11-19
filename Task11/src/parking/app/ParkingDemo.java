package parking.app;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ParkingDemo {
    public static void main(String[] args) {
        ParkingFacility facility = new ParkingFacility(new int[]{2, 1});
        System.out.println("Campus Smart Parking — baseline");
        ParkingTicket bike = facility.enter(new TwoWheeler("TN01AA1111"), Pass.NONE,
                LocalDateTime.parse("2026-11-20T09:00:00"));
        ParkingTicket car = facility.enter(new Car("TN02BB2222"), Pass.MONTHLY,
                LocalDateTime.parse("2026-11-20T10:00:00"));
        System.out.println("Bike ticket=" + bike.number() + " slot=" + bike.slot() + " fee="
                + facility.exit("TN01AA1111", LocalDateTime.parse("2026-11-20T10:30:00")));
        System.out.println("Monthly-pass car ticket=" + car.number() + " fee="
                + facility.exit("TN02BB2222", LocalDateTime.parse("2026-11-20T12:00:00")));
        System.out.println("Boundary 0 min / 60 min / 61 min (car): "
                + new Car("TN03CC3333").calculateFee(0) + " / "
                + new Car("TN03CC3333").calculateFee(60) + " / "
                + new Car("TN03CC3333").calculateFee(61));
        facility.enter(new Car("TN04DD4444"), Pass.NONE, LocalDateTime.parse("2026-11-20T11:00:00"));
        facility.enter(new Car("TN05EE5555"), Pass.NONE, LocalDateTime.parse("2026-11-20T11:01:00"));
        System.out.println("Capacity last slot: " + (facility.enter(new Car("TN06FF6666"), Pass.NONE,
                LocalDateTime.parse("2026-11-20T11:02:00")) != null));
        try {
            facility.enter(new Car("TN07GG7777"), Pass.NONE, LocalDateTime.now());
        } catch (IllegalStateException ex) {
            System.out.println("Full facility rejected: " + ex.getMessage());
        }
        System.out.println("Duplicate active registration rejected: " + facility.duplicateRejected("TN04DD4444"));
        System.out.println("Unknown vehicle type rejected: " + VehicleFactory.unknownRejected("hoverboard"));
        System.out.println("Invalid slot rejected: " + !facility.release(new Slot(5, 0)));
        System.out.println("Negative duration rejected: " + VehicleFactory.negativeDurationRejected());
        System.out.println("Negative capacity rejected: " + VehicleFactory.negativeCapacityRejected());
        System.out.println("Hand-check collection for bike + monthly car: Rs 20 + Rs 0 = Rs 20");
    }
}

abstract class Vehicle {
    private final String regNo;
    protected Vehicle(String regNo) {
        if (regNo == null || regNo.isBlank()) throw new IllegalArgumentException("Registration required");
        this.regNo = regNo;
    }
    public final String regNo() { return regNo; }
    protected abstract double ratePerHour();
    public double calculateFee(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("Duration cannot be negative");
        int hours = minutes / 60 + (minutes % 60 == 0 ? 0 : 1);
        return hours * ratePerHour();
    }
    @Override public boolean equals(Object other) {
        return other instanceof Vehicle v && getClass() == v.getClass() && regNo.equals(v.regNo);
    }
    @Override public int hashCode() { return 31 * getClass().hashCode() + regNo.hashCode(); }
}
final class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) { super(regNo); }
    @Override protected double ratePerHour() { return 10.0; }
}
class Car extends Vehicle {
    Car(String regNo) { super(regNo); }
    @Override protected double ratePerHour() { return 30.0; }
}
final class ElectricCar extends Car {
    ElectricCar(String regNo) { super(regNo); }
    @Override protected double ratePerHour() { return 45.0; }
}

enum Pass { NONE, MONTHLY }

final class VehicleFactory {
    private VehicleFactory() { }
    static Vehicle create(String type, String regNo) {
        return switch (type.toLowerCase()) {
            case "two-wheeler" -> new TwoWheeler(regNo);
            case "car" -> new Car(regNo);
            case "electric-car" -> new ElectricCar(regNo);
            default -> throw new IllegalArgumentException("Unknown vehicle type: " + type);
        };
    }
    static boolean unknownRejected(String type) {
        try { create(type, "TEST000"); return false; }
        catch (IllegalArgumentException ex) { return true; }
    }
    static boolean negativeDurationRejected() {
        try { new Car("TEST001").calculateFee(-1); return false; }
        catch (IllegalArgumentException ex) { return true; }
    }
    static boolean negativeCapacityRejected() {
        try { new ParkingFacility(new int[]{-1}); return false; }
        catch (IllegalArgumentException ex) { return true; }
    }
}

record Slot(int floor, int number) {
    @Override public String toString() { return "(" + floor + "," + number + ")"; }
}

record ParkingTicket(long number, Vehicle vehicle, Slot slot, LocalDateTime entry, Pass pass) {
    String regNo() { return vehicle.regNo(); }
    private static final AtomicLong NEXT = new AtomicLong(1000);
    static ParkingTicket issue(Vehicle vehicle, Slot slot, LocalDateTime entry, Pass pass) {
        return new ParkingTicket(NEXT.getAndIncrement(), vehicle, slot, entry, pass);
    }
}

final class ParkingFacility {
    private final String[][] occupancy;
    private final Map<String, ParkingTicket> active = new HashMap<>();
    private final Map<String, Pass> passes = new HashMap<>();
    ParkingFacility(int[] slotsPerFloor) {
        occupancy = new String[slotsPerFloor.length][];
        for (int f = 0; f < slotsPerFloor.length; f++) {
            if (slotsPerFloor[f] < 0) throw new IllegalArgumentException("Negative capacity");
            occupancy[f] = new String[slotsPerFloor[f]];
        }
    }
    ParkingTicket enter(Vehicle vehicle, Pass pass, LocalDateTime time) {
        if (active.containsKey(vehicle.regNo())) throw new IllegalArgumentException("Duplicate registration");
        for (int f = 0; f < occupancy.length; f++) for (int s = 0; s < occupancy[f].length; s++) {
            if (occupancy[f][s] == null) {
                Slot slot = new Slot(f, s); occupancy[f][s] = vehicle.regNo();
                ParkingTicket t = ParkingTicket.issue(vehicle, slot, time, pass);
                active.put(vehicle.regNo(), t); passes.put(vehicle.regNo(), pass); return t;
            }
        }
        throw new IllegalStateException("No free slot");
    }
    double exit(String regNo, LocalDateTime time) {
        ParkingTicket t = active.remove(regNo);
        if (t == null) throw new IllegalArgumentException("No active ticket");
        occupancy[t.slot().floor()][t.slot().number()] = null;
        passes.remove(regNo);
        long minutes = Math.max(0, Duration.between(t.entry(), time).toMinutes());
        return t.pass() == Pass.MONTHLY ? 0.0 : t.vehicle().calculateFee((int) minutes);
    }
    boolean release(Slot slot) {
        if (slot == null || slot.floor() < 0 || slot.floor() >= occupancy.length) return false;
        if (slot.number() < 0 || slot.number() >= occupancy[slot.floor()].length) return false;
        String regNo = occupancy[slot.floor()][slot.number()];
        if (regNo == null || !active.containsKey(regNo)) return false;
        active.remove(regNo); passes.remove(regNo); occupancy[slot.floor()][slot.number()] = null;
        return true;
    }
    boolean duplicateRejected(String regNo) {
        try { enter(new Car(regNo), Pass.NONE, LocalDateTime.now()); return false; }
        catch (IllegalArgumentException ex) { return true; }
    }
}
