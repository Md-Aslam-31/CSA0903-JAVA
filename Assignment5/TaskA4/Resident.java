import java.io.Serializable;
import java.sql.Connection;

public final class Resident implements Serializable {
    private static final long serialVersionUID=1L;
    final String registerNo,name,phoneNumber;
    final transient String password;
    final transient Connection liveConnection;
    public Resident(String registerNo,String name,String phoneNumber,String password,Connection liveConnection){
        this.registerNo=registerNo;this.name=name;this.phoneNumber=phoneNumber;this.password=password;this.liveConnection=liveConnection;
    }
}
