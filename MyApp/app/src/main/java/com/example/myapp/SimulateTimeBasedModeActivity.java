package com.example.myapp;

import androidx.appcompat.app.AppCompatActivity;

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

public class SimulateTimeBasedModeActivity extends AppCompatActivity {

    // Defining the xml page Attributes
    private Button btnStartMode;
    private TextView txtMode, txtHeadset;

    private AudioManager audioManager;
    private boolean firstRun;
    private int state = Consts.InsuranceModeOn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulate_time_based_mode);

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
                if(state == Consts.InsuranceModeOn) {
                    btnStartMode.setText("Stop Insurance Mode");
                    state = Consts.InsuranceModeOff;
                    startInsuranceMode();
                }else {
                    btnStartMode.setText("Start Insurance Mode");
                    state = Consts.InsuranceModeOn;
                    stopInsuranceMode();
                }
            }
        });
    }

    // OVERRIDE phone volume keys
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if((txtHeadset.getText().toString().equals("No Headset Detected")) && (txtMode.getText().toString().equals("Insurance Mode ON"))) {
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
                    Toast.makeText(getApplicationContext(), "3.5mm headphones connected - stop Insurance mode", Toast.LENGTH_SHORT).show();
                    txtHeadset.setText("Headset Detected");
                    stopInsuranceMode();
                } else if (headphoneState == Consts.headphonesDisconnected) {
                    if (!firstRun) {

                        Toast.makeText(getApplicationContext(), "3.5mm headphones disconnected - start Insurance mode", Toast.LENGTH_SHORT).show();
                        txtHeadset.setText("No Headset Detected");

                    } else {
                        firstRun = false;
                    }
                }
            }
        }
    };

    public void startInsuranceMode() {

        txtMode.setText("Insurance Mode ON");
        audioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_PLAY_SOUND);
        Toast.makeText(SimulateTimeBasedModeActivity.this, "Entering Insurance mode", Toast.LENGTH_SHORT).show();
    }

    public void stopInsuranceMode(){
        audioManager.adjustVolume(AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_PLAY_SOUND);
        Toast.makeText(SimulateTimeBasedModeActivity.this, "Exiting Insurance Mode", Toast.LENGTH_SHORT).show();
        txtMode.setText("Insurance Mode OFF");
    }
}