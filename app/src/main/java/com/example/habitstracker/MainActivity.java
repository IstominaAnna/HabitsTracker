package com.example.habitstracker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitstracker.data.DBHelper;
import com.example.habitstracker.data.HabitContract;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private ImageView check;
    private LinearLayout linearLayoutEmpty;
    List<MainHabit> habits;

    TextView textViewBestName;
    TextView textViewBestDays;


    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayoutEmpty = findViewById(R.id.layout_for_empty);

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        textViewBestName = findViewById(R.id.viewNameHabit);
        textViewBestDays = findViewById(R.id.viewDaysBest);
        check = findViewById(R.id.check);

        dbHelper = new DBHelper(this, DBHelper.DATABASE_NAME, null, 1);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HanitCreation.class);
                startActivity(intent);
            }
        });

        }

    @Override
    protected void onStart() {
        super.onStart();

        if(getHabits ()){
            mRecyclerView.setVisibility(View.VISIBLE);
            linearLayoutEmpty.setVisibility(View.GONE);
            DBAsyncTask ask = new DBAsyncTask();
            ask.execute();

        }
        else {
            mRecyclerView.setVisibility(View.GONE);
            linearLayoutEmpty.setVisibility(View.VISIBLE);
        }

        mAdapter = new MyRecyclerViewAdapter(habits);
        mRecyclerView.setAdapter(mAdapter);
     //   mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getHabits ()){
            mRecyclerView.setVisibility(View.VISIBLE);
            linearLayoutEmpty.setVisibility(View.GONE);
            DBAsyncTask ask = new DBAsyncTask();
            ask.execute();

        }
        else {
            mRecyclerView.setVisibility(View.GONE);
            linearLayoutEmpty.setVisibility(View.VISIBLE);
        }

        mAdapter = new MyRecyclerViewAdapter(habits);
        mRecyclerView.setAdapter(mAdapter);
    }

    class MainHabit {
        String name;
        int photoId;
        int star1Id;
        int star2Id;
        int star3Id;
        int star4Id;
        int star5Id;
        MainHabit(String name, int photoId, int star1Id, int star2Id, int star3Id, int star4Id, int star5Id) {
            this.name = name;
            this.photoId = photoId;
            this.star1Id =  star1Id;
            this.star2Id = star2Id;
            this.star3Id = star3Id;
            this.star4Id = star4Id;
            this.star5Id = star5Id;

        }
    }



    boolean getHabits () {


        List<String> outputList = new ArrayList<>();
         habits = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Date currentData = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy");
        String currentDataString = sdf.format(currentData);


        Cursor cursor = db.query(HabitContract.Habits.TABLE_NAME, new String[] {HabitContract.Habits._ID, HabitContract.Habits.NAME, HabitContract.Habits.PROGRESS, HabitContract.Habits.START_DATE, HabitContract.Habits.END_DATE, HabitContract.Habits.DAYS_WEEK}, null,
                null, null, null, null);

        cursor.moveToFirst();
        int nameColIndex = cursor.getColumnIndex(HabitContract.Habits.NAME);
        int idIndex = cursor.getColumnIndex(HabitContract.Habits._ID);
        int progressIndex = cursor.getColumnIndex(HabitContract.Habits.PROGRESS);
        int startIndex = cursor.getColumnIndex(HabitContract.Habits.START_DATE);
        int endIndex = cursor.getColumnIndex(HabitContract.Habits.END_DATE);
        int weekIndex = cursor.getColumnIndex(HabitContract.Habits.DAYS_WEEK);
        do {
                try {
                    int photo;
                    outputList.add(cursor.getString(nameColIndex));
                    String name = cursor.getString(nameColIndex);
                    int id = cursor.getInt(idIndex);
                    int progress = cursor.getInt(progressIndex);


                    SimpleDateFormat sdfk = new SimpleDateFormat("dd.MM.yyyy");
                    Calendar c = Calendar.getInstance();
                    Calendar a  = Calendar.getInstance();
                    c.setTime(sdfk.parse(cursor.getString(startIndex)));
                    a.setTime(sdfk.parse(cursor.getString(endIndex)));
                    String workoutDays = cursor.getString(weekIndex);
                    long days = 0;
                    if (workoutDays.equals("Каждый день")) {
                     //   c.add(Calendar.DATE, 1);
                        long diff = a.getTimeInMillis() - c.getTimeInMillis();

                        days = diff / (24 * 60 * 60 * 1000) ;

                    } else {

                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                    String weekDay = new String();
                    do {

                        weekDay = dayFormat.format(c.getTime());
                        if (workoutDays.indexOf(weekDay) != -1) {
                            days++;
                        }
                        c.add(Calendar.DATE, 1);
                    } while (c.getTime().compareTo(a.getTime()) < 0);
                    }
                    int numberStars = getStars(days, progress);

                    String selectQuery = "SELECT "+HabitContract.DaysProgress.DATE_MARKED + " FROM " + HabitContract.DaysProgress.TABLE_NAME+ " WHERE "
                            + HabitContract.DaysProgress.DATE_MARKED + " = '" + currentDataString +
                            "' and " + HabitContract.DaysProgress.HABIT_ID + " = '" + id + "' ;";
                    SQLiteStatement sqLiteStatement = db.compileStatement(selectQuery);

                    try {
                        long k = sqLiteStatement.simpleQueryForLong();
                        photo = R.drawable.checkbox2;
                    }
                    catch (Exception e){
                        photo = R.drawable.checkbox1;

                    }
                    if(numberStars == 0){
                    habits.add(new MainHabit(name, photo,R.drawable.star_empty,R.drawable.star_empty, R.drawable.star_empty, R.drawable.star_empty, R.drawable.star_empty));
                    }
                    else if (numberStars == 1){
                        habits.add(new MainHabit(name, photo,R.drawable.stars_full,R.drawable.star_empty, R.drawable.star_empty, R.drawable.star_empty, R.drawable.star_empty));
                    }
                    else if (numberStars == 2){
                        habits.add(new MainHabit(name, photo,R.drawable.stars_full,R.drawable.stars_full, R.drawable.star_empty, R.drawable.star_empty, R.drawable.star_empty));
                    }
                    else if (numberStars == 3){
                        habits.add(new MainHabit(name, photo,R.drawable.stars_full,R.drawable.stars_full, R.drawable.stars_full, R.drawable.star_empty, R.drawable.star_empty));
                    }
                    else if (numberStars == 4){
                        habits.add(new MainHabit(name, photo,R.drawable.stars_full,R.drawable.stars_full, R.drawable.stars_full, R.drawable.stars_full, R.drawable.star_empty));
                    }
                    else{
                        habits.add(new MainHabit(name, photo,R.drawable.stars_full,R.drawable.stars_full, R.drawable.stars_full, R.drawable.stars_full, R.drawable.stars_full));
                    }

                }
                catch (Exception e){
                    return false;
                }



        } while (cursor.moveToNext());
        cursor.close();

           return true;

    }


    private int getStars(long days, int progress) {
        double k = days * 0.2; // 20% на одну звезду
        return (int)(progress / k);


    }


    class DBAsyncTask extends AsyncTask<Void, Void,   Void> {

        long daysWasDone;
        long maxDoneDays;
        long progressDays;
        long bestDays = 0;
        String nameBest = new String();


        @Override
        protected Void doInBackground(Void... arg) {
            //здесь был лист
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            //--------запись прогресса для текущего дня

            Date currentTime = Calendar.getInstance().getTime(); //getTime()
            SimpleDateFormat sdfp = new SimpleDateFormat("EEEE");
            String dayOfTheWeek = sdfp.format(currentTime);

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
            String currentDataString = sdf.format(currentTime);
            Calendar calendarCurrent = Calendar.getInstance();
            try {
                calendarCurrent.setTime(sdf.parse(currentDataString));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String output = sdf.format(calendarCurrent.getTime());
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            ContentValues values = new ContentValues();


            Cursor cursor2 = db.query(HabitContract.Habits.TABLE_NAME, new String[]{HabitContract.Habits._ID, HabitContract.Habits.NAME, HabitContract.Habits.START_DATE, HabitContract.Habits.DAYS_WEEK},
                    null, null, null, null, null);

            cursor2.moveToFirst();
            int dataColIndex = cursor2.getColumnIndex(HabitContract.Habits.START_DATE);
            int daysWeekIndex = cursor2.getColumnIndex(HabitContract.Habits.DAYS_WEEK);
            int idIndex = cursor2.getColumnIndex(HabitContract.Habits._ID);
            int nameIndex = cursor2.getColumnIndex(HabitContract.Habits.NAME);

            do {
                 daysWasDone = 0;
                 maxDoneDays = 0;
                 progressDays = 0;

                Calendar calendarStart = Calendar.getInstance();
                String startData = cursor2.getString(dataColIndex);
                String workoutDays = cursor2.getString(daysWeekIndex);
                String id = cursor2.getString(idIndex);
                String nameHabit = cursor2.getString(nameIndex);

                Cursor cursor = db.query(HabitContract.DaysProgress.TABLE_NAME, new String[] {HabitContract.DaysProgress.DATE_MARKED}, HabitContract.DaysProgress.HABIT_ID  + " = ? ",
                        new String[] {id}, null, null, null);
                daysWasDone = cursor.getCount();

                try {
                    calendarStart.setTime(sdf.parse(startData));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (workoutDays.equals("Каждый день")) {
                    calendarCurrent.add(Calendar.DATE, 1);
                    long diff = calendarCurrent.getTimeInMillis() - calendarStart.getTimeInMillis();

                    maxDoneDays = diff / (24 * 60 * 60 * 1000);

                } else {
                    String weekDay = new String();
                    do {
                        weekDay = dayFormat.format(calendarStart.getTime());
                        if (workoutDays.indexOf(weekDay) != -1) {
                            maxDoneDays++;
                        }
                        calendarStart.add(Calendar.DATE, 1);
                    } while (calendarStart.getTime().compareTo(calendarCurrent.getTime()) < 0);
                }


                progressDays = 2 * daysWasDone - maxDoneDays;
                if (progressDays <= 0) progressDays = 0;

                if(progressDays >= bestDays){
                    bestDays = progressDays;
                    nameBest = nameHabit;
                }

                values.put(HabitContract.Habits.PROGRESS, progressDays);
                db.update(HabitContract.Habits.TABLE_NAME, values, HabitContract.Habits._ID + " = ?",
                        new String[]{id});
            }while (cursor2.moveToNext());


            return null;
        }
        protected  void onPostExecute( Void ar){
            textViewBestName.setText(nameBest);
            textViewBestDays.setText(String.valueOf(bestDays));
        }
    }
}

