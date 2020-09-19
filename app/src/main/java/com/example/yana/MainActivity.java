package com.example.yana;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    //Variables
    MediaPlayer beepTone;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        beepTone = MediaPlayer.create(getApplicationContext(),R.raw.beep_tone);
        beepTone.setLooping(true);
        beepTone.start();

        button = (Button) findViewById(R.id.btn_continue);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivityLogin();
            }
        });
    }
    public void stopBeep(View v){
        beepTone.release();
    }

    public void openActivityLogin() {
        Intent intent = new Intent(this, login.class);
        startActivity(intent);
        beepTone.release();
    }

        @Override
        public void onBackPressed () {
    }
}
