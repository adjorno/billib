# Deployment Guide: Supabase + Railway

This guide covers deploying the Billboard Charts REST API to production using Supabase (PostgreSQL) and Railway (application hosting).

## Prerequisites

- GitHub account with billib repository
- Credit card (for Supabase and Railway, though free tiers available)

---

## Part 1: Deploy Database to Supabase (15 minutes)

### Step 1: Create Supabase Project

1. Go to https://supabase.com
2. Sign up / Log in with GitHub
3. Click **"New Project"**
4. Fill in:
   - **Name**: `billib-production`
   - **Database Password**: Generate strong password (save it!)
   - **Region**: Choose closest to you (e.g., `us-east-1`)
   - **Pricing Plan**: Free tier (up to 500 MB, sufficient for our 270 MB data)
5. Click **"Create new project"** (takes ~2 minutes)

### Step 2: Get Connection Details

1. In Supabase dashboard, go to **Settings** → **Database**
2. Scroll to **Connection string** section
3. Copy the **URI** format (not transaction pooler):
   ```
   postgresql://postgres.[project-ref]:[password]@aws-0-us-east-1.pooler.supabase.com:5432/postgres
   ```
4. Save these values (you'll need them for Railway):
   - **Host**: `aws-0-us-east-1.pooler.supabase.com` (or your region)
   - **Port**: `5432`
   - **Database**: `postgres`
   - **User**: `postgres.[project-ref]`
   - **Password**: Your database password

### Step 3: Enable PostgreSQL Extensions

1. In Supabase dashboard, go to **Database** → **Extensions**
2. Search and enable:
   - ✅ `pg_trgm` (for full-text search)
   - ✅ `btree_gin` (for multi-column indexes)

Or use SQL Editor:
```sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS btree_gin;
```

### Step 4: Create Database Schema

1. Go to **SQL Editor** in Supabase dashboard
2. Copy contents of `src/main/resources/db/migration/V2__simple_schema.sql`
3. Paste and click **"Run"**
4. Verify tables created:
   ```sql
   SELECT table_name FROM information_schema.tables
   WHERE table_schema = 'public'
   ORDER BY table_name;
   ```
   Expected: ARTIST, CHART, CHART_LIST, CHART_TRACK, CHART_TRACK_POSITION, etc.

### Step 5: Import Data to Supabase

Run the importer locally pointing to Supabase:

```bash
cd billib-importer

./gradlew run --args="--db-url=jdbc:postgresql://[supabase-host]:5432/postgres \
  --db-user=postgres.[project-ref] \
  --db-password=[your-password] \
  --data-path=/Users/adjorno/Developer/Sources/billibdata/data"
```

Expected output:
```
✅ Imported 35,921 artists
✅ Imported 108,736 tracks
✅ Imported 23 charts
✅ Imported 1,690,866 chart positions
⏱️  Total time: ~2-3 minutes
```

### Step 6: Verify Data

In Supabase SQL Editor:
```sql
SELECT COUNT(*) FROM artist;           -- Expected: 35,921
SELECT COUNT(*) FROM track;            -- Expected: 108,736
SELECT COUNT(*) FROM chart_track_position;  -- Expected: 1,690,866

-- Test a query
SELECT * FROM chart ORDER BY _id LIMIT 10;
```

---

## Part 2: Deploy REST API to Railway (10 minutes)

### Step 1: Prepare Repository

1. Commit all deployment files to git:
   ```bash
   git add .github/workflows/build.yml
   git add railway.json
   git add src/main/resources/application-production.properties
   git add DEPLOYMENT.md
   git commit -m "Add deployment configuration for Railway"
   git push origin master
   ```

### Step 2: Create Railway Project

1. Go to https://railway.app
2. Sign up / Log in with GitHub
3. Click **"New Project"**
4. Select **"Deploy from GitHub repo"**
5. Authorize Railway to access your GitHub
6. Select repository: **`billib`**
7. Railway will detect Gradle and start building automatically

### Step 3: Configure Environment Variables

1. In Railway dashboard, click your service
2. Go to **Variables** tab
3. Add the following environment variables:

```bash
# Spring profile
SPRING_PROFILES_ACTIVE=production

# Database connection (from Supabase)
DATABASE_URL=jdbc:postgresql://[supabase-host]:5432/postgres
DATABASE_USERNAME=postgres.[project-ref]
DATABASE_PASSWORD=[your-supabase-password]

# Server port (Railway provides this automatically)
PORT=8080
```

4. Click **"Deploy"** or wait for auto-deploy

### Step 4: Monitor Deployment

1. Go to **Deployments** tab
2. Watch build logs:
   - ✅ Gradle build
   - ✅ Tests pass
   - ✅ JAR created
   - ✅ Application starts
3. Expected deploy time: ~3-5 minutes

### Step 5: Get Your API URL

1. In Railway dashboard, go to **Settings** tab
2. Scroll to **Networking** section
3. Click **"Generate Domain"**
4. Your API will be available at: `https://[your-app].railway.app`

### Step 6: Test Your Production API

```bash
# Test health endpoint
curl https://[your-app].railway.app/actuator/health

# Test charts endpoint
curl https://[your-app].railway.app/chart/all

# Test specific chart
curl https://[your-app].railway.app/chartList/getByDate?chart=hot-100&date=2024-01-06
```

Expected response:
```json
{
  "chart": {...},
  "tracks": [...]
}
```

---

## Part 3: CI/CD Pipeline

### GitHub Actions Workflow

The `.github/workflows/build.yml` file automatically:
- ✅ Runs on every push to `master`
- ✅ Builds the project with Gradle
- ✅ Runs all tests
- ✅ Uploads build artifacts

### Railway Auto-Deploy

Railway automatically deploys when:
- ✅ Code is pushed to `master` branch
- ✅ GitHub Actions build succeeds
- ✅ No manual intervention needed

### Deployment Flow

```
1. Developer pushes code to GitHub (master)
   ↓
2. GitHub Actions runs build + tests
   ↓
3. Railway detects new commit
   ↓
4. Railway builds Docker container
   ↓
5. Railway deploys to production
   ↓
6. New version live (with zero downtime)
```

---

## Monitoring & Logs

### Railway Logs

1. Go to Railway dashboard → Your service
2. Click **"View Logs"**
3. Real-time application logs displayed

### Supabase Database Monitoring

1. Go to Supabase dashboard → **Database**
2. View metrics:
   - Database size
   - Active connections
   - Query performance
   - Table sizes

### Health Check Endpoint

Production API includes Spring Boot Actuator:
```bash
curl https://[your-app].railway.app/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

---

## Cost Estimates

### Supabase (Database)
- **Free tier**: 500 MB database, 2 GB bandwidth/month
- **Pro tier**: $25/month (8 GB database, 50 GB bandwidth)
- **Your usage**: ~270 MB data = Free tier sufficient

### Railway (Application Hosting)
- **Free tier**: $5 in credits per month (hobbyist projects)
- **Pro tier**: $20/month + usage-based pricing
- **Estimated cost**: $10-20/month for low-moderate traffic

### Total Monthly Cost
- **Development**: $0 (both free tiers)
- **Production (low traffic)**: $0-25/month
- **Production (high traffic)**: $45-70/month

---

## Rollback Strategy

### Railway Rollback (instant)

1. Go to Railway dashboard → **Deployments**
2. Find previous working deployment
3. Click **"Redeploy"**
4. Previous version restored in ~30 seconds

### Database Rollback

Supabase provides:
- **Point-in-time recovery** (Pro tier only)
- **Daily backups** (Free tier: 7 days retention)

To restore:
1. Go to Supabase dashboard → **Database** → **Backups**
2. Select backup
3. Click **"Restore"**

---

## Custom Domain (Optional)

### Add Custom Domain to Railway

1. Purchase domain (Cloudflare, Namecheap, etc.)
2. In Railway, go to **Settings** → **Networking**
3. Click **"Add custom domain"**
4. Enter your domain: `api.yourdomain.com`
5. Add DNS records (Railway provides instructions):
   ```
   Type: CNAME
   Name: api
   Value: [your-app].railway.app
   ```
6. Wait for SSL certificate (automatic, ~5 minutes)

---

## Security Best Practices

### Database
- ✅ Use Supabase connection pooler (default)
- ✅ Rotate database password periodically
- ✅ Enable SSL connections (Supabase default)
- ✅ Set up IP allowlist (if needed)

### Application
- ✅ Enable HTTPS only (Railway default)
- ✅ Add rate limiting for public endpoints
- ✅ Configure CORS for your frontend domain
- ✅ Use environment variables for secrets (never commit)
- ✅ Enable Spring Security for admin endpoints

### Example CORS Configuration

Add to your Spring Boot application:

```kotlin
@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("https://yourdomain.com")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}
```

---

## Troubleshooting

### Issue: Railway build fails

**Solution**: Check build logs in Railway dashboard
- Common issue: Missing environment variables
- Fix: Add required variables in Railway Variables tab

### Issue: Database connection timeout

**Solution**: Verify Supabase connection string
```bash
psql "postgresql://postgres.[ref]:[password]@[host]:5432/postgres"
```

### Issue: Application starts but endpoints return 404

**Solution**: Check Spring profile is set to `production`
```bash
SPRING_PROFILES_ACTIVE=production
```

### Issue: Out of memory on Railway

**Solution**: Increase memory limit in Railway settings
- Free tier: 512 MB
- Pro tier: Up to 8 GB

---

## Next Steps

1. ✅ **Set up monitoring**: Add Sentry or Datadog for error tracking
2. ✅ **Add caching**: Redis for frequently accessed data
3. ✅ **Enable CDN**: Cloudflare for static assets
4. ✅ **Rate limiting**: Protect against abuse
5. ✅ **API documentation**: Swagger/OpenAPI
6. ✅ **Frontend deployment**: Deploy Kotlin Multiplatform app

---

## Support

- **Railway**: https://railway.app/help
- **Supabase**: https://supabase.com/docs
- **Project issues**: https://github.com/adjorno/billib/issues
