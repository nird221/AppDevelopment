package com.example.youkoldemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SimulateHomeActivity extends AppCompatActivity {

    // Defining the xml page Attributes
    private Button btnStartMode;
    private TextView txtMode, txtHeadset;

    private AudioManager audioManager;
    private boolean firstRun;
    private int state = Consts.youkolModeOn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulate_home);

        // Defining the xml page Attributes
        btnStartMode = findViewById(R.id.btnStartMode);
        txtHeadset = findViewById(R.id.txtHeadset);
        txtMode = findViewById(R.id.txtModeS);

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mBroadcastReceiver, receiverFilter);

        btnStartMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state == Consts.youkolModeOn) {
                    btnStartMode.setText("Stop Youkol Mode");
                    state = Consts.youkolModeOff;
                    startYoukolMode();
                }else {
                    btnStartMode.setText("Start Youkol Mode");
                    state = Consts.youkolModeOn;
                    stopYoukolMode();
                }
            }
        });
    }

    // OVERRIDE phone volume keys
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if((txtHeadset.getText().toString().equals("No Headset Detected")) && (txtMode.getText().toString().equals("Youkol Mode ON"))) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    audioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_PLAY_SOUND);
                    return true;
                default:
                    return super.dispatchKeyEvent(event);
            }
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // Check if a WIRED device has connected / disconnected
            if (Intent.ACTION_HEADSET_PLUG.equals(action)){
                int headphoneState = intent.getIntExtra("state", Consts.defaultValue);

                if (headphoneState == Consts.headphonesConnected) {
                    Toast.makeText(getApplicationContext(), "3.5mm headphones connected - stop Youkol mode", Toast.LENGTH_SHORT).show();
                    txtHeadset.setText("Headset Detected");
                    stopYoukolMode();
                } else if (headphoneState == Consts.headphonesDisconnected) {
                    if (!firstRun) {

                        Toast.makeText(getApplicationContext(), "3.5mm headphones disconnected - start Youkol mode", Toast.LENGTH_SHORT).show();
                        txtHeadset.setText("No Headset Detected");

                    } else {
                        firstRun = false;
                    }
                }
            }
        }
    };

    public void startYoukolMode() {

        txtMode.setText("Youkol Mode ON");
        audioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_PLAY_SOUND);
        Toast.makeText(SimulateHomeActivity.this, "Entering Youkol mode", Toast.LENGTH_SHORT).show();
    }

    public void stopYoukolMode(){
        audioManager.adjustVolume(AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_PLAY_SOUND);
        Toast.makeText(SimulateHomeActivity.this, "Exiting Youkol Mode", Toast.LENGTH_SHORT).show();
        txtMode.setText("Youkol Mode OFF");
    }
}