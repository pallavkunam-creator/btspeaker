package com.kdev.btspeaker;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int A2DP_SINK_PROFILE = 11;
    private static final int REQUEST_PERMISSIONS = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int VERSION_S = 31;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothProfile a2dpSinkProxy;
    private TextView tvStatus, tvDevice;
    private Button btnToggle;
    private boolean isRunning = false;

    private BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    try { updateDevice("Connected: " + device.getName()); }
                    catch (Exception e) { updateDevice("Connected: Unknown"); }
                    updateStatus("Audio streaming active!");
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                updateDevice("None");
                updateStatus("Waiting for connection...");
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) stopSpeaker();
            }
        }
    };

    private BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == A2DP_SINK_PROFILE) {
                a2dpSinkProxy = proxy;
                updateStatus("Ready! Go to PC Bluetooth\nand pair with this phone");
                makeDiscoverable();
            }
        }
        @Override
        public void onServiceDisconnected(int profile) {
            if (profile == A2DP_SINK_PROFILE) a2dpSinkProxy = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvDevice = (TextView) findViewById(R.id.tvDevice);
        btnToggle = (Button) findViewById(R.id.btnToggle);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            updateStatus("Bluetooth not supported!");
