import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Generates exact-size attendance logs and compares single-byte and block reads. */
public final class FileBenchmarks {
    private static final int BLOCK=64*1024;
    static long countUnbuffered(Path p)throws IOException{
        long lines=0,bytes=0;int last=-1;
        try(InputStream in=new FileInputStream(p.toFile())){int b;while((b=in.read())!=-1){bytes++;last=b;if(b=='\n')lines++;}}
        return lines+(bytes>0&&last!='\n'?1:0);
    }
    static long countBuffered(Path p)throws IOException{
        long lines=0,bytes=0;int last=-1;byte[] b=new byte[BLOCK];
        try(InputStream in=Files.newInputStream(p)){int n;while((n=in.read(b))!=-1){bytes+=n;for(int i=0;i<n;i++){int v=b[i]&255;last=v;if(v=='\n')lines++;}}}
        return lines+(bytes>0&&last!='\n'?1:0);
    }
    static void generate(Path p,long size)throws IOException{
        byte[] block=new byte[BLOCK];Arrays.fill(block,(byte)'x');for(int i=79;i<block.length;i+=80)block[i]='\n';
        try(OutputStream out=new BufferedOutputStream(Files.newOutputStream(p))){long left=size;while(left>0){int n=(int)Math.min(block.length,left);out.write(block,0,n);left-=n;}}
    }
    public static void main(String[] args)throws Exception{
        Path dir=Path.of(args.length==0?"Assignment5/TaskB1/inputs":args[0]);Files.createDirectories(dir);
        System.out.println("bytes,unbuffered_lines,unbuffered_ms,buffered_lines,buffered_ms");
        for(long mb:new long[]{1,10,50}){Path p=dir.resolve("attendance-"+mb+"MiB.log");generate(p,mb*1024*1024);
            long t=System.nanoTime(),u=countUnbuffered(p);long um=(System.nanoTime()-t)/1_000_000;
            t=System.nanoTime();long v=countBuffered(p);long bm=(System.nanoTime()-t)/1_000_000;
            if(u!=v)throw new AssertionError("count mismatch");System.out.printf(Locale.ROOT,"%d,%d,%d,%d,%d%n",Files.size(p),u,um,v,bm);
        }
    }
}
