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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.RequiresApi;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
// * { @ link Fragment} which can display a view.
// * <p>
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
    String[] ListElements = new String[] {
            "Hello",
            "List created",
    };

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothHidDevice mBlHidDevice;
    private BluetoothDevice mBtDevice;

    private void getProxy(List<String> ListElementsArrayList, ArrayAdapter<String> adapter) {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        }
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            onScreenLog(ListElementsArrayList,  adapter, "Bluetooth is not available");
        }

        mBluetoothAdapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.HID_DEVICE) {
                    Log.d(TAG, "Got HID device");
                    mBlHidDevice = (BluetoothHidDevice) proxy;
                    // see next section
                    registerApp(ListElementsArrayList , adapter, mBlHidDevice);
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
    public synchronized void registerApp(List<String> ListElementsArrayList, ArrayAdapter<String> adapter, final BluetoothHidDevice bleHidD){

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
            public void onSetReport (BluetoothDevice device,
                                     byte type,
                                     byte id,
                                     byte[] data){
                onScreenLog(ListElementsArrayList, adapter, "asked for set Report");
            }

            @Override
            public void onConnectionStateChanged(BluetoothDevice device, final int state) {
                onScreenLog(ListElementsArrayList, adapter, "onConnectionStateChanged: device= " + device + " state= " + state);
//                android.util.Log.v(TAG, "onConnectionStateChanged: device=" + device + " state=" + state);
            }
        });

        onScreenLog(ListElementsArrayList, adapter, registered?"app registered" : "registration failed :(");
    }

    private void listDevices(List<String> ListElementsArrayList, ArrayAdapter<String> adapter){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device : pairedDevices){
            onScreenLog(ListElementsArrayList,  adapter,  "name:" + device.getName()+ " " + mBlHidDevice.getConnectionState(device));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private void btConnect(List<String> ListElementsArrayList, ArrayAdapter<String> adapter) {
        BluetoothDevice device;
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for(BluetoothDevice device1 : pairedDevices){
            if(device1.getName().equals("MANRHIOWO")){
                onScreenLog(ListElementsArrayList,  adapter, "got it");
                device = device1;

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
                    onScreenLog(ListElementsArrayList, adapter, connected?"connected!":"failed to connect :(");
                }
            }
        }


    }


    private void moveCommand(List<String> ListElementsArrayList, ArrayAdapter<String> adapter){
        byte[] data = new byte[]{
                (byte)0, (byte)0x14, (byte)0x14,
        };

        Boolean sent = mBlHidDevice.sendReport(mBtDevice, 0, data);
        onScreenLog(ListElementsArrayList, adapter, sent?"command sent":"command failed to send");

    }

    private void clickCommand(List<String> ListElementsArrayList, ArrayAdapter<String> adapter){
        byte[] data = new byte[]{
                (byte)0b1, (byte)0, (byte)0,
        };

        Boolean sent = mBlHidDevice.sendReport(mBtDevice, 0, data);
        onScreenLog(ListElementsArrayList, adapter, sent?"command sent":"command failed to send");
    }

    private void onScreenLog(List<String> ListElementsArrayList, ArrayAdapter<String> adapter, String s){
        ListElementsArrayList.add(s);
        adapter.notifyDataSetChanged();
        System.out.println( s);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_bluetooth_chat);

        ListView listview = findViewById(R.id.listV1);

        final List<String> ListElementsArrayList = new ArrayList<>(Arrays.asList(ListElements));
        final ArrayAdapter<String> adapter = new ArrayAdapter<>
                (MainActivity.this, android.R.layout.simple_list_item_1, ListElementsArrayList);
        listview.setAdapter(adapter);


        Button bt = findViewById(R.id.button_send);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScreenLog(ListElementsArrayList,  adapter, "proxy button clicked");
                getProxy(ListElementsArrayList,  adapter);
            }
        });

        Button bt2 = findViewById(R.id.ListConnButton);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScreenLog(ListElementsArrayList,  adapter, "listing devices: ");
                listDevices(ListElementsArrayList,  adapter);
            }
        });

        Button bt3 = findViewById(R.id.button2);
        bt3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScreenLog(ListElementsArrayList,  adapter, "bt2 button clicked");
                btConnect(ListElementsArrayList,  adapter);
            }
        });

        Button bt4 = findViewById(R.id.command);
        bt4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScreenLog(ListElementsArrayList,  adapter, "move button clicked");
                moveCommand(ListElementsArrayList,  adapter);
            }
        });

        Button bt5 = findViewById(R.id.lclickButton);
        bt5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScreenLog(ListElementsArrayList,  adapter, "click button clicked");
                clickCommand(ListElementsArrayList,  adapter);
            }
        });



        if (savedInstanceState == null) {
            onScreenLog(ListElementsArrayList,  adapter, "no saved instance");
        }else{
            onScreenLog(ListElementsArrayList,  adapter, "saved instance");
        }
    }



}
