import java.sql.*;
import java.util.*;

/** Portable, entity-specific example of Dao<Student,Long>. */
interface Dao<T, K> {
    K insert(T entity) throws SQLException;
    Optional<T> findById(K id) throws SQLException;
    List<T> findAll() throws SQLException;
    boolean update(T entity) throws SQLException;
    boolean delete(K id) throws SQLException;
}
record Student(long id, String registerNo, String name, String email) { }
final class StudentDao implements Dao<Student, Long> {
    private final String url,user,password;
    StudentDao(String url,String user,String password){this.url=url;this.user=user;this.password=password;}
    private Connection open() throws SQLException{return DriverManager.getConnection(url,user,password);}
    private Student map(ResultSet r)throws SQLException{return new Student(r.getLong("id"),r.getString("register_no"),r.getString("name"),r.getString("email"));}
    public Long insert(Student s)throws SQLException{
        try(Connection c=open();PreparedStatement p=c.prepareStatement("INSERT INTO student(register_no,name,email) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)){
            p.setString(1,s.registerNo());p.setString(2,s.name());p.setString(3,s.email());p.executeUpdate();
            try(ResultSet k=p.getGeneratedKeys()){if(!k.next())throw new SQLException("No generated key");return k.getLong(1);}
        }
    }
    public Optional<Student> findById(Long id)throws SQLException{
        try(Connection c=open();PreparedStatement p=c.prepareStatement("SELECT id,register_no,name,email FROM student WHERE id=?")){
            p.setLong(1,id);try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(map(r)):Optional.empty();}
        }
    }
    public List<Student> findAll()throws SQLException{
        List<Student> out=new ArrayList<>();try(Connection c=open();PreparedStatement p=c.prepareStatement("SELECT id,register_no,name,email FROM student ORDER BY id");ResultSet r=p.executeQuery()){while(r.next())out.add(map(r));}return out;
    }
    public boolean update(Student s)throws SQLException{
        try(Connection c=open();PreparedStatement p=c.prepareStatement("UPDATE student SET register_no=?,name=?,email=? WHERE id=?")){p.setString(1,s.registerNo());p.setString(2,s.name());p.setString(3,s.email());p.setLong(4,s.id());return p.executeUpdate()==1;}
    }
    public boolean delete(Long id)throws SQLException{try(Connection c=open();PreparedStatement p=c.prepareStatement("DELETE FROM student WHERE id=?")){p.setLong(1,id);return p.executeUpdate()==1;}}
}
