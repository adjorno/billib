-- ============================================================================
-- Billboard Charts Database Schema - Simplified (No Partitioning)
-- ============================================================================

-- Enable required PostgreSQL extensions
CREATE EXTENSION IF NOT EXISTS pg_trgm;  -- Trigram similarity for full-text search
CREATE EXTENSION IF NOT EXISTS btree_gin;  -- For multi-column GIN indexes

-- ============================================================================
-- Core Dimension Tables
-- ============================================================================

-- JOURNAL: Billboard publication sources
CREATE TABLE JOURNAL (
    _id BIGSERIAL PRIMARY KEY,
    NAME VARCHAR(255) NOT NULL UNIQUE
);

-- CHART: Chart types (Hot 100, Country, etc.)
CREATE TABLE CHART (
    _id BIGSERIAL PRIMARY KEY,
    NAME VARCHAR(255) NOT NULL,
    JOURNAL_ID BIGINT NOT NULL REFERENCES JOURNAL(_id),
    LIST_SIZE INTEGER NOT NULL,
    START_DATE DATE NOT NULL,
    END_DATE DATE,
    UNIQUE(NAME, JOURNAL_ID)
);

CREATE INDEX idx_chart_journal ON CHART(JOURNAL_ID);

-- WEEK: Chart weeks (dates)
CREATE TABLE WEEK (
    WEEK_ID BIGSERIAL PRIMARY KEY,
    DATE VARCHAR(20) NOT NULL UNIQUE
);

CREATE INDEX idx_week_date ON WEEK(DATE);

-- ARTIST: Music artists
CREATE TABLE ARTIST (
    _id BIGSERIAL PRIMARY KEY,
    NAME VARCHAR(500) NOT NULL UNIQUE,
    NAME_NORMALIZED VARCHAR(500) GENERATED ALWAYS AS (LOWER(NAME)) STORED
);

CREATE INDEX idx_artist_name ON ARTIST(NAME);
CREATE INDEX idx_artist_name_normalized ON ARTIST(NAME_NORMALIZED);
CREATE INDEX idx_artist_name_trgm ON ARTIST USING gin(NAME gin_trgm_ops);

-- TRACK: Songs/tracks with denormalized artist name
CREATE TABLE TRACK (
    _id BIGSERIAL PRIMARY KEY,
    TITLE VARCHAR(500) NOT NULL,
    ARTIST_ID BIGINT NOT NULL REFERENCES ARTIST(_id),

    -- Denormalized artist name for faster queries
    ARTIST_NAME VARCHAR(500),  -- Populated during import

    -- Computed metrics (populated post-import)
    FIRST_CHART_DATE DATE,      -- First appearance on any chart
    PEAK_GLOBAL_RANK INTEGER,   -- Best rank across all charts
    TOTAL_WEEKS_ON_CHART INTEGER DEFAULT 0,  -- Total appearances
    UNIQUE(TITLE, ARTIST_ID)
);

CREATE INDEX idx_track_artist ON TRACK(ARTIST_ID);
CREATE INDEX idx_track_title ON TRACK(TITLE);
CREATE INDEX idx_track_artist_name ON TRACK(ARTIST_NAME);
CREATE INDEX idx_track_title_trgm ON TRACK USING gin(TITLE gin_trgm_ops);
CREATE INDEX idx_track_first_chart_date ON TRACK(FIRST_CHART_DATE);

-- ============================================================================
-- Chart Instance Table
-- ============================================================================

-- CHART_LIST: Specific chart instances (e.g., "Hot 100 for week of 2024-01-06")
CREATE TABLE CHART_LIST (
    _id BIGSERIAL PRIMARY KEY,
    CHART_ID BIGINT NOT NULL REFERENCES CHART(_id),
    WEEK_ID BIGINT NOT NULL REFERENCES WEEK(WEEK_ID),
    NUMBER INTEGER,
    PREVIOUS_CHART_LIST_ID BIGINT,
    UNIQUE(CHART_ID, WEEK_ID)
);

CREATE INDEX idx_chart_list_chart ON CHART_LIST(CHART_ID);
CREATE INDEX idx_chart_list_week ON CHART_LIST(WEEK_ID);
CREATE INDEX idx_chart_list_prev ON CHART_LIST(PREVIOUS_CHART_LIST_ID);

-- ============================================================================
-- Fact Table: Chart Track Positions (NO PARTITIONING)
-- ============================================================================

