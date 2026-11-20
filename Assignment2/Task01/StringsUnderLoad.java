import java.util.Locale;

public class StringsUnderLoad {
    static volatile long sink;
    static final int ROUNDS = 5;
    static String field(int i) { return "FIELD-" + i + "-PAX"; }
    interface Builder { String build(int fields); }
    static String plus(int n) { String s=""; for(int i=0;i<n;i++) s += field(i)+"|"; return s; }
    static String stringBuilder(int n) { StringBuilder b=new StringBuilder(n*14); for(int i=0;i<n;i++) b.append(field(i)).append('|'); return b.toString(); }
    static String stringBuffer(int n) { StringBuffer b=new StringBuffer(n*14); for(int i=0;i<n;i++) b.append(field(i)).append('|'); return b.toString(); }
    static long execute(Builder b,int tickets,int fields) { long sum=0; for(int t=0;t<tickets;t++) sum+=b.build(fields).length(); sink=sum; return sum; }
    static double measure(Builder b,int tickets,int fields) { long t=System.nanoTime(); execute(b,tickets,fields); return (System.nanoTime()-t)/1_000_000.0; }
    public static void main(String[] args) {
        int[][] workloads={{1,10},{10000,50},{2000,100}};
        Builder[] builders={StringsUnderLoad::plus,StringsUnderLoad::stringBuilder,StringsUnderLoad::stringBuffer};
        String[] names={"StringPlus","StringBuilder","StringBuffer"};
        System.out.println("workload,tickets,fields,method,avg_ms,rounds,estimated_intermediate_result_strings,checksum");
        for(int w=0;w<workloads.length;w++) { int tickets=workloads[w][0], fields=workloads[w][1];
            for(int m=0;m<builders.length;m++) { measure(builders[m],Math.min(tickets,100),fields); double total=0; long checksum=0;
                for(int r=0;r<ROUNDS;r++) { total+=measure(builders[m],tickets,fields); checksum=sink; }
                long estimated=names[m].equals("StringPlus")?(long)tickets*(fields-1):tickets;
                System.out.printf(Locale.ROOT,"W%d,%d,%d,%s,%.3f,%d,%d,%d%n",w+1,tickets,fields,names[m],total/ROUNDS,ROUNDS,estimated,checksum);
            }
        }
        System.out.println("object_estimate_note=StringPlus estimates at least one new result String per concatenation; exact allocation depends on compiler/JIT; builders create one final String per ticket plus builder storage.");
        System.out.println("decision=StringBuilder per ticket (not shared); StringBuffer only where one mutable buffer is actually shared across threads, with a higher synchronization cost.");
    }
}
