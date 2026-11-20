import java.nio.file.*;
import java.io.*;
import java.util.*;

public final class PassGrowthBenchmark {
    static String[] find(Path file,String id)throws IOException{
        try(BufferedReader r=Files.newBufferedReader(file)){String line;while((line=r.readLine())!=null){String[] p=line.split(",",-1);if(p.length==5&&p[0].equals(id))return p;}}
        return null;
    }
    public static void main(String[] args)throws Exception{
        Path dir=Path.of("Assignment5/TaskB1/inputs");Files.createDirectories(dir);System.out.println("records,find_last_ms");
        for(int count:new int[]{1_000,10_000,100_000}){Path f=dir.resolve("passes-"+count+".csv");
            try(BufferedWriter w=Files.newBufferedWriter(f)){for(int i=0;i<count;i++)w.write("P"+i+",R"+i+",Student"+i+",12,4500.0\n");}
            String key="P"+(count-1);for(int i=0;i<2;i++)find(f,key);
            long t=System.nanoTime();for(int i=0;i<5;i++)if(find(f,key)==null)throw new AssertionError();
            System.out.printf(Locale.ROOT,"%d,%.3f%n",count,(System.nanoTime()-t)/5_000_000.0);
        }
    }
}
