-- Billboard Charts PostgreSQL Schema
-- Version 1: Initial schema with optimized denormalization and partitioning

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS pg_trgm;  -- For full-text search
CREATE EXTENSION IF NOT EXISTS btree_gin; -- For multi-column GIN indexes

-- ============================================================================
-- Core Dimension Tables
-- ============================================================================

-- JOURNAL: Billboard publication sources
CREATE TABLE JOURNAL (
    _id BIGSERIAL PRIMARY KEY,
    NAME VARCHAR(255) NOT NULL UNIQUE
);

-- CHART: Chart types (Hot 100, Billboard 200, etc.)
CREATE TABLE CHART (
    _id BIGSERIAL PRIMARY KEY,
    NAME VARCHAR(255) NOT NULL,
    JOURNAL_ID BIGINT REFERENCES JOURNAL(_id),
    LIST_SIZE INTEGER,
    START_DATE VARCHAR(20),
    END_DATE VARCHAR(20)
);

CREATE INDEX idx_chart_journal ON CHART(JOURNAL_ID);

-- WEEK: Week identifiers for chart instances
CREATE TABLE WEEK (
    WEEK_ID BIGSERIAL PRIMARY KEY,
    DATE VARCHAR(20) NOT NULL UNIQUE
);

CREATE INDEX idx_week_date ON WEEK(DATE);

-- CHART_LIST: Specific chart instance for a week
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

-- ARTIST: Music artists with normalized search field
CREATE TABLE ARTIST (
    _id BIGSERIAL PRIMARY KEY,
    NAME VARCHAR(500) NOT NULL,
    NAME_NORMALIZED VARCHAR(500) GENERATED ALWAYS AS (LOWER(TRIM(NAME))) STORED,
    UNIQUE(NAME)
);

CREATE INDEX idx_artist_name ON ARTIST(NAME);
CREATE INDEX idx_artist_name_normalized ON ARTIST(NAME_NORMALIZED);
CREATE INDEX idx_artist_name_trgm ON ARTIST USING gin(NAME gin_trgm_ops);

