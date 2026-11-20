import java.nio.file.*;
import java.util.*;

public final class PassStoreChecks {
    public static void main(String[] args)throws Exception{
        Path f=Files.createTempFile("pass-checks",".csv");PassStore s=new PassStore(f);
        s.add("P1","R1","Asha",4,10.0);Files.writeString(f,"bad,row\nP2,R2,Noor,x,15\n\nP3,R3,Kiran,7,20.0\n",StandardOpenOption.APPEND);
        if(!Arrays.equals(s.find("P1"),new String[]{"P1","R1","Asha","4","10.0"}))throw new AssertionError("normal add/find");
        if(s.find("P999")!=null)throw new AssertionError("missing ID");
        if(!Arrays.equals(s.find("P3"),new String[]{"P3","R3","Kiran","7","20.0"}))throw new AssertionError("damaged row recovery");
        System.out.println("PASS: add/find, not-found, malformed shape, invalid number, empty line, continue-after-damage");Files.delete(f);
    }
}
