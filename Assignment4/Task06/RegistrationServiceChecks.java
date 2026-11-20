import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Reproducible JDBC checks used for the three required registration cases. */
public final class RegistrationServiceChecks {
    public static void main(String[] args) throws Exception {
        Class.forName("org.h2.Driver");
        Database db = new Database("jdbc:h2:mem:eventchecks;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""); db.initialize(true);
        RegistrationService service = new RegistrationService(db);
        service.register("STU200", "Mira Shah", "Hackathon");
        System.out.println("NORMAL: STU200 / Mira Shah / Hackathon -> saved");
        try { RegistrationService.validate("", "Mira Shah", "Hackathon"); throw new AssertionError("invalid ID accepted"); }
        catch (IllegalArgumentException expected) { System.out.println("INVALID: blank student ID -> " + expected.getMessage()); }
        try { service.register("STU100", "Asha Rao", "Cultural Night"); throw new AssertionError("duplicate accepted"); }
        catch (SQLException expected) { System.out.println("DUPLICATE: STU100 / Cultural Night -> SQLState " + expected.getSQLState()); }
        int rows = service.page("Hackathon", 25, 0).size();
        if (rows != 1) throw new AssertionError("filter returned " + rows + " rows");
        System.out.println("FILTER: Hackathon returned " + rows + " row");
        System.out.println("PASS: normal, invalid, duplicate, filter");
    }
}
