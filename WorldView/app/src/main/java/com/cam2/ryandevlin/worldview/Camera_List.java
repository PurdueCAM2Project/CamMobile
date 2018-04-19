package com.cam2.ryandevlin.worldview;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hussni on 3/30/2018.
 * This is the list activity for Search Cameras function
 */

public class Camera_List extends AppCompatActivity {
    private final static String TAG ="Camera_List";
    private ArrayAdapter adapter;
    ArrayList<Camera> filtered_cams = new ArrayList<Camera>();
    ListView list = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cam_list);
        list = findViewById(R.id.camList);
        EditText theFilter = findViewById(R.id.searchFilter);
        Intent Incomingintent = getIntent();
        Bundle bun = Incomingintent.getExtras();
        final ArrayList<Camera> newcam = (ArrayList<Camera>) bun.getSerializable("cameras");
        filtered_cams = (ArrayList<Camera>) bun.getSerializable("cameras");
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, newcam);
        list.setAdapter(adapter);
        final int camlength = newcam.size();


        theFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                (Camera_List.this).adapter.getFilter().filter(s);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Camera curr_camera = (Camera) adapter.getItem(position);
                Log.d(TAG, "onMarkerClick: sets camera object");
                String str = curr_camera.source_url;
                Log.d(TAG, "onMarkerClick: get url string");
                Intent intent = new Intent(Camera_List.this,web_cam.class);
                intent.putExtra("source",str);
                Log.d(TAG, "onMarkerClick: put Extra in intent");
                startActivity(intent);

            }
        });



    }
}