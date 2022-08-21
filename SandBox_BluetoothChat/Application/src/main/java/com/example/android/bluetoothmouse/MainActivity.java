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


package com.example.android.bluetoothmouse;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android.common.activities.SampleActivityBase;

import java.util.ArrayList;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class MainActivity extends SampleActivityBase {
    public static final String TAG = "MainActivity";

    private BleMouse mouse = new BleMouse(this);

    final ArrayList<Bdevice> ListCardsArrayList = new ArrayList<>();

    private void listDevices() {
        ListCardsArrayList.clear();
        ListCardsArrayList.addAll(mouse.listDevices());
    }

    public boolean click(MotionEvent motionEvent, int command) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mouse.clickCommand(command);
                break;
            case MotionEvent.ACTION_UP:
                mouse.clickReleaseCommand();
            case MotionEvent.ACTION_CANCEL:
                mouse.clickReleaseCommand();
                break;
        }
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mouse.getProxy();

        setContentView(R.layout.fragment_bluetooth_control);

        RecyclerView recV = findViewById(R.id.recview);
        BDeviceCardAdapter cardAdapter = new BDeviceCardAdapter(MainActivity.this, ListCardsArrayList, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bdevice element = ListCardsArrayList.get(i);
                mouse.btConnect(element.deviceName);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recV.setLayoutManager(linearLayoutManager);
        recV.setAdapter(cardAdapter);


        Button bt2 = findViewById(R.id.ListConnButton);
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listDevices();
                cardAdapter.notifyDataSetChanged();
            }
        });

        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(50);
        seekBar.setProgress(30);

        JoystickView joystick = (JoystickView) findViewById(R.id.joystickView);
        joystick.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {

                if(strength < 20){
                    return;
                }
                strength = strength ^2;
                int x = (int) (strength * Math.cos(Math.toRadians(angle)));
                int y = (int) (strength * Math.sin(Math.toRadians(angle)));
                System.out.println(angle+" "+strength+ " " + x +" "+ y);
                int scale = seekBar.getMax() - seekBar.getProgress() + 1;

                mouse.moveCommand(x/scale, -y/scale);
//                System.out.println(x+" "+y);
            }
        }, 17);



        Button bt4 = findViewById(R.id.command);
        bt4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return click(motionEvent, 1);
            }
        });

        Button bt5 = findViewById(R.id.lclickButton);
        bt5.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return click(motionEvent, 2);
            }
        });

    }
}
