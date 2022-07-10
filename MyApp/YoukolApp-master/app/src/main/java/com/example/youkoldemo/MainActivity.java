package com.example.youkoldemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppUtility {

    // Defining the xml attributes we would like to edit
    private Button screenButton;
    private String message;
    private TextView txtMode, txtThread, txtMacAdress, txtBState;

    private BluetoothAdapter bluetoothAdapter = null;
    private ArrayList<String> arrayListpaired;
    private ArrayList<BluetoothDevice> arrayListPairedBluetoothDevices;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        screenButton = findViewById(R.id.btnScreen);
        txtBState = findViewById(R.id.txtBState);
        txtMode = findViewById(R.id.txtMode);
        screenButton.setVisibility(View.GONE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        arrayListpaired = new ArrayList<String>();
        arrayListPairedBluetoothDevices = new ArrayList<BluetoothDevice>();
        adapter= new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, arrayListpaired);

        IntentFilter filter3 = new IntentFilter();
        filter3.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter3.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mBroadcastReceiver3, filter3);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        message = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        txtMacAdress = findViewById(R.id.txtMacAdress);
        txtThread = findViewById(R.id.txtThread);
        txtMacAdress.setText(message);

        txtMacAdress.setVisibility(View.INVISIBLE);
        txtThread.setVisibility(View.INVISIBLE);

        screenButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Toast.makeText(MainActivity.this, "Cannot access screen while in Youkol Mode", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getPairedDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mBroadcastReceiver3);
    }

    // function that OVERRIDE the hardware back button
    @Override
    public void onBackPressed() {

        //Check if the user is in youkol mode:
        if (txtMode.getText().toString().equals("Youkol Mode ON")){
            //Pop up message and not allowing the user to go back while in youkol mode
            Toast.makeText(MainActivity.this, "You can't go back while in Youkol mode", Toast.LENGTH_SHORT).show();
        }else {
            super.onBackPressed();
        }
    }

    // func that OVERRIDE and block all user exit from app tries
    @Override
    protected void onUserLeaveHint() {

        if (txtMode.getText().toString().equals("Youkol Mode ON")) {
            // Pop up message and not allowing the user to go back while in youkol mode
            Toast.makeText(MainActivity.this, "You can't exit app while in Youkol mode", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {
            super.onUserLeaveHint();
        }
    }

    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        BluetoothDevice device;
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            checkPermission(Manifest.permission.BLUETOOTH, Consts.BLUETOOTH_PERMISSION_CODE);
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                if ((device.getAddress().equals(message)) && (txtMode.getText().toString().equals("Youkol Mode OFF"))) {
                    startYoukolMode();
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                if ((device.getAddress().equals(message)) && (txtMode.getText().toString().equals("Youkol Mode ON"))) {
                    stopYoukolMode();
                }
            }
        }
    };

    // func that irritate threw every paired device and checks if he is connected
    private void getPairedDevices() {
        BluetoothSocket mmSocket;
        BluetoothDevice mmDevice;
        BluetoothSocket tmp = null;
        checkPermission(Manifest.permission.BLUETOOTH, Consts.BLUETOOTH_PERMISSION_CODE);
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        if(pairedDevice.size()>0)
        {
            for(BluetoothDevice device : pairedDevice)
            {
                txtThread.setText(device.getAddress());
                mmDevice = device;
                if (txtMacAdress.getText().toString().equals(device.getAddress()))
                {
                    try {
                        tmp = mmDevice.createRfcommSocketToServiceRecord(Consts.MY_UUID);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mmSocket = tmp;
                    bluetoothAdapter.cancelDiscovery();
                    try {
                        mmSocket.connect();
                    } catch (IOException e) {
                        try {
                            mmSocket.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            txtBState.setText("Not Connected");
                            stopYoukolMode();
                        }
                        return;
                    }
                    startYoukolMode();
                    txtBState.setText("Connected");
                }
                arrayListPairedBluetoothDevices.add(device);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void startYoukolMode(){

        txtMode.setText("Youkol Mode ON");
        screenButton.setVisibility(View.VISIBLE);
        Toast.makeText(MainActivity.this, "Entering Youkol mode", Toast.LENGTH_SHORT).show();

    }

    public void stopYoukolMode(){

        Toast.makeText(MainActivity.this, "Device Disconnected, Exiting Youkol Mode", Toast.LENGTH_SHORT).show();
        txtMode.setText("Youkol Mode OFF");
        screenButton.setVisibility(View.GONE);

    }


}