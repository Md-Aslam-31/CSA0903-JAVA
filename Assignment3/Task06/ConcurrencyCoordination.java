import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
public class ConcurrencyCoordination{
 static class Account{final int id;int cents;Account(int id,int c){this.id=id;cents=c;}}
 static void unsafeTransfer(Account from,Account to,CountDownLatch both)throws InterruptedException{
  synchronized(from){both.countDown();both.await();synchronized(to){from.cents-=1;to.cents+=1;}}
 }
 static void safeTransfer(Account a,Account b){
  Account first=a.id<b.id?a:b,second=a.id<b.id?b:a;
  synchronized(first){synchronized(second){a.cents-=1;b.cents+=1;}}
 }
 static class TokenCounter{
  final int[] buf=new int[5];int count,in,out,min=0,max=0;
  synchronized void put(int token)throws InterruptedException{
   while(count==buf.length)wait();buf[in]=token;in=(in+1)%buf.length;count++;max=Math.max(max,count);notifyAll();
  }
  synchronized int take()throws InterruptedException{
   while(count==0)wait();int v=buf[out];out=(out+1)%buf.length;count--;min=Math.min(min,count);notifyAll();return v;
  }
 }
 static class IfVariant{
  final int[] b=new int[1];int count,in,out;final CountDownLatch waiting=new CountDownLatch(2);
  IfVariant(boolean full){if(full){b[0]=1;count=1;}}
  synchronized void putIf(int v)throws InterruptedException{if(count==b.length){waiting.countDown();wait();}b[in]=v;in=(in+1)%b.length;count++;notifyAll();}
  synchronized int takeIf()throws InterruptedException{if(count==0){waiting.countDown();wait();}int v=b[out];out=(out+1)%b.length;count--;notifyAll();return v;}
  void awaitWaiters()throws InterruptedException{waiting.await();}
  synchronized int size(){return count;}
 }
 public static void main(String[] args)throws Exception{
  Account a=new Account(1,100),b=new Account(2,100);CountDownLatch both=new CountDownLatch(2);
  Thread t1=new Thread(()->{try{unsafeTransfer(a,b,both);}catch(InterruptedException e){Thread.currentThread().interrupt();}});
  Thread t2=new Thread(()->{try{unsafeTransfer(b,a,both);}catch(InterruptedException e){Thread.currentThread().interrupt();}});
  t1.setDaemon(true);t2.setDaemon(true);t1.start();t2.start();t1.join(100);t2.join(100);
  System.out.println("Reverse-order transfer deadlocked: "+(t1.isAlive()&&t2.isAlive()));
  Account x=new Account(1,100),y=new Account(2,100);
  Thread safe1=new Thread(()->{for(int i=0;i<100;i++)safeTransfer(x,y);});
  Thread safe2=new Thread(()->{for(int i=0;i<100;i++)safeTransfer(y,x);});safe1.start();safe2.start();safe1.join();safe2.join();
  System.out.println("Ordered-lock transfers completed: "+(!safe1.isAlive()&&!safe2.isAlive())+"; total cents="+(x.cents+y.cents));
  TokenCounter c=new TokenCounter();AtomicInteger sum=new AtomicInteger();Thread cook=new Thread(()->{try{for(int i=1;i<=20;i++)c.put(i);}catch(InterruptedException e){Thread.currentThread().interrupt();}});
  Thread[] students=new Thread[2];for(int i=0;i<2;i++)students[i]=new Thread(()->{try{for(int k=0;k<10;k++)sum.addAndGet(c.take());}catch(InterruptedException e){Thread.currentThread().interrupt();}});
  cook.start();for(Thread t:students)t.start();cook.join();for(Thread t:students)t.join();
  System.out.println("Sum of tokens collected: "+sum.get()+" (expected 210)");
  System.out.println("Observed buffer count range: "+c.min+".."+c.max);
  for(int r=1;r<=5;r++){
   IfVariant cv=new IfVariant(false);AtomicInteger doubled=new AtomicInteger();
   Thread c1=new Thread(()->{try{doubled.addAndGet(cv.takeIf());}catch(InterruptedException e){Thread.currentThread().interrupt();}});
   Thread c2=new Thread(()->{try{doubled.addAndGet(cv.takeIf());}catch(InterruptedException e){Thread.currentThread().interrupt();}});
   c1.start();c2.start();cv.awaitWaiters();cv.putIf(42);c1.join();c2.join();
   System.out.println("if-take trial "+r+": two consumers collected "+doubled.get()+" from one token");
   IfVariant pv=new IfVariant(true);Thread p1=new Thread(()->{try{pv.putIf(2);}catch(InterruptedException e){Thread.currentThread().interrupt();}});
   Thread p2=new Thread(()->{try{pv.putIf(3);}catch(InterruptedException e){Thread.currentThread().interrupt();}});
   p1.start();p2.start();pv.awaitWaiters();pv.takeIf();p1.join();p2.join();
   System.out.println("if-put trial "+r+": count="+pv.size()+" in capacity 1");
  }
 }
}
