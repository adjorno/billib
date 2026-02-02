# Railway Environment Variables Required

Set these in Railway Dashboard → Variables:

## Build Configuration
- `RAILPACK_GRADLE_VERSION=9.2.1`
- `RAILPACK_JDK_VERSION=21`

## Database Configuration (Supabase Session Mode Pooler)

**IMPORTANT**: Use Supabase's Session Mode pooler connection (IPv4-compatible).
Railway does NOT support outbound IPv6, so direct connections (db.*.supabase.co) will fail.

Get your Session Mode connection string from:
Supabase Dashboard → Project Settings → Database → Connection String → Session Mode

### Environment Variables:
- `DATABASE_URL=jdbc:postgresql://aws-1-eu-north-1.pooler.supabase.com:5432/postgres`
- `DATABASE_USERNAME=postgres.ptegywuuusdhqzwueezv` (includes project ref)
- `DATABASE_PASSWORD=<your-password>`

### Notes:
- Session Mode pooler uses port 5432 (not 6543)
- Username format: `postgres.<project-ref>` (not just `postgres`)
- Hostname: `aws-0-<region>.pooler.supabase.com` (IPv4-compatible)
- Direct connection `db.*.supabase.co` is IPv6-only and incompatible with Railway
