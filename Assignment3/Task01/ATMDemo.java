import java.io.*;
import java.nio.file.*;
import java.util.*;
public class ATMDemo {
 static final int BALANCE=500; static Path cardReader;
 static void withdraw(String input) throws IOException,InsufficientFundsException {
  try(Scanner s=new Scanner(input)){
   int account=s.nextInt(), amount=s.nextInt();
   if(account<=0)throw new IllegalArgumentException("Account number must be positive");
   if(amount<0)throw new IllegalArgumentException("Withdrawal cannot be negative");
   if(amount>BALANCE)throw new InsufficientFundsException("Requested amount exceeds balance");
   String state=Files.readString(cardReader); if(state.isBlank())throw new IOException("Empty card data");
   System.out.println("Dispensed "+amount+" for account "+account);
  } catch(InputMismatchException|SecurityException e){throw e;}
  finally{System.out.println("ATM cleanup: transaction closed");}
 }
 static void runCase(String label,String input,Path reader){
  cardReader=reader; System.out.println("-- "+label+" --");
  try{withdraw(input);}
  catch(InputMismatchException|SecurityException e){System.out.println("Input/access error: "+e.getClass().getSimpleName());}
  catch(InsufficientFundsException e){System.out.println("Business rule: "+e.getMessage());}
  catch(IllegalArgumentException e){System.out.println("Invalid value: "+e.getMessage());}
  catch(IOException e){System.out.println("Card reader error: "+e.getClass().getSimpleName());}
 }
 public static void main(String[] args)throws Exception{
  Path card=Files.createTempFile("atm-card",".txt"); Files.writeString(card,"card-ready");
  runCase("valid","1001 100",card); runCase("non-numeric input","1001 abc",card);
  runCase("negative amount","1001 -20",card); runCase("over balance","1001 600",card);
  runCase("missing card-reader file","1001 20",card.resolveSibling("missing-card-reader.dat"));
  Files.deleteIfExists(card);
  Thread trace=new Thread(()->{throw new IllegalStateException("unexpected ATM fault");},"ATM-trace");
  trace.setUncaughtExceptionHandler((t,e)->{System.out.println("Uncaught exception trace:");e.printStackTrace(System.out);});
  trace.start();trace.join();
 }
}
class InsufficientFundsException extends Exception{InsufficientFundsException(String m){super(m);}}
