import java.nio.file.*;

public final class AdversarialPassStoreDemo {
    public static void main(String[] args)throws Exception{
        Path p=Path.of("Assignment5/TaskB2/inputs/adversarial-passes.csv");Files.createDirectories(p.getParent());
        Files.writeString(p,"P2001,R1,WrongFieldCount\nP2002,R2,Noor,8,not-a-number\n\nP2004,R4,Valid,12,4500.0\n");
        PassStore store=new PassStore(p);
        System.out.println("FIND P2004="+java.util.Arrays.toString(store.find("P2004")));
        System.out.println("FIND P2002="+java.util.Arrays.toString(store.find("P2002")));
    }
}
