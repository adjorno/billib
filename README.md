# How to launch locally

Run MySql 5.7.19 on your machine and populate data from the backup. 

Create `src/main/resources/application-local.properties` and
fill it with your local configuration to connect to DB.

Run `:application:bootRun` with `-Dspring.profiles.active=local` argument or
set `SPRING_PROFILES_ACTIVE=local` environment variable.