-- CHART_TRACK_POSITION: Denormalized fact table for all chart positions
CREATE TABLE CHART_TRACK_POSITION (
    _id BIGSERIAL PRIMARY KEY,
    TRACK_ID BIGINT NOT NULL,
    CHART_LIST_ID BIGINT NOT NULL,
    _RANK INTEGER NOT NULL,
    LAST_WEEK_RANK INTEGER DEFAULT 0,

    -- Denormalized dimensions for query performance
    WEEK_DATE DATE NOT NULL,
    CHART_ID BIGINT NOT NULL,
    ARTIST_ID BIGINT NOT NULL,
    TRACK_TITLE VARCHAR(500) NOT NULL,
    ARTIST_NAME VARCHAR(500) NOT NULL,

    -- Generated computed columns
    RANK_CHANGE INTEGER GENERATED ALWAYS AS (LAST_WEEK_RANK - _RANK) STORED,
    IS_DEBUT BOOLEAN GENERATED ALWAYS AS (LAST_WEEK_RANK = 0) STORED,

    UNIQUE(TRACK_ID, CHART_LIST_ID)
);

-- Indexes for efficient querying
CREATE INDEX idx_ctp_track_date ON CHART_TRACK_POSITION(TRACK_ID, WEEK_DATE DESC);
CREATE INDEX idx_ctp_chart_week ON CHART_TRACK_POSITION(CHART_LIST_ID, _RANK);
CREATE INDEX idx_ctp_artist_date ON CHART_TRACK_POSITION(ARTIST_ID, WEEK_DATE DESC);
CREATE INDEX idx_ctp_week_date ON CHART_TRACK_POSITION(WEEK_DATE);
CREATE INDEX idx_ctp_is_debut ON CHART_TRACK_POSITION(IS_DEBUT) WHERE IS_DEBUT = true;
CREATE INDEX idx_ctp_chart_week_date ON CHART_TRACK_POSITION(CHART_ID, WEEK_DATE DESC);

-- ============================================================================
-- Legacy CHART_TRACK Table (for backwards compatibility)
-- ============================================================================

CREATE TABLE CHART_TRACK (
    _id BIGSERIAL PRIMARY KEY,
    TRACK_ID BIGINT NOT NULL REFERENCES TRACK(_id),
    CHART_LIST_ID BIGINT NOT NULL REFERENCES CHART_LIST(_id),
    _RANK INTEGER NOT NULL,
    LAST_WEEK_RANK INTEGER DEFAULT 0,
    UNIQUE(TRACK_ID, CHART_LIST_ID)
);

CREATE INDEX idx_chart_track_track ON CHART_TRACK(TRACK_ID);
CREATE INDEX idx_chart_track_chart_list ON CHART_TRACK(CHART_LIST_ID);
CREATE INDEX idx_chart_track_rank ON CHART_TRACK(_RANK);

-- ============================================================================
-- Supporting Tables
-- ============================================================================

-- ARTIST_RELATION: Tracks artist collaborations
CREATE TABLE ARTIST_RELATION (
    _id BIGSERIAL PRIMARY KEY,
    ARTIST_ID_1 BIGINT NOT NULL REFERENCES ARTIST(_id),
    ARTIST_ID_2 BIGINT NOT NULL REFERENCES ARTIST(_id),
    TRACK_ID BIGINT NOT NULL REFERENCES TRACK(_id),
    UNIQUE(ARTIST_ID_1, ARTIST_ID_2, TRACK_ID),
    CHECK (ARTIST_ID_1 < ARTIST_ID_2)
);

CREATE INDEX idx_artist_rel_artist1 ON ARTIST_RELATION(ARTIST_ID_1);
CREATE INDEX idx_artist_rel_artist2 ON ARTIST_RELATION(ARTIST_ID_2);
CREATE INDEX idx_artist_rel_track ON ARTIST_RELATION(TRACK_ID);

-- DAY_TRACK: Daily track selections
CREATE TABLE DAY_TRACK (
    _id BIGSERIAL PRIMARY KEY,
    TRACK_ID BIGINT NOT NULL REFERENCES TRACK(_id),
    DATE DATE NOT NULL,
    UNIQUE(TRACK_ID, DATE)
);

CREATE INDEX idx_day_track_date ON DAY_TRACK(DATE);

-- TREND_TYPE: Types of trending categories
CREATE TABLE TREND_TYPE (
    _id BIGSERIAL PRIMARY KEY,
    NAME VARCHAR(255) NOT NULL UNIQUE
);

-- TREND_TRACK: Trending tracks
CREATE TABLE TREND_TRACK (
    _id BIGSERIAL PRIMARY KEY,
    TRACK_ID BIGINT NOT NULL REFERENCES TRACK(_id),
    TREND_TYPE_ID BIGINT NOT NULL REFERENCES TREND_TYPE(_id),
    DATE DATE NOT NULL,
    UNIQUE(TRACK_ID, TREND_TYPE_ID, DATE)
);

