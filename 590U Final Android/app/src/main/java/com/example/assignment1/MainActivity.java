package com.example.assignment1;

import android.Manifest;
import android.app.Activity;
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
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileOutputStream;
import java.util.UUID;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import weka.core.DenseInstance;
import weka.classifiers.Classifier;
import android.content.res.AssetManager;

public class MainActivity extends Activity {
    private BluetoothAdapter bluetoothAdapter;
    final float time = System.currentTimeMillis();
    private int REQUEST_ENABLE_BT = 21;
    private final int WRITE_REQUEST_CODE = 1;
    private int window = 0;
    private FileOutputStream file;
    private String emg_val = "";
    private Boolean record = false;
    private int fatigue = 1;
    private DescriptiveStatistics stats = new DescriptiveStatistics(10);
    private final int windowSize = 5;
    private double[] features = new double[6];
    private ImageView bicep;
    private int[] images = new int[4];
    private Classifier model;


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
                    record=true;
                    if(record && !emg_val.isEmpty()) {
                        Long tsLong = System.currentTimeMillis()/1000;
                        String ts = tsLong.toString();
                        byte b[] = String.format("%s,%s,%d\n", emg_val, ts, fatigue).getBytes();//converting string into byte array
                        try {
                            //file.write(b);
                            double val = Double.parseDouble(emg_val);
                            stats.addValue(val);
                            window = (window + 1)%windowSize;
                            if(stats.getN() >= 10 && window == 0){
                                features = new double[3];
                                features[0] = stats.getMean();
                                features[1] = stats.getVariance();
                                //features[2] = stats.getMax();
                                //features[3] = stats.getMin();
                                features[2] = stats.getMax() - stats.getMin();
                                Log.d("Mean", Double.toString(features[0]));
                                Log.d("Var", Double.toString(features[1]));
                                Log.d("Range", Double.toString(features[2]));
                                DenseInstance instance = new DenseInstance(1.0,features);
                                double prediction = model.classifyInstance(instance);
                                //features[5] = stats.getStandardDeviation();
                                //##################################################
                                //Put classifier here classify(features)
                                //int level =  (int)(2E-06*val*val - 0.005*val + 3.9831);
                                Log.d("Prediction", Double.toString(prediction));
                                TextView values2 = findViewById(R.id.values2);
                                values2.setText(String.format("%.2f", prediction));
                                bicep.setImageResource(images[(int)(prediction)]);
                            }
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
        images[0] = R.mipmap.blue_foreground;
        images[1] = R.mipmap.green_foreground;
        images[2] = R.mipmap.orange_foreground;
        images[3] = R.mipmap.red_foreground;
        bicep = findViewById(R.id.imageView2);
        AssetManager assetManager = getAssets();
        try {

            model = (Classifier) weka.core.SerializationHelper.read(assetManager.open("model.pt"));
        }

        catch (Exception e){
            Log.d("FAIL", "FAILED TO LOAD MODEL");
        }
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
        startButton.setVisibility(View.GONE);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //file = openFileOutput("EMGdata.csv", Context.MODE_PRIVATE);
                    record=true;
                    stats.clear();
                    window = 0;
                }catch(Exception e){System.out.println(e);}
            }
        });

        final Button stopButton = findViewById(R.id.stop_button);
        stopButton.setVisibility(View.GONE);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record=false;
                try{
                    //file.close();
                    stats.clear();
                    window = 0;
                }catch(Exception e){System.out.println(e);}
            }
        });

        final Button buttonf1 = findViewById(R.id.button_f1);
        buttonf1.setVisibility(View.GONE);
        /*buttonf1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fatigue=1;
            }
        });*/
        final Button buttonf2 = findViewById(R.id.button_f2);
        buttonf2.setVisibility(View.GONE);
        /*buttonf2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fatigue=2;
            }
        });*/
        final Button buttonf3 = findViewById(R.id.button_f3);
        buttonf3.setVisibility(View.GONE);
        /*buttonf3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fatigue=3;
            }
        });*/
        final Button buttonf4 = findViewById(R.id.button_f4);
        buttonf4.setVisibility(View.GONE);
        /*buttonf4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fatigue=4;
            }
        });*/



        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestPermissions(permissions, WRITE_REQUEST_CODE);

    }

}
