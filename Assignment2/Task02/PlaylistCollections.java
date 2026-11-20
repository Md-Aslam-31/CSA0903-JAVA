import java.util.*;
public class PlaylistCollections {
 static volatile long sink; static final int ROUNDS=5;
 static ArrayList<Integer> array(int n){ArrayList<Integer>x=new ArrayList<>(n+1000);for(int i=0;i<n;i++)x.add(i);return x;}
 static LinkedList<Integer> linked(int n){LinkedList<Integer>x=new LinkedList<>();for(int i=0;i<n;i++)x.add(i);return x;}
 static ArrayDeque<Integer> deque(int n){ArrayDeque<Integer>x=new ArrayDeque<>(n+1000);for(int i=0;i<n;i++)x.addLast(i);return x;}
 static double ms(Runnable r){long t=System.nanoTime();r.run();return(System.nanoTime()-t)/1e6;}
 static void run(int n){int ops=Math.max(20,1000000/n);double[]sum=new double[14];
  for(int round=0;round<ROUNDS;round++){
   ArrayList<Integer>a0=new ArrayList<>();LinkedList<Integer>l0=new LinkedList<>();ArrayDeque<Integer>d0=new ArrayDeque<>();
   sum[0]+=ms(()->{for(int i=0;i<10000;i++)a0.add(i);sink=a0.size();});sum[1]+=ms(()->{for(int i=0;i<10000;i++)l0.add(i);sink=l0.size();});sum[2]+=ms(()->{for(int i=0;i<10000;i++)d0.addLast(i);sink=d0.size();});
   ArrayList<Integer>af=array(n);LinkedList<Integer>lf=linked(n);ArrayDeque<Integer>df=deque(n);
   sum[3]+=ms(()->{for(int i=0;i<ops;i++){af.add(0,-i);af.remove(0);}sink=af.size();});sum[4]+=ms(()->{for(int i=0;i<ops;i++){lf.add(0,-i);lf.remove(0);}sink=lf.size();});sum[5]+=ms(()->{for(int i=0;i<ops;i++){df.addFirst(-i);df.removeFirst();}sink=df.size();});
   ArrayList<Integer>am=array(n);LinkedList<Integer>lm=linked(n);
   sum[6]+=ms(()->{for(int i=0;i<ops;i++){int k=am.size()/2;am.add(k,-i);am.remove(k);}sink=am.size();});sum[7]+=ms(()->{for(int i=0;i<ops;i++){int k=lm.size()/2;lm.add(k,-i);lm.remove(k);}sink=lm.size();});
   ArrayList<Integer>ak=array(n);LinkedList<Integer>lk=linked(n);ArrayDeque<Integer>dk=deque(n);
   sum[8]+=ms(()->{long s=0;for(int i=0;i<ops;i++)s+=ak.get(n/2);sink=s;});sum[9]+=ms(()->{long s=0;for(int i=0;i<ops;i++)s+=lk.get(n/2);sink=s;});sum[10]+=ms(()->{long z=0;for(int j=0;j<ops;j++){int k=0;for(int v:dk){if(k++==n/2){z+=v;break;}}}sink=z;});
   ArrayList<Integer>ai=array(n);LinkedList<Integer>li=linked(n);ArrayDeque<Integer>di=deque(n);
   sum[11]+=ms(()->{long s=0;for(int v:ai)s+=v;sink=s;});sum[12]+=ms(()->{long s=0;for(int v:li)s+=v;sink=s;});sum[13]+=ms(()->{long s=0;for(int v:di)s+=v;sink=s;});
  }
  for(int i=0;i<sum.length;i++)sum[i]/=ROUNDS;
  System.out.printf(Locale.ROOT,"N=%d rounds=%d ops=%d end10k_ms_A/L/D=%.3f/%.3f/%.3f front_ms_A/L/D=%.3f/%.3f/%.3f middle_ms_A/L=%.3f/%.3f getK_ms_A/L/Diterator=%.3f/%.3f/%.3f iterate_ms_A/L/D=%.3f/%.3f/%.3f%n",n,ROUNDS,ops,sum[0],sum[1],sum[2],sum[3],sum[4],sum[5],sum[6],sum[7],sum[8],sum[9],sum[10],sum[11],sum[12],sum[13]);
 }
 public static void main(String[]args){System.out.println("Each value averages 5 rounds; list construction is outside the timer. end operations=10,000; others=scaled ops.");for(int n:new int[]{1000,5000,20000})run(n);}
}

