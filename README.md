# Production profile

Set `SPRING_PROFILES_ACTIVE=h2` environment variable.

# How to launch with local MySql database connection

Run MySql 5.7.19 on your machine and populate data from the backup. 

Create `src/main/resources/application-local.properties` and
fill it with your local configuration to connect to DB.

Run `:application:bootRun` with `-Dspring.profiles.active=local` argument

Run `:application:bootRun` with `-Dspring.profiles.active=local` argument or
set `SPRING_PROFILES_ACTIVE=local` environment variable.

# Setup in-memory database

See [tutorial](docs/in-memory-db-setup.md).
