package com.example.myapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

public class BluetoothSetUpActivity extends AppUtility {

    // Defining the xml page Attributes
    private ListView listViewPaired, listViewDetected;
    private Button btnSearch, btnOn, btnOff;

    private ArrayList<String> arrayListpaired;
    private ArrayAdapter<String> adapter, detectedAdapter;
    private ArrayList<BluetoothDevice> arrayListPairedBluetoothDevices;
    private BluetoothDevice bdDevice;
    private ListItemClickedonPaired listItemClickedonPaired;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ArrayList<BluetoothDevice> arrayListBluetoothDevices = null;
    private ListItemClicked listItemClicked;

    public static final String EXTRA_MESSAGE = "com.example.myapp.MESSAGE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_set_up);

        // Defining the xml page Attributes
        listViewDetected = findViewById(R.id.listViewDetected);
        listViewPaired = findViewById(R.id.listViewPaired);
        btnSearch = findViewById(R.id.buttonSearch);
        btnOn = findViewById(R.id.buttonOn);
        btnOff = findViewById(R.id.buttonOff);


        arrayListpaired = new ArrayList<String>();
        arrayListPairedBluetoothDevices = new ArrayList<BluetoothDevice>();
        listItemClickedonPaired = new ListItemClickedonPaired();
        arrayListBluetoothDevices = new ArrayList<BluetoothDevice>();
        adapter = new ArrayAdapter<String>(BluetoothSetUpActivity.this, android.R.layout.simple_list_item_1, arrayListpaired);
        detectedAdapter = new ArrayAdapter<String>(BluetoothSetUpActivity.this, android.R.layout.simple_list_item_single_choice);
        listViewDetected.setAdapter(detectedAdapter);
        listItemClicked = new ListItemClicked();
        detectedAdapter.notifyDataSetChanged();
        listViewPaired.setAdapter(adapter);

        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(Manifest.permission.BLUETOOTH, Consts.BLUETOOTH_PERMISSION_CODE);
                if (!bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.enable();
                }
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(Manifest.permission.BLUETOOTH, Consts.BLUETOOTH_PERMISSION_CODE);
                if (bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.disable();
                }
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(Manifest.permission.BLUETOOTH, Consts.BLUETOOTH_PERMISSION_CODE);
                Toast.makeText(BluetoothSetUpActivity.this, "Start Searching...", Toast.LENGTH_SHORT).show();

                // Register for broadcasts when a device is discovered.
                IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                BluetoothSetUpActivity.this.registerReceiver(bluetoothSearchBroadcast, intentFilter);
                bluetoothAdapter.startDiscovery();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // unregister the ACTION_FOUND receiver.
        unregisterReceiver(bluetoothSearchBroadcast);
    }


    @Override
    protected void onStart() {
        super.onStart();
        getPairedDevices();

        // Define the onClick ListViews actions
        listViewDetected.setOnItemClickListener(listItemClicked);
        listViewPaired.setOnItemClickListener(listItemClickedonPaired);
    }

    // Create a BroadcastReceiver for ACTION_FOUND to find bluetooth devices.
    private final BroadcastReceiver bluetoothSearchBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkPermission(Manifest.permission.BLUETOOTH, Consts.BLUETOOTH_PERMISSION_CODE);
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                boolean alreadyInList = true;
                for (int i = 0; i < arrayListBluetoothDevices.size(); i++) {
                    if (device.getAddress().equals(arrayListBluetoothDevices.get(i).getAddress())) {
                        alreadyInList = false;
                    }
                }
                if (alreadyInList) {
                    // Do not include in list if item contains null
                    if (!device.getName().contains("null")) {
                        detectedAdapter.add(device.getName() + " " + device.getType() + "\n" + device.getAddress());
                        arrayListBluetoothDevices.add(device);
                        detectedAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    // get the already paired devices of the phone
    private void getPairedDevices() {
        checkPermission(Manifest.permission.BLUETOOTH, Consts.BLUETOOTH_PERMISSION_CODE);
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        if (pairedDevice.size() > 0) {
            arrayListpaired.clear();
            for (BluetoothDevice device : pairedDevice) {

                // Do not include in list if item contains null
                if (!device.getName().contains("null")) {
                    String strDeviceClass;
                    switch (device.getBluetoothClass().getDeviceClass()) {
                        //Reference: https://developer.android.com/reference/android/bluetooth/BluetoothClass.Device
                        case 1028:
                            strDeviceClass = "BT Headset";
                            break;
                        case 1056:
                            strDeviceClass = "BT Car Audio";
                            break;
                        case 1032:
                            strDeviceClass = "BT Handsfree";
                            break;
                        case 1048:
                            strDeviceClass = "BT Headphones";
                            break;
                        case 1064:
                            strDeviceClass = "BT HiFi Audio";
                            break;
                        case 1044:
                            strDeviceClass = "BT Loudspeaker";
                            break;
                        case 1040:
                            strDeviceClass = "BT Microphone";
                            break;
                        case 1052:
                            strDeviceClass = "BT Portable Audio";
                            break;
                        case 1084:
                            strDeviceClass = "BT Loudspeaker ";
                            break;
                        default:
                            strDeviceClass = "_";
                            break;
                    }

                    arrayListpaired.add(strDeviceClass + " " + device.getName() + " " + device.getBluetoothClass() + "\n" + device.getAddress());
                    arrayListPairedBluetoothDevices.add(device);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    // set listViewDetected OnItemClick
    class ListItemClicked implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            bdDevice = arrayListBluetoothDevices.get(position);
            checkPermission(Manifest.permission.BLUETOOTH, Consts.BLUETOOTH_PERMISSION_CODE);
            boolean isBonded = false;
            try {
                // Check if the device can pair with the phone
                isBonded = createBond(bdDevice);
                if (isBonded) {
                    arrayListpaired.add(bdDevice.getName() + "\n" + bdDevice.getAddress());
                    getPairedDevices();
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Toast.makeText(BluetoothSetUpActivity.this, "The Bond is created:" + isBonded, Toast.LENGTH_SHORT).show();
        }
    }

    // set listViewPaired OnItemClick
    class ListItemClickedonPaired implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            bdDevice = arrayListPairedBluetoothDevices.get(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(BluetoothSetUpActivity.this);
            builder.setCancelable(true);
            builder.setTitle("Set Insurance Mode Device:");
            builder.setMessage("Would you like to set this Device to Insurance mode");
            builder.setPositiveButton("Confirm",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(BluetoothSetUpActivity.this, InsuranceModeTestActivity.class);
                            String message = bdDevice.getAddress();
                            intent.putExtra(EXTRA_MESSAGE, message);
                            startActivity(intent);
                        }
                    });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // If the user don't set up a device don't do anything
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    // func to create a bond between the phone and the selected device
    public boolean createBond(BluetoothDevice btDevice)
            throws Exception {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }
}