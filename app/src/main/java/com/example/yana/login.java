package com.example.yana;

        import android.content.Intent;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.text.method.HideReturnsTransformationMethod;
        import android.text.method.PasswordTransformationMethod;
        import android.view.View;
        import android.widget.Button;
        import android.widget.CheckBox;
        import android.widget.CompoundButton;
        import android.widget.EditText;
        import android.widget.ProgressBar;
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

        import java.util.HashMap;
        import java.util.Map;


public class login extends AppCompatActivity {

    //Variables
    private static Button btn_login;
    private EditText username, password;
    private ProgressBar loading;
    private CheckBox check_loginPass;
    private static String URL_LOGIN = "http://192.168.43.188/finalYearProject/android/loginApp.php";
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //create new session manager
        sessionManager = new SessionManager(this);

        //Assign values to variables
        loading = findViewById(R.id.progressBar2);
        username = findViewById(R.id.txt_username);
        password = findViewById(R.id.txt_password);
        btn_login = findViewById(R.id.btn_login);
        check_loginPass = findViewById(R.id.check_loginPass);

        // click login button
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mUser = username.getText().toString().trim();
                String mPass = password.getText().toString().trim();
                //validate
                if (!mUser.isEmpty() || !mPass.isEmpty()){
                    Login(mUser,mPass);
                }else {
                    username.setError("Enter Your Username.");
                    password.setError("Enter Your Password.");
                }
            }
        });

        //Hide and show password
        check_loginPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked){
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }else {
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });
    }

    //send username and password and verify. then get user info from server
    private void Login(final String username, final String password){
        loading.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
        new Response.Listener<String>(){
                @Override
                    public void onResponse(String response){
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("success");
                            JSONArray jsonArray = jsonObject.getJSONArray("login");

                            if (success.equals("1")) {
                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject object = jsonArray.getJSONObject(i);
                                    //assign user info into variables
                                    String name = object.getString("name").trim();
                                    String telephone = object.getString("telephone").trim();
                                    String password = object.getString("password").trim();
                                    //assign user info into session
                                    sessionManager.createSession(name, telephone, password);

                                Intent intent = new Intent(login.this, dashboard.class);
                                intent.putExtra("name", name);
                                intent.putExtra("telephone",telephone);
                                intent.putExtra("password",password);
                                startActivity(intent);

                                loading.setVisibility(View.GONE);
                            }
                            }else {
                                Toast.makeText(login.this,"Incorrect Username or Password", Toast.LENGTH_SHORT).show();
                                loading.setVisibility(View.GONE);
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                            loading.setVisibility(View.GONE);
                            btn_login.setVisibility(View.VISIBLE);
                            Toast.makeText(login.this,"Incorrect Username or Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loading.setVisibility(View.GONE);
                        btn_login.setVisibility(View.VISIBLE);
                        Toast.makeText(login.this,"Server Error. Please Try Again!", Toast.LENGTH_SHORT).show();
                    }
                })

        {
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("username",username);
                params.put("password",password);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}