-- TRACK: Music tracks with denormalized artist info and computed metrics
CREATE TABLE TRACK (
    _id BIGSERIAL PRIMARY KEY,
    TITLE VARCHAR(500) NOT NULL,
    ARTIST_ID BIGINT NOT NULL REFERENCES ARTIST(_id),
    ARTIST_NAME VARCHAR(500),  -- Denormalized for search performance
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
-- Partitioned Fact Table: Chart Track Positions
-- ============================================================================

-- CHART_TRACK_POSITION: Denormalized fact table partitioned by year
-- This replaces CHART_TRACK with strategic denormalization for read performance
CREATE TABLE CHART_TRACK_POSITION (
    _id BIGSERIAL,
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

    PRIMARY KEY (_id, WEEK_DATE)
) PARTITION BY RANGE (WEEK_DATE);

-- Create year partitions (2010-2025)
CREATE TABLE CHART_TRACK_POSITION_Y2010 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2010-01-01') TO ('2011-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2011 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2011-01-01') TO ('2012-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2012 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2012-01-01') TO ('2013-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2013 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2013-01-01') TO ('2014-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2014 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2014-01-01') TO ('2015-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2015 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2015-01-01') TO ('2016-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2016 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2016-01-01') TO ('2017-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2017 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2017-01-01') TO ('2018-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2018 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2018-01-01') TO ('2019-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2019 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2019-01-01') TO ('2020-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2020 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2020-01-01') TO ('2021-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2021 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2021-01-01') TO ('2022-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2022 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2022-01-01') TO ('2023-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2023 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2023-01-01') TO ('2024-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2024 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2025 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');

CREATE TABLE CHART_TRACK_POSITION_Y2026 PARTITION OF CHART_TRACK_POSITION
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');

-- Indexes on partitioned table (applied to all partitions)
CREATE INDEX idx_ctp_track_date ON CHART_TRACK_POSITION(TRACK_ID, WEEK_DATE DESC);
CREATE INDEX idx_ctp_chart_list ON CHART_TRACK_POSITION(CHART_LIST_ID);
CREATE INDEX idx_ctp_chart_week ON CHART_TRACK_POSITION(CHART_ID, WEEK_DATE DESC);
CREATE INDEX idx_ctp_artist_date ON CHART_TRACK_POSITION(ARTIST_ID, WEEK_DATE DESC);
CREATE INDEX idx_ctp_debut ON CHART_TRACK_POSITION(WEEK_DATE, IS_DEBUT) WHERE IS_DEBUT = true;

-- ============================================================================
-- Legacy CHART_TRACK table (kept for backward compatibility)
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
CREATE INDEX idx_chart_track_list ON CHART_TRACK(CHART_LIST_ID);

-- ============================================================================
-- Supporting Tables
-- ============================================================================

-- ARTIST_RELATION: Artist collaborations and relationships
CREATE TABLE ARTIST_RELATION (
    _id BIGSERIAL PRIMARY KEY,
    ARTIST_ID BIGINT NOT NULL REFERENCES ARTIST(_id),
    RELATED_ARTIST_ID BIGINT NOT NULL REFERENCES ARTIST(_id),
    RELATION_TYPE VARCHAR(50),
    UNIQUE(ARTIST_ID, RELATED_ARTIST_ID)
);

CREATE INDEX idx_artist_rel_artist ON ARTIST_RELATION(ARTIST_ID);
CREATE INDEX idx_artist_rel_related ON ARTIST_RELATION(RELATED_ARTIST_ID);

-- DUPLICATE_ARTIST: Track duplicate artist names
CREATE TABLE DUPLICATE_ARTIST (
    _id BIGSERIAL PRIMARY KEY,
    NAME VARCHAR(500) NOT NULL
);

-- DUPLICATE_TRACK: Track duplicate track titles
CREATE TABLE DUPLICATE_TRACK (
    _id BIGSERIAL PRIMARY KEY,
    TITLE VARCHAR(500) NOT NULL
);

-- DAY_TRACK: Daily track statistics
CREATE TABLE DAY_TRACK (
    _id BIGSERIAL PRIMARY KEY,
    TRACK_ID BIGINT NOT NULL REFERENCES TRACK(_id),
    DATE DATE NOT NULL,
    PLAY_COUNT INTEGER DEFAULT 0,
    UNIQUE(TRACK_ID, DATE)
);

CREATE INDEX idx_day_track_track ON DAY_TRACK(TRACK_ID);
CREATE INDEX idx_day_track_date ON DAY_TRACK(DATE);

-- TREND_TYPE: Types of trending patterns
CREATE TABLE TREND_TYPE (
    _id BIGSERIAL PRIMARY KEY,
    NAME VARCHAR(100) NOT NULL UNIQUE
);

-- TREND_TRACK: Track trending analysis
CREATE TABLE TREND_TRACK (
    _id BIGSERIAL PRIMARY KEY,
    TRACK_ID BIGINT NOT NULL REFERENCES TRACK(_id),
    TREND_TYPE_ID BIGINT NOT NULL REFERENCES TREND_TYPE(_id),
    DATE DATE NOT NULL,
    SCORE DECIMAL(10,2),
    UNIQUE(TRACK_ID, TREND_TYPE_ID, DATE)
);

CREATE INDEX idx_trend_track_track ON TREND_TRACK(TRACK_ID);
CREATE INDEX idx_trend_track_type ON TREND_TRACK(TREND_TYPE_ID);
CREATE INDEX idx_trend_track_date ON TREND_TRACK(DATE);

-- ============================================================================
-- Materialized Views for Performance
-- ============================================================================

-- GLOBAL_RANK_TRACK: Pre-computed global rankings for tracks
-- Uses weighted scoring: better chart position + more weeks = higher rank
CREATE MATERIALIZED VIEW GLOBAL_RANK_TRACK AS
SELECT
    TRACK_ID,
    ROW_NUMBER() OVER (
        ORDER BY
            SUM((c.LIST_SIZE - ctp._RANK + 1) * (c.LIST_SIZE - ctp._RANK + 1)) DESC,
            COUNT(*) DESC
    ) as RANK,
    COUNT(*) as TOTAL_APPEARANCES,
    MIN(ctp._RANK) as PEAK_POSITION,
    MIN(ctp.WEEK_DATE) as FIRST_CHART_DATE,
    MAX(ctp.WEEK_DATE) as LAST_CHART_DATE
FROM CHART_TRACK_POSITION ctp
JOIN CHART_LIST cl ON cl._id = ctp.CHART_LIST_ID
JOIN CHART c ON c._id = cl.CHART_ID
GROUP BY TRACK_ID;

CREATE UNIQUE INDEX idx_grt_track ON GLOBAL_RANK_TRACK(TRACK_ID);
CREATE INDEX idx_grt_rank ON GLOBAL_RANK_TRACK(RANK);

-- GLOBAL_RANK_ARTIST: Pre-computed global rankings for artists
CREATE MATERIALIZED VIEW GLOBAL_RANK_ARTIST AS
SELECT
    ARTIST_ID,
    ROW_NUMBER() OVER (
        ORDER BY
            SUM((c.LIST_SIZE - ctp._RANK + 1) * (c.LIST_SIZE - ctp._RANK + 1)) DESC,
            COUNT(DISTINCT ctp.TRACK_ID) DESC,
            COUNT(*) DESC
    ) as RANK,
    COUNT(DISTINCT ctp.TRACK_ID) as UNIQUE_TRACKS,
    COUNT(*) as TOTAL_APPEARANCES,
    MIN(ctp._RANK) as PEAK_POSITION
FROM CHART_TRACK_POSITION ctp
JOIN CHART_LIST cl ON cl._id = ctp.CHART_LIST_ID
JOIN CHART c ON c._id = cl.CHART_ID
GROUP BY ARTIST_ID;

CREATE UNIQUE INDEX idx_gra_artist ON GLOBAL_RANK_ARTIST(ARTIST_ID);
CREATE INDEX idx_gra_rank ON GLOBAL_RANK_ARTIST(RANK);

-- ============================================================================
-- Functions and Triggers
-- ============================================================================

-- Function to refresh materialized views (call weekly after data import)
CREATE OR REPLACE FUNCTION refresh_global_rankings()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY GLOBAL_RANK_TRACK;
    REFRESH MATERIALIZED VIEW CONCURRENTLY GLOBAL_RANK_ARTIST;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- Initial Data
-- ============================================================================

-- Insert Billboard journal
INSERT INTO JOURNAL (_id, NAME) VALUES (1, 'Billboard') ON CONFLICT (NAME) DO NOTHING;

-- ============================================================================
-- Comments for Documentation
-- ============================================================================

COMMENT ON TABLE CHART_TRACK_POSITION IS 'Partitioned fact table with denormalized dimensions for optimal read performance';
COMMENT ON COLUMN CHART_TRACK_POSITION.RANK_CHANGE IS 'Generated column: change in rank from previous week (positive = moved up)';
COMMENT ON COLUMN CHART_TRACK_POSITION.IS_DEBUT IS 'Generated column: true if this is the tracks first appearance on this chart';
COMMENT ON TABLE GLOBAL_RANK_TRACK IS 'Materialized view with weighted global rankings, refresh weekly after import';
COMMENT ON TABLE GLOBAL_RANK_ARTIST IS 'Materialized view with weighted global artist rankings, refresh weekly after import';
