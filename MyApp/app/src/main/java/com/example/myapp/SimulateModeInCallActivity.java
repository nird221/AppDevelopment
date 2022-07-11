package com.example.myapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

public class SimulateModeInCallActivity extends AppCompatActivity {

    private AudioManager mAudioManager;
    private int headsetState = Consts.headphonesDisconnected;
    private SettingsContentObserver mSettingsContentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulate_mode_in_call);

        // Get the Audio Manager service.
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        this.registerVolumeChangeListener();
        this.registerBroadcast();
    }

    // OVERRIDE the onDestroy func of the page
    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(audioStateChangeBroadcast);

        this.unRegisterVolumeChangeListener();
    }

    // Adding Actions Checks to the Broadcast and register it
    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        filter.addAction(AudioManager.ACTION_SPEAKERPHONE_STATE_CHANGED);
        this.registerReceiver(audioStateChangeBroadcast, filter);
    }

    // Broadcasting changes in bluetooth headset Audio Output while in call
    private final BroadcastReceiver audioStateChangeBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // Detect Changes in Bluetooth device audio
            if (intent.getAction().equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {

                int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, Consts.defaultValue);

                // New Bluetooth Device audio route appeared
                if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                    Toast.makeText(context, "connected", Toast.LENGTH_SHORT).show();
                    headsetState = Consts.headphonesConnected;
                    mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                }

                // Bluetooth audio route disappeared
                else if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                    Toast.makeText(context, "disconnected", Toast.LENGTH_SHORT).show();
                    mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                    mAudioManager.setMicrophoneMute(true);
                    mAudioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI);
                    headsetState = Consts.headphonesDisconnected;
                }
            } else if (intent.getAction().equals(AudioManager.ACTION_SPEAKERPHONE_STATE_CHANGED)) {

                if (mAudioManager.isSpeakerphoneOn()) {
                    Toast.makeText(context, "Speaker phone On", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Speaker phone Off", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void registerVolumeChangeListener() {
        mSettingsContentObserver = new SettingsContentObserver(this, new Handler());
        getApplicationContext().getContentResolver().registerContentObserver(android.provider.
                Settings.System.CONTENT_URI, true, mSettingsContentObserver);
    }

    private void unRegisterVolumeChangeListener() {
        getApplicationContext().getContentResolver().unregisterContentObserver(mSettingsContentObserver);
    }

    public class SettingsContentObserver  extends ContentObserver {
        Context context;

        public SettingsContentObserver(Context c, Handler handler) {
            super(handler);
            context=c;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            if ((mAudioManager.getMode() == AudioManager.MODE_IN_CALL) && (headsetState == Consts.headphonesDisconnected)) {
                Toast.makeText(context, "Volume Changed no Headphones Connected", Toast.LENGTH_SHORT).show();
                mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                mAudioManager.adjustVolume(AudioManager.ADJUST_MUTE, AudioManager.FLAG_PLAY_SOUND);
                mAudioManager.setMicrophoneMute(true);
            } else {
                Toast.makeText(context, "Volume Changed Headphones Connected", Toast.LENGTH_SHORT).show();

            }
        }
    }
}