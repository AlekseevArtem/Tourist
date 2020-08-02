package ru.job4j.tourist.store;

public class TrackDbSchema {
    public static final class TrackTable {
        public static final String NAME = "track";

        public static final class Cols {
            public static final String NAME = "name";
            public static final String COLOR = "color";
            public static final String WIDTH = "width";
        }
    }

    public static final class CoordinatesTable {
        public static final String NAME = "coordinates";

        public static final class Cols {
            public static final String TRACK_ID = "track_id";
            public static final String LATITUDE = "latitude";
            public static final String LONGITUDE = "longitude";
        }
    }
}
