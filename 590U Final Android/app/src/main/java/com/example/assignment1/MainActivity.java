package com.example.assignment1;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.UUID;


public class MainActivity extends Activity {
    private BluetoothAdapter bluetoothAdapter;
    final float time = System.currentTimeMillis();
    private int REQUEST_ENABLE_BT = 21;
    private final int WRITE_REQUEST_CODE = 1;

    private FileOutputStream file;
    private String emg_val = "";
    private Boolean record = false;
    private int fatigue = 1;


    private final BluetoothGattCallback gattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    if (newState == 2){
                        gatt.discoverServices();
                    }

                }
                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {

                    emg_val = new String(characteristic.getValue());
                    emg_val = emg_val.replaceAll("\\D+","");
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            TextView values = findViewById(R.id.values);
                            values.setText(emg_val);
                        }
                    });

                    if(record && !emg_val.isEmpty()) {
                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();
                        byte b[] = String.format("%s,%s,%d\n", emg_val, ts, fatigue).getBytes();//converting string into byte array
                        try {
                            file.write(b);
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    }

                    Log.d("data", emg_val);
                }
                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                   BluetoothGattCharacteristic characteristicID =  gatt.getService(UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")).getCharacteristic(UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E"));
                   gatt.setCharacteristicNotification(characteristicID, true);
                    BluetoothGattDescriptor descriptor = characteristicID.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);


                }
            };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        BluetoothGatt EMG = bluetoothAdapter.getRemoteDevice("C9:9E:F0:4F:DD:4B").connectGatt(this,true, gattCallback);
        //EMG.getService(UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")).getCharacteristic(UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E"));

        final Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    file = openFileOutput("EMGdata.csv", Context.MODE_PRIVATE);
                    record=true;
                }catch(Exception e){System.out.println(e);}
            }
        });

        final Button stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record=false;
                try{
                    file.close();
                }catch(Exception e){System.out.println(e);}
            }
        });

        final Button buttonf1 = findViewById(R.id.button_f1);
        buttonf1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fatigue=1;
            }
        });
        final Button buttonf2 = findViewById(R.id.button_f2);
        buttonf2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fatigue=2;
            }
        });
        final Button buttonf3 = findViewById(R.id.button_f3);
        buttonf3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fatigue=3;
            }
        });
        final Button buttonf4 = findViewById(R.id.button_f4);
        buttonf4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fatigue=4;
            }
        });



        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestPermissions(permissions, WRITE_REQUEST_CODE);

    }

}
