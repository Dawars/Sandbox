package me.dawars.sandbox;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.nio.ByteBuffer;
import java.util.Set;


public class SpheroWearTiltActivity extends Activity implements
        SensorEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = SpheroWearTiltActivity.class.getName();

    private static final String ORIENTATION_MESSAGE_PATH = "/orientation";
    private static final String ORIENTATION_CAPABILITY_NAME = "recieve_orientation";
    GoogleApiClient mGoogleApiClient;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private TextView mTextData;
    private String transcriptionNodeId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sphero_wear_tilt);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        mTextData = (TextView) findViewById(R.id.activity_wear_comm_data);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        // Now you can use the Data Layer API
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                        // Request access only to the Wearable API
                .addApi(Wearable.API)//ifAvailable for comp
                .build();
    }

    private void setupVoiceTranscription() {
        new Thread("CapCheck") {
            @Override
            public void run() {
                CapabilityApi.GetCapabilityResult result =
                        Wearable.CapabilityApi.getCapability(
                                mGoogleApiClient, ORIENTATION_CAPABILITY_NAME,
                                CapabilityApi.FILTER_REACHABLE).await();

                updateTranscriptionCapability(result.getCapability());


            }
        }.start();

        CapabilityApi.CapabilityListener capabilityListener =
                new CapabilityApi.CapabilityListener() {
                    @Override
                    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
                        updateTranscriptionCapability(capabilityInfo);
                    }
                };

        Wearable.CapabilityApi.addCapabilityListener(
                mGoogleApiClient,
                capabilityListener,
                ORIENTATION_CAPABILITY_NAME);
    }

    private void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();

        transcriptionNodeId = pickBestNodeId(connectedNodes);
    }

    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        byte[] data = new byte[4 * 3];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.putFloat(event.values[0]);
        bb.putFloat(event.values[1]);
        bb.putFloat(event.values[2]);

        requestTranscription(data);

        mTextData.setText("X: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
    }

    private void requestTranscription(byte[] voiceData) {
        if (transcriptionNodeId != null) {
            Wearable.MessageApi.sendMessage(mGoogleApiClient, transcriptionNodeId,
                    ORIENTATION_MESSAGE_PATH, voiceData).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.w(TAG, "Failed to send message");
                            }
                        }
                    }
            );
        } else {
            Log.w(TAG, "Unable to retrieve node with transcription capability");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        mGoogleApiClient.connect();

        setupVoiceTranscription();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mSensor);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

}
