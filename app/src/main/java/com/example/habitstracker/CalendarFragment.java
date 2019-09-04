package com.example.habitstracker;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.Calendar;

public class CalendarFragment extends AppCompatDialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return  new DatePickerDialog(getActivity(),(DatePickerDialog.OnDateSetListener)getActivity(),year, month, day);
    }


}
