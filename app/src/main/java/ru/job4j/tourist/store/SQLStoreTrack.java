package ru.job4j.tourist.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import ru.job4j.tourist.Mark;
import ru.job4j.tourist.Track;

public class SQLStoreTrack extends SQLiteOpenHelper {
    public static final String DB = "tourist_tracks.db";
    public static final int VERSION = 1;
    private List<Track> mTracks = new ArrayList<>();
    private static SQLStoreTrack INST;

    private SQLStoreTrack(@Nullable Context context) {
        super(context, DB, null , VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + TrackDbSchema.TrackTable.NAME + " (" +
                        "id integer primary key autoincrement, " +
                        TrackDbSchema.TrackTable.Cols.NAME + " string, " +
                        TrackDbSchema.TrackTable.Cols.COLOR + " integer, " +
                        TrackDbSchema.TrackTable.Cols.WIDTH + " integer " +
                        ")"
        );

        db.execSQL(
                "create table " + TrackDbSchema.CoordinatesTable.NAME + " (" +
                        "id integer primary key autoincrement, " +
                        TrackDbSchema.CoordinatesTable.Cols.TRACK_ID + " integer, " +
                        TrackDbSchema.CoordinatesTable.Cols.LATITUDE + " real, " +
                        TrackDbSchema.CoordinatesTable.Cols.LONGITUDE + " real " +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static SQLStoreTrack getInstance(Context context) {
        if (INST == null){
            INST = new SQLStoreTrack(context);
            INST.updateStore();
        }
        return INST;
    }

    private void updateStore (){
        Cursor cursor = this.getWritableDatabase().query(
                TrackDbSchema.TrackTable.NAME,
                null,
                null, null,
                null, null, null
        );
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            this.mTracks.add(new Track(
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getString(cursor.getColumnIndex(TrackDbSchema.TrackTable.Cols.NAME)),
                    cursor.getInt(cursor.getColumnIndex(TrackDbSchema.TrackTable.Cols.COLOR)),
                    cursor.getInt(cursor.getColumnIndex(TrackDbSchema.TrackTable.Cols.WIDTH)),
                    getCoordinates(cursor.getInt(cursor.getColumnIndex("id")))
            ));
            cursor.moveToNext();
        }
        cursor.close();
    }

    private List<LatLng> getCoordinates(int TrackID) {
        List<LatLng> coordinates = new ArrayList<>();
        Cursor cursor = this.getWritableDatabase().query(
                TrackDbSchema.CoordinatesTable.NAME,
                null,
                TrackDbSchema.CoordinatesTable.Cols.TRACK_ID + " = ?",
                new String[]{String.valueOf(TrackID)},
                null, null, null
        );
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            coordinates.add(new LatLng(
                    cursor.getDouble(cursor.getColumnIndex(TrackDbSchema.CoordinatesTable.Cols.LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(TrackDbSchema.CoordinatesTable.Cols.LONGITUDE))
            ));
            cursor.moveToNext();
        }
        cursor.close();
        return coordinates;
    }

    public List<Track> getTracks() {
        return this.mTracks;
    }

    public void addTrack(Track track) {
        ContentValues value = new ContentValues();
        value.put(TrackDbSchema.TrackTable.Cols.NAME, track.getName());
        value.put(TrackDbSchema.TrackTable.Cols.COLOR, track.getColor());
        value.put(TrackDbSchema.TrackTable.Cols.WIDTH, track.getWidth());
        int id = (int) this.getWritableDatabase().insert(TrackDbSchema.TrackTable.NAME, null, value);
        track.setId(id);
        track.getCoordinates().stream().forEach(coord -> {
            ContentValues valueCoord = new ContentValues();
            valueCoord.put(TrackDbSchema.CoordinatesTable.Cols.TRACK_ID, id);
            valueCoord.put(TrackDbSchema.CoordinatesTable.Cols.LATITUDE, coord.latitude);
            valueCoord.put(TrackDbSchema.CoordinatesTable.Cols.LONGITUDE, coord.longitude);
            this.getWritableDatabase().insert(TrackDbSchema.CoordinatesTable.NAME, null, valueCoord);
        });
        this.mTracks.add(track);
    }

    public Track findTrackByID(int id) {
        return mTracks.get(getPositionOfTrackById(id));
    }

    public int getPositionOfTrackById(int id) {
        for (int index = 0 ; index < mTracks.size(); index ++) {
            if (mTracks.get(index).getId() == id) {
                return index;
            }
        }
        return -1;
    }
}