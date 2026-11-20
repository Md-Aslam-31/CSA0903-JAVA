import java.io.IOException;
class ExamException extends Exception{ExamException(String m){super(m);}ExamException(String m,Throwable c){super(m,c);}}
class ExamClosedException extends RuntimeException{ExamClosedException(String m){super(m);}}
class StudentNotRegisteredException extends ExamException{StudentNotRegisteredException(String m){super(m);}}
class SubmissionStorageException extends ExamException{SubmissionStorageException(String m,Throwable c){super(m,c);}}
class Storage{
 void save(String file,long size)throws SubmissionStorageException{
  try{if(size>500)throw new IOException("Disk quota exceeded");System.out.println("Saved "+file);}
  catch(IOException e){throw new SubmissionStorageException("Could not store "+file,e);}
 }
}
class SubmissionService{
 private final Storage storage=new Storage();
 void submit(String f,long n,boolean registered)throws ExamException{
  if(!registered)throw new StudentNotRegisteredException("Student is not registered");
  storage.save(f,n);
 }
}
public class StorageDemo{
 static void controller(String f,long n,boolean registered,boolean open){
  try{if(!open)throw new ExamClosedException("Exam window has closed");
   new SubmissionService().submit(f,n,registered);System.out.println("Submission accepted");
  }catch(ExamException e){System.out.println("Error: "+e.getMessage());
   if(e.getCause()!=null){System.out.println("Cause: "+e.getCause().getMessage());System.out.println("Cause class: "+e.getCause().getClass().getSimpleName());if(f.equals("answer02.pdf"))e.printStackTrace(System.out);}
  }catch(ExamClosedException e){System.out.println("Error: "+e.getMessage());}
 }
 public static void main(String[] args){
  controller("answer01.pdf",120,true,true);controller("answer02.pdf",800,true,true);
  controller("answer03.pdf",50,false,true);controller("answer04.pdf",50,true,false);
  SubmissionStorageException lost=new SubmissionStorageException("Could not store file",null);System.out.println("Cause when omitted: "+lost.getCause());
 }
}
