package com.btapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static com.btapp.Utils.convertToJSON;

public class BTMainActivity extends Activity {

    //BLUETOOTH
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice = null;

    private int count = 1;
    private boolean isConnected;

    private final byte delimiter = 33;
    private int readBufferPosition = 0;

    private workerThread wt = null;
    private Thread thread = null;

    //GYROSCOPE or ACCELEROMETER
    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private SensorEventListener gyroEventListener;
    private Sensor senAccelerometer;
   private Accelerometer ac;

    //GUI
    private boolean gameOn; //has the game started or not????


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ACCELEROMETER
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//        sensorManager.registerListener((SensorEventListener) this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        //BLUETOOTH
        final Handler handler = new Handler();

        final TextView myLabel = (TextView) findViewById(R.id.coordinates);
        final Button startButton = (Button) findViewById(R.id.startbutton);
        final Button ITButton = (Button) findViewById(R.id.ITButton);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        wt = new workerThread(null, handler);

        if(senAccelerometer == null){
            Toast.makeText(this, "This device does not support Accelerometer", Toast.LENGTH_LONG).show();
            finish();
        }


        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v){
                String start = "START";
                String stop = "STOP";
                if(startButton.getText().equals(start)){
                   // onResume();
                    gameOn = true;
                    Toast.makeText(getContext(), "Game started!", Toast.LENGTH_SHORT).show();

                    startButton.setText(stop);
                    startButton.setBackgroundColor(Color.parseColor("#ff0000"));
                }else{
                    gameOn = false;
                    //onPause();
                    startButton.setText(start);
                    Toast.makeText(getContext(), "Game stopped!", Toast.LENGTH_SHORT).show();
                    startButton.setBackgroundColor(Color.parseColor("#1be81f"));
                }

            }});




        checkBluetooth(mBluetoothAdapter);

        ac = new Accelerometer();


        ac.onSensorChanged(ac.getSensorEvent());

        if(gameOn && ac.isSensorAc(ac.getSensor())){
            Handler hand = new Handler();
            hand.postDelayed(new Runnable() {
                @Override
                public void run() {
                    doAction(convertToJSON("x",ac.getX(),"y",ac.getY(),"z",ac.getZ()));
                    count++;
                }
            },500);


        }
    }

    public Context getContext(){
        return this;
    }

    public void checkBluetooth(BluetoothAdapter mBluetoothAdapter){
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size()>0){
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("raspberrypi")) //Note, you will need to change this to match the name of your device
                {
                    Log.e("Labyrintspelet", device.getName());
                    mmDevice = device;
                    break;
                }
            }

        }

    }


    public void startSocket(String msg2send) {
        //UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        // UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"); //Standard SerialPortService ID
        UUID uuid = UUID.fromString(("01195ea7-dc96-4750-9c34-4d28e110f201"));
        try {
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            System.out.println("Trying to connect");
            if (!mmSocket.isConnected()) {
                mmSocket.connect();
                System.out.println("Socket connected");
                isConnected = true;
            }

            String msg = msg2send;
            //msg += "\n";
            OutputStream mmOutputStream = mmSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());

        } catch (IOException e) {

        }

    }

    public void doAction(String action) {
        if(thread != null){
            System.out.println("HYPELORD " + thread.isAlive());
        }

        if(thread == null || !thread.isAlive()){
            wt.setBtMsg(action);
            thread = new Thread(wt);
            thread.start();
        } else {
            try {
                OutputStream mmOutputStream = mmSocket.getOutputStream();
                mmOutputStream.write(action.getBytes());
            } catch (IOException e) {

            }
        }

    }

   /* @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        boolean change = true;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            xValue = Float.toString(x);
            yValue = Float.toString(y);
            zValue = Float.toString(z);

            if(change && gameOn) {

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        doAction(convertToJSON("x",xValue,"y",yValue,"z",zValue));
                        count++;
                    }
                }, 500);



            }else{

            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }*/


    final class workerThread implements Runnable {

        private String btMsg;
        private Handler handler;


        public workerThread(String msg, Handler hr) {
            btMsg = msg;
            handler = hr;

        }

        public void setBtMsg(String msg) {
            btMsg = msg;
        }

        public void run() {
            startSocket(btMsg);
            while (!Thread.currentThread().isInterrupted()) {
                int bytesAvailable;
                boolean workDone = false;

                try {

                    final InputStream mmInputStream;
                    mmInputStream = mmSocket.getInputStream();
                    bytesAvailable = mmInputStream.available();
                    if (bytesAvailable > 0) {

                        byte[] packetBytes = new byte[bytesAvailable];
                        Log.e("Labyrintspel recv bt", "bytes available");
                        int amountOfData = count*1024; //number of times sent data times one byte
                        byte[] readBuffer = new byte[amountOfData];
                        mmInputStream.read(packetBytes);

                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = packetBytes[i];
                            if (b == delimiter) {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                final String data = new String(encodedBytes, "US-ASCII");
                                readBufferPosition = 0;
                                workDone = true;
                                break;


                            } else {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }

                        if (workDone == true) {
                            mmSocket.close();
                            break;
                        }

                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
    }


}