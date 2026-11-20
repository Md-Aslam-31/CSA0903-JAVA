import java.util.*;

public class RedesignComparison {
    static volatile long sink;
    static String[] fields(int n){String[]a=new String[n];for(int i=0;i<n;i++)a[i]="FIELD-"+i+"-PAX";return a;}
    static String original(String[]a){String s="";for(String x:a)s+=x+'|';return s;}
    static String revised(String[]a){StringBuilder b=new StringBuilder(a.length*14);for(String x:a)b.append(x).append('|');return b.toString();}
    interface F{String make(String[]a);}
    static double measure(F f,String[][]data){long st=System.nanoTime(),sum=0;for(String[]a:data)sum+=f.make(a).length();sink=sum;return(System.nanoTime()-st)/1_000_000.0;}
    public static void main(String[]args){int tickets=10000,fieldsPerTicket=50,rounds=7;String[][]data=new String[tickets][];for(int i=0;i<tickets;i++)data[i]=fields(fieldsPerTicket);
        for(int i=0;i<2;i++){measure(RedesignComparison::original,data);measure(RedesignComparison::revised,data);}
        double old=0,neo=0;for(int r=0;r<rounds;r++){old+=measure(RedesignComparison::original,data);neo+=measure(RedesignComparison::revised,data);}
        System.out.printf(Locale.ROOT,"condition=tickets:%d,fields:%d,rounds:%d%noriginal_string_plus_avg_ms=%.3f%nredesigned_builder_avg_ms=%.3f%nspeedup=%.2fx%nchecksum=%d%n",tickets,fieldsPerTicket,rounds,old/rounds,neo/rounds,old/neo,sink);
        System.out.println("diagnosis=Repeated immutable concatenation copies the growing prefix; estimated result Strings per ticket=fields-1.");
        System.out.println("objective=Build identical ticket text while reducing intermediate result Strings to one final String per ticket.");
        System.out.println("tradeoff=Builder requires a mutable temporary and capacity choice; for very short tickets the timing difference may be noise.");
    }
}
