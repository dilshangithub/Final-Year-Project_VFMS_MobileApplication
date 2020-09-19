package com.example.yana;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
public class fuelManagement extends AppCompatActivity {

    //Variables
    private TextView dName;
    private static final String TAG = "fuelManagement";
    private static String URL_GETVEHICLE = "http://192.168.43.188/finalYearProject/android/fuelManagement_getVehicle.php";
    private static String URL_INSERT = "http://192.168.43.188/finalYearProject/android/fuelManagement_insertInfo.php";
    private TextView mDisplayDate;
    private TextView mDisplayTime;
    private TextView vehicleNo;
    private TextView unitPrice;
    private TextView noOfLiters;
    private TextView totalCost;
    private TextView calculateTotal;
    private DatePickerDialog.OnDateSetListener mdateSetListener;
    Button submitBtn;
    Button clearBtn;
    ImageButton backButton;
    String getDriverName;
    SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_management);

        //Assign values to variables
        backButton = (ImageButton) findViewById(R.id.btn_back);
        submitBtn = (Button) findViewById(R.id.btn_enter);
        clearBtn = (Button) findViewById(R.id.btn_reset);
        dName = findViewById(R.id.txt_dName);
        mDisplayDate = findViewById(R.id.txt_dateInput);
        mDisplayTime = findViewById(R.id.txt_timeInput);
        vehicleNo = findViewById(R.id.txt_vehicleNo);
        unitPrice = findViewById(R.id.txt_unitPrice);
        noOfLiters = findViewById(R.id.txt_noOfLiters);
        totalCost = findViewById(R.id.txt_totalCostDispaly);
        calculateTotal = findViewById(R.id.view_totalCost);

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

        //back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });

        //Driver name
        dName.setText(getDriverName);

        //Date Picker for input date to database
        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        fuelManagement.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mdateSetListener,
                        year,month,day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mdateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG,"onDateSet: yyyy/mm/dd: "+ year + "/" + month + "/" + day);

                String date =  year + "/" + month + "/" + day;
                mDisplayDate.setText(date);
            }
        };

        //Time Picker for input date to database
        mDisplayTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;

                mTimePicker = new TimePickerDialog(fuelManagement.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        mDisplayTime.setText(String.format("%02d:%02d",selectedHour,selectedMinute));
                    }
                }
                , hour, minute, false);//24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();

            }
        });

        //create Spinner(Drop down list)
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(fuelManagement.this,
                android.R.layout.simple_expandable_list_item_1,getResources().getStringArray(R.array.fuelType));
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(arrayAdapter);

        //get vehicle number from database
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_GETVEHICLE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            JSONArray jsonArray = jsonObject.getJSONArray("vehicleData");

                            if (success.equals("1")){
                                for (int i=0; i<jsonArray.length(); i++){
                                    JSONObject object = jsonArray.getJSONObject(i);

                                    String getVehicleNo = object.getString("vehicleNo").trim();

                                    vehicleNo.setText(getVehicleNo);
                                    vehicleNo.setEnabled(false);
                                    vehicleNo.setClickable(false);
                                    vehicleNo.setFocusable(false);
                                }
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(fuelManagement.this,"Server Error. Please Try Again!", Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                //get device IMEI number
                TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String IMEInumber = manager.getDeviceId();

                Map<String,String> params = new HashMap<>();
                params.put("imei",IMEInumber);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

        //Clear all the input fields
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDisplayDate.setText("");
                mDisplayTime.setText("");
                unitPrice.setText("");
                noOfLiters.setText("");
                totalCost.setText("");
                unitPrice.requestFocus();
                spinner.setSelection(0);
            }
        });

        //Insert data to database
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mDisplayTime.getText().equals("") || mDisplayDate.getText().equals("") || unitPrice.getText().equals("") || noOfLiters.getText().equals("") || totalCost.getText().equals("")){
                    mDisplayDate.setError("");
                    mDisplayTime.setError("");
                    unitPrice.setError("Enter Unit Price!");
                    totalCost.setError("");
                    noOfLiters.setError("Enter No of Liters!");
                } else {
                    SaveFuelData();
                    mDisplayDate.setText("");
                    mDisplayTime.setText("");
                    unitPrice.setText("");
                    noOfLiters.setText("");
                    totalCost.setText("");
                    unitPrice.requestFocus();
                    spinner.setSelection(0);
                }

            }
        });

        //Calculate and display total cost of fuel
        calculateTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    double up = Double.parseDouble(unitPrice.getText().toString());
                    double tl = Double.parseDouble(noOfLiters.getText().toString());
                    double total;
                    total = (up*tl);
                    totalCost.setText(String.format("%.2f",total));
                }catch (Exception e) {
                    e.printStackTrace();
                    unitPrice.setError("Enter Unit Price!");
                    noOfLiters.setError("Enter No of Liters!");
                }
            }
        });
    }

    //Function for insert data
    private void SaveFuelData() {

        final String date = this.mDisplayDate.getText().toString().trim();
        final String time = this.mDisplayTime.getText().toString().trim();
        final String vehicleNo = this.vehicleNo.getText().toString().trim();
        final String unitPrice = this.unitPrice.getText().toString().trim();
        final String noOfLiters = this.noOfLiters.getText().toString().trim();
        final String totalCost = this.totalCost.getText().toString().trim();
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        final String fuelType = spinner.getSelectedItem().toString();

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving..");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_INSERT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            if (success.equals("1")){

                                //Display success message dialog
                                final AlertDialog alertDialogUploadFuel = new AlertDialog.Builder(fuelManagement.this).create();
                                final View viewUploadFuel = LayoutInflater.from(fuelManagement.this).inflate(R.layout.fuel_info_submit_dialog,null);
                                alertDialogUploadFuel.setView(viewUploadFuel);

                                TextView f_Type = (TextView)viewUploadFuel.findViewById(R.id.txt_fuelTypeDialog);
                                TextView totalLiters = (TextView)viewUploadFuel.findViewById(R.id.txt_totalLitersDialog);
                                TextView u_Price = (TextView)viewUploadFuel.findViewById(R.id.txt_UnitPriceDialog);
                                TextView totalPrice = (TextView)viewUploadFuel.findViewById(R.id.txt_TotalPriceDialog);

                                f_Type.setText("Fuel Type: "+ fuelType.toString());
                                totalLiters.setText("Total Liters: "+ noOfLiters.toString());
                                u_Price.setText("Unit Price: "+ unitPrice.toString());
                                totalPrice.setText("Total Price: "+ totalCost.toString());

                                alertDialogUploadFuel.setView(viewUploadFuel);
                                alertDialogUploadFuel.show();

                                //Hide alert after 3 seconds
                                final Handler handler = new Handler();
                                final Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        if (alertDialogUploadFuel.isShowing()){
                                            alertDialogUploadFuel.dismiss();
                                            backActivity();
                                        }
                                    }
                                };
                                alertDialogUploadFuel.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        handler.removeCallbacks(runnable);
                                    }
                                });
                                handler.postDelayed(runnable,5000);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            progressDialog.dismiss();
                            Toast.makeText(fuelManagement.this,"Insertion Not Successfully!",Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(fuelManagement.this,"Server Error!",Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String,String> getParams()throws AuthFailureError{
                Map<String,String> params = new HashMap<>();
                params.put("date",date);
                params.put("time",time);
                params.put("vehicleNo",vehicleNo);
                params.put("fuelType",fuelType);
                params.put("u_price",unitPrice);
                params.put("noOfLiters",noOfLiters);
                params.put("totalCost",totalCost);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
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
