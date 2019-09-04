package com.example.habitstracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.applandeo.materialcalendarview.utils.DateUtils;
import com.example.habitstracker.data.DBHelper;
import com.example.habitstracker.data.HabitContract;
import com.example.habitstracker.data.HabitContract.Habits;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static java.lang.String.valueOf;

public class Habit extends AppCompatActivity {
    DBHelper dbHelper;
    String position;
    String namefordel;
    String motivation;
    String start;
    String end;
    String clock;
    String daysWeek;
    int progress;
    int daysMarked;
    String id;
    TextView textViewDaysWasDone;
    TextView textViewProgress;
    List<Calendar> calendars = new ArrayList<>();
    SQLiteDatabase db;
    GraphView graphView;
    double x;
    long endInMillis;
    long maxDoneDaysX;
    List<Struct> outputList;




    com.applandeo.materialcalendarview.CalendarView calendarView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit);

        dbHelper = new DBHelper(this, DBHelper.DATABASE_NAME, null, 1);


        textViewDaysWasDone = findViewById(R.id.textViewDaysWasDone);
        textViewProgress = findViewById(R.id.textViewCurrentSuccess);
        graphView = findViewById(R.id.gv_graph);


        id = new String();
        namefordel = new String();
        motivation = new String();
        start = new String();
        end = new String();
        clock = new String();
        daysWeek = new String();

        Intent myIntent = getIntent();
        if(myIntent != null){
            position = myIntent.getStringExtra("myKey");
            FirstAsyncTask ask = new FirstAsyncTask();
            ask.execute();
            outputList = new ArrayList<>();
        }



        //----------------------календарь---------------------------
        calendarView = (CalendarView) findViewById(R.id.calendarViewFinal);
        OutAsyncTask ask = new OutAsyncTask();
        ask.execute();
        //----------------построение графика
        GraphAsyncTask asyncTask = new GraphAsyncTask();
        asyncTask.execute();

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar clickedDayCalendar = eventDay.getCalendar();


                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

                    long diff = clickedDayCalendar.getTimeInMillis();
                    Calendar cl = Calendar.getInstance();
                    cl.setTimeInMillis(diff);
                    int month = cl.get(Calendar.MONTH) + 1;
                    String date = cl.get(Calendar.DAY_OF_MONTH) + "." + month + "." + cl.get(Calendar.YEAR);
                   // long days = diff / (24 * 60 * 60 * 1000);
                MyAsyncTask ask1 = new MyAsyncTask();
                ask1.execute(date);
                Toast toast = Toast.makeText(getApplicationContext(),
                       "" + date, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
        //------------------------------------------------------------
        ScrollView sv = (ScrollView)findViewById(R.id.scroll);
        sv.smoothScrollTo(0,0);
    }

    public class Pair {
        long a;
        int b;
        Pair() {};
        Pair(long a, int b) {
            this.a = a;
            this.b = b;
        }
    }
    //------добавление точек
    private DataPoint[] getDataPoint() {
        String[] columns = {HabitContract.DaysProgress.DATE_MARKED, HabitContract.DaysProgress.DATA_PROGRESS};
        Cursor cursor = db.query(HabitContract.DaysProgress.TABLE_NAME, columns, HabitContract.DaysProgress.HABIT_ID + " = ? ",
                new String[]{id}, null, null, null);

        DataPoint[]dataPoints;
        int markedColIndex = cursor.getColumnIndex(HabitContract.DaysProgress.DATE_MARKED);
        int progressColIndex = cursor.getColumnIndex(HabitContract.DaysProgress.DATA_PROGRESS);
        if(cursor.getCount()!=0) {
            dataPoints = new DataPoint[cursor.getCount()];
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

            List<Pair>sortPair = new ArrayList<>();
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                try {
                    calendar.setTime(sdf.parse(cursor.getString(markedColIndex)));
                  //  sortCalendars.add(calendar.getTimeInMillis());
                    Pair pair = new Pair(calendar.getTimeInMillis(),cursor.getInt(progressColIndex));
                    sortPair.add(pair);
                } catch (ParseException ep) {
                    ep.printStackTrace();
                }
            }

            Collections.sort(sortPair, new Comparator<Pair>() {
                @Override
                public int compare(Pair o1, Pair o2) {
                    if (o1.a > o2.a){ return 1;}
                    else if(o1.a < o2.a) {return -1; }
                    else {return 0;}
                }
            });

            for(int i = 0; i < sortPair.size(); i++){
                dataPoints[i] = new DataPoint(sortPair.get(i).a, sortPair.get(i).b);

            }
        }
        else {
            dataPoints = new DataPoint[]{
                    new DataPoint(x,0)
            };
        }
        return dataPoints;
    }


    @Override
