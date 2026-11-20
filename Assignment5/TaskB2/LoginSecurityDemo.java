import java.sql.*;

/** Adversarial login demonstration in an isolated in-memory H2 test database. */
public final class LoginSecurityDemo {
    private final Connection con;
    LoginSecurityDemo(Connection con)throws SQLException{this.con=con;try(Statement s=con.createStatement()){
        s.execute("CREATE TABLE users(id VARCHAR(50) PRIMARY KEY,pwd VARCHAR(80) NOT NULL)");
        s.execute("INSERT INTO users VALUES('admin','correct-hash')");
    }}
    boolean unsafeLogin(String id,String pwd)throws SQLException{
        String sql="SELECT * FROM users WHERE id= '"+id+"' AND pwd= '"+pwd+"'";
        try(Statement s=con.createStatement();ResultSet r=s.executeQuery(sql)){return r.next();}
    }
    boolean safeLogin(String id,String pwd)throws SQLException{
        try(PreparedStatement p=con.prepareStatement("SELECT * FROM users WHERE id=? AND pwd=?")){
            p.setString(1,id);p.setString(2,pwd);try(ResultSet r=p.executeQuery()){return r.next();}
        }
    }
    public static void main(String[] args)throws Exception{
        Class.forName("org.h2.Driver");
        try(Connection c=DriverManager.getConnection("jdbc:h2:mem:injection;MODE=MySQL;DB_CLOSE_DELAY=-1","sa","")){
            LoginSecurityDemo d=new LoginSecurityDemo(c);String id="admin' OR '1'='1' -- ";String password="wrong";
            System.out.println("NORMAL invalid password: unsafe="+d.unsafeLogin("admin",password)+", prepared="+d.safeLogin("admin",password));
            System.out.println("INJECTION id="+id+" : unsafe="+d.unsafeLogin(id,password)+", prepared="+d.safeLogin(id,password));
        }
    }
}
