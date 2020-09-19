package com.example.yana;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {
    SharedPreferences sharedPreferences;

    public SharedPreferences.Editor editor;
    public Context context;
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME ="LOGIN";
    private static final String LOGIN = "IS_LOGIN";
    public static final String NAME = "NAME";
    public static final String TELEPHONE = "TELEPHONE";
    public static final String PASSWORD = "PASSWORD";

    public SessionManager(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }
    public void createSession(String name, String telephone, String password){
        editor.putBoolean(LOGIN,true);
        editor.putString(NAME,name);
        editor.putString(TELEPHONE,telephone);
        editor.putString(PASSWORD,password);
        editor.apply();
    }

    public boolean isLogin(){
        return sharedPreferences.getBoolean(LOGIN,false);
    }
    public void checkLogin(){
        if (!this.isLogin()){
            Intent i = new Intent(context,login.class);
            context.startActivity(i);
            ((dashboard) context).finish();
        }
    }

    public HashMap<String, String> getUserDetail(){
        HashMap<String, String> user = new HashMap<>();
        user.put(NAME, sharedPreferences.getString(NAME,null));
        user.put(TELEPHONE, sharedPreferences.getString(TELEPHONE,null));
        user.put(PASSWORD,sharedPreferences.getString(PASSWORD,null));
        return user;
    }

    public void logout(){
        editor.clear();
        editor.commit();
        Intent i = new Intent(context,MainActivity.class);
        context.startActivity(i);
        ((dashboard) context).finish();
    }
}
