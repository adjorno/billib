# Railway PostgreSQL Migration Guide

## Status: Database Ready, Data Export Complete

### ✅ Completed Steps

1. **Railway PostgreSQL Created**
   - Service: `Postgres-93e4`
   - Database: `railway`
   - Credentials: Stored securely in Railway environment variables

2. **Environment Variables Updated**
   - Railway billib service configured to use new PostgreSQL
   - Variables set in Railway dashboard (not committed to git)
   - Connection uses Railway internal networking

3. **Data Successfully Loaded Locally**
   - **1,692,356** chart track positions
   - **108,823** tracks
   - **35,947** artists
   - **3,525** weeks (1958-08-04 to 2026-02-14)
   - Schema: V2 (non-partitioned)
   - Export file: `billib_export.sql` (168MB)

### 🔄 Remaining Step: Import Data to Railway

The data is ready in `billib_export.sql` but needs to be imported after deployment.

#### Import Process:

1. **Deploy application to Railway** - creates schema via migrations
2. **Import data** - upload and restore the SQL export
3. **Verify** - test API endpoints and data integrity

### Database Schema

- **Version**: V2 (non-partitioned, simplified)
- **Tables**: 14 main tables
- **Indexes**: Optimized for query performance
- **Extensions**: `pg_trgm`, `btree_gin` for full-text search

### Data Verification Queries

```sql
-- Check row counts
SELECT 'ARTIST' as table, COUNT(*) FROM ARTIST
UNION ALL SELECT 'TRACK', COUNT(*) FROM TRACK
UNION ALL SELECT 'CHART_TRACK_POSITION', COUNT(*) FROM CHART_TRACK_POSITION;

-- Sample data
SELECT name FROM ARTIST WHERE name LIKE '%Taylor Swift%' LIMIT 5;

-- Date range
SELECT MIN(date) as earliest, MAX(date) as latest FROM WEEK;
```

### Cost Savings

- **Before**: Supabase Pro at $25/month
- **After**: Railway PostgreSQL at ~$5-7/month
- **Savings**: ~$18-20/month (72-80% reduction)

### Security Notes

- Database credentials stored in Railway environment variables only
- No passwords or connection strings committed to git
- Internal Railway networking (`.railway.internal` domain)
- Only accessible from within Railway infrastructure
