/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.example.android.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHidDevice;
import android.bluetooth.BluetoothHidDeviceAppSdpSettings;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import io.github.controlwear.virtual.joystick.android.JoystickView;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * // * { @ link Fragment} which can display a view.
 * // * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MainActivity extends SampleActivityBase {

    private static final byte[] descriptor = new byte[]{
            0x05, 0x01,                    // USAGE_PAGE (Generic Desktop)
            0x09, 0x02,                    // USAGE (Mouse)
            (byte) 0xa1, 0x01,                    // COLLECTION (Application)
            0x09, 0x01,                    //   USAGE (Pointer)
            (byte) 0xa1, 0x00,                    //   COLLECTION (Physical)
            0x05, 0x09,                    //     USAGE_PAGE (Button)
            0x19, 0x01,                    //     USAGE_MINIMUM (Button 1)
            0x29, 0x03,                    //     USAGE_MAXIMUM (Button 3)
            0x15, 0x00,                    //     LOGICAL_MINIMUM (0)
            0x25, 0x01,                    //     LOGICAL_MAXIMUM (1)
            (byte) 0x95, 0x03,                    //     REPORT_COUNT (3)
            0x75, 0x01,                    //     REPORT_SIZE (1)
            (byte) 0x81, 0x02,                    //     INPUT (Data,Var,Abs)
            (byte) 0x95, 0x01,                    //     REPORT_COUNT (1)
            0x75, 0x05,                    //     REPORT_SIZE (5)
            (byte) 0x81, 0x03,                    //     INPUT (Cnst,Var,Abs)
            0x05, 0x01,                    //     USAGE_PAGE (Generic Desktop)
            0x09, 0x30,                    //     USAGE (X)
            0x09, 0x31,                    //     USAGE (Y)
            0x15, (byte) 0x81,                    //     LOGICAL_MINIMUM (-127)
            0x25, 0x7f,                    //     LOGICAL_MAXIMUM (127)
            0x75, 0x08,                    //     REPORT_SIZE (8)
            (byte) 0x95, 0x02,                    //     REPORT_COUNT (2)
            (byte) 0x81, 0x06,                    //     INPUT (Data,Var,Rel)
            (byte) 0xc0,                          //   END_COLLECTION
            (byte) 0xc0                           // END_COLLECTION

    };

    public static final String TAG = "MainActivity";
    String[] ListElements = new String[]{
            "Hello",
            "List created",
    };

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothHidDevice mBlHidDevice;
    private BluetoothDevice mBtDevice;

    final List<String> ListElementsArrayList = new ArrayList<>(Arrays.asList(ListElements));

    private void getProxy() { //List<String> ListElementsArrayList, ArrayAdapter<String> adapter
        // If the adapter is null, then Bluetooth is not supported
        System.out.println("getting Proxy");

        if (mBluetoothAdapter == null) {
//            onScreenLog(ListElementsArrayList,  adapter, "Bluetooth is not available");
            System.out.println("Bluetooth is not available");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        }


        mBluetoothAdapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    Log.d(TAG, "Got HID device");
                    mBlHidDevice = (BluetoothHidDevice) proxy;
                    // see next section
                    registerApp(mBlHidDevice);
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    Log.d(TAG, "Lost HID device");
                }
            }
        }, BluetoothProfile.HID_DEVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public synchronized void registerApp(final BluetoothHidDevice bleHidD) {

        BluetoothHidDeviceAppSdpSettings sdp = new BluetoothHidDeviceAppSdpSettings(
                "BlueExp",
                "Android HID Foray",
                "Android",
                BluetoothHidDevice.SUBCLASS1_MOUSE,
                descriptor
        );
//        Executor executor = Executors.newSingleThreadExecutor();
        ///TODO: Replace below executor with above in-line
        Executor executor = new Executor() {
            @Override
            public void execute(Runnable runnable) {
                runnable.run();
                listDevices();
            }
        };

        ///TODO: Just use Executors.newsimgletrheadexectur() here
        Boolean registered = bleHidD.registerApp(sdp, null, null, executor, new BluetoothHidDevice.Callback() {
            @Override
            public void onGetReport(BluetoothDevice device, byte type, byte id, int bufferSize) {

                android.util.Log.v(TAG, "onGetReport: device=" + device + " type=" + type
                        + " id=" + id + " bufferSize=" + bufferSize);
            }

            @Override
            public void onSetReport(BluetoothDevice device,
                                    byte type,
                                    byte id,
                                    byte[] data) {
//                onScreenLog(ListElementsArrayList, adapter, "asked for set Report");
            }

            @Override
            public void onConnectionStateChanged(BluetoothDevice device, final int state) {
//                onScreenLog(ListElementsArrayList, adapter, "onConnectionStateChanged: device= " + device + " state= " + state);
//                android.util.Log.v(TAG, "onConnectionStateChanged: device=" + device + " state=" + state);
            }
        });

//        onScreenLog(ListElementsArrayList, adapter, registered?"app registered" : "registration failed :(");
        System.out.println("hid profile registered");
    }

    private void listDevices() {
        List<String> listo = mBluetoothAdapter.getBondedDevices().stream()
                .map(d -> String.format("name: '%s' %s", d.getName(), mBlHidDevice.getConnectionState(d)))
                .collect(Collectors.toList());
        ListElementsArrayList.clear();
        ListElementsArrayList.addAll(listo);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void btConnect(String deviceName) {
        BluetoothDevice device;
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        device = mBluetoothAdapter.getBondedDevices().stream()
                .filter(d -> d.getName().equals(deviceName)).findAny().orElse(null);

        android.util.Log.i(TAG, "btConnect: device=" + device);

        // disconnect from everything else
        for (BluetoothDevice btDev : mBlHidDevice.getDevicesMatchingConnectionStates(new int[]{
                BluetoothProfile.STATE_CONNECTING,
                BluetoothProfile.STATE_CONNECTED
        })) {
            mBlHidDevice.disconnect(btDev);
        }
        if (device != null) {
            mBtDevice = device;
            Boolean connected = mBlHidDevice.connect(device);
            System.out.println(connected ? "connected! " : "failed to connect " + deviceName);
        }
    }


    private void moveCommand(int x, int y) {
        char signedx = (char) x;
        char signedy = (char) y;
        byte[] data = new byte[]{
                (byte) 0, (byte) (signedx & 0xFF), (byte) (signedy & 0xFF),
        };

        Boolean sent = mBlHidDevice.sendReport(mBtDevice, 0, data);
    }

    private void clickCommand(int click) { // 1= L, 2 = R
        byte[] data = (click == 1) ?
                new byte[]{(byte) 0b1, (byte) 0, (byte) 0,}
                        :
                new byte[]{(byte) 0b10, (byte) 0, (byte) 0,};

        Boolean sent = mBlHidDevice.sendReport(mBtDevice, 0, data);
    }

    private void clickReleaseCommand() {
        byte[] data = new byte[]{
                (byte) 0, (byte) 0, (byte) 0,
        };
        Boolean sent = mBlHidDevice.sendReport(mBtDevice, 0, data);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getProxy();

        setContentView(R.layout.fragment_bluetooth_chat);

        RecyclerView recV = findViewById(R.id.recview);

        ListView listview = findViewById(R.id.listV1);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>
                (MainActivity.this, android.R.layout.simple_list_item_1, ListElementsArrayList);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String element = ListElementsArrayList.get(i);
                String deviceName = element.substring(element.indexOf("'") + 1, element.lastIndexOf("'"));
                btConnect(deviceName);
            }
        });
        Button bt2 = findViewById(R.id.ListConnButton);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listDevices();
                adapter.notifyDataSetChanged();
            }
        });


        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {

                if(strength < 20){
                    return;
                }
                int x = (int) (strength * Math.cos(Math.toRadians(angle)));
                int y = (int) (strength * Math.sin(Math.toRadians(angle)));
                System.out.println(angle+" "+strength+ " " + x +" "+ y);
                moveCommand(x/10, -y/10);
//                System.out.println(x+" "+y);
            }
        }, 17);

        Button bt4 = findViewById(R.id.command);
        bt4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        clickCommand(1);
                        break;
                    case MotionEvent.ACTION_UP:
                        clickReleaseCommand();
                    case MotionEvent.ACTION_CANCEL:
                        clickReleaseCommand();
                        break;
                }
                return false;
            }
        });

        Button bt5 = findViewById(R.id.lclickButton);
        bt5.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        clickCommand(2);
                        break;
                    case MotionEvent.ACTION_UP:
                        clickReleaseCommand();
                    case MotionEvent.ACTION_CANCEL:
                        clickReleaseCommand();
                        break;
                }
                return false;
            }
        });

    }
}
