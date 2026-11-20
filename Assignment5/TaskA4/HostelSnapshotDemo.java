import java.io.*;
import java.util.*;

public final class HostelSnapshotDemo {
    static final class Room implements Serializable {
        private static final long serialVersionUID=1L;
        final String id; final List<Resident> residents;
        Room(String id,List<Resident> residents){this.id=id;this.residents=residents;}
    }
    static final class Hostel implements Serializable {
        private static final long serialVersionUID=1L;
        final String name; final List<Room> rooms;
        Hostel(String name,List<Room> rooms){this.name=name;this.rooms=rooms;}
    }
    public static void main(String[] args)throws Exception{
        Hostel original=new Hostel("North Hostel",List.of(new Room("N-01",List.of(new Resident("R001","Asha","9000000000","never-write-this",null)))));
        byte[] bytes;try(ByteArrayOutputStream b=new ByteArrayOutputStream();ObjectOutputStream o=new ObjectOutputStream(b)){o.writeObject(original);bytes=b.toByteArray();}
        Hostel copy;try(ObjectInputStream i=new ObjectInputStream(new ByteArrayInputStream(bytes))){copy=(Hostel)i.readObject();}
        Resident r=copy.rooms.get(0).residents.get(0);System.out.println("Loaded="+copy.name+", resident="+r.name+", phone="+r.phoneNumber);
        System.out.println("Transient credential="+r.password+", connection="+r.liveConnection);
    }
}
