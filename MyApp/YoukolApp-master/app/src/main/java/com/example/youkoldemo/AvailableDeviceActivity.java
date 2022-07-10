package com.example.youkoldemo;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioPlaybackConfiguration;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.ArrayList;
import java.util.List;

public class AvailableDeviceActivity extends AppUtility {

    // Defining the xml page Attributes
    private Button btnChange;
    private ListView scanList;
    private ArrayList<String> arrayList = new ArrayList<>();
    private int headphoneState;
    private Boolean firstRun = true;
    private Boolean youkolMode = false;

    // Defining the AudioManager Attribute
    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_device);

        // Defining the xml page Attributes
        scanList = findViewById(R.id.scanList);
        btnChange = findViewById(R.id.btnChange);

        this.registerBroadcast();

        // Adding Items to the list
        populateListItem("Screen");
        populateListItem("Headset");
        populateListItem("Speakerphone");
        populateListItem("Microphone");

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.registerAudioPlaybackCallback(audioPlaybackCallback, null);

        // button to change the Audio output
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //While Listening to music/media:
                //Changes the AUDIO OUTPUT to phone Speaker even if device connected
                mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                mAudioManager.stopBluetoothSco();
                mAudioManager.setBluetoothScoOn(false);
                mAudioManager.setSpeakerphoneOn(true);
            }
        });
    }

    // func that OVERRIDE the onDestroy func of the page
    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(deviceConnectionBroadcast);
    }

    //func that OVERRIDE the back button on the nav bar
    @Override
    public void onBackPressed() {
        if (youkolMode) {
            Toast.makeText(AvailableDeviceActivity.this, "Going back is not allowed in Youkol mode", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    // func that OVERRIDE the exit of a user from the page (except threw HOME button)
    @Override
    protected void onUserLeaveHint() {
        if (youkolMode) {
            Toast.makeText(getApplicationContext(), "Can't get out of the page while in Youkol mode", Toast.LENGTH_SHORT).show();
        } else {
            super.onUserLeaveHint();
        }
    }

    // func that OVERRIDE the touch events on the page
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (youkolMode) {
            hideNavBar();
        }
        return super.onTouchEvent(e);
    }

    // Adding Actions Checks to the Broadcast and register it
    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        this.registerReceiver(deviceConnectionBroadcast, filter);
    }

    // Broadcasting changes in devices connections
    private final BroadcastReceiver deviceConnectionBroadcast = new BroadcastReceiver() {
        BluetoothDevice device;
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            checkPermission(Manifest.permission.BLUETOOTH, Consts.BLUETOOTH_PERMISSION_CODE);
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            // Check if a Bluetooth device has connected
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

                Toast.makeText(getApplicationContext(), device.getName() + " connected - stop Youkol mode", Toast.LENGTH_SHORT).show();
                populateListItem(device.getName());
                stopYoukolMode();
            }

            // Check if a Bluetooth device has disconnected
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {

                Toast.makeText(getApplicationContext(), device.getName() + " disconnected - start Youkol mode", Toast.LENGTH_SHORT).show();
                removeListItem(device.getName());
                startYoukolMode();
            }

            // Check if a WIRED device has connected / disconnected
            else if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                headphoneState = intent.getIntExtra("state", Consts.defaultValue);

                if (headphoneState == Consts.headphonesConnected) {

                    if (!firstRun) {
                        Toast.makeText(getApplicationContext(), "3.5mm headphones connected - stop Youkol mode", Toast.LENGTH_SHORT).show();
                    } else {
                        firstRun = false;
                    }
                    populateListItem("3.5mm headphones");
                    stopYoukolMode();

                } else if (headphoneState == Consts.headphonesDisconnected) {
                    Toast.makeText(getApplicationContext(), "3.5mm headphones disconnected - start Youkol mode", Toast.LENGTH_SHORT).show();
                    removeListItem("3.5mm headphones");
                    startYoukolMode();
                }
            }
        }
    };

    //Called whenever the playback activity and configuration has changed
    private final AudioManager.AudioPlaybackCallback audioPlaybackCallback = new AudioManager.AudioPlaybackCallback() {
        @Override
        public void onPlaybackConfigChanged(List<AudioPlaybackConfiguration> configs) {
            Toast.makeText(getApplicationContext(), "onPlaybackConfigChanged", Toast.LENGTH_SHORT).show();
        }
    };

    //func to add item to the list
    private void populateListItem(String strR) {
        Context context = getApplicationContext();
        ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayList);
        arrayList.add(strR);
        scanList.setAdapter(arrayAdapter);
    }

    //func to remove item from a list
    private void removeListItem(String strR) {
        Context context = getApplicationContext();
        ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayList);
        arrayList.remove(strR);
        scanList.setAdapter(arrayAdapter);
    }

    private void startYoukolMode() {
        Toast.makeText(getApplicationContext(), "Youkol mode has started", Toast.LENGTH_SHORT).show();

        // Mute the device Audio
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI);

        hideNavBar();
        youkolMode = true;
    }

    private void stopYoukolMode() {
        Toast.makeText(getApplicationContext(), "Youkol mode has stopped", Toast.LENGTH_SHORT).show();

        // Unmute the device Audio
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustVolume(AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI);

        youkolMode = false;
    }



    //func to hide the navigation and status bar
    public void hideNavBar() {
        // Hide both the navigation bar and the status bar.
        View decorView = getWindow().getDecorView();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController == null) {
            return;
        }

        // Configure the behavior of the hidden system bars
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }


}