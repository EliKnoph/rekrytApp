package com.btapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;

import static com.btapp.Utils.convertToJSON;

public class Accelerometer implements SensorEventListener {

    private String xValue; //x-värdet från accelerometern som sträng etc..
    private String yValue;
    private String zValue;

    SensorEvent sens;
    Sensor sensor;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        setSensorEvent(sensorEvent);
        setSensor(mySensor);

        float x = sensorEvent.values[0];
        xValue = Float.toString(x);
        float y = sensorEvent.values[0];
        yValue = Float.toString(y);
        float z = sensorEvent.values[0];
        zValue = Float.toString(z);

    }

    public boolean isSensorAc(Sensor mySensor){

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            return true;

        }

        return false;

    }

    public void setSensor(Sensor sens){
        sensor = sens;

    }

    public Sensor getSensor(){
        return sensor;
    }


    public void setSensorEvent(SensorEvent sensorEvent){
        sens = sensorEvent;

    }

    public SensorEvent getSensorEvent(){

        return sens;

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public String getX(){

        return xValue;
    }

    public String getY(){
        return yValue;
    }

    public String getZ(){

        return zValue;
    }

    @Override
    public String toString() {
        return super.toString() + "x: " + xValue + "y: " + yValue + "z: " + zValue + sensor.toString() + sens.toString();
    }
}
