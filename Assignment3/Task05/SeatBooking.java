import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
public class SeatBooking{
 final boolean[] booked;final AtomicInteger bookingsMade=new AtomicInteger();SeatBooking(int n){booked=new boolean[n];}
 synchronized boolean bookMethod(int s){if(booked[s])return false;booked[s]=true;bookingsMade.incrementAndGet();return true;}
 boolean bookBlock(int s){synchronized(this){if(booked[s])return false;booked[s]=true;bookingsMade.incrementAndGet();return true;}}
 boolean bookUnsafe(int s,CyclicBarrier[] gates)throws Exception{
  if(booked[s])return false;gates[s].await();booked[s]=true;bookingsMade.incrementAndGet();return true;
 }
 static int unsafeRun()throws Exception{
  SeatBooking b=new SeatBooking(10);CyclicBarrier[] g=new CyclicBarrier[10];
  for(int i=0;i<10;i++)g[i]=new CyclicBarrier(10);
  Thread[] t=new Thread[100];for(int i=0;i<100;i++){final int seat=i%10;
   t[i]=new Thread(()->{try{b.bookUnsafe(seat,g);}catch(Exception e){throw new RuntimeException(e);}});t[i].start();}
  for(Thread x:t)x.join();return b.bookingsMade.get();
 }
 static long timed(boolean method)throws Exception{
  int n=10000,w=8;SeatBooking b=new SeatBooking(n);Thread[] ts=new Thread[w];long st=System.nanoTime();
  for(int k=0;k<w;k++){final int lo=k*n/w,hi=(k+1)*n/w;ts[k]=new Thread(()->{
   for(int s=lo;s<hi;s++){if(method)b.bookMethod(s);else b.bookBlock(s);}
  });ts[k].start();}
  for(Thread t:ts)t.join();return (System.nanoTime()-st)/1_000_000;
 }
 public static void main(String[] args)throws Exception{
  for(int i=1;i<=10;i++)System.out.println("Unsynchronized run "+i+": seats sold="+unsafeRun());
  SeatBooking m=new SeatBooking(10);Thread[] users=new Thread[100];
  for(int i=0;i<100;i++){final int s=i%10;users[i]=new Thread(()->m.bookMethod(s));users[i].start();}
  for(Thread t:users)t.join();System.out.println("Synchronized method: seats sold="+m.bookingsMade.get());
  SeatBooking b=new SeatBooking(10);Thread[] blocks=new Thread[100];
  for(int i=0;i<100;i++){final int s=i%10;blocks[i]=new Thread(()->b.bookBlock(s));blocks[i].start();}
  for(Thread t:blocks)t.join();System.out.println("Synchronized block: seats sold="+b.bookingsMade.get());
  long method=0,blockTime=0;for(int r=1;r<=5;r++){long a=timed(true),z=timed(false);method+=a;blockTime+=z;System.out.println("10,000 booking timing round "+r+": method="+a+" ms, block="+z+" ms");}
  System.out.println("Five-round averages: method="+(method/5.0)+" ms, block="+(blockTime/5.0)+" ms");
 }
}
