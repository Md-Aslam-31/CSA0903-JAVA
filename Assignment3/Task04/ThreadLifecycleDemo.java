import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.ThreadLocalRandom;
public class ThreadLifecycleDemo{
 static final List<String> finished=new CopyOnWriteArrayList<>();
 static final int[] scanned=new int[4];static String lastStatus;
 static class ExtStation extends Thread{final int id;ExtStation(int i){super("Ext-"+i);id=i;}public void run(){work(id);}}
 static class RunStation implements Runnable{final int id;RunStation(int i){id=i;}public void run(){work(id);}}
 static void work(int id){try{Thread.sleep(ThreadLocalRandom.current().nextInt(3,20));}catch(InterruptedException e){Thread.currentThread().interrupt();}scanned[id-1]=25;finished.add("S"+id+"(25)");}
 static String trial(int priority)throws InterruptedException{
  finished.clear();Arrays.fill(scanned,0);Thread[] ts={new ExtStation(1),new ExtStation(2),new Thread(new RunStation(3)),new Thread(new RunStation(4))};
  for(Thread t:ts)t.setPriority(priority);boolean aliveAtStart=false;for(Thread t:ts){t.start();aliveAtStart|=t.isAlive();}
  for(Thread t:ts)t.join();boolean aliveAfterJoin=false;int total=0;for(Thread t:ts)aliveAfterJoin|=t.isAlive();for(int n:scanned)total+=n;
  lastStatus="alive after start="+aliveAtStart+", alive after join="+aliveAfterJoin+", total="+total;return String.join(" < ",finished)+" | "+lastStatus;
 }
 public static void main(String[] args)throws Exception{
  System.out.println("Ten observed completion orders (NORMAL priority):");
  for(int i=1;i<=10;i++)System.out.println(i+": "+trial(Thread.NORM_PRIORITY));
  System.out.println("MIN priority sample: "+trial(Thread.MIN_PRIORITY));System.out.println("MAX priority sample: "+trial(Thread.MAX_PRIORITY));
  Thread main=Thread.currentThread();Thread direct=new Thread(()->System.out.println("run() executes on main: "+(Thread.currentThread()==main)));
  direct.run();System.out.println("run() returned; it did not start a worker");
  CountDownLatch started=new CountDownLatch(1);Thread daemon=new Thread(()->{started.countDown();try{Thread.sleep(1000);}catch(InterruptedException ignored){}System.out.println("daemon completed");});
  daemon.setDaemon(true);daemon.start();started.await();System.out.println("main ends without joining daemon");
 }
}
