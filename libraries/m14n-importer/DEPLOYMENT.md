# Railway PostgreSQL Deployment

Deploy Billboard Charts database to Railway from raw JSON data.

## Prerequisites

- Railway PostgreSQL service with public proxy URL
- Railway CLI (`railway`) linked to the project
- PostgreSQL client tools (`psql`, `pg_dump`)
- Raw JSON data at /path/to/m14ndata/data/
- Local PostgreSQL running with a `m14ndb` database

## Overview

**Do NOT import directly to Railway via JDBC.** The Railway TCP proxy has a ~3 minute
connection timeout that kills long-running inserts. The correct approach:

1. Import everything locally (fast, no network)
2. Run local Spring app to generate trends and DayTrack
3. `pg_dump` local DB and stream to Railway via `psql` (COPY is ~100× faster than JDBC)

Total time: ~15 minutes.

---

## Step 1 — Prepare Local Database

Create and populate a fresh local database.

```bash
# Drop and recreate (skip if m14ndb already exists and is empty)
dropdb m14ndb 2>/dev/null; createdb m14ndb

# Apply schema
psql -U YOUR_POSTGRES_USER -d m14ndb \
  -f libraries/m14n-importer/src/main/resources/db/schema.sql
```

---

## Step 2 — Import All Chart Data Locally

The local import is fast (no network latency, local disk I/O).
`ON CONFLICT DO NOTHING` makes reruns safe — only missing rows are inserted.

```bash
cd libraries/m14n-importer
./gradlew run --args="\
  --data-path=/Users/adjorno/Developer/Sources/m14ndata/data \
  --db-url=jdbc:postgresql://localhost:5432/m14ndb \
  --db-user=adjorno"
```

Duration: ~1–10 minutes depending on how much data already exists.

**Expected counts after import:**

| Table                | Count     |
|----------------------|-----------|
| artist               | 38,195    |
| track                | 116,162   |
| chart                | 24        |
| week                 | 3,526     |
| chart_list           | 32,172    |
| chart_track          | 1,789,295 |
| chart_track_position | 1,789,275 |
| trend_type           | 4         |

---

## Step 3 — Generate Trends and DayTrack

Start the local Spring app and generate the missing derived data.

```bash
# Build JAR if needed
./gradlew :backend:bootJar

# Start on port 8081 (avoids conflict with other services)
java -Dspring.profiles.active=local -Dserver.port=8081 \
  -jar backend/build/libs/M14N-*.jar &
sleep 8  # Wait for startup
```

**Get latest Hot 100 week:**

```bash
LATEST_WEEK=$(psql -U adjorno -d m14ndb -t -c "
  SELECT w.date FROM week w
  JOIN chart_list cl ON cl.week_id = w.week_id
  JOIN chart c ON c._id = cl.chart_id
  WHERE c.name = 'Hot 100'
  ORDER BY w.date DESC LIMIT 1;" | tr -d ' ')
echo "Latest week: $LATEST_WEEK"
```

**Generate trends for latest week:**

```bash
curl -X POST "http://localhost:8081/generateTrends?week=$LATEST_WEEK&type=0"
```

Note: if `trend_track` already has rows for this week, this will return 500 (duplicate key).
That is fine — it means trends already exist.

**Generate Track of the Day for next 14 days:**

```bash
for i in $(seq 0 13); do
  DATE=$(date -v +${i}d +%Y-%m-%d)   # macOS; use 'date -d "+${i} days"' on Linux
  curl -s -X POST "http://localhost:8081/track/day?date=$DATE" > /dev/null
  echo "DayTrack: $DATE"
done
```

**Verify locally:**

```bash
curl -s http://localhost:8081/trends | python3 -c "
import sys, json
d = json.load(sys.stdin)
print('week:', d['week'], '| lists:', len(d['trendLists']))
for tl in d['trendLists']:
    print(' ', tl['name'], '—', len(tl['tracks']), 'tracks')"

curl -s http://localhost:8081/track/day
curl -s http://localhost:8081/chart/all | python3 -c "import sys,json; d=json.load(sys.stdin); print(len(d), 'charts')"
```

**Stop the local app:**

```bash
kill $(pgrep -f "M14N-0.0.1.jar")
```

---

## Step 4 — Prepare Railway Database

