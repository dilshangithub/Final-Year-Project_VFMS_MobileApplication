package com.example.yana;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.HashMap;

public class dashboard extends AppCompatActivity {

    //Variables
    private TextView dName;
    private static Button logout;
    private static Button fuel;
    private static Button tripInfo;
    private static Button imei;
    private static Button changePass;
    String getDriverName;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //Assign values to variables
        dName = findViewById(R.id.txt_dName);
        logout = (Button)findViewById(R.id.btn_logout);
        fuel = (Button) findViewById(R.id.btn_fuelManagement);
        tripInfo = (Button) findViewById(R.id.btn_tripInfo);
        imei = (Button) findViewById(R.id.btn_imei);
        changePass = (Button) findViewById(R.id.btn_changePassword);

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

        //logout button
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                sessionManager.logout();
            }
        });

        //fuelInfo button
        fuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFuelManagement();
            }
        });

        //tripInfo button
        tripInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTripInfo();
            }
        });

        //message button
        imei.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImei();
            }
        });

        //change password button
        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChangePassword();
            }
        });

    }

    @Override
    public void onBackPressed() {
    }

    //logout from app
    public void  logoutActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }

    //fuel management navigate
    public void openFuelManagement(){
        Intent intent = new Intent(this,fuelManagement.class);
        startActivity(intent);
    }

    //trip info navigate
    public void openTripInfo(){
        Intent intent = new Intent(this,tripInformation.class);
        startActivity(intent);
    }

    //message navigate
    public void openImei(){
        Intent intent = new Intent(this, imeiView.class);
        startActivity(intent);
    }

    //change password navigate
    public void openChangePassword(){
        Intent intent = new Intent(this,changePassword.class);
        startActivity(intent);
    }
}
