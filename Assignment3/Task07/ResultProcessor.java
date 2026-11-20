import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
public class ResultProcessor{
 static class Result{final int id,score;Result(int i,int s){id=i;score=s;}}
 static class Outcome{final boolean ok;final String message;Outcome(boolean b,String m){ok=b;message=m;}}
 static Outcome process(Path p){
  try{String line=Files.readString(p).trim();String[] x=line.split(",");
   if(x.length!=2)throw new IllegalArgumentException("bad record");
   int score=Integer.parseInt(x[1]);if(score<0||score>100)throw new IllegalArgumentException("score out of range");
   Result r=new Result(Integer.parseInt(x[0]),score);return new Outcome(true,"OK "+r.id+":"+r.score);
  }catch(IOException|RuntimeException e){return new Outcome(false,p.getFileName()+": "+e.getClass().getSimpleName());}
 }
 static long[] run(List<Path> files,int threads)throws Exception{
  ExecutorService pool=Executors.newFixedThreadPool(threads);CompletionService<Outcome> cs=new ExecutorCompletionService<>(pool);
  ConcurrentLinkedQueue<Outcome> results=new ConcurrentLinkedQueue<>();
  for(Path p:files)cs.submit(()->{Outcome o=process(p);results.add(o);return o;});
  for(int i=0;i<files.size();i++)cs.take().get();
  int ok=0,fail=0;for(Outcome o:results){if(o.ok)ok++;else fail++;}
  pool.shutdown();pool.awaitTermination(1,TimeUnit.MINUTES);
  if(ok+fail!=files.size())throw new AssertionError("incomplete summary");
  return new long[]{ok,fail};
 }
 public static void main(String[] args)throws Exception{
  Path dir=Files.createTempDirectory("scores");List<Path> paths=new ArrayList<>();
  List<Path> hand=new ArrayList<>();
  hand.add(Files.writeString(dir.resolve("hand-ok-1.txt"),"1,90"));
  hand.add(Files.writeString(dir.resolve("hand-bad-1.txt"),"CORRUPT"));
  hand.add(dir.resolve("hand-missing.txt"));
  hand.add(Files.writeString(dir.resolve("hand-ok-2.txt"),"2,75"));
  hand.add(Files.writeString(dir.resolve("hand-bad-2.txt"),"3,101"));
  long[] small=run(hand,2);System.out.println("Hand-checked 5-file set: success="+small[0]+", failure="+small[1]);
  if(small[0]!=2||small[1]!=3)throw new AssertionError("small-set count mismatch");
  for(int i=0;i<10000;i++){Path p=dir.resolve(String.format("score-%05d.txt",i));paths.add(p);
   if(i%97==0)continue;
   if(i%89==0)Files.writeString(p,"CORRUPT");
   else Files.writeString(p,i+","+(i%101));
  }
  for(int n:new int[]{1,2,4,8}){long st=System.nanoTime();long[] r=run(paths,n);long ms=(System.nanoTime()-st)/1_000_000;
   System.out.println("pool="+n+", ms="+ms+", success="+r[0]+", failure="+r[1]+", sum="+(r[0]+r[1]));}
  for(Path p:paths)Files.deleteIfExists(p);for(Path p:hand)Files.deleteIfExists(p);Files.deleteIfExists(dir);
 }
}
