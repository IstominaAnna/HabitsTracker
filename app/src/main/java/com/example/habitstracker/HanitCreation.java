package com.example.habitstracker;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.habitstracker.data.DBHelper;
import com.example.habitstracker.data.HabitContract;

import java.text.DateFormat;
import java.util.Calendar;

public class HanitCreation extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private static final int NOTIFY_ID = 101;

    public String chosenDaysString = new String();
    private int countDays = 0;
    public  TextView daysWeekForTraining;
    private TextView textViewReminder;
    private TextView textViewStartDay;
    private  TextView textViewEndDay;
    public boolean dataSet = true; //true - dayStart, false - dayEnd
    int number_change = 0; //сохранение / редактирование
    private ImageView cross;
    String chosenDays;

    //--------------бд---------------
    DBHelper dbHelper;

    EditText editTextHabit;
    EditText editTextMotivation;

    String DBStartDateString;
    String DBEndDateString;
    String DBReminderTime;
    String id;
    int hourReminder;
    int minuteReminder;

    //--------------бд---------------



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hanit_creation);
        CardView remainder = findViewById(R.id.card_view_Reminder);
        CardView dataStart = findViewById(R.id.card_view_daysInputStart);
        CardView dataEnd = findViewById(R.id.card_view_daysInputEnd);
        CardView remainderFrequency = findViewById(R.id.card_view_reminderFrequency);
        daysWeekForTraining = findViewById(R.id.week_days_training);
        textViewReminder = findViewById(R.id.textView_reminder);
        Button buttonSave = findViewById(R.id.id_save_button);

        textViewStartDay = findViewById(R.id.textView_startDay);
        textViewEndDay = findViewById(R.id.textView_endDay);

        cross = findViewById(R.id.imageViewCross);

        Toolbar toolbar = findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Создать привычку ");

        editTextHabit = findViewById(R.id.editText_habit);
        editTextMotivation = findViewById(R.id.editText_motivation);

        dbHelper = new DBHelper(this, DBHelper.DATABASE_NAME, null, 1);


        Intent myIntent = getIntent();

        if(myIntent != null){
            try {
                String ol = myIntent.getStringExtra("im");
                number_change = Integer.parseInt(myIntent.getStringExtra("im"));
            }
            catch (Exception e){

            }

             if(number_change!= 0) {
                 getSupportActionBar().setTitle("Редактировать ");
                 id = myIntent.getStringExtra("id");
                 String nameHabit = myIntent.getStringExtra("nameHabit");
                 String motivation = myIntent.getStringExtra("motivation");
                 String start = myIntent.getStringExtra("start");
                 String end = myIntent.getStringExtra("end");
                 String reminder = myIntent.getStringExtra("reminder");
                 String daysWeek = myIntent.getStringExtra("days_week");


                 editTextHabit.setText(nameHabit);
                 editTextMotivation.setText(motivation);
                 textViewStartDay.setText(start);
                 textViewEndDay.setText(end);
                 textViewReminder.setText(reminder);
                 daysWeekForTraining.setText(daysWeek);

             }



        }

        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Click", "on card days click");
                cross.setVisibility(View.GONE);
                textViewReminder.setText("");


            }

        });

        dataStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Click", "on card days click");
                dataSet = true;
                FragmentManager manager = getSupportFragmentManager();
                CalendarFragment calendarFragment = new CalendarFragment();
                calendarFragment.show(manager, "dialog");
            }

        });
        dataEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataSet = false;
                FragmentManager manager = getSupportFragmentManager();
                CalendarFragment calendarFragment = new CalendarFragment();
                calendarFragment.show(manager, "dialog");
            }
        });

        remainder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getSupportFragmentManager();
                TimePickerFragment timePickerFragment = new TimePickerFragment();
                timePickerFragment.show(manager, "time picker");
            }
        });


        remainderFrequency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("Click", "on card days click");
                FragmentManager manager = getSupportFragmentManager();
                DaysForTrainingDialogFragment myDialogFragment = new DaysForTrainingDialogFragment ();
                myDialogFragment.show(manager, "dialog");
            }
        });
        String reminderTime = textViewReminder.getText().toString();
        if(!reminderTime.equals("")){
            ImageView cross = findViewById(R.id.imageViewCross);
            cross.setVisibility(View.VISIBLE);
        }

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SQLiteDatabase database = dbHelper.getWritableDatabase();



                String habitName = editTextHabit.getText().toString();
                String motivation = editTextMotivation.getText().toString();
                String startDay = textViewStartDay.getText().toString();
                String endDay = textViewEndDay.getText().toString();
                String reminderTime = textViewReminder.getText().toString();
                chosenDays = daysWeekForTraining.getText().toString();



                if(habitName.isEmpty()){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Укажите название привычки!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if(startDay.isEmpty()){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Укажите дату начала!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else if(endDay.isEmpty()){
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Укажите дату конца!", Toast.LENGTH_SHORT);
                        toast.show();
                }

                else {
                    SQLiteDatabase db  = dbHelper.getReadableDatabase();

                    String selectQuery = "SELECT "+HabitContract.Habits.NAME + " FROM " + HabitContract.Habits.TABLE_NAME + " WHERE " + HabitContract.Habits.NAME + " = '" + habitName + "' ;";
                    SQLiteStatement sqLiteStatement = db.compileStatement(selectQuery);
                        ContentValues values = new ContentValues();
                        values.put(HabitContract.Habits.NAME, habitName);
                        values.put(HabitContract.Habits.MOTIVATION, motivation);
                        values.put(HabitContract.Habits.REMINDER_TIME, DBReminderTime);
                        values.put(HabitContract.Habits.DAYS_WEEK, chosenDays);

                        if(DBStartDateString != null){
                            values.put(HabitContract.Habits.START_DATE, DBStartDateString);
                        }
                        if(DBEndDateString != null){
                        values.put(HabitContract.Habits.END_DATE, DBEndDateString);
                        }
                        chosenDaysString = "";

                        if(number_change == 0 ){
                            try {
                                long k = sqLiteStatement.simpleQueryForLong();
                                Toast.makeText(getApplicationContext(), "Такая привычка уже есть!", Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e){
                                //прогресс и количество дней сначала равны нулю
                                values.put(HabitContract.Habits.PROGRESS, 0);
                                values.put(HabitContract.Habits.NUMBER_DAYS_MARKED, 0);
                                long newS = database.insert(HabitContract.Habits.TABLE_NAME, null, values);
                                int idk =(int) newS;
                                //---настройка уведомлений
                                if (!reminderTime.equals("")) {
                                    Calendar calendar = Calendar.getInstance();

                                    calendar.set(Calendar.HOUR_OF_DAY, hourReminder);
                                    calendar.set(Calendar.MINUTE, minuteReminder);
                                    Intent intentClock = new Intent(getApplicationContext(), Notification_reciever.class);
                                    intentClock.putExtra("name", habitName);
                                    intentClock.putExtra("motivation", motivation);
                                    intentClock.putExtra("id", idk);

                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                                            idk, intentClock, PendingIntent.FLAG_UPDATE_CURRENT);
                                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                                }
//                                else {
//                                    Intent intentClock = new Intent(getApplicationContext(),Notification_reciever.class);
//                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
//                                            Integer.parseInt(id), intentClock, PendingIntent.FLAG_UPDATE_CURRENT);
//                                    AlarmManager alarmManager =(AlarmManager) getSystemService(ALARM_SERVICE);
//                                    alarmManager.cancel(pendingIntent);
//                                }
                                //---------

                                Intent intent = new Intent(HanitCreation.this, MainActivity.class);
                                startActivity(intent);

                            }
                        }
                        else {


                            String name;

                            Cursor cursor = db.query(HabitContract.Habits.TABLE_NAME, new String[] {HabitContract.Habits.NAME}, HabitContract.Habits._ID + " = " +id,
                                    null, null, null, null);

                            cursor.moveToFirst();
                            int nameColIndex = cursor.getColumnIndex(HabitContract.Habits.NAME);
                            do {
                                name = cursor.getString(nameColIndex);

                            } while (cursor.moveToNext());
                            cursor.close();
                            if(name.equals(habitName)){
                                int updCount = database.update(HabitContract.Habits.TABLE_NAME, values, HabitContract.Habits._ID + " = ?",
                                        new String[]{id});
                                Log.d("update", "updated rows count = " + updCount);
                                //---настройка уведомлений
                                if (!reminderTime.equals("")) {
                                    Calendar calendar = Calendar.getInstance();

                                    calendar.set(Calendar.HOUR_OF_DAY, hourReminder);
                                    calendar.set(Calendar.MINUTE, minuteReminder);
                                    Intent intentClock = new Intent(getApplicationContext(), Notification_reciever.class);
                                    intentClock.putExtra("name", habitName);
                                    intentClock.putExtra("motivation", motivation);
                                    intentClock.putExtra("id", id);

                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                                            Integer.parseInt(id), intentClock, PendingIntent.FLAG_UPDATE_CURRENT);
                                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                                }
                                else {
                                    Intent intentClock = new Intent(getApplicationContext(),Notification_reciever.class);
                                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                                            Integer.parseInt(id), intentClock, PendingIntent.FLAG_UPDATE_CURRENT);
                                    AlarmManager alarmManager =(AlarmManager) getSystemService(ALARM_SERVICE);
                                    alarmManager.cancel(pendingIntent);
                                }
                                //---------

                                Intent intent = new Intent(HanitCreation.this, MainActivity.class);
                                startActivity(intent);
                            }
                            else {
                                try {
                                    long k = sqLiteStatement.simpleQueryForLong();
                                    Toast.makeText(getApplicationContext(), "Такая привычка уже есть!", Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e){
                                    int updCount = database.update(HabitContract.Habits.TABLE_NAME, values, HabitContract.Habits._ID + " = ?",
                                            new String[]{id});
                                    Log.d("update", "updated rows count = " + updCount);
                                    //---настройка уведомлений
                                    if (!reminderTime.equals("")) {
                                        Calendar calendar = Calendar.getInstance();

                                        calendar.set(Calendar.HOUR_OF_DAY, hourReminder);
                                        calendar.set(Calendar.MINUTE, minuteReminder);
                                        Intent intentClock = new Intent(getApplicationContext(), Notification_reciever.class);
                                        intentClock.putExtra("name", habitName);
                                        intentClock.putExtra("motivation", motivation);
                                        intentClock.putExtra("id", id);


                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                                                Integer.parseInt(id), intentClock, PendingIntent.FLAG_UPDATE_CURRENT);
                                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                                        //---------
                                    }
                                    else {
                                        Intent intentClock = new Intent(getApplicationContext(),Notification_reciever.class);
                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                                                Integer.parseInt(id), intentClock, PendingIntent.FLAG_UPDATE_CURRENT);
                                        AlarmManager alarmManager =(AlarmManager) getSystemService(ALARM_SERVICE);
                                        alarmManager.cancel(pendingIntent);

                                    }
                                        Intent intent = new Intent(HanitCreation.this, MainActivity.class);
                                        startActivity(intent);

                                }

                            }


                        }
                            db.close();

                    }


                }
        });


    }



    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        String currentDateString = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(calendar.getTime());

        if(dataSet){
            DBStartDateString = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(calendar.getTime());

            textViewStartDay.setText("ОТ " + currentDateString);
            DBStartDateString =  DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(calendar.getTime());
            textViewStartDay.setTextColor(Color.BLACK);

            if(number_change != 0){

            }
        }
        else
        {
            DBEndDateString = DateFormat.getDateInstance(DateFormat.DATE_FIELD).format(calendar.getTime());

            textViewEndDay.setText("ДО " + currentDateString);
            textViewEndDay.setTextColor(Color.BLACK);

        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        DBReminderTime = hourOfDay + ":" + minute;
        hourReminder = hourOfDay;
        minuteReminder = minute;
        textViewReminder.setText("Напоминание в "+ hourOfDay + " : " + minute);
        textViewReminder.setTextColor(Color.BLACK);

        cross.setVisibility(View.VISIBLE);



    }

    public void clicked(String str){

        countDays++;
        if(countDays < 7){

            chosenDaysString += str + " ";

        }
        else chosenDaysString = "Каждый день";

    }
    public void setChosenDaysString(){

            daysWeekForTraining.setText(chosenDaysString);
            daysWeekForTraining.setTextColor(Color.BLACK);
            chosenDaysString = "";


        countDays = 0;
    }


}
