# Production profile

Set `SPRING_PROFILES_ACTIVE=h2` environment variable.

# How to launch with local MySql database connection

Run MySql 5.7.19 on your machine and populate data from the backup. 

Create `src/main/resources/application-local.properties` and
fill it with your local configuration to connect to DB.

Run `:application:bootRun` with `-Dspring.profiles.active=local` argument

Run `:application:bootRun` with `-Dspring.profiles.active=local` argument or
set `SPRING_PROFILES_ACTIVE=local` environment variable.

# BilliB in-memory

This is in-memory implementation of the BilliB DAO (artists, tracks, charts etc). Original implementation was SQL based, but there is no need to access the data via SQL because the DB size is less then 1GB.

## Usage

[MySqlReader.kt](inmemory-rest/src/main/kotlin/com/m14n/billib/reader/MySqlReader.kt) is an example of how to load DAO.

# Setup in-memory database

See [tutorial](docs/in-memory-db-setup.md).

