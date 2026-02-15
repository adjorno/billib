# Billboard Charts REST API

REST API for accessing historical Billboard chart data (1958-present).

**Production API**: https://billib-production.up.railway.app/

## Quick Start

### Prerequisites
- JDK 21, PostgreSQL 15+, Gradle 9.2.1

### Local Development

```bash
# 1. Setup database
createdb billibdb
psql billibdb < billib-importer/src/main/resources/db/schema.sql

# 2. Import data
cd billib-importer
./gradlew run --args="--data-path=/path/to/billibdata/data --db-url=jdbc:postgresql://localhost:5432/billibdb --db-user=YOUR_USER"

# 3. Run API
cd ..
./gradlew bootRun -Dspring.profiles.active=postgres
```

### Production Deployment
See [billib-importer/DEPLOYMENT.md](billib-importer/DEPLOYMENT.md) for Railway deployment guide.

## API Endpoints

**Base URL**: https://billib-production.up.railway.app

- `GET /chart/all` - List all charts
- `GET /chartList/getByDate?chart={name}&date={YYYY-MM-DD}` - Chart for specific date
- `GET /track/{id}/history` - Track chart history
- `GET /artist/{id}/tracks` - Artist's tracks
- `GET /search?q={query}` - Search artists and tracks

## Database

- **35,947 artists** - 1958-present
- **108,823 tracks** - Full-text search enabled
- **1,692,356 chart positions** - Denormalized for performance
- **Query performance**: < 10ms for most queries

## Technology

- Kotlin 2.3.0 + Spring Boot 3.5.0
- PostgreSQL 15 with JPA/Hibernate
- Railway (PostgreSQL + API hosting)
- Gradle 9.2.1, Java 21

## License

MIT License - See LICENSE file for details.

## Contributing

**Pull requests are not accepted** due to the growth of agentic AIs.

For bugs, feature requests, or questions:
- Open an issue on GitHub
- Contact via personal message

All contributions will be reviewed and implemented by the maintainer.