public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_habit, menu);
    return true;
}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_edit) {
            Intent intent = new Intent(this, HanitCreation.class);
            intent.putExtra("im", "1");
            intent.putExtra("id", id);
            intent.putExtra("nameHabit",  namefordel);
            intent.putExtra("motivation", motivation);
            intent.putExtra("start", start);
            intent.putExtra("end", end);
            intent.putExtra("reminder", clock);
            intent.putExtra("days_week", daysWeek);
            startActivity(intent);

        }
        else if(item.getItemId() == R.id.action_delete){
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            int del = db.delete(Habits.TABLE_NAME, Habits.NAME + " = ?",new String[]{valueOf(namefordel) });
            db.delete(HabitContract.DaysProgress.TABLE_NAME, HabitContract.DaysProgress.HABIT_ID + " = ?",new String[]{valueOf(id) });


            Intent intentClock = new Intent(getApplicationContext(),Notification_reciever.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                    Integer.parseInt(id), intentClock, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager =(AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);



        }
        return true;
    }



    class  MyAsyncTask extends AsyncTask<String, Void, Void> {
        String data;
        protected  Void doInBackground(String... arg) {
            data = arg[0];

            SQLiteDatabase db = dbHelper.getWritableDatabase();

            String selectQuery = "SELECT " + HabitContract.DaysProgress.DATE_MARKED + " FROM " + HabitContract.DaysProgress.TABLE_NAME + " WHERE "
                    + HabitContract.DaysProgress.HABIT_ID + " = '" + id +
                    "' and " + HabitContract.DaysProgress.DATE_MARKED + " = '" + data + "' ;";
            SQLiteStatement sqLiteStatement = db.compileStatement(selectQuery);

            ContentValues values = new ContentValues();

            try {
                long k = sqLiteStatement.simpleQueryForLong();
                int del = db.delete(HabitContract.DaysProgress.TABLE_NAME, HabitContract.DaysProgress.HABIT_ID + " = ?" + " and " +
                        HabitContract.DaysProgress.DATE_MARKED + " = ?", new String[]{valueOf(id), data});


                Log.d("DELETE", "del = " + del);
            } catch (Exception e) {
                values.put(HabitContract.DaysProgress.DATE_MARKED, data);
                values.put(HabitContract.DaysProgress.HABIT_ID, id);



                //----------------------Запись прогресса для каждой отмеченной даты
                long maxDoneDays = 0;
                long daysWasDone = 0;
                long progress = 0;


                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy" );

                //Календарь текущей отмеченной даты
                Calendar calendarCurrent = Calendar.getInstance();
                String workoutDays = new String();
                try {
                    calendarCurrent.setTime(sdf.parse(data));
                } catch (ParseException ep) {
                    ep.printStackTrace();
                }

                Calendar calendarStart = Calendar.getInstance();
                Calendar calendarStartSave = Calendar.getInstance();

                Cursor cursor2 = db.query(Habits.TABLE_NAME, new String[]{Habits.START_DATE, Habits.DAYS_WEEK}, Habits._ID + " = ? " ,
                        new String[]{id}, null, null, null, "1");
                cursor2.moveToFirst();
                int dataColIndex = cursor2.getColumnIndex(Habits.START_DATE);
                int daysWeekIndex = cursor2.getColumnIndex(Habits.DAYS_WEEK);
                String startData = cursor2.getString(dataColIndex);
                workoutDays = cursor2.getString(daysWeekIndex);


                //считывается была ли такая привычка с нулем
                Cursor cursorProgress = db.query(HabitContract.DaysProgress.TABLE_NAME, new String[]
                        {HabitContract.DaysProgress.DATE_MARKED}, HabitContract.DaysProgress.DATA_PROGRESS+ " = ? "  + " and " +
                        HabitContract.DaysProgress.HABIT_ID + " = ?",
                        new String[]{valueOf(0), id}, null, null, null);

                List<Long> calendarList = new ArrayList<>();
                if(cursorProgress.getCount() != 0){
                    cursorProgress.moveToFirst();
                    int dateIndex = cursorProgress.getColumnIndex(HabitContract.DaysProgress.DATE_MARKED);
                    do {
                        String date = cursorProgress.getString(dateIndex);
                        try{
                            calendarStart.setTime(sdf.parse(date));
                            calendarList.add(calendarStart.getTimeInMillis());
                        }
                        catch (ParseException ex){

                        }
                    }
                    while (cursorProgress.moveToNext());

                    Collections.sort(calendarList);
                    long k = calendarList.get(calendarList.size()-1);
                    calendarStart.setTimeInMillis(k);
                    calendarStartSave.setTimeInMillis(k);
                }


                else {

                    try {
                        calendarStart.setTime(sdf.parse(startData));
                        calendarStartSave.setTime(sdf.parse(startData));

                    } catch (ParseException ep) {
                        ep.printStackTrace();
                    }


                }
                    if (workoutDays.equals("Каждый день")) {
                        // calendarCurrent.add(Calendar.DATE, 1);
                        long diff = calendarCurrent.getTimeInMillis() - calendarStart.getTimeInMillis();

                        maxDoneDays = diff / (24 * 60 * 60 * 1000) ;

                    } else {

                        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                        String weekDay = new String();
                        do {
                            weekDay = dayFormat.format(calendarStart.getTime());
                            if (workoutDays.indexOf(weekDay) != -1) {
                                maxDoneDays++;
                            }
                            calendarStart.add(Calendar.DATE, 1);
                        } while (calendarStart.getTime().compareTo(calendarCurrent.getTime()) < 0);

                    }


                Cursor cursor = db.query(HabitContract.DaysProgress.TABLE_NAME, new String[] {HabitContract.DaysProgress.DATE_MARKED}, HabitContract.DaysProgress.HABIT_ID  + " = ? ",
                        new String[] {id}, null, null, null);

                cursor.moveToFirst();
                int nameColIndex = cursor.getColumnIndex(HabitContract.DaysProgress.DATE_MARKED);

                    do {
                        try {
                            String data = cursor.getString(nameColIndex);
                            SimpleDateFormat sdfk = new SimpleDateFormat("dd.MM.yyyy");
                            Calendar c = DateUtils.getCalendar();
                            c.setTime(sdfk.parse(data));
                            //----ключевой момент
                            if (c.getTime().compareTo(calendarCurrent.getTime()) < 0) {
                                if(calendarStartSave.getTime().compareTo(c.getTime())<0 |(calendarStartSave.getTime().compareTo(c.getTime())==0)){
                                    daysWasDone++;
                                }

                            }

                        } catch (Exception ep) {
                            progress = 1;
                            Log.d("Fall", "Catch in OutAsyncTask");
                        }
                    } while (cursor.moveToNext());


                //--------------------
                if(progress == 0){
                progress = 2 * daysWasDone - maxDoneDays + 1;
                if(progress < 0) progress = 0;

                }
                values.put(HabitContract.DaysProgress.DATA_PROGRESS, progress);
                long newS = db.insert(HabitContract.DaysProgress.TABLE_NAME, null, values);
                Log.d("INSERT", "newS = " + newS + " DATE_MARKED = " + data + " HABIT_ID = " + id + " progress" + progress);
                cursor.close();


            } //--конец catch


            return null;
        }
        protected  void onPostExecute(Void arg){
           OutAsyncTask ask = new OutAsyncTask();
            ask.execute();
            //graphView.removeAllSeries();
            GraphAsyncTask asyncTask = new GraphAsyncTask();
            asyncTask.execute();
        }
        }

        class OutAsyncTask extends AsyncTask<Void, Void,   List<Calendar>> {
            DataPoint[]dataPoints;
            long daysWasDone = 0;
            long maxDoneDays = 0;
            long progressDays = 0;
            int allDaysWasDone = 0;
            Calendar min;


            @Override
            protected List<Calendar> doInBackground(Void... arg) {


                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.query(HabitContract.DaysProgress.TABLE_NAME, new String[] {HabitContract.DaysProgress.DATE_MARKED}, HabitContract.DaysProgress.HABIT_ID  + " = ? ",
                        new String[] {id}, null, null, null);

                cursor.moveToFirst();
                int nameColIndex = cursor.getColumnIndex(HabitContract.DaysProgress.DATE_MARKED);

                do {
                    try {
                        String data = cursor.getString(nameColIndex);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
                        Calendar c = DateUtils.getCalendar();
                        c.setTime(sdf.parse(data));
                        calendars.add(c);
                        allDaysWasDone++;


                    }
                    catch (Exception e){
                        Log.d("Fall", "Catch in OutAsyncTask");
                    }



                } while (cursor.moveToNext());
                cursor.close();

                //--------запись прогресса для текущего дня

                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy" );

                //Календарь текущей  даты
                Calendar calendarCurrent = Calendar.getInstance();
                String workoutDays = new String();


                Calendar calendarStart = Calendar.getInstance();
                Calendar calendarStartSave = Calendar.getInstance();

                Cursor cursor2 = db.query(Habits.TABLE_NAME, new String[]{Habits.START_DATE, Habits.DAYS_WEEK}, Habits._ID + " = ? " ,
                        new String[]{id}, null, null, null, "1");
                int dataColIndex = cursor2.getColumnIndex(Habits.START_DATE);
                int daysWeekIndex = cursor2.getColumnIndex(Habits.DAYS_WEEK);

                cursor2.moveToFirst();
                String start = cursor2.getString(dataColIndex);
                try {
                    min = Calendar.getInstance();
                    min.setTime(sdf.parse(start));
                }
                catch (Exception e){

                }
                String startData = cursor2.getString(dataColIndex);
                workoutDays = cursor2.getString(daysWeekIndex);


                //считывается была ли такая привычка с нулем
                Cursor cursorProgress = db.query(HabitContract.DaysProgress.TABLE_NAME, new String[]
                                {HabitContract.DaysProgress.DATE_MARKED}, HabitContract.DaysProgress.DATA_PROGRESS+ " = ? "  + " and " +
                                HabitContract.DaysProgress.HABIT_ID + " = ?",
                        new String[]{valueOf(0), id}, null, null, null);

                List<Long> calendarList = new ArrayList<>();
                if(cursorProgress.getCount() != 0){
                    cursorProgress.moveToFirst();
                    int dateIndex = cursorProgress.getColumnIndex(HabitContract.DaysProgress.DATE_MARKED);
                    do {
                        String date = cursorProgress.getString(dateIndex);
                        try{
                            calendarStart.setTime(sdf.parse(date));
                            calendarList.add(calendarStart.getTimeInMillis());
                        }
                        catch (ParseException ex){

                        }
                    }
                    while (cursorProgress.moveToNext());

                    Collections.sort(calendarList);
                    long k = calendarList.get(calendarList.size()-1);
                    calendarStart.setTimeInMillis(k);
                    calendarStartSave.setTimeInMillis(k);
                }


                else {

                    try {
                        calendarStart.setTime(sdf.parse(startData));
                        calendarStartSave.setTime(sdf.parse(startData));


                    } catch (ParseException ep) {
                        ep.printStackTrace();
                    }


                }
                if (workoutDays.equals("Каждый день")) {
                    long diff = calendarCurrent.getTimeInMillis() - calendarStart.getTimeInMillis();

                    maxDoneDays = diff / (24 * 60 * 60 * 1000)+1;

                } else {

                    SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
                    String weekDay = new String();
                    do {
                        weekDay = dayFormat.format(calendarStart.getTime());
                        if (workoutDays.indexOf(weekDay) != -1) {
                            maxDoneDays++;
                        }
                        calendarStart.add(Calendar.DATE, 1);
                    } while (calendarStart.getTime().compareTo(calendarCurrent.getTime()) < 0);

                }


                Cursor cursor3 = db.query(HabitContract.DaysProgress.TABLE_NAME, new String[] {HabitContract.DaysProgress.DATE_MARKED}, HabitContract.DaysProgress.HABIT_ID  + " = ? ",
                        new String[] {id}, null, null, null);

                cursor3.moveToFirst();
                int nameColIndex1 = cursor3.getColumnIndex(HabitContract.DaysProgress.DATE_MARKED);

                do {
                    try {
                        String data = cursor3.getString(nameColIndex1);
                        SimpleDateFormat sdfk = new SimpleDateFormat("dd.MM.yyyy");
                        Calendar c = DateUtils.getCalendar();
                        c.setTime(sdfk.parse(data));
                        //----ключевой момент
                        if (c.getTime().compareTo(calendarCurrent.getTime()) < 0) {
                            if(calendarStartSave.getTime().compareTo(c.getTime())<0 |(calendarStartSave.getTime().compareTo(c.getTime())==0)){
                                daysWasDone++;
                            }

                        }

                    } catch (Exception ep) {
                        progressDays = 1;
                        Log.d("Fall", "Catch in OutAsyncTask");
                    }
                } while (cursor3.moveToNext());


                //--------------------
                if(progressDays == 0){
                    progressDays = (int) (2 * daysWasDone - maxDoneDays);
                    if(progressDays < 0) progressDays = 0;

                }
                return calendars;
            }
            protected  void onPostExecute( List<Calendar> calendars){

                calendarView.setSelectedDates(calendars);
                calendars.clear();
                textViewDaysWasDone.setText(String.valueOf(allDaysWasDone));
                textViewProgress.setText(String.valueOf(progressDays));
               // maxDoneDaysX = maxDoneDays;

            }
        }

        class GraphAsyncTask extends AsyncTask<Void, Void,   Void> {
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>( new DataPoint[0]);
            protected  void onPreExecute(){

                graphView.removeAllSeries();
            }
            @Override
            protected Void doInBackground(Void... arg) {

                series.resetData(getDataPoint());

                return null;

            }
            protected  void onPostExecute( Void arg){


                //----вывод графика
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM");
                graphView.addSeries(series);

                graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){
                    @Override
                    public String formatLabel(double value, boolean isValueX) {
                        if(isValueX){
                            return  sdf.format(new Date((long)value));

                        }
                        else{
                            return super.formatLabel(value, isValueX);
                        }
                    }
                });

                series.setDrawDataPoints(true); //выделение точек
                series.setDataPointsRadius(15);
                series.setThickness(7);

                graphView.getViewport().setScrollable(true);
                graphView.getViewport().setScalable(true);
                graphView.getViewport().setXAxisBoundsManual(true);
                graphView.getViewport().setYAxisBoundsManual(true);
                graphView.getViewport().setMinY(0);
                int k = Integer.valueOf( String.valueOf(textViewDaysWasDone.getText()))+1;

                graphView.getViewport().setMaxY(k);
                graphView.getViewport().scrollToEnd();
            }

        }

    class FirstAsyncTask extends AsyncTask<Void, Void,   String> {

        protected  void onPreExecute(){
        }
        long days;
        @Override
        protected String doInBackground(Void... arg) {

            db = dbHelper.getReadableDatabase();
            String[] projection = {
                    Habits._ID,
                    Habits.NAME,
                    Habits.MOTIVATION,
                    Habits.START_DATE,
                    Habits.END_DATE,
                    Habits.REMINDER_TIME,
                    Habits.DAYS_WEEK,
                    Habits.PROGRESS,
                    Habits.NUMBER_DAYS_MARKED };
            Cursor cursor = db.query(Habits.TABLE_NAME, projection, null,
                    null, null, null, null);

            cursor.moveToFirst();
            int idColIndex = cursor.getColumnIndex(Habits._ID);
            int nameColIndex = cursor.getColumnIndex(Habits.NAME);
            int indexMotivation = cursor.getColumnIndex(Habits.MOTIVATION);
            int indexStart = cursor.getColumnIndex(Habits.START_DATE);
            int indexEnd = cursor.getColumnIndex(Habits.END_DATE);
            int indexClock = cursor.getColumnIndex(Habits.REMINDER_TIME);
            int indexDaysWeek = cursor.getColumnIndex( Habits.DAYS_WEEK);
            int indexProgress = cursor.getColumnIndex(Habits.PROGRESS);
            int indexMarkedDays = cursor.getColumnIndex(Habits.NUMBER_DAYS_MARKED);
            do {
                Struct struct = new Struct();
                struct.id = cursor.getString(idColIndex);
                struct.name = cursor.getString(nameColIndex);
                struct.start = cursor.getString(indexStart);
                struct.end = cursor.getString(indexEnd);
                struct.progress = cursor.getInt(indexProgress);
                struct.daysMarked = cursor.getInt(indexMarkedDays);

                struct.motivation = cursor.getString(indexMotivation);
                struct.clock = cursor.getString(indexClock);
                struct.daysWeek = cursor.getString(indexDaysWeek);

                outputList.add(struct);

            } while (cursor.moveToNext());
            cursor.close();

            id = outputList.get( Integer.parseInt(position)).id;
            motivation = outputList.get( Integer.parseInt(position)).motivation;
            daysWeek = outputList.get( Integer.parseInt(position)).daysWeek;
            namefordel = outputList.get( Integer.parseInt(position)).name;
            clock = outputList.get( Integer.parseInt(position)).clock;
            start = outputList.get( Integer.parseInt(position)).start;
            end = outputList.get( Integer.parseInt(position)).end;
            progress = outputList.get( Integer.parseInt(position)).progress;
            daysMarked = outputList.get( Integer.parseInt(position)).daysMarked;
            String ActionBar = outputList.get( Integer.parseInt(position)).name;



            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            Calendar c = Calendar.getInstance();
            Calendar a  = Calendar.getInstance();;
            try {
                c.setTime(sdf.parse((outputList.get( Integer.parseInt(position)).start)));
                a.setTime(sdf.parse((outputList.get( Integer.parseInt(position)).end)));
                c.add(Calendar.DATE, 1);
                endInMillis = a.getTimeInMillis();
                long diff = a.getTimeInMillis() - c.getTimeInMillis();
                days = diff / (24 * 60 * 60 * 1000);


            } catch (ParseException e) {
                e.printStackTrace();
            }


            return ActionBar;

        }
        protected  void onPostExecute( String bar){
            Toolbar toolbar = findViewById(R.id.toolbar2);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(bar);


            TextView textViewDayForTraining = findViewById(R.id.textViewDaysForTraining);
            textViewDayForTraining.setText(Long.toString(days));

            TextView textViewCurrentSuccess = findViewById(R.id.textViewCurrentSuccess);
            TextView textViewDaysWasDone = findViewById(R.id.textViewDaysWasDone);

            textViewCurrentSuccess.setText(Integer.toString(outputList.get(Integer.parseInt(position)).progress));
            textViewDaysWasDone.setText(Integer.toString(outputList.get(Integer.parseInt(position)).daysMarked));


        }

    }

    }
class Struct {
    String id;
    String name;
    String motivation;
    String start;
    String end;
    String clock;
    String daysWeek;
    int progress;
    int daysMarked;
};


