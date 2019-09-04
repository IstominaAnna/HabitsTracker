package com.example.habitstracker.data;

import android.provider.BaseColumns;

public class HabitContract {
    public static final class Habits implements BaseColumns {
        public final static String TABLE_NAME = "habits";

        public final static String _ID = BaseColumns._ID;
        public final static String NAME = "name";
        public final static String MOTIVATION = "motivation";
        public final static String START_DATE = "startDate";
        public final static String END_DATE = "endDate";
        public final static String REMINDER_TIME = "reminderTime";
        public final static String DAYS_WEEK = "daysWeek";
        public final static String PROGRESS = "progress";
        public final static String NUMBER_DAYS_MARKED = "numberDaysMarked";

    }
    public static final class DaysProgress implements BaseColumns {
        public final static String TABLE_NAME = "daysProgress";

        public final static String _ID = BaseColumns._ID;
        public final static String HABIT_ID = "habitId";
        public final static String DATE_MARKED = "dateMarked";
        public final static String DATA_PROGRESS = "dataProgress";

    }
}
