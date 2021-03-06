package ru.job4j.tourist.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ru.job4j.tourist.Mark;

public class SQLStoreMarks extends SQLiteOpenHelper {
    public static final String DB = "tourist_marks.db";
    public static final int VERSION = 1;
    private List<Mark> mMarks = new ArrayList<>();
    private static SQLStoreMarks INST;

    private SQLStoreMarks(@Nullable Context context) {
        super(context, DB, null , VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + MarkDbSchema.TouristMarksTable.NAME + " (" +
                        "id integer primary key autoincrement, " +
                        MarkDbSchema.TouristMarksTable.Cols.LATITUDE + " real, " +
                        MarkDbSchema.TouristMarksTable.Cols.LONGITUDE + " real, " +
                        MarkDbSchema.TouristMarksTable.Cols.TITLE + " text " +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public static SQLStoreMarks getInstance(Context context) {
        if (INST == null){
            INST = new SQLStoreMarks(context);
            INST.updateStore();
        }
        return INST;
    }

    private void updateStore (){
        Cursor cursor = this.getWritableDatabase().query(
                MarkDbSchema.TouristMarksTable.NAME,
                null,
                null, null,
                null, null, null
        );
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            this.mMarks.add(new Mark(
                    cursor.getInt(cursor.getColumnIndex("id")),
                    cursor.getDouble(cursor.getColumnIndex(MarkDbSchema.TouristMarksTable.Cols.LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(MarkDbSchema.TouristMarksTable.Cols.LONGITUDE)),
                    cursor.getString(cursor.getColumnIndex(MarkDbSchema.TouristMarksTable.Cols.TITLE))
            ));
            cursor.moveToNext();
        }
        cursor.close();
    }

    public List<Mark> getMarks() {
        return this.mMarks;
    }

    public void addMark(Mark mark) {
        ContentValues value = new ContentValues();
        value.put(MarkDbSchema.TouristMarksTable.Cols.LATITUDE, mark.getLatitude());
        value.put(MarkDbSchema.TouristMarksTable.Cols.LONGITUDE, mark.getLongitude());
        value.put(MarkDbSchema.TouristMarksTable.Cols.TITLE, mark.getTitle());
        int id = (int) this.getWritableDatabase().insert(MarkDbSchema.TouristMarksTable.NAME, null, value);
        mark.setId(id);
        this.mMarks.add(mark);
    }

    public Mark findMarkByID(int id) {
        return mMarks.get(getPositionOfTaskById(id));
    }

    public int getPositionOfTaskById(int id) {
        for (int index = 0 ; index < mMarks.size(); index ++) {
            if (mMarks.get(index).getId() == id) {
                return index;
            }
        }
        return -1;
    }
}