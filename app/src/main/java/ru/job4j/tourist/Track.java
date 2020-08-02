package ru.job4j.tourist;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Track {
    private int id;
    private String name;
    private int color;
    private int width;
    private List<LatLng> coordinates;

    public Track(int id, String name, int color, int width, List<LatLng> coordinates) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.width = width;
        this.coordinates = coordinates;
    }

    public Track(String name, int color, int width, List<LatLng> coordinates) {
        this.name = name;
        this.color = color;
        this.width = width;
        this.coordinates = coordinates;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public List<LatLng> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<LatLng> coordinates) {
        this.coordinates = coordinates;
    }
}