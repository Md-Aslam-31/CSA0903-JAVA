import java.util.*;
public class PharmacyMaps {
 static volatile long sink; static final int ROUNDS=5;
 static String code(int i){return "M"+String.format(Locale.ROOT,"%06d",(i*7919)%1000000);}
 static double put(Map<String,Integer> m,String[]k){long t=System.nanoTime();for(int i=0;i<k.length;i++)m.put(k[i],i%500);sink=m.size();return(System.nanoTime()-t)/1e6;}
 static double get(Map<String,Integer>m,String[]q){long t=System.nanoTime(),s=0;for(String k:q)s+=m.get(k);sink=s;return(System.nanoTime()-t)/1e6;}
 static double iter(Map<String,Integer>m){long t=System.nanoTime(),s=0;for(int v:m.values())s+=v;sink=s;return(System.nanoTime()-t)/1e6;}
 static double avgPut(int n,String[]k,int type){double sum=0;for(int r=0;r<ROUNDS;r++){Map<String,Integer>m=type==0?new HashMap<>(n*2):type==1?new TreeMap<>():new LinkedHashMap<>();sum+=put(m,k);}return sum/ROUNDS;}
 static void run(int n){String[]k=new String[n];for(int i=0;i<n;i++)k[i]=code(i);String[]q=new String[100000];for(int i=0;i<q.length;i++)q[i]=k[i%n];Map<String,Integer>h=new HashMap<>(n*2),t=new TreeMap<>(),l=new LinkedHashMap<>();for(int i=0;i<n;i++){h.put(k[i],i%500);t.put(k[i],i%500);l.put(k[i],i%500);}double hp=avgPut(n,k,0),tp=avgPut(n,k,1),lp=avgPut(n,k,2),hg=0,tg=0,lg=0,hi=0,ti=0,li=0;for(int r=0;r<ROUNDS;r++){hg+=get(h,q);tg+=get(t,q);lg+=get(l,q);hi+=iter(h);ti+=iter(t);li+=iter(l);}System.out.printf(Locale.ROOT,"N=%d,put5avg_ms_H/T/L=%.3f/%.3f/%.3f,get100k5avg_ms_H/T/L=%.3f/%.3f/%.3f,iterate5avg_ms_H/T/L=%.3f/%.3f/%.3f%n",n,hp,tp,lp,hg/5,tg/5,lg/5,hi/5,ti/5,li/5);System.out.println("first_key_H/T/L="+h.keySet().iterator().next()+"/"+t.keySet().iterator().next()+"/"+l.keySet().iterator().next());}
 public static void main(String[]args){for(int n:new int[]{1000,10000,50000})run(n);}
}
