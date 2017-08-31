package com.adjorno.billib.rest.db;

import com.m14n.ex.Ex;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

public class TrackUtils {

    public static void updateTrackExt(Track track, TrackCoverRepository trackCoverRepository,
            SpotifyUrlRepository spotifyUrlRepository) {
        updateTrackWithTrackCover(track, trackCoverRepository);
        updateTrackWithSpotifyUrl(track, spotifyUrlRepository);
    }

    public static void updateTracksExt(List<Track> tracks, TrackCoverRepository trackCoverRepository,
            SpotifyUrlRepository spotifyUrlRepository) {
        updateTracksWithTrackCover(tracks, trackCoverRepository);
        updateTracksWithSpotifyUrl(tracks, spotifyUrlRepository);
    }

    private static void updateTracksWithTrackCover(List<Track> tracks, TrackCoverRepository trackCoverRepository) {
        if (Ex.isNotEmpty(tracks)) {
            final List<TrackCover> covers = trackCoverRepository.findCoversByTrackIds(asTrackIds(tracks));
            for (TrackCover theTrackCover : covers) {
                for (Track theTrack : tracks) {
                    if (theTrack.getId().equals(theTrackCover.getTrackId())) {
                        theTrack.setCoverUrl(theTrackCover.getCoverUrl());
                        break;
                    }
                }
            }
        }
    }

    private static void updateTrackWithTrackCover(Track track, TrackCoverRepository trackCoverRepository) {
        TrackCover theCover = trackCoverRepository.findOne(track.getId());
        if (theCover != null) {
            track.setCoverUrl(theCover.getCoverUrl());
        }
    }

    public static void updateTracksWithSpotifyUrl(List<Track> tracks, SpotifyUrlRepository spotifyUrlRepository) {
        if (Ex.isNotEmpty(tracks)) {
            final List<SpotifyUrl> theSpotifyUrls = spotifyUrlRepository.findCoversByTrackIds(asTrackIds(tracks));
            for (SpotifyUrl theSpotifyUrl : theSpotifyUrls) {
                for (Track theTrack : tracks) {
                    if (theTrack.getId().equals(theSpotifyUrl.getTrackId())) {
                        theTrack.setSpotifyUrl(theSpotifyUrl.getSpotifyUrl());
                        break;
                    }
                }
            }
        }
    }

    public static void updateTrackWithSpotifyUrl(Track track, SpotifyUrlRepository spotifyUrlRepository) {
        final SpotifyUrl theSpotifyUrl = spotifyUrlRepository.findOne(track.getId());
        if (theSpotifyUrl != null) {
            track.setSpotifyUrl(theSpotifyUrl.getSpotifyUrl());
        }
    }

    public static List<Track> asTracks(List<ChartTrack> tracks) {
        return Collections.unmodifiableList(new AbstractList<Track>() {
            @Override
            public Track get(int index) {
                return tracks.get(index).getTrack();
            }

            @Override
            public int size() {
                return tracks.size();
            }
        });
    }

    public static List<Track> asTrackList(List<TrendTrack> trendTracks) {
        return Collections.unmodifiableList(new AbstractList<Track>() {
            @Override
            public Track get(int index) {
                return trendTracks.get(index).getTrack();
            }

            @Override
            public int size() {
                return trendTracks.size();
            }
        });
    }

    public static List<Long> asTrackIds(List<Track> tracks) {
        return Collections.unmodifiableList(new AbstractList<Long>() {
            @Override
            public Long get(int index) {
                return tracks.get(index).getId();
            }

            @Override
            public int size() {
                return tracks.size();
            }
        });
    }

    public static String getBestTracksQuery(long chartId, int limit, String from, String to) {
        final StringBuilder theQueryBuilder = new StringBuilder(
                "SELECT TRACK._ID, TRACK.TITLE, TRACK.ARTIST_ID, ARTIST._ID, ARTIST.NAME FROM CHART_LIST " +
                        "INNER JOIN CHART_TRACK ON CHART_LIST._ID = CHART_TRACK.CHART_LIST_ID " +
                        "INNER JOIN CHART ON CHART._ID = CHART_LIST.CHART_ID " +
                        "INNER JOIN TRACK ON TRACK._ID = CHART_TRACK.TRACK_ID " +
                        "INNER JOIN WEEK ON WEEK.WEEK_ID = CHART_LIST.WEEK_ID " +
                        "INNER JOIN ARTIST ON ARTIST._ID = TRACK.ARTIST_ID " + "WHERE CHART_ID = " + chartId + " ");
        if (!Ex.isEmpty(from)) {
            theQueryBuilder.append("AND date(WEEK.DATE) > date('").append(from).append("') ");
        }
        if (!Ex.isEmpty(to)) {
            theQueryBuilder.append("AND date(WEEK.DATE) < date('").append(to).append("') ");
        }
        theQueryBuilder.append("GROUP BY TRACK_ID ORDER BY SUM((LIST_SIZE + 1 - RANK) * (LIST_SIZE + 1 - RANK)) DESC ");
        theQueryBuilder.append("LIMIT ").append(limit);

        return theQueryBuilder.toString();
    }


    public static String getSearchQuery(String[] keywords, int offset, int limit, boolean alphabetical) {
        final StringBuilder theQueryBuilder = new StringBuilder(
                "SELECT TRACK._ID, TRACK.TITLE, TRACK.ARTIST_ID, ARTIST._ID, ARTIST.NAME FROM TRACK" +
                        " JOIN ARTIST ON ARTIST._ID = TRACK.ARTIST_ID");
        if (!alphabetical) {
            theQueryBuilder.append(" JOIN GLOBAL_RANK_TRACK ON TRACK._ID = GLOBAL_RANK_TRACK.TRACK_ID");
        }
        boolean where = false;
        for (String keyWord : keywords) {
            if (!where) {
                theQueryBuilder.append(" WHERE");
                where = true;
            } else {
                theQueryBuilder.append(" AND");
            }
            theQueryBuilder.append(" (ARTIST.NAME LIKE '%").append(keyWord.replaceAll("'", "''")).append("%'").
                    append(" OR TRACK.TITLE LIKE '%").append(keyWord.replaceAll("'", "''")).append("%')");
        }
        theQueryBuilder.append(" ORDER BY ").append(alphabetical
                ? "ARTIST.NAME ASC, TRACK.TITLE ASC"
                : "GLOBAL_RANK_TRACK.RANK ASC");
        if (limit != 0) {
            theQueryBuilder.append(" LIMIT ").append(offset).append(", ").append(limit);
        }
        return theQueryBuilder.toString();
    }

    public static String getCountSearchQuery(String[] keywords) {
        final StringBuilder theQueryBuilder = new StringBuilder(
                "SELECT COUNT(TRACK._ID) FROM TRACK" +
                        " JOIN ARTIST ON ARTIST._ID = TRACK.ARTIST_ID");
        boolean where = false;
        for (String keyWord : keywords) {
            if (!where) {
                theQueryBuilder.append(" WHERE");
                where = true;
            } else {
                theQueryBuilder.append(" AND");
            }
            theQueryBuilder.append(" (ARTIST.NAME LIKE '%").append(keyWord.replaceAll("'", "''")).append("%'").
                    append(" OR TRACK.TITLE LIKE '%").append(keyWord.replaceAll("'", "''")).append("%')");
        }
        return theQueryBuilder.toString();
    }

    public static Track findTrack(List<Track> tracks, Long trackId) {
        for (Track theTrack : tracks) {
            if (theTrack.getId().equals(trackId)) {
                return theTrack;
            }
        }
        return null;
    }
}
