package com.example.habitstracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialogFragment;


public class DaysForTrainingDialogFragment  extends AppCompatDialogFragment {
    TextView chosenDays;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String[] weekDays = getResources().getStringArray(R.array.week_Days);
        final boolean[] checkedItemsArray = {true, true, true, true, true, false, false};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Выберите дни для тренировки")
                .setMultiChoiceItems(weekDays, checkedItemsArray,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which, boolean isChecked) {

                                checkedItemsArray[which] = isChecked;
                            }


                        })

                .setPositiveButton("ОК",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                StringBuilder state = new StringBuilder();

                                for (int i = 0; i < weekDays.length; i++) {
                                    state.append(weekDays[i]);
                                    if (checkedItemsArray[i]){
                                        ((HanitCreation)getActivity()).clicked(weekDays[i]);


                                    }

                                }
                                ((HanitCreation)getActivity()).setChosenDaysString();

                            }
                        })

                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {

                                dialog.cancel();
                            }
                        });

        return builder.create();
    }
}
