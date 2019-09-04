package com.example.habitstracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "DBhabits5";

    public static final int DATABASE_VERSION = 1;

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_HABITS_TABLE = "create table " + HabitContract.Habits.TABLE_NAME +" (" +
                HabitContract.Habits._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                HabitContract.Habits.NAME + " TEXT NOT NULL, " +
                HabitContract.Habits.MOTIVATION + " TEXT, " +
                HabitContract.Habits.START_DATE + " TEXT NOT NULL, " +
                HabitContract.Habits.END_DATE + " TEXT NOT NULL, " +
                HabitContract.Habits.REMINDER_TIME + " TEXT, " +
                HabitContract.Habits.DAYS_WEEK + " TEXT, " +
                HabitContract.Habits.PROGRESS + " INTEGER, " +
                HabitContract.Habits.NUMBER_DAYS_MARKED + " INTEGER " +
                ");";
        String SQL_CREATE_DAYS_PROGRESS_TABLE = "create table " + HabitContract.DaysProgress.TABLE_NAME +" (" +
                HabitContract.DaysProgress._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                HabitContract.DaysProgress.DATE_MARKED + " TEXT NOT NULL, " +
                HabitContract.DaysProgress.DATA_PROGRESS + " INTEGER, " +
                HabitContract.DaysProgress.HABIT_ID + " integer not null,"
                + "foreign key "
                + "(" +  HabitContract.DaysProgress.HABIT_ID + ")" + " references "
                + HabitContract.Habits.TABLE_NAME+ "(" + HabitContract.Habits._ID + " ));";

        db.execSQL(SQL_CREATE_HABITS_TABLE);
        db.execSQL(SQL_CREATE_DAYS_PROGRESS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
