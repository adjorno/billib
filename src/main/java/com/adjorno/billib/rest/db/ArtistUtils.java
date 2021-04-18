package com.adjorno.billib.rest.db;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArtistUtils {
    public static final Pattern PATTERN_DUET_WITH = Pattern.compile("\\([D|d]uet [W|w]ith (.+)\\)");
    public static final Pattern PATTERN_WITH = Pattern.compile("\\([W|w]ith (.+)\\)");
    public static final Pattern PATTERN_FEATURING = Pattern.compile("\\([F|f]eaturing (.+)\\)");

    public static boolean equalsEasy(String a1, String a2, ArtistRepository artistRepository,
            DuplicateArtistRepository duplicateArtistRepository) {
        return a1.equalsIgnoreCase(a2);
    }

    public static boolean equals(String a1, String a2, ArtistRepository artistRepository,
            DuplicateArtistRepository duplicateArtistRepository) {
        String[] aa1 = artistAlternatives(a1, artistRepository, duplicateArtistRepository);
        String[] aa2 = artistAlternatives(a2, artistRepository, duplicateArtistRepository);
        for (int i = 0; i < aa1.length; i++) {
            for (int j = 0; j < aa2.length; j++) {
                if (aa1[i].equalsIgnoreCase(aa2[j])) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String[] artistAlternatives(String artistName, ArtistRepository artistRepository,
            DuplicateArtistRepository duplicateArtistRepository) {
        List<String> theResult = new ArrayList<>();
        Artist theArtist = artistRepository.findByName(artistName);
        theResult.add(artistName);
        if (theArtist != null) {
            List<DuplicateArtist> theDuplicates = duplicateArtistRepository.findByArtist(theArtist);
            for (DuplicateArtist theDuplicateArtist : theDuplicates) {
                theResult.add(theDuplicateArtist.getDuplicateName());
            }
        }
        return theResult.toArray(new String[0]);
    }

    public static String optimizeName(String original) {
        return optimizeFeaturing(optimizeWith(optimizeDuetWith(original.replaceAll("(?i) featuring ", " feat. ").
                replaceAll("(?i) with ", " & ").replaceAll("(?i) / ", " & "))));
    }

    public static String optimizeDuetWith(String artistName) {
        Matcher m = PATTERN_DUET_WITH.matcher(artistName);
        if (m.find()) {
            String duet = m.group(1);
            String toReplace = m.group();
            return artistName.replaceAll("\\(" + toReplace + "\\)", "& " + duet);
        }
        return artistName;
    }

    public static String optimizeWith(String artistName) {
        Matcher m = PATTERN_WITH.matcher(artistName);
        if (m.find()) {
            String duet = m.group(1);
            String toReplace = m.group();
            return artistName.replaceAll("\\(" + toReplace + "\\)", "& " + duet);
        }
        return artistName;
    }

    public static String optimizeFeaturing(String artistName) {
        Matcher m = PATTERN_FEATURING.matcher(artistName);
        if (m.find()) {
            String duet = m.group(1);
            String toReplace = m.group();
            return artistName.replaceAll("\\(" + toReplace + "\\)", "feat. " + duet);
        }
        return artistName;
    }

    public static String[] splitCollaboration(String artist) {
        return artist.toLowerCase()
                .split("( & )" + "|(, )" + "|( and )" + "|( vs\\.? )" + "|( feat\\.? )" + "|( y )" + "|( / )" +
                        "|( presents )" + "|( pres\\.? )" + "|( starr?ing )" + "|( introducing )" + "| ( \\+ )");
    }

    public static List<Artist> asSingleArtists(List<ArtistRelation> artistRelations) {
        return Collections.unmodifiableList(new AbstractList<Artist>() {
            @Override
            public Artist get(int index) {
                return artistRelations.get(index).getSingle();
            }

            @Override
            public int size() {
                return artistRelations.size();
            }
        });
    }

    public static List<Artist> asBandArtists(List<ArtistRelation> artistRelations) {
        return Collections.unmodifiableList(new AbstractList<Artist>() {
            @Override
            public Artist get(int index) {
                return artistRelations.get(index).getBand();
            }

            @Override
            public int size() {
                return artistRelations.size();
            }
        });
    }

    public static List<Long> asArtistIds(List<Artist> artists) {
        return Collections.unmodifiableList(new AbstractList<Long>() {
            @Override
            public Long get(int index) {
                return artists.get(index).getId();
            }

            @Override
            public int size() {
                return artists.size();
            }
        });
    }

    public static String getSearchQuery(String[] keywords, int offset, int limit, boolean alphabetical) {
        final StringBuilder theQueryBuilder = new StringBuilder("SELECT ARTIST._ID, ARTIST.NAME FROM ARTIST");
        if (!alphabetical) {
            theQueryBuilder.append(" JOIN GLOBAL_RANK_ARTIST ON ARTIST._ID = GLOBAL_RANK_ARTIST.ARTIST_ID");
        }
        boolean where = false;
        for (String keyWord : keywords) {
            if (!where) {
                theQueryBuilder.append(" WHERE");
                where = true;
            } else {
                theQueryBuilder.append(" AND");
            }
            theQueryBuilder.append(" ARTIST.NAME LIKE '%").append(keyWord.replaceAll("'", "''")).append("%'");
        }
        theQueryBuilder.append(" ORDER BY ").append(alphabetical
                ? "ARTIST.NAME ASC"
                : "GLOBAL_RANK_ARTIST._RANK ASC");
        if (limit != 0) {
            theQueryBuilder.append(" LIMIT ").append(offset).append(", ").append(limit);
        }
        return theQueryBuilder.toString();
    }

    public static String getCountSearchQuery(String[] keywords) {
        final StringBuilder theQueryBuilder = new StringBuilder("SELECT COUNT(*) FROM ARTIST");
        boolean where = false;
        for (String keyWord : keywords) {
            if (!where) {
                theQueryBuilder.append(" WHERE");
                where = true;
            } else {
                theQueryBuilder.append(" AND");
            }
            theQueryBuilder.append(" ARTIST.NAME LIKE '%").append(keyWord.replaceAll("'", "''")).append("%'");
        }
        return theQueryBuilder.toString();
    }

    public static List<Artist> asArtists(List<Track> tracks) {
        return Collections.unmodifiableList(new AbstractList<Artist>() {
            @Override
            public Artist get(int index) {
                return tracks.get(index).getArtist();
            }

            @Override
            public int size() {
                return tracks.size();
            }
        });
    }
}
