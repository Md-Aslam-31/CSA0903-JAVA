# A5 - Transaction correctness and SQL injection

## Atomic wallet transfer

```text
ALGORITHM TRANSFER(sourceID, destinationID, amount)
BEGIN
    REQUIRE amount > 0 and sourceID != destinationID
    SET connection autoCommit = FALSE
    TRY
        UPDATE wallets SET balance = balance - amount
            WHERE wallet_id = sourceID AND balance >= amount
        IF updated row count != 1 THEN RAISE insufficient funds / missing wallet
        UPDATE wallets SET balance = balance + amount WHERE wallet_id = destinationID
        IF updated row count != 1 THEN RAISE missing destination
        COMMIT
    CATCH any failure
        ROLLBACK
        RAISE failure
    FINALLY restore original autoCommit
END
```

Lock/update ordering must be consistent (for example lower wallet ID first) to reduce deadlocks; database constraints and DECIMAL money values protect invariants. If source and destination are separate auto-commit updates and execution fails between them, the debit may persist without the credit: funds are lost. Transactions add log/lock/commit overhead and may increase contention, but preserve atomicity; their asymptotic row-update cost remains O(1) per indexed wallet, two updates total.

The unsafe login concatenates input. An ID such as `admin' -- ` can close the string and comment out the password predicate; `admin' OR '1'='1' -- ` makes the WHERE condition true for rows. Use `SELECT id FROM users WHERE id=? AND pwd_hash=?`, bind both values with `PreparedStatement`, and store a password hash (production authentication should use a slow salted password-hashing function). A parameter is data and cannot become SQL syntax. Runnable H2-only examples are in `TaskB2/`.
