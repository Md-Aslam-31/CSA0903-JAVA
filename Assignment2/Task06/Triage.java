import java.util.*;

public class Triage {
    static final Comparator<Patient> ORDER=Comparator.comparingInt((Patient p)->p.severity).thenComparingLong(p->p.arrival);
    private long counter=0;private final PriorityQueue<Patient>queue=new PriorityQueue<>(ORDER);
    void admit(String name,int severity){if(severity<1||severity>5)throw new IllegalArgumentException("severity 1..5");queue.add(new Patient(name,severity,counter++));}
    Patient next(){return queue.poll();}
    static final class Patient {final String name;final int severity;final long arrival;Patient(String n,int s,long a){name=n;severity=s;arrival=a;}public String toString(){return name+"(S"+severity+")";}}
    static volatile long sink;
    static double run(int n){long start=System.nanoTime();PriorityQueue<Patient>q=new PriorityQueue<>(ORDER);for(int i=0;i<n;i++)q.add(new Patient("P"+i,1+(i%5),i));long sum=0;while(!q.isEmpty())sum+=q.poll().arrival;sink=sum;return(System.nanoTime()-start)/1_000_000.0;}
    public static void main(String[]args){Triage er=new Triage();er.admit("Ravi",3);er.admit("Meena",1);er.admit("John",3);er.admit("Asha",5);er.admit("Kumar",1);er.admit("Lata",2);er.admit("Dev",3);er.admit("Zoya",2);StringBuilder order=new StringBuilder();List<Patient> treatmentOrder=new ArrayList<>(er.queue);treatmentOrder.sort(ORDER);System.out.println("Sorted waiting list (treatment order): "+treatmentOrder);Patient p;while((p=er.next())!=null)order.append(p).append(' ');System.out.println("Treatment order: "+order.toString().trim());
        System.out.println("PriorityQueue iterator order is not guaranteed to be treatment order; copy and sort for display.");
        for(int n:new int[]{1000,10000,50000}){double total=0;for(int r=0;r<5;r++)total+=run(n);System.out.printf(Locale.ROOT,"growth,n=%d,add_and_poll_avg_ms=%.3f,rounds=5%n",n,total/5);}
        System.out.println("Fairness: aging raises effective priority after a wait threshold; store original severity and arrival time, recompute at dispatch or use scheduled promotion. Aging trades strict severity order for bounded waits.");
    }
}
