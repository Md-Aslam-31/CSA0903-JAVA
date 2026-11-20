import java.io.*;
import java.nio.file.*;
import java.util.*;
public class ResourceSafety{
 static int open=0;static final List<StoreReader> leaked=new ArrayList<>();
 static class StoreReader extends BufferedReader{
  StoreReader(Path path)throws IOException{super(Files.newBufferedReader(path));open++;}
  public String readLine()throws IOException{
   String s=super.readLine();
   if("CORRUPT".equals(s)){
    throw new IOException("corrupt record");
   }
   return s;
  }
  public void close()throws IOException{try{super.close();}finally{open--;}}
 }
 static String manual(Path[] paths,String[] names){
  StringBuilder out=new StringBuilder();open=0;
  for(int i=0;i<names.length;i++){StoreReader r=null;StringBuilder store=new StringBuilder();try{r=new StoreReader(paths[i]);String line;
   while((line=r.readLine())!=null)store.append(line).append('\n');r.close();out.append(store);
  }catch(IOException e){System.out.println("manual log: "+names[i]+" -> "+e.getMessage());if(r!=null)leaked.add(r);}}
  return out.toString();
 }
 static String safe(Path[] paths,String[] names){
  StringBuilder out=new StringBuilder();open=0;
  for(int i=0;i<names.length;i++){StringBuilder store=new StringBuilder();try(StoreReader r=new StoreReader(paths[i])){
   String line;while((line=r.readLine())!=null)store.append(line).append('\n');out.append(store);
  }catch(IOException e){System.out.println("twr log: "+names[i]+" -> "+e.getMessage());}}
  return out.toString();
 }
 static String manualFinally(Path[] paths,String[] names){
  StringBuilder out=new StringBuilder();open=0;
  for(int i=0;i<names.length;i++){StoreReader r=null;StringBuilder store=new StringBuilder();try{r=new StoreReader(paths[i]);String line;
   while((line=r.readLine())!=null)store.append(line).append('\n');out.append(store);
  }catch(IOException e){System.out.println("finally log: "+names[i]+" -> "+e.getMessage());}
  finally{if(r!=null)try{r.close();}catch(IOException e){System.out.println("close log: "+e.getMessage());}}}
  return out.toString();
 }
 static class FailingResource implements AutoCloseable{
  void use()throws IOException{throw new IOException("read failed");}
  public void close()throws IOException{throw new IOException("close failed");}
 }
 public static void main(String[] args)throws IOException{
  String[] n={"North.txt","South.txt","West.txt"};
  String[] d={"North sales=5","South sales=7","West sales=3\nCORRUPT"};Path dir=Files.createTempDirectory("daily-sales");Path[] p=new Path[n.length];
  for(int i=0;i<n.length;i++){p[i]=dir.resolve(n[i]);Files.writeString(p[i],d[i]);}
  String a=manual(p,n);System.out.println("Manual report: "+a.replace('\n','|'));System.out.println("Manual open after failure: "+open);
  for(StoreReader r:leaked)r.close();leaked.clear();
  String b=safe(p,n);System.out.println("TWR report: "+b.replace('\n','|'));System.out.println("TWR open after failure: "+open);
  String c=manualFinally(p,n);System.out.println("Manual finally report matches TWR: "+c.equals(b));System.out.println("Manual finally open after failure: "+open);
  try(FailingResource r=new FailingResource()){r.use();}catch(IOException e){
   System.out.println("Primary: "+e.getMessage());System.out.println("Suppressed: "+e.getSuppressed()[0].getMessage());}
  for(StoreReader r:leaked)r.close();for(Path path:p)Files.deleteIfExists(path);Files.deleteIfExists(dir);
 }
}
