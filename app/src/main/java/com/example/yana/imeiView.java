package com.example.yana;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.HashMap;

public class imeiView extends AppCompatActivity {

    //Variables
    ImageButton backButton;
    private static TextView previewImei;
    private TextView dName;
    String getDriverName;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imei_view);

        //Assign values to variables
        dName = findViewById(R.id.txt_dName);
        dName = findViewById(R.id.txt_dName);
        previewImei = findViewById(R.id.txt_imeiPreview);

        //Session start
        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();

        HashMap<String, String > user = sessionManager.getUserDetail();
        getDriverName = user.get(sessionManager.NAME);

        //for display date and time on top
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        sleep(100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView ttime = (TextView) findViewById(R.id.txt_time);
                                TextView tdate = (TextView) findViewById(R.id.txt_date);

                                long date = System.currentTimeMillis();
                                SimpleDateFormat stimef = new SimpleDateFormat("hh:mm a");
                                ttime.setText(stimef.format(date));
                                stimef = new SimpleDateFormat("dd-MM-yyyy");
                                tdate.setText(stimef.format(date));
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        t.start();

        //Driver name
        dName.setText(getDriverName);

        //back button
        backButton = (ImageButton) findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });

        //Preview mobile IMEI number
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String IMEInumber = manager.getDeviceId();
        previewImei.setText(IMEInumber);
    }
    @Override
    public void onBackPressed() {
    }

    //move to back (previous page)
    public void backActivity(){
        Intent intent = new Intent(this,dashboard.class);
        startActivity(intent);
    }
}
