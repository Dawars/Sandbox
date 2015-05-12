package me.dawars.sandbox;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.nio.ByteBuffer;

import orbotix.robot.base.Robot;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.Sphero;
import orbotix.view.connection.SpheroConnectionView;


public class SpheroWearTiltActivity extends Activity implements
        MessageApi.MessageListener {

    private static final String ORIENTATION_MESSAGE_PATH = "/orientation";

    private GoogleApiClient mGoogleApiClient;

    private SpheroConnectionView mSpheroConnectionView;
    private Sphero mSphero;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sphero_wear_tilt);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Wearable.MessageApi.addListener(mGoogleApiClient, SpheroWearTiltActivity.this);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {

                    }
                })
                .build();

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
    protected void onResume() {
        super.onStart();
        mGoogleApiClient.connect();
        mSpheroConnectionView.startDiscovery();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        if (mSphero != null) {
            mSphero.disconnect(); // Disconnect Robot properly
        }

    }


    private void onOrientationDataChanged(final byte[] data) {

        ByteBuffer b = ByteBuffer.wrap(data);

        final float gravityX = b.getFloat();
        final float gravityY = b.getFloat();
        final float gravityZ = b.getFloat();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (mSphero != null) {
                    float speed = (float) (Math.sqrt(gravityY * gravityY + gravityX * gravityX) / 10);
                    float degree = (float) Math.toDegrees(Math.atan2(-gravityX, -gravityY)) + 180F;
                    mSphero.drive(degree, speed / 2F);

                    mSphero.setColor(getColorValue(gravityX), getColorValue(gravityY), getColorValue(gravityZ));

                }
            }
        });
    }

    private int getColorValue(float value) {
        return (int) (10F / (value) * 255F);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(ORIENTATION_MESSAGE_PATH)) {
            onOrientationDataChanged(messageEvent.getData());
        }
    }
}
