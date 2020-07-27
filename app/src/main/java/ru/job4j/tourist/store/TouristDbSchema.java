package ru.job4j.tourist.store;

public class TouristDbSchema {
    public static final class TouristTable {
        public static final String NAME = "tourist";

        public static final class Cols {
            public static final String LATITUDE = "latitude";
            public static final String LONGITUDE = "longitude";
            public static final String TITLE = "title";
        }
    }
}
