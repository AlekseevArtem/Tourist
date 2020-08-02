package ru.job4j.tourist.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ru.job4j.tourist.Track;

public class MemStoreTrack {
    private Track track;
    private static MemStoreTrack INST;

    private MemStoreTrack(Track track) {
        this.track = track;
    }

    public static MemStoreTrack getInstance(Track track) {
        if (INST == null){
            INST = new MemStoreTrack(track);
        }
        return INST;
    }


    public Track getTrack() {
        return track;
    }
}
