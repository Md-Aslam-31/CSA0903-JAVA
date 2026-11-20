import java.sql.*;
import java.util.*;

/** JDBC timing, metadata and transaction investigation using the local H2 test DB. */
public final class JdbcBenchmarks {
    static final String URL="jdbc:h2:mem:jdbcbench;MODE=MySQL;DB_CLOSE_DELAY=-1";
    static void create(Connection c)throws SQLException{try(Statement s=c.createStatement()){s.execute("DROP TABLE IF EXISTS bus_pass");s.execute("CREATE TABLE bus_pass(pass_id VARCHAR(30) PRIMARY KEY,reg_no VARCHAR(30) NOT NULL,name VARCHAR(100) NOT NULL,route_no INT NOT NULL,amount DECIMAL(10,2) NOT NULL)");}}
    static List<String[]> rows(int n){List<String[]> x=new ArrayList<>(n);for(int i=0;i<n;i++)x.add(new String[]{"P"+i,"R"+i,"Student"+i,String.valueOf(i%50),"4500.00"});return x;}
    static long statement(Connection c,List<String[]> rows)throws SQLException{
        c.setAutoCommit(false);long t=System.nanoTime();try(Statement s=c.createStatement()){
            for(String[] p:rows)s.executeUpdate("INSERT INTO bus_pass VALUES('"+p[0]+"','"+p[1]+"','"+p[2]+"',"+p[3]+","+p[4]+")");c.commit();return System.nanoTime()-t;
        }catch(SQLException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
    }
    static long prepared(Connection c,List<String[]> rows)throws SQLException{
        c.setAutoCommit(false);long t=System.nanoTime();try(PreparedStatement p=c.prepareStatement("INSERT INTO bus_pass VALUES(?,?,?,?,?)")){
            for(String[] r:rows){bind(p,r);p.executeUpdate();}c.commit();return System.nanoTime()-t;
        }catch(SQLException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
    }
    static void bind(PreparedStatement p,String[] r)throws SQLException{p.setString(1,r[0]);p.setString(2,r[1]);p.setString(3,r[2]);p.setInt(4,Integer.parseInt(r[3]));p.setDouble(5,Double.parseDouble(r[4]));}
    static long batch(Connection c,List<String[]> rows)throws SQLException{return timedBatch(c,rows);}
    static long timedBatch(Connection c,List<String[]> rows)throws SQLException{long t=System.nanoTime();PassLoader.loadAll(c,rows);return System.nanoTime()-t;}
    static long batchAutoCommit(Connection c,List<String[]> rows)throws SQLException{
        c.setAutoCommit(true);long t=System.nanoTime();
        try(PreparedStatement p=c.prepareStatement("INSERT INTO bus_pass VALUES(?,?,?,?,?)")){
            int n=0;for(String[] r:rows){bind(p,r);p.addBatch();n++;if(n%1000==0)p.executeBatch();}p.executeBatch();return System.nanoTime()-t;
        }finally{c.setAutoCommit(true);}
    }
    static int count(Connection c)throws SQLException{try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM bus_pass")){r.next();return r.getInt(1);}}
    static void metadata(Connection c)throws SQLException{
        DatabaseMetaData m=c.getMetaData();System.out.println("DB product="+m.getDatabaseProductName()+" "+m.getDatabaseProductVersion());
        System.out.println("JDBC driver="+m.getDriverName()+" "+m.getDriverVersion());
        try(ResultSet r=m.getTables(null,null,"%",new String[]{"TABLE"})){System.out.print("Tables=");while(r.next())System.out.print(r.getString("TABLE_NAME")+" ");System.out.println();}
        try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM bus_pass WHERE 1=0")){ResultSetMetaData x=r.getMetaData();for(int i=1;i<=x.getColumnCount();i++)System.out.println(x.getColumnName(i)+" -> "+x.getColumnTypeName(i));}
    }
    static void failureDemo(Connection c,boolean autoCommit)throws SQLException{
        create(c);c.setAutoCommit(autoCommit);String sql="INSERT INTO bus_pass VALUES(?,?,?,?,?)";int staged=0;
        try(PreparedStatement p=c.prepareStatement(sql)){
            for(int i=0;i<10000;i++){
                String[] row={"F"+i,"R"+i,"FailureTest"+i,"1",i==5000?"bad-amount":"100.00"};
                bind(p,row);p.addBatch();staged++;if(staged%1000==0)p.executeBatch();
            }
            p.executeBatch();if(!autoCommit)c.commit();
        }catch(RuntimeException|SQLException e){if(!autoCommit)c.rollback();System.out.println("FORCED FAILURE autoCommit="+autoCommit+" reason="+e.getClass().getSimpleName()+" rowsRemaining="+count(c));}
        finally{c.setAutoCommit(true);}
    }
    public static void main(String[] args)throws Exception{
        Class.forName("org.h2.Driver");try(Connection c=DriverManager.getConnection(URL,"sa","")){
            create(c);metadata(c);System.out.println("size,Statement_tx_ms,Prepared_tx_ms,Batch_tx_ms,Batch_autocommit_ms,rows");
            for(int n:new int[]{1000,10000,100000}){
                List<String[]> data=rows(n);create(c);long a=statement(c,data);int ca=count(c);
                create(c);long b=prepared(c,data);int cb=count(c);
                create(c);long d=batch(c,data);int cd=count(c);
                create(c);long e=batchAutoCommit(c,data);int ce=count(c);
                if(ca!=n||cb!=n||cd!=n||ce!=n)throw new AssertionError("row count mismatch");
                System.out.printf(Locale.ROOT,"%d,%.3f,%.3f,%.3f,%.3f,%d/%d/%d/%d%n",n,a/1e6,b/1e6,d/1e6,e/1e6,ca,cb,cd,ce);
            }
            List<String[]> ten=rows(10);create(c);statement(c,ten);System.out.println("10 rows Statement="+count(c));metadata(c);
            create(c);prepared(c,ten);System.out.println("10 rows PreparedStatement="+count(c));
            create(c);batch(c,ten);System.out.println("10 rows Batch="+count(c));metadata(c);
            failureDemo(c,true);failureDemo(c,false);
        }
    }
}
