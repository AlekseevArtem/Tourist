package ru.job4j.tourist;

public class Mark {
    private int id;
    private double latitude;
    private double longitude;
    private String title;

    public Mark(int id, double latitude, double longitude, String title) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
    }

    public Mark(double latitude, double longitude, String title) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
