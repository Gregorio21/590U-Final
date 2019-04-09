package com.example.assignment1;

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
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;


public class MainActivity extends Activity {
    private BluetoothAdapter bluetoothAdapter;
    final float time = System.currentTimeMillis();
    private int REQUEST_ENABLE_BT = 21;
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
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            TextView values = (TextView) findViewById(R.id.values);
                            values.setText(new String(characteristic.getValue()));
                        }
                    });

                    Log.d("data", new String(characteristic.getValue()));
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


    }

}
