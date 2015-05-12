package me.dawars.sandbox;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;

import orbotix.robot.base.Robot;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.Sphero;
import orbotix.view.connection.SpheroConnectionView;


public class SpheroTiltActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

    private SpheroConnectionView mSpheroConnectionView;
    private Sphero mSphero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sphero_tilt);

        //Sensor
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);


        mSpheroConnectionView = (SpheroConnectionView) findViewById(R.id.sphero_connection_view);

        ConnectionListener mConnectionListener = new ConnectionListener() {
            @Override
            public void onConnected(Robot sphero) {
                mSpheroConnectionView.setVisibility(View.INVISIBLE);
                mSphero = (Sphero) sphero;

                mSphero.setBackLEDBrightness(1.0f);
                mSphero.setColor(0, 0, 255);

            }

            @Override
            public void onConnectionFailed(Robot sphero) {
            }

            @Override
            public void onDisconnected(Robot sphero) {
                mSpheroConnectionView.startDiscovery();
            }
        };
        mSpheroConnectionView.addConnectionListener(mConnectionListener);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float gravityX = event.values[0];
        float gravityY = event.values[1];
        float gravityZ = event.values[2];

        if (mSphero != null) {
            float speed = (float) (Math.sqrt(gravityY * gravityY + gravityX * gravityX) / 10);
            float degree = (float) Math.toDegrees(Math.atan2(-gravityX, -gravityY)) + 180F;
            mSphero.drive(degree, speed / 2F);

            mSphero.setColor(getColorValue(gravityX), getColorValue(gravityY), getColorValue(gravityZ));

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private int getColorValue(float value) {
        return (int) (10F / (value) * 255F);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        mSpheroConnectionView.startDiscovery();
    }

    @Override
    protected void onDestroy() {
        mSpheroConnectionView.clearListeners();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        if (mSphero != null) {
            mSphero.disconnect(); // Disconnect Robot properly
        }

        mSensorManager.unregisterListener(this, mSensor);
    }

}
