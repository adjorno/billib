# How to launch locally

Create `src/main/resources/application-local.properties` and
fill it with your local configuration to connect to DB.

Run `:application:bootRun` with `-Dspring.profiles.active=local` argument

# Setup in-memory database

See [tutorial](docs/in-memory-db-setup.md).
