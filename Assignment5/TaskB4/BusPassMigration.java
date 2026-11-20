import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;

/** Streaming CSV-to-JDBC migration: bounded batch memory, reject log, one commit. */
public final class BusPassMigration {
    static int migrate(Connection c,Path source,Path rejects)throws Exception{
        boolean old=c.getAutoCommit();c.setAutoCommit(false);int accepted=0,failed=0,batch=0,lineNo=0;
        String sql="INSERT INTO bus_pass(pass_id,reg_no,name,route_no,amount) VALUES(?,?,?,?,?)";
        try(BufferedReader in=Files.newBufferedReader(source,StandardCharsets.UTF_8);
            BufferedWriter bad=Files.newBufferedWriter(rejects,StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);
            PreparedStatement p=c.prepareStatement(sql)){
            String line;
            while((line=in.readLine())!=null){lineNo++;String[] f=line.split(",",-1);
                try{
                    if(f.length!=5)throw new IllegalArgumentException("expected 5 fields");
                    if(f[0].isBlank()||f[1].isBlank()||f[2].isBlank())throw new IllegalArgumentException("required field empty");
                    int route=Integer.parseInt(f[3]);double amount=Double.parseDouble(f[4]);if(route<0||amount<0)throw new IllegalArgumentException("negative route/amount");
                    p.setString(1,f[0]);p.setString(2,f[1]);p.setString(3,f[2]);p.setInt(4,route);p.setDouble(5,amount);p.addBatch();batch++;accepted++;
                    if(batch==1000){p.executeBatch();batch=0;}
                }catch(IllegalArgumentException ex){bad.write("line "+lineNo+": "+ex.getMessage()+" | "+line);bad.newLine();failed++;}
            }
            if(batch>0)p.executeBatch();c.commit();
        }catch(Exception ex){c.rollback();throw ex;}
        finally{c.setAutoCommit(old);}
        System.out.println("MIGRATION accepted="+accepted+", rejected="+failed+", committed=true");return accepted;
    }
    static void createTable(Connection c)throws SQLException{try(Statement s=c.createStatement()){
        s.execute("CREATE TABLE bus_pass(pass_id VARCHAR(30) PRIMARY KEY,reg_no VARCHAR(30) NOT NULL,name VARCHAR(100) NOT NULL,route_no INT NOT NULL,amount DECIMAL(10,2) NOT NULL)");
    }}
    static void inspect(Connection c)throws SQLException{
        DatabaseMetaData m=c.getMetaData();System.out.println("DATABASE="+m.getDatabaseProductName()+" "+m.getDatabaseProductVersion());
        System.out.println("DRIVER="+m.getDriverName()+" "+m.getDriverVersion());
        try(ResultSet r=m.getTables(null,null,"%",new String[]{"TABLE"})){System.out.print("TABLES=");while(r.next())System.out.print(r.getString("TABLE_NAME")+" ");System.out.println();}
    }
    public static void main(String[] args)throws Exception{
        Class.forName("org.h2.Driver");Path input=Path.of("Assignment5/TaskB4/inputs/migration-sample.csv");Path rejects=Path.of("Assignment5/TaskB4/outputs/rejected-records.txt");
        Files.createDirectories(input.getParent());Files.createDirectories(rejects.getParent());
        if(!Files.exists(input))Files.writeString(input,"P1,R1,Asha,12,4500.00\nP2,R2,Noor,7,3800.00\nBROKEN,TOO,SHORT\nP3,R3,Kiran,no-route,4100.00\nP4,R4,Divya,9,4000.00\n");
        try(Connection c=DriverManager.getConnection("jdbc:h2:mem:migration;MODE=MySQL;DB_CLOSE_DELAY=-1","sa","")){
            createTable(c);migrate(c,input,rejects);inspect(c);
            try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM bus_pass")){r.next();System.out.println("ROWS="+r.getInt(1));}
        }
    }
}
