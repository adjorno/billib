# M14N

Historical Billboard chart data — backend API, web app, and mobile apps.

- **Web**: https://m14n.com
- **API**: https://api.m14n.com ([Swagger](https://api.m14n.com))
- **iOS / Android**: coming to app stores

## Architecture

```mermaid
graph TD
    subgraph clients["Client Platforms"]
        direction LR
        AN["Android"]
        WEB["Web — wasmJs\nm14n.com"]
        JVM["JVM Desktop"]
    end

    subgraph kmp["Compose Multiplatform — Shared Kotlin"]
        direction TB
        UI["UI Layer\nScreens · ViewModels · Nav3"]
        DATA["Data Layer\nChartRepository · ArtworkRepository\nSQLDelight offline cache"]
        NET["Network\nKtorM14nApi · FirebaseAuthRepository"]
    end

    subgraph ext["External Services"]
        FB["Firebase Auth\nAnonymous · Email · Google · Apple"]
        AM["Apple Music API\nArtwork"]
    end

    subgraph backend["Backend — Spring Boot · api.m14n.com"]
        direction TB
        SEC["Security\nFirebaseJwtDecoder · ApiKeyAuthFilter"]
        CTRL["REST Controllers\nChart · Track · Artist · Search · Trends · User"]
        DAL["JPA Repositories"]
        PG[("PostgreSQL\ncharts · tracks · artists · users")]
    end

    subgraph pipeline["Data Pipeline"]
        IMP["m14n-importer\nBillboard HTML scraper"]
    end

    subgraph cicd["CI / CD — GitHub Actions"]
        direction LR
        GHA["lint · detekt · tests"]
        CF["Cloudflare Pages"]
        RW["Railway"]
    end

    AN & WEB & JVM --> UI
    UI --> DATA
    DATA --> NET
    NET <-->|sign-in / token| FB
    NET -->|Bearer JWT · HTTPS| SEC
    NET <-->|artwork URLs| AM
    FB -.->|verifyIdToken| SEC
    SEC --> CTRL
    CTRL --> DAL
    DAL --> PG
    IMP -->|seed · update| PG
    GHA --> CF
    GHA --> RW
```

## Quick Start

### Prerequisites
- JDK 21, PostgreSQL 15+, Gradle 9.2.1

### Local Development

```bash
# 1. Setup database
createdb m14ndb
psql m14ndb < m14n-importer/src/main/resources/db/schema.sql

# 2. Import data
./gradlew :libraries:m14n-importer:import \
  --args="--data-path=/path/to/m14ndata/data --db-url=jdbc:postgresql://localhost:5432/m14ndb --db-user=YOUR_USER"

# 3. Run API
SPRING_PROFILES_ACTIVE=local ./gradlew :backend:bootRun
```

## License

MIT License — See LICENSE file for details.

## Contributing

**Pull requests are not accepted** due to the growth of agentic AIs.

For bugs, feature requests, or questions open an issue or contact via personal message.
All contributions will be reviewed and implemented by the maintainer.
