package com.example.habitstracker;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.applandeo.materialcalendarview.utils.DateUtils;
import com.example.habitstracker.data.DBHelper;
import com.example.habitstracker.data.HabitContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.lang.String.valueOf;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.MyViewHolder> {
    List<MainActivity.MainHabit> habits;


    public MyRecyclerViewAdapter(List<MainActivity.MainHabit> myDataset) {

        habits = myDataset;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;
        public ImageView check;
        public ImageView star1;
        public ImageView star2;
        public ImageView star3;
        public ImageView star4;
        public ImageView star5;


        public MyViewHolder(final View v) {
            super(v);
            List<Integer>idList = new ArrayList<>();

            class ClickAsyncTask extends AsyncTask<Integer, Void, Void> {

                @Override
                protected Void doInBackground(Integer... args) {
                    int adapterPosition = args[0];
                    DBHelper dbHelper = new DBHelper(v.getContext(), DBHelper.DATABASE_NAME, null, 1);
                    SQLiteDatabase db = dbHelper.getReadableDatabase();

                    Date currentData = Calendar.getInstance().getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("d.M.yyyy");
                    String data = sdf.format(currentData);

                    Cursor cursor = db.query(HabitContract.Habits.TABLE_NAME, new String[]{HabitContract.Habits._ID}, null,
                            null, null, null, null);
                    cursor.moveToFirst();
                    int idIndex = cursor.getColumnIndex(HabitContract.Habits._ID);
                    do {
                        idList.add(cursor.getInt(idIndex));
                    } while (cursor.moveToNext());
                    cursor.close();

                    String id = "";
                    for (int i = 0; i < idList.size(); i++){
                        if (i == adapterPosition){
                            id = valueOf(idList.get(i));
                        }
                    }
                    String selectQuery = "SELECT " + HabitContract.DaysProgress.DATE_MARKED + " FROM " + HabitContract.DaysProgress.TABLE_NAME + " WHERE "
                            + HabitContract.DaysProgress.HABIT_ID + " = '" + id +
                            "' and " + HabitContract.DaysProgress.DATE_MARKED + " = '" + data + "' ;";
                    SQLiteStatement sqLiteStatement = db.compileStatement(selectQuery);

                    ContentValues values = new ContentValues();
                    try {
                        long k = sqLiteStatement.simpleQueryForLong();
                        int del = db.delete(HabitContract.DaysProgress.TABLE_NAME, HabitContract.DaysProgress.HABIT_ID + " = ?" + " and " +
                                HabitContract.DaysProgress.DATE_MARKED + " = ?", new String[]{id, data});

                    } catch (Exception e) {
                        values.put(HabitContract.DaysProgress.DATE_MARKED, data);
                        values.put(HabitContract.DaysProgress.HABIT_ID, id);



                        //----------------------Запись прогресса для  отмеченной даты
                        long maxDoneDays = 0;
                        long daysWasDone = 0;
                        long progress = 0;


                        SimpleDateFormat sdfp = new SimpleDateFormat("dd.MM.yy" );

                        //Календарь текущей отмеченной даты
                        Calendar calendarCurrent = Calendar.getInstance();
                        String workoutDays = new String();
                        try {
                            calendarCurrent.setTime(sdfp.parse(data));
                        } catch (ParseException ep) {
                            ep.printStackTrace();
                        }

                        Calendar calendarStart = Calendar.getInstance();
                        Calendar calendarStartSave = Calendar.getInstance();

                        Cursor cursor2 = db.query(HabitContract.Habits.TABLE_NAME, new String[]{HabitContract.Habits.START_DATE, HabitContract.Habits.DAYS_WEEK}, HabitContract.Habits._ID + " = ? " ,
                                new String[]{id}, null, null, null, "1");
                        cursor2.moveToFirst();
                        int dataColIndex = cursor2.getColumnIndex(HabitContract.Habits.START_DATE);
                        int daysWeekIndex = cursor2.getColumnIndex(HabitContract.Habits.DAYS_WEEK);
                        String startData = cursor2.getString(dataColIndex);
                        workoutDays = cursor2.getString(daysWeekIndex);



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
                            cursorProgress.close();

                            Collections.sort(calendarList);
                            long k = calendarList.get(calendarList.size()-1);
                            calendarStart.setTimeInMillis(k);
                            calendarStartSave.setTimeInMillis(k);
                        }



                        else {

                            try {
                                calendarStart.setTime(sdfp.parse(startData));
                                calendarStartSave.setTime(sdfp.parse(startData));

                            } catch (ParseException ep) {
                                ep.printStackTrace();
                            }


                        }
                        if (workoutDays.equals("Каждый день")) {

                            long diff = calendarCurrent.getTimeInMillis() - calendarStart.getTimeInMillis();

                            maxDoneDays = diff / (24 * 60 * 60 * 1000);

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
                        int nameColIndex = cursor3.getColumnIndex(HabitContract.DaysProgress.DATE_MARKED);

                        do {
                            try {
                                String data1 = cursor3.getString(nameColIndex);
                                SimpleDateFormat sdfk = new SimpleDateFormat("dd.MM.yyyy");
                                Calendar c = DateUtils.getCalendar();
                                c.setTime(sdfk.parse(data1));
                                if (c.getTime().compareTo(calendarCurrent.getTime()) < 0) {
                                    if(calendarStartSave.getTime().compareTo(c.getTime())<0 |(calendarStartSave.getTime().compareTo(c.getTime())==0)){
                                        daysWasDone++;
                                    }

                                }

                            } catch (Exception ep) {
                                progress = 1;
                                Log.d("Fall", "Catch in OutAsyncTask");
                            }
                        } while (cursor3.moveToNext());


                        //--------------------
                        if(progress == 0){
                            progress = 2 * daysWasDone - maxDoneDays + 1;
                            if(progress < 0) progress = 0;

                        }
                        values.put(HabitContract.DaysProgress.DATA_PROGRESS, progress);
                        long newS = db.insert(HabitContract.DaysProgress.TABLE_NAME, null, values);
                        Log.d("INSERT", "newS = " + newS + " DATE_MARKED = " + data + " HABIT_ID = " + id + " progress" + progress);
                        cursor.close();


                    }


                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    if (check.getDrawable().getConstantState() == v.getResources().getDrawable( R.drawable.checkbox1).getConstantState()) {
                            check.setImageResource(R.drawable.checkbox2);
                        }
                        else {
                        check.setImageResource(R.drawable.checkbox1);
                        }




                }
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i("Click", "on item click #" + getAdapterPosition());

                    Intent intent = new Intent(v.getContext(), Habit.class);

                    intent.putExtra("myKey", Integer.toString(getAdapterPosition()));
                    v.getContext().startActivity(intent);
                }
            });

            mTextView = v.findViewById(R.id.habit_name);
            check = itemView.findViewById(R.id.check);
            star1 = v.findViewById(R.id.star1);
            star2 = v.findViewById(R.id.star2);
            star3 = v.findViewById(R.id.star3);
            star4 = v.findViewById(R.id.star4);
            star5 = v.findViewById(R.id.star5);

            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long p = (long) getAdapterPosition();
                    ClickAsyncTask clickAsyncTask = new ClickAsyncTask();
                    clickAsyncTask.execute(getAdapterPosition());

                }
            });


        }



    }


    @Override
    public MyRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_item, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.mTextView.setText(habits.get(position).name);
        holder.check.setImageResource(habits.get(position).photoId);
        holder.star1.setImageResource(habits.get(position).star1Id);
        holder.star2.setImageResource(habits.get(position).star2Id);
        holder.star3.setImageResource(habits.get(position).star3Id);
        holder.star4.setImageResource(habits.get(position).star4Id);
        holder.star5.setImageResource(habits.get(position).star5Id);


    }

    @Override
    public int getItemCount() {
        return habits.size();
    }


}

