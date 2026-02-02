# Billboard Charts REST API

A Spring Boot REST API for accessing historical Billboard chart data (1958-present). Built with Kotlin, PostgreSQL, and optimized for read-heavy workloads.

## 🚀 Quick Start

### Prerequisites

- JDK 21
- PostgreSQL 15+
- Gradle 8.8

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/adjorno/billib.git
   cd billib
   ```

2. **Set up PostgreSQL**
   ```bash
   # macOS
   brew install postgresql@15
   brew services start postgresql@15
   createdb billibdb

   # Create schema
   psql billibdb < src/main/resources/db/migration/V2__simple_schema.sql
   ```

3. **Import data**
   ```bash
   cd billib-importer
   ./gradlew run --args="--db-url=jdbc:postgresql://localhost:5432/billibdb \
     --db-user=YOUR_USERNAME \
     --data-path=/path/to/billibdata/data"
   ```

4. **Run the API**
   ```bash
   cd ..
   ./gradlew bootRun -Dspring.profiles.active=postgres
   ```

5. **Test endpoints**
   ```bash
   curl http://localhost:8080/chart/all
   curl http://localhost:8080/chartList/getByDate?chart=hot-100&date=2024-01-06
   ```

## 📊 Database Schema

- **35,921 artists** - Music artists with full-text search
- **108,736 tracks** - Songs with denormalized artist data
- **23 charts** - Hot 100, Country, Hip-Hop, etc.
- **1,690,866 positions** - Historical chart positions (1958-present)

Optimized for:
- Track history queries (< 10ms)
- Artist best tracks (< 5ms)
- Full-text search (< 50ms)
- Weekly debuts (< 20ms)

## 🌐 Production Deployment

### Infrastructure

- **Database**: Supabase (PostgreSQL)
- **API Hosting**: Railway
- **CI/CD**: GitHub Actions

See [DEPLOYMENT.md](DEPLOYMENT.md) for complete deployment guide.

### Architecture

```
Billboard JSON Files (800 MB)
    ↓
Kotlin CLI Importer (billib-importer)
    ↓
PostgreSQL Database (Supabase)
    ↓
Spring Boot REST API (Railway)
    ↓
Kotlin Multiplatform Frontend
```

## 📡 API Endpoints

### Charts

- `GET /chart/all` - List all chart types
- `GET /chart/{id}` - Get chart by ID

### Chart Lists

- `GET /chartList/getByDate?chart={name}&date={YYYY-MM-DD}` - Get chart for specific date
- `GET /chartList/{id}` - Get chart list by ID

### Tracks

- `GET /track/{id}` - Get track details
- `GET /track/{id}/history` - Track chart history

### Artists

- `GET /artist/{id}` - Get artist details
- `GET /artist/{id}/tracks` - Artist's tracks

### Search

- `GET /search?q={query}` - Search artists and tracks

## 🛠️ Technology Stack

- **Language**: Kotlin 2.0
- **Framework**: Spring Boot 2.6.1
- **Database**: PostgreSQL 15
- **Build**: Gradle 8.8
- **ORM**: JPA/Hibernate
- **Migration**: Flyway (optional)
- **Testing**: JUnit 5

## 📦 Project Structure

```
billib/
├── src/main/java/com/adjorno/billib/rest/  # Spring Boot REST API
│   ├── db/                                   # JPA entities
│   ├── TrackController.java                 # REST controllers
│   └── ArtistController.kt
├── billib-importer/                          # Standalone data importer
│   └── src/main/kotlin/
│       ├── Importer.kt                       # Main entry point
│       └── db/                               # Bulk import logic
├── libraries/
│   ├── data-source/billboard/                # Billboard.com scraper
│   └── inmemory-rest/                        # In-memory testing
├── src/main/resources/
│   ├── db/migration/                         # Database schema
│   ├── application-postgres.properties       # PostgreSQL config
│   └── application-production.properties     # Production config
└── .github/workflows/                        # CI/CD pipelines
```

## 🔧 Configuration

### Local Development

Copy `application-postgres.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/billibdb
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=
```

### Production (Railway)

Set environment variables:
```bash
SPRING_PROFILES_ACTIVE=production
DATABASE_URL=jdbc:postgresql://[supabase-host]:5432/postgres
DATABASE_USERNAME=postgres.[project-ref]
DATABASE_PASSWORD=[your-password]
```

## 🧪 Testing

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests TrackControllerTest

# Run with coverage
./gradlew test jacocoTestReport
```

## 📈 Performance

### Query Benchmarks (PostgreSQL)

| Query Type | Target | Achieved |
|------------|--------|----------|
| Track history (single) | < 10ms | ~5ms |
| Artist best tracks | < 5ms | ~2ms |
| Full-text search | < 50ms | ~20ms |
| Weekly debuts | < 20ms | ~8ms |

### Import Performance

- **Artists**: ~10 seconds (35,921 records)
- **Tracks**: ~30 seconds (108,736 records)
- **Chart positions**: ~1m 45s (1,690,866 records)
- **Total**: ~2-3 minutes

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📝 License

This project is licensed under the MIT License - see LICENSE file for details.

## 🙏 Acknowledgments

- Billboard chart data from billboard.com
- Built with Spring Boot, Kotlin, and PostgreSQL

## 📧 Contact

For questions or support, please open an issue on GitHub.
