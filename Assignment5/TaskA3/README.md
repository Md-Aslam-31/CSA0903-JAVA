# A3 - Generic data-access design

StudentDB, CourseDB and MarksDB repeat: acquire connection, prepare/execute SQL, bind values, iterate `ResultSet`, map rows into objects, handle SQL failures, and close JDBC resources. Their varying pieces are entity/table names, columns, primary-key types and object mapping.

```java
public interface Dao<T, K> {
    K insert(T entity) throws SQLException;
    Optional<T> findById(K id) throws SQLException;
    List<T> findAll() throws SQLException;
    boolean update(T entity) throws SQLException;
    boolean delete(K id) throws SQLException;
}
```

`StudentDao implements Dao<Student, Long>` binds `id`, `register_no`, `name`, `email` using PreparedStatement, reads `getLong("id")`, etc., and obtains generated keys after insert. Student fields map as `id -> student.id PRIMARY KEY`, `registerNo -> register_no UNIQUE`, `name -> name`, `email -> email`. Use try-with-resources for connection/statement/result set. Each DAO owns entity-specific SQL and mapping; a shared base class may provide safe connection/lifecycle helpers, but must not hide meaningful query differences.

Putting SQL in services/GUI scatters the table/column contract: a rename can touch each query and mapper in multiple classes. With a DAO the change is localized to the entity mapping and DAO SQL, plus the migration. The per-operation asymptotic DB work is unchanged (e.g. indexed find O(log n), full listing O(n)); DAO indirection adds O(1) method-call overhead and O(1) request memory, negligible beside database/network work. A concrete example is `StudentDao.java`.
