package com.btapp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import com.btapp.BTMainActivity;
import com.btapp.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothConnectionService extends Service {

    //BLUETOOTH
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;

    final byte delimiter = 33;
    int readBufferPosition = 0;

    private workerThread wt = null;
    private Thread thread = null;

    public BluetoothConnectionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final Handler handler = new Handler();


        wt = new workerThread(null, handler);

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBluetooth);
            //startActivityForResult(enableBluetooth, 0);

        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("raspberrypi")) //Note, you will need to change this to match the name of your device
                {
                    Log.e("Labyrintspelet", device.getName());
                    mmDevice = device;
                    break;
                }
            }

        }

        doAction("Starting device");
       // return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {

        super.onDestroy();
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

    final class workerThread implements Runnable {

        private String btMsg;
        private Handler handler;
        private TextView myLabel;

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
                        byte[] readBuffer = new byte[1024];
                        mmInputStream.read(packetBytes);

                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = packetBytes[i];
                            if (b == delimiter) {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                final String data = new String(encodedBytes, "US-ASCII");
                                readBufferPosition = 0;

                                //The variable data now contains our full command
                                /*handler.post(new Runnable() {
                                    public void run() {
                                        myLabel.setText(data);
                                    }
                                });*/

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
