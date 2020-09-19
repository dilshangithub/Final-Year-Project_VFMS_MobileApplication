package com.example.yana;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class changePassword extends AppCompatActivity {

    //Variables
    ImageButton backButton;
    private TextView dName;
    private static Button btn_change_pass;
    private static TextView current_pass;
    private static TextView new_pass;
    private static TextView confirm_pass;
    private CheckBox check_currentPass;
    private CheckBox check_newPass;
    private CheckBox check_confPass;
    private static String URL_UPDATEPASSWORD ="http://192.168.43.188/finalYearProject/android/changePassData.php";
    String getDriverName;
    String getPassword;
    String getID;
    SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        //Assign password values to variables
        backButton = (ImageButton) findViewById(R.id.btn_back);
        dName = findViewById(R.id.txt_dName);
        current_pass = findViewById(R.id.txt_currentPassword);
        new_pass = findViewById(R.id.txt_newPassword);
        confirm_pass = findViewById(R.id.txt_confirmPassword);
        btn_change_pass = findViewById(R.id.btn_updatePassword);
        check_currentPass = findViewById(R.id.check_currentPass);
        check_newPass = findViewById(R.id.check_newPass);
        check_confPass = findViewById(R.id.check_confPass);

        //Session start
        sessionManager = new SessionManager(this);
        sessionManager.checkLogin();

        HashMap<String, String > user = sessionManager.getUserDetail();
        getDriverName = user.get(sessionManager.NAME);
        getPassword = user.get(sessionManager.PASSWORD);
        getID = user.get(sessionManager.TELEPHONE);

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

        btn_change_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cp = current_pass.getText().toString().trim();
                String np = new_pass.getText().toString().trim();
                String conP = confirm_pass.getText().toString().trim();

                if (!cp.isEmpty() || !np.isEmpty() || !conP.isEmpty()){
                    if (cp.equals(getPassword)){
                        if (np.equals(conP) && !np.isEmpty() && !conP.isEmpty()){
                            UpdatePassword();
                        }else {
                            Toast.makeText(changePassword.this,"Your new and retype password is not match!", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(changePassword.this,"Your current password is wrong!", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    current_pass.setError("Enter Your Current Password.");
                    new_pass.setError("Enter Your New Password.");
                    confirm_pass.setError("Enter Confirm Password.");
                }
            }
        });

        //Hide and show current password
        check_currentPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked){
                    current_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }else {
                    current_pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        //Hide and show new password
        check_newPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked){
                    new_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }else {
                    new_pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        //Hide and show confirm password
        check_confPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked){
                    confirm_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }else {
                    confirm_pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

    }

    private void UpdatePassword(){
    final String password = this.new_pass.getText().toString().trim();
    final String id = getID;

    final ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setMessage("Updating...");
    progressDialog.show();

    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_UPDATEPASSWORD,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progressDialog.dismiss();

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String success = jsonObject.getString("success");

                        if (success.equals("1")){
                            confirm_pass.setText("");
                            current_pass.setText("");
                            new_pass.setText("");
                            current_pass.requestFocus();
                            check_confPass.setChecked(false);
                            check_currentPass.setChecked(false);
                            check_newPass.setChecked(false);

                            //Alert dialog
                            final AlertDialog alertDialogPWUpdate = new AlertDialog.Builder(changePassword.this).create();
                            final View viewThankYOu = LayoutInflater.from(changePassword.this).inflate(R.layout.driver_satisfaction_dialog,null);
                            alertDialogPWUpdate.setView(viewThankYOu);

                            TextView warning = (TextView)viewThankYOu.findViewById(R.id.textView7);
                            warning.setText("Warning!");

                            TextView driverName = (TextView)viewThankYOu.findViewById(R.id.txt_driverNameTripEnd);
                            driverName.setText(getDriverName+",");

                            TextView message = (TextView)viewThankYOu.findViewById(R.id.textView22);
                            message.setText("Password Update Successfully!");

                            alertDialogPWUpdate.setView(viewThankYOu);
                            alertDialogPWUpdate.show();

                            //Hide alert after 3 seconds
                            final Handler handler = new Handler();
                            final Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    if (alertDialogPWUpdate.isShowing()){
                                        alertDialogPWUpdate.dismiss();
                                    }
                                }
                            };
                            alertDialogPWUpdate.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    handler.removeCallbacks(runnable);
                                }
                            });
                            handler.postDelayed(runnable,3000);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                        Toast.makeText(changePassword.this,"Update Not Successfully!",Toast.LENGTH_SHORT).show();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    Toast.makeText(changePassword.this,"Server Error!",Toast.LENGTH_SHORT).show();
                }
            })
        {
            @Override
                protected Map<String,String> getParams() throws AuthFailureError{
                Map<String,String> params = new HashMap<>();
                params.put("password",password);
                params.put("id",id);
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