CREATE INDEX idx_trend_track_date ON TREND_TRACK(DATE);
CREATE INDEX idx_trend_track_type ON TREND_TRACK(TREND_TYPE_ID);

-- DUPLICATE_TRACK and DUPLICATE_ARTIST: Track potential duplicates
CREATE TABLE DUPLICATE_TRACK (
    _id BIGSERIAL PRIMARY KEY,
    TRACK_ID_1 BIGINT NOT NULL REFERENCES TRACK(_id),
    TRACK_ID_2 BIGINT NOT NULL REFERENCES TRACK(_id),
    UNIQUE(TRACK_ID_1, TRACK_ID_2),
    CHECK (TRACK_ID_1 < TRACK_ID_2)
);

CREATE TABLE DUPLICATE_ARTIST (
    _id BIGSERIAL PRIMARY KEY,
    ARTIST_ID_1 BIGINT NOT NULL REFERENCES ARTIST(_id),
    ARTIST_ID_2 BIGINT NOT NULL REFERENCES ARTIST(_id),
    UNIQUE(ARTIST_ID_1, ARTIST_ID_2),
    CHECK (ARTIST_ID_1 < ARTIST_ID_2)
);

-- ============================================================================
-- Materialized Views for Global Rankings
-- ============================================================================

-- Placeholder - will be populated after data import
-- These will be created by: REFRESH MATERIALIZED VIEW <name>

CREATE MATERIALIZED VIEW GLOBAL_RANK_TRACK AS
SELECT
    TRACK_ID,
    ROW_NUMBER() OVER (
        ORDER BY SUM((c.LIST_SIZE - ctp._RANK) * (c.LIST_SIZE - ctp._RANK)) DESC
    ) as RANK,
    COUNT(*) as TOTAL_APPEARANCES,
    MIN(ctp._RANK) as PEAK_POSITION,
    MIN(ctp.WEEK_DATE) as FIRST_APPEARANCE,
    MAX(ctp.WEEK_DATE) as LAST_APPEARANCE
FROM CHART_TRACK_POSITION ctp
JOIN CHART c ON c._id = ctp.CHART_ID
GROUP BY TRACK_ID;

CREATE UNIQUE INDEX idx_gtr_track ON GLOBAL_RANK_TRACK(TRACK_ID);
CREATE INDEX idx_gtr_rank ON GLOBAL_RANK_TRACK(RANK);

CREATE MATERIALIZED VIEW GLOBAL_RANK_ARTIST AS
SELECT
    ARTIST_ID,
    ROW_NUMBER() OVER (
        ORDER BY SUM((c.LIST_SIZE - ctp._RANK) * (c.LIST_SIZE - ctp._RANK)) DESC
    ) as RANK,
    COUNT(DISTINCT ctp.TRACK_ID) as TOTAL_TRACKS,
    COUNT(*) as TOTAL_APPEARANCES,
    MIN(ctp._RANK) as PEAK_POSITION,
    MIN(ctp.WEEK_DATE) as FIRST_APPEARANCE,
    MAX(ctp.WEEK_DATE) as LAST_APPEARANCE
FROM CHART_TRACK_POSITION ctp
JOIN CHART c ON c._id = ctp.CHART_ID
GROUP BY ARTIST_ID;

CREATE UNIQUE INDEX idx_gra_artist ON GLOBAL_RANK_ARTIST(ARTIST_ID);
CREATE INDEX idx_gra_rank ON GLOBAL_RANK_ARTIST(RANK);

-- ============================================================================
-- Helper Functions
-- ============================================================================

-- Function to refresh all materialized views
CREATE OR REPLACE FUNCTION refresh_global_rankings() RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY GLOBAL_RANK_TRACK;
    REFRESH MATERIALIZED VIEW CONCURRENTLY GLOBAL_RANK_ARTIST;
END;
$$ LANGUAGE plpgsql;

-- Initialize Billboard journal
INSERT INTO JOURNAL (_id, NAME) VALUES (1, 'Billboard') ON CONFLICT DO NOTHING;

-- Add helpful comments
COMMENT ON TABLE CHART_TRACK_POSITION IS 'Denormalized fact table for all chart positions across all charts and weeks';
COMMENT ON TABLE CHART_TRACK IS 'Legacy table maintained for backwards compatibility';
COMMENT ON MATERIALIZED VIEW GLOBAL_RANK_TRACK IS 'Pre-computed global track rankings across all charts, refresh after data updates';
COMMENT ON MATERIALIZED VIEW GLOBAL_RANK_ARTIST IS 'Pre-computed global artist rankings across all charts, refresh after data updates';
