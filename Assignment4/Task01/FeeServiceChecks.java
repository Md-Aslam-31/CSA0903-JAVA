import java.math.BigDecimal;

/** Small executable service-level checks; no visible frame or external database required. */
public final class FeeServiceChecks {
    public static void main(String[] args) throws Exception {
        FeeService service = new FeeService(new DemoFeeGateway());
        require(service.pay("STU100", new BigDecimal("250.00")).contains("Payment accepted"), "valid payment");
        reject(service, "", "10.00", "registration number");
        reject(service, "STU100", "0", "positive");
        reject(service, "STU100", "1.234", "decimals");
        System.out.println("Valid: STU100 / INR 250.00 -> accepted");
        System.out.println("Invalid ID, zero amount, and >2 decimal places -> rejected by FeeService");
        System.out.println("PASS: 4 service checks");
    }
    private static void reject(FeeService service, String id, String amount, String expected) throws Exception {
        try { service.pay(id, new BigDecimal(amount)); throw new AssertionError("Expected rejection: " + expected); }
        catch (IllegalArgumentException ok) { require(ok.getMessage().contains(expected), ok.getMessage()); }
    }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
}