Get the public proxy URL from the Railway dashboard or:

```bash
railway variables | grep DATABASE_PUBLIC_URL
```

Format: `postgresql://postgres:PASSWORD@host.proxy.rlwy.net:PORT/railway`

**Apply schema to Railway:**

```bash
RAILWAY_URL="postgresql://postgres:PASSWORD@host.proxy.rlwy.net:PORT/railway"

psql "$RAILWAY_URL" \
  -f libraries/m14n-importer/src/main/resources/db/schema.sql
```

---

## Step 5 — Stream All Data to Railway

Dump local DB and pipe directly to Railway. COPY statements complete in seconds.

```bash
pg_dump -U adjorno -d m14ndb \
  --data-only \
  --no-privileges \
  --disable-triggers \
  | psql "$RAILWAY_URL"
```

The `journal` table will show a duplicate key error — this is expected (schema.sql seeds it).
All other tables should show `COPY N` with the correct counts.

---

## Step 6 — Refresh Materialized Views

```bash
psql "$RAILWAY_URL" -c "
REFRESH MATERIALIZED VIEW GLOBAL_RANK_TRACK;
REFRESH MATERIALIZED VIEW GLOBAL_RANK_ARTIST;"
```

---

## Step 7 — Verify

```bash
psql "$RAILWAY_URL" -c "
SELECT 'artist'               as tbl, COUNT(*) FROM artist
UNION ALL SELECT 'track',              COUNT(*) FROM track
UNION ALL SELECT 'chart',              COUNT(*) FROM chart
UNION ALL SELECT 'week',               COUNT(*) FROM week
UNION ALL SELECT 'chart_list',         COUNT(*) FROM chart_list
UNION ALL SELECT 'chart_track',        COUNT(*) FROM chart_track
UNION ALL SELECT 'chart_track_position', COUNT(*) FROM chart_track_position
UNION ALL SELECT 'trend_type',         COUNT(*) FROM trend_type
UNION ALL SELECT 'trend_track',        COUNT(*) FROM trend_track
UNION ALL SELECT 'day_track',          COUNT(*) FROM day_track
UNION ALL SELECT 'GLOBAL_RANK_TRACK',  COUNT(*) FROM GLOBAL_RANK_TRACK
UNION ALL SELECT 'GLOBAL_RANK_ARTIST', COUNT(*) FROM GLOBAL_RANK_ARTIST;"
```

**Smoke test live API:**

```bash
curl -s https://api.m14n.com/trends | python3 -c "
import sys, json
d = json.load(sys.stdin)
print('week:', d['week'], '| lists:', len(d['trendLists']))"

curl -s https://api.m14n.com/track/day
curl -s https://api.m14n.com/chart/all | python3 -c "import sys,json; print(len(json.load(sys.stdin)), 'charts')"
```

---

## Step 8 — Redeploy App (if needed)

Redeploy only the **app** service (not Postgres):

```bash
railway service m14n
railway redeploy --yes
railway logs --lines 20
```

> **WARNING**: Never run `railway redeploy` on the **Postgres** service after importing data.
> Railway restores from its latest cloud backup on every Postgres restart, which will
> overwrite all imported data with whatever was backed up last.

---

## Troubleshooting

**Import times out / `Operation timed out`**
- Do not import directly to Railway via JDBC — use the local → pg_dump → psql approach above.

**Trends return 0 tracks**
- Check `chart_track` table is populated: `SELECT COUNT(*) FROM chart_track;`
- Regenerate trends: `curl -X POST "https://api.m14n.com/generateTrends?week=YYYY-MM-DD&type=0"`

**DayTrack POST returns empty**
- Check `day_track` table: `SELECT * FROM day_track ORDER BY date;`
- The POST is idempotent — existing entries are not overwritten

**`railway redeploy` on Postgres wiped the data**
- Railway restored from its cloud backup (which may predate the import)
- Re-run from Step 4 onwards (local data is intact, just re-stream to Railway)

**`journal` duplicate key on pg_dump restore**
- Expected — schema.sql seeds one journal row; pg_dump tries to insert it again
- Safe to ignore, all other data copies correctly

## Storage Requirements

- Data + indexes: ~1.7 GB
- Railway volume: 5 GB recommended
