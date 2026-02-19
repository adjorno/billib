# Railway PostgreSQL Deployment

Deploy Billboard Charts database to Railway from raw JSON data.

## Prerequisites

- Railway PostgreSQL (5GB+ storage recommended)
- PostgreSQL client tools (`pg_restore`, `psql`)
- Raw JSON data at `/Users/adjorno/Developer/Sources/m14ndata/data/`

## Quick Deploy

### 1. Setup Railway PostgreSQL

1. Create PostgreSQL database in Railway (Hobby plan, 5GB storage)
2. Get public connection URL from Railway dashboard:
   ```
   postgresql://postgres:PASSWORD@host.proxy.rlwy.net:PORT/railway
   ```

### 2. Create Schema

```bash
psql "postgresql://postgres:PASSWORD@host:PORT/railway" \
  -f m14n-importer/src/main/resources/db/schema.sql
```

### 3. Import Data

**Option A: Direct import to Railway (recommended)**
```bash
cd m14n-importer
railway run ./gradlew run --args="--data-path=/path/to/m14ndata/data"
```
Duration: ~2-3 minutes

**Option B: Via local export**
```bash
# 1. Create local database
createdb m14ndb
psql m14ndb -f m14n-importer/src/main/resources/db/schema.sql

# 2. Import data locally
cd m14n-importer
./gradlew run --args="--data-path=/path/to/data --db-url=jdbc:postgresql://localhost:5432/m14ndb --db-user=YOUR_USER"

# 3. Export
pg_dump -Fc m14ndb > m14n_export.dump

# 4. Restore to Railway
pg_restore -d "postgresql://postgres:PASSWORD@host:PORT/railway" --no-owner --no-acl --verbose --clean --if-exists m14n_export.dump
```
Duration: ~1-2 minutes for restore

### 4. Verify

```bash
psql "postgresql://postgres:PASSWORD@host:PORT/railway"

SELECT 'artist' as table, COUNT(*) FROM artist
UNION ALL SELECT 'track', COUNT(*) FROM track
UNION ALL SELECT 'chart_track_position', COUNT(*) FROM chart_track_position;

-- Expected: 35,947 | 108,823 | 1,692,356
```

### 5. Deploy Application

```bash
railway redeploy --yes
railway logs  # Verify connection
```

## Troubleshooting

**"No space left on device"**
- Increase storage to 5GB in Railway settings
- Wipe volume if partially imported

**"Connection timeout"**
- Use public proxy URL from Railway dashboard
- NOT internal hostname (`postgres.railway.internal`)

**Import too slow**
- Use binary dump (`.dump`) not SQL file (`.sql`)
- Increase Railway database resources

## Storage Requirements

- Data: ~460MB
- Indexes: ~200MB
- Total with overhead: ~700MB (5GB recommended)
