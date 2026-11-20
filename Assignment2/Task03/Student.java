import java.util.*;

public class Student {
    private final String regNo;
    private final String name;
    Student(String regNo,String name){this.regNo=Objects.requireNonNull(regNo);this.name=name;}
    String regNo(){return regNo;}
    @Override public boolean equals(Object o){return o instanceof Student s&&regNo.equals(s.regNo);}
    // Correct contract: equal registration numbers produce equal hashes.
    @Override public int hashCode(){return Objects.hash(regNo);}
    @Override public String toString(){return regNo+" "+name;}
    public static void main(String[]args){
        Set<Student> correct=new HashSet<>();Student a=new Student("192311001","Arun");
        correct.add(a);correct.add(new Student("192311002","Divya"));correct.add(new Student("192311001","Arun again"));
        System.out.println("Corrected HashSet unique students: "+correct.size()+" (expected 2)");
        Set<Student> broken=new HashSet<>();broken.add(new Broken("192311001","Arun"));broken.add(new Broken("192311002","Divya"));broken.add(new Broken("192311001","Arun again"));
        System.out.println("Broken implementation duplicate scans (observed): "+broken.size()+" (prediction: usually 3; identity hashes differ, but accidental collisions can change this)");
        Student m=new Student("MUTABLE-ID","Mira");Set<Student> changed=new HashSet<>();changed.add(m);
        System.out.println("Lookup before changing identity: "+changed.contains(m));
        // Demonstrates why hash keys must be immutable; use the deliberately mutable class only for this probe.
        MutableStudent mutable=new MutableStudent("OLD","Nila");Set<MutableStudent> set=new HashSet<>();set.add(mutable);mutable.regNo="NEW";
        System.out.println("Mutable key lookup after hash field changes: "+set.contains(mutable)+"; size="+set.size());
        TreeSet<Student> tree=new TreeSet<>(Comparator.comparing(Student::regNo));tree.add(new Student("192311001","A"));tree.add(new Student("192311001","B"));
        System.out.println("TreeSet same regNo size: "+tree.size()+" (comparator returns 0, so one entry)");
    }
    static final class Broken {final String id,name;Broken(String i,String n){id=i;name=n;}public boolean equals(Object o){return o instanceof Broken b&&id.equals(b.id);}public String toString(){return id+" "+name;}}
    static final class MutableStudent {String regNo;final String name;MutableStudent(String r,String n){regNo=r;name=n;}public boolean equals(Object o){return o instanceof MutableStudent m&&regNo.equals(m.regNo);}public int hashCode(){return regNo.hashCode();}}
}
