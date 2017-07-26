
package com.example.carrie.carrie_test1;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Created by rita on 2017/5/20.
 */
@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class TimeDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    EditText txtTime;
    TextView settimetxt;
    long settime;

    public TimeDialog(View view,TextView textview){
//        final LinearLayout newView = (LinearLayout)activity.getLayoutInflater().inflate(R.layout.activity_edt, null);
        Log.d("fffff","1");
        txtTime=(EditText)view;
        Log.d("fffff",view.toString());
        settimetxt=textview;
        Log.d("fffff1",settimetxt.toString());

    }


    public Dialog onCreateDialog(Bundle savedInstanceState){
        Log.d("fffff","2");
        final Calendar c=Calendar.getInstance();
        int hour=c.get(Calendar.HOUR_OF_DAY);
        int minute=c.get(Calendar.MINUTE);
        settime=c.getTimeInMillis();
        return new TimePickerDialog(getActivity(), this,hour,minute, DateFormat.is24HourFormat(getActivity()));

    }
    public void onTimeSet(TimePicker view, int hour, int minute){
        Log.d("fffff","3");
        String time=hour+":"+minute;
        Log.d("fffff","4");
        txtTime.setText(time);
        Log.d("fffff","5");
        String set = String.valueOf(settime);
        settimetxt.setText(set);
    }




}