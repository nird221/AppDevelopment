package com.example.youkoldemo;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class HomePageActivity extends AppUtility {

    // Defining xml Attributes
    private Button btnBluetoothSet, btnSimulate, btnDeviceList, btnAudioOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Defining the xml page buttons
        btnBluetoothSet = findViewById(R.id.btnBluetoothSet);
        btnSimulate = findViewById(R.id.btnSimulateYK);
        btnDeviceList = findViewById(R.id.btnDeviceList);
        btnAudioOut = findViewById(R.id.btnAudioOut);

        // check the relevant permissions for the following flows
        checkPermission(Manifest.permission.BLUETOOTH, Consts.BLUETOOTH_PERMISSION_CODE);
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, Consts.LOCATION_PERMISSION_CODE);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        TextView toolbarTxt = findViewById(R.id.txtToolbar);
        if(message.length() > 0) {
            toolbarTxt.setText("Hey user:  " + message);
        }

        // Set the buttons On Click Functions
        btnBluetoothSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, SetBluetoothActivity.class);
                startActivity(intent);
            }
        });

        btnSimulate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, SimulateHomeActivity.class);
                startActivity(intent);
            }
        });

        btnDeviceList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, AvailableDeviceActivity.class);
                startActivity(intent);
            }
        });

        btnAudioOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, AudioOutputActivity.class);
                startActivity(intent);
            }
        });

    }
}