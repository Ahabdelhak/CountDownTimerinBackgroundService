package com.countdowntimerservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_start, btn_cancel;
    private TextView tv_timer;
    String date_time;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    EditText et_hours;

    SharedPreferences mpref;
    SharedPreferences.Editor mEditor;
    ProgressBar progressBarCircle;

    private long timeCountInMilliSeconds = 1 * 60000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        listener();

    }

    private void init() {
        btn_start = (Button) findViewById(R.id.btn_timer);
        tv_timer = (TextView) findViewById(R.id.tv_timer);
        et_hours = (EditText) findViewById(R.id.et_hours);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        progressBarCircle = (ProgressBar) findViewById(R.id.progressBarCircle);



        mpref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mEditor = mpref.edit();

        try {
            String str_value = mpref.getString("data", "");
            if (str_value.matches("")) {
                et_hours.setEnabled(true);
                btn_start.setEnabled(true);
                tv_timer.setText("");

            } else {

                if (mpref.getBoolean("finish", false)) {
                    et_hours.setEnabled(true);
                    btn_start.setEnabled(true);
                    tv_timer.setText("");
                } else {

                    et_hours.setEnabled(false);
                    btn_start.setEnabled(false);
                    tv_timer.setText(str_value);
                }
            }
        } catch (Exception e) {

        }

    }

    private void listener() {
        btn_start.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);

    }

    private void setTimerValues() {
        int time = 0;
        if (!et_hours.getText().toString().isEmpty()) {
            // fetching value from edit text and type cast to integer
            time = Integer.parseInt(et_hours.getText().toString().trim());
        } else {
            // toast message to fill edit text
          //  Toast.makeText(getApplicationContext(), getString(R.string.str_min), Toast.LENGTH_LONG).show();
        }
        // assigning values after converting to milliseconds
        timeCountInMilliSeconds = time * 60 * 1000;
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_timer:

                if (et_hours.getText().toString().length() > 0) {

                    int int_hours = Integer.valueOf(et_hours.getText().toString());

          //          if (int_hours<=24) {

                        et_hours.setEnabled(false);
                        btn_start.setEnabled(false);

                        setTimerValues();
                        setProgressBarValues();

                        calendar = Calendar.getInstance();
                        simpleDateFormat = new SimpleDateFormat("mm:ss");
                        date_time = simpleDateFormat.format(calendar.getTime());

                        mEditor.putString("data", date_time).commit();
                        mEditor.putString("hours", et_hours.getText().toString()).commit();


                        Intent intent_service = new Intent(getApplicationContext(), Timer_Service.class);
                        startService(intent_service);
//                    }else {
//                        Toast.makeText(getApplicationContext(),"Please select the value below 24 hours",Toast.LENGTH_SHORT).show();
//                    }
/*
                    mTimer = new Timer();
                    mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 5, NOTIFY_INTERVAL);*/
                } else {
                    Toast.makeText(getApplicationContext(), "Please select value", Toast.LENGTH_SHORT).show();
                }
                break;


            case R.id.btn_cancel:

             Intent intent = new Intent(getApplicationContext(),Timer_Service.class);
             stopService(intent);

                mEditor.clear().commit();

                et_hours.setEnabled(true);
                btn_start.setEnabled(true);
                tv_timer.setText("");

                setProgressBarValues();

                break;

        }

    }


    private void setProgressBarValues() {

        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle.setProgress((int) timeCountInMilliSeconds / 1000);
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(!intent.getStringExtra("time").equals("0:1")){
                String str_time = intent.getStringExtra("time");
                tv_timer.setText(str_time);

                int time = convertTime(str_time);
                progressBarCircle.setProgress((int) (time / 1000));
                Log.e("TimePro", String.valueOf(time));


            }else if(intent.getStringExtra("time").equals("0:1")){

                tv_timer.setText("0:0");

                //finish();
                // TODO: 5/11/2020  clear shared pref >>
                // TODO: 5/11/2020   if accept then start 20 min
                // TODO: 5/10/2020 if reject >> Refresh and check rejection order, back to history
            }

        }
    };


    int convertTime(String timeString) {
        String[] time = timeString.split ( ":" );
        int pos = time.length - 1;
        long res = 0;
        if( pos >=0 ){
            res = res + TimeUnit.SECONDS.toMillis( Long.parseLong( time[pos] ));
            pos --;
        }
        if( pos >=0 ){
            res = res + TimeUnit.MINUTES.toMillis( Long.parseLong( time[pos] ));
            pos --;
        }
        if( pos >=0 ){
            res = res + TimeUnit.HOURS.toMillis( Long.parseLong( time[pos] ));
            pos --;
        }
        return (int)res;
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver,new IntentFilter(Timer_Service.str_receiver));

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}
