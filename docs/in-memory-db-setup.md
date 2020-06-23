# Setup in-memory database

In order to configure H2 SQL in-memory database, the following steps should be performed:
1. Export dump of the database to SQL file. This can be done via UI tools (Workbench, DBeaver) or console command. In export settings disable (if possible) table locks, batch data insertion (in Workbench it is `extended-insert` option). Otherwise, H2 will take a lot of time to start a database.
2. Use [SqlBackupH2Converter.kt](../src/main/kotlin/com/adjorno/billib/sql/SqlBackupH2Converter.kt) script to convert SQL script received at step 1 to H2 compatible backup. The script performs the following manipulations with input SQL:
   - inserts `drop all objects delete files;` to the beginning of file
   - Removes all table locks and unlocks
   - Fixes table creation command by deleting post-create string
   - Replaces `\'` with `''`
3. Replace converted SQL script with [data.sql](../src/main/resources/data.sql).
4. Update configuration of the server (`application.properties` file):
```
spring.datasource.url=jdbc:h2:mem:billib
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=sa
hibernate.dialect=org.hibernate.dialect.H2Dialect
```

Now, the server is configured to use H2 in-memory database.