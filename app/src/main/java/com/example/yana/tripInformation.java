package com.example.yana;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class tripInformation extends AppCompatActivity {

    //Variables
    private TextView dName;
    private Button button_start;
    private Button button_end;
    private Button button_navi;
    private TextView txt_from;
    private TextView txt_to;
    private TextView txt_deptTime;
    private TextView txt_arrTime;
    private TextView txt_description;
    private BroadcastReceiver broadcastReceiver;
    private static String URL_GETVEHICLE = "http://192.168.43.188/finalYearProject/android/fuelManagement_getVehicle.php";
    private static String URL_GETTRIPINFO = "http://192.168.43.188/finalYearProject/android/getTripInfo.php";
    private static String URL_UPDATETRIPSTATUS = "http://192.168.43.188/finalYearProject/android/updateTripStatus.php";
    private static String URL_UPDATECODINATES = "http://192.168.43.188/finalYearProject/android/updateCodinates.php";
    public static boolean tripStatus = false;
    ImageButton backButton;
    String getDriverName;
    String vNo;
    String getScheduleNo;
    String getstartLatitude;
    String getstartLongitude;
    String getdestinationLatitude;
    String getdestinationLongitude;
    String getCurrentLatitude;
    String getCurrentLongitude;
    String locationDate;
    String locationTime;
    SessionManager sessionManager;


    @Override
    protected void onResume() {
        super.onResume();
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    saveOnDatabase(intent.getExtras().get("coordinates_long").toString(), intent.getExtras().get("coordinates_lat").toString());
                    getCurrentLatitude = intent.getExtras().get("coordinates_lat").toString();
                    getCurrentLongitude = intent.getExtras().get("coordinates_long").toString();
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_information);

        //Assign values to variables
        button_end = (Button) findViewById(R.id.btn_end);
        button_navi = (Button) findViewById(R.id.btn_navigation);
        button_start = (Button) findViewById(R.id.btn_start);
        txt_from = (TextView) findViewById(R.id.txt_from);
        txt_to = (TextView) findViewById(R.id.txt_to);
        txt_deptTime = (TextView) findViewById(R.id.txt_depTime);
        txt_arrTime = (TextView) findViewById(R.id.txt_arrTime);
        txt_description = (TextView) findViewById(R.id.txt_description);
        dName = findViewById(R.id.txt_dName);

        //Check the status of the trip and show and hide buttons
        if (tripStatus) {
            button_start.setVisibility(View.INVISIBLE);
        } else {
            button_end.setVisibility(View.INVISIBLE);
            button_navi.setVisibility(View.INVISIBLE);
        }

        //Session start
        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();

        HashMap<String, String> user = sessionManager.getUserDetail();
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

        //back button
        backButton = (ImageButton) findViewById(R.id.btn_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });

        //Driver name
        dName.setText(getDriverName);

        //Button enable
        if (!runtime_permission())
            enable_buttons();

        //..............................................................................................
        //get Trip information from database
        //..............................................................................................

        final StringRequest requestTripInfo = new StringRequest(Request.Method.POST, URL_GETTRIPINFO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject2 = new JSONObject(response);
                            String success = jsonObject2.getString("success");
                            JSONArray jsonArray = jsonObject2.getJSONArray("tripData");

                            if (success.equals("1")) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);

                                    String getFrom = object.getString("from").trim();
                                    String getTo = object.getString("to").trim();
                                    getstartLatitude = object.getString("startLatitude").trim();
                                    getstartLongitude = object.getString("startLongitude").trim();
                                    getdestinationLatitude = object.getString("destinationLatitude").trim();
                                    getdestinationLongitude = object.getString("destinationLongitude").trim();
                                    String getArrTime = object.getString("arrTime").trim();
                                    String getDeptTime = object.getString("deptTime").trim();
                                    String getDescription = object.getString("description").trim();
                                    getScheduleNo = object.getString("scheduleNo").trim();

                                    if (getFrom.matches("")) {
                                        button_start.setEnabled(false);
                                    }
                                    txt_from.setText(getFrom);
                                    txt_to.setText(getTo);
                                    txt_arrTime.setText(getArrTime);
                                    txt_deptTime.setText(getDeptTime);
                                    txt_description.setText(getDescription);
                                }
                            } else if (success.equals("0")) {
                                button_start.setVisibility(View.INVISIBLE);

                                //Alert dialog
                                final AlertDialog alertDialogTripEmpty = new AlertDialog.Builder(tripInformation.this).create();
                                final View viewThankYOu = LayoutInflater.from(tripInformation.this).inflate(R.layout.driver_satisfaction_dialog, null);
                                alertDialogTripEmpty.setView(viewThankYOu);

                                TextView warning = (TextView) viewThankYOu.findViewById(R.id.textView7);
                                warning.setText("Warning!");

                                TextView driverName = (TextView) viewThankYOu.findViewById(R.id.txt_driverNameTripEnd);
                                driverName.setText(getDriverName + ",");

                                TextView message = (TextView) viewThankYOu.findViewById(R.id.textView22);
                                message.setText("You haven't any scheduled trips!");

                                alertDialogTripEmpty.setView(viewThankYOu);
                                alertDialogTripEmpty.show();
                                alertDialogTripEmpty.setCanceledOnTouchOutside(false);

                                //Hide alert after 3 seconds
                                final Handler handler = new Handler();
                                final Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (alertDialogTripEmpty.isShowing()) {
                                            alertDialogTripEmpty.dismiss();
                                            backActivity();
                                        }
                                    }
                                };
                                alertDialogTripEmpty.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        handler.removeCallbacks(runnable);
                                    }
                                });
                                handler.postDelayed(runnable, 3000);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(tripInformation.this, "Server Error. Please Try Again!", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params1 = new HashMap<>();

                params1.put("vehicleNo", vNo);
                return params1;
            }
        };

        //get vehicle number from database

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_GETVEHICLE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            JSONArray jsonArray = jsonObject.getJSONArray("vehicleData");

                            if (success.equals("1")) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    vNo = object.getString("vehicleNo").trim();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(tripInformation.this, "Server Error.. Please Try Again!", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //get device IMEI number
                TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String IMEInumber = manager.getDeviceId();

                Map<String, String> params = new HashMap<>();
                params.put("imei", IMEInumber);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
        requestQueue.start();
        requestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<Object>() {
            @Override
            public void onRequestFinished(Request<Object> request) {
                RequestQueue requestQueue2 = Volley.newRequestQueue(tripInformation.this);
                requestQueue2.add(requestTripInfo);
            }
        });

        button_navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNavigate();
            }
        });
    }

    //..............................................................................................
    //Update location
    //..............................................................................................

    private void saveOnDatabase(final String longt, final String lat) {
        final StringRequest requestUpdateLocation = new StringRequest(Request.Method.POST, URL_UPDATECODINATES,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");

                            if (success.equals("1")) {
                               // Toast.makeText(tripInformation.this, response, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(tripInformation.this, "Location Update Failed!", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //get device IMEI number
                TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String IMEInumber = manager.getDeviceId();

                locationDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                locationTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

                Map<String, String> params2 = new HashMap<>();
                params2.put("imei", IMEInumber);
                params2.put("longitude", longt);
                params2.put("latitude", lat);
                params2.put("date", locationDate);
                params2.put("time", locationTime);
                return params2;
            }
        };
        RequestQueue requestQueue3 = Volley.newRequestQueue(this);
        requestQueue3.add(requestUpdateLocation);
    }

    //..............................................................................................
    // Start and End button
    //..............................................................................................

    private void enable_buttons() {
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), GPS_Service.class);
                startService(i);
                tripStatus = true;
                button_end.setVisibility(View.VISIBLE);
                button_navi.setVisibility(View.VISIBLE);
                button_start.setVisibility(View.INVISIBLE);
            }
        });

        button_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog alertDialog = new AlertDialog.Builder(tripInformation.this).create();
                final View view = LayoutInflater.from(tripInformation.this).inflate(R.layout.trip_confi_dialog, null);

                alertDialog.setView(view);
                view.findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), GPS_Service.class);
                        stopService(i);
                        UpdateTripStatus();
                        tripStatus = false;
                        button_end.setVisibility(View.INVISIBLE);
                        button_start.setVisibility(View.VISIBLE);
                        alertDialog.dismiss();

                        txt_from.setText("");
                        txt_to.setText("");
                        txt_arrTime.setText("");
                        txt_deptTime.setText("");
                        txt_description.setText("");


                        final AlertDialog alertDialogThankYou = new AlertDialog.Builder(tripInformation.this).create();
                        final View viewThankYOu = LayoutInflater.from(tripInformation.this).inflate(R.layout.driver_satisfaction_dialog, null);
                        alertDialogThankYou.setView(viewThankYOu);

                        TextView driverName = (TextView) viewThankYOu.findViewById(R.id.txt_driverNameTripEnd);
                        driverName.setText(getDriverName + ",");

                        alertDialogThankYou.setView(viewThankYOu);
                        alertDialogThankYou.show();
                        alertDialogThankYou.setCanceledOnTouchOutside(false);

                        //Hide alert after 3 seconds
                        final Handler handler = new Handler();
                        final Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                if (alertDialogThankYou.isShowing()) {
                                    alertDialogThankYou.dismiss();
                                    backActivity();
                                }
                            }
                        };
                        alertDialogThankYou.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                handler.removeCallbacks(runnable);
                            }
                        });
                        handler.postDelayed(runnable, 3000);
                    }
                });
                view.findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.setView(view);
                alertDialog.show();
            }
        });
    }

    //..............................................................................................
    // update trip status
    //..............................................................................................

    private void UpdateTripStatus() {
        StringRequest requestUpdateStatus = new StringRequest(Request.Method.POST, URL_UPDATETRIPSTATUS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");

                            if (success.equals("1")) {
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(tripInformation.this, "Update Not Successfully!", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(tripInformation.this, "Server Error!", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("scheduleNo", getScheduleNo);
                return params;
            }
        };

        RequestQueue requestQueue2 = Volley.newRequestQueue(this);
        requestQueue2.add(requestUpdateStatus);

    }

    //..............................................................................................
    //Runtime permission
    //..............................................................................................

    private boolean runtime_permission() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                enable_buttons();
            } else {
                runtime_permission();
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

    //move to back (previous page)
    public void backActivity() {
        Intent intent = new Intent(this, dashboard.class);
        startActivity(intent);
    }

    //..............................................................................................
    //Open google map
    //..............................................................................................

    public void openNavigate() {
        //get all the destination points and current location of the vehicle
        String s_Latitude = getstartLatitude;
        String s_Longitude = getstartLongitude;
        String d_Latitude = getdestinationLatitude;
        String d_Longitude = getdestinationLongitude;
        String c_latitude = getCurrentLatitude;
        String c_longitude = getCurrentLongitude;

        //Open google map according to destinations.
        String uri = "http://www.google.com/maps/dir/" + c_latitude + "," + c_longitude + "/" + s_Latitude + "," + s_Longitude + "/" + d_Latitude + "," + d_Longitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }
}

