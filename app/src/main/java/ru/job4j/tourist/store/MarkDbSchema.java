package ru.job4j.tourist.store;

public class MarkDbSchema {
    public static final class TouristMarksTable {
        public static final String NAME = "tourist_marks";

        public static final class Cols {
            public static final String LATITUDE = "latitude";
            public static final String LONGITUDE = "longitude";
            public static final String TITLE = "title";
        }
    }
}
