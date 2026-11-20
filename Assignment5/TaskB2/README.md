# B2 - Adversarial inputs

The isolated H2 test schema contains one sample user. The input `admin' OR '1'='1' -- ` makes the concatenated condition true while the password is wrong; the prepared query treats it as a literal ID and rejects it. The example uses only a local in-memory database created for this assignment. Production passwords should be salted hashes rather than stored plaintext.

The damaged pass input contains a wrong-field-count row, a nonnumeric amount, an empty line, and a later valid record. `PassStore.find` reports each malformed line and continues to find the later good row. Validation/escaping is O(m) in input length; PreparedStatement also binds parameters in O(m), but keeps data separate from SQL grammar. Regex/quote escaping is weaker because it misses encoding, collation, comment, type and database-specific edge cases and is easy to apply inconsistently. Parameterization is the robust boundary.
