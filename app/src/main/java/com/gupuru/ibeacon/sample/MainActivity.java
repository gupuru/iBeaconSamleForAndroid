package com.gupuru.ibeacon.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;


public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private static final String IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    private static final String UUID = "DCE285F8-27B9-4D0E-981E-CF9C675972ED";

    private BeaconManager beaconManager;
    private BeaconTransmitter beaconTransmitter;
    private Beacon beacon;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text_status);

        //マシュマロ判別
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //マシュマロ
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //未許可
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            }
        }

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_FORMAT));

        beacon = new Beacon.Builder()
                .setId1(UUID)
                .setId2("1")
                .setId3("80")
                .setManufacturer(0x004C)
                .build();
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout(IBEACON_FORMAT);
        beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.bind(this);
        if (!beaconTransmitter.isStarted()) {
            beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    super.onStartSuccess(settingsInEffect);
                    setTextView("Advertisement start succeeded");
                }

                @Override
                public void onStartFailure(int errorCode) {
                    setTextView("Advertisement start failed");
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconManager.unbind(this);
        beaconTransmitter.stopAdvertising();
    }

    @Override
    public void onBeaconServiceConnect() {
        final Identifier uuid = Identifier.parse(UUID);
        final Region mRegion = new Region("ibeacon", uuid, null, null);

        beaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                setTextView("didEnterRegion");
                try {
                    beaconManager.startRangingBeaconsInRegion(mRegion);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                setTextView("didExitRegion");
                try {
                    beaconManager.stopRangingBeaconsInRegion(mRegion);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                setTextView("didDetermineStateForRegion");
            }
        });

        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                // 検出したビーコンの情報を全部Logに書き出す
                for(Beacon beacon : beacons) {
                    String message = "UUID:" + beacon.getId1() + ", major:" + beacon.getId2()
                            + ", minor:" + beacon.getId3() + ", Distance:" + beacon.getDistance()
                            + ",RSSI" + beacon.getRssi() + ", Name:" + beacon.getBluetoothName();
                    setTextView(message);
                }
            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(mRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void setTextView(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(textView.getText().toString() + "\n\n" + message);
            }
        });
    }

}
