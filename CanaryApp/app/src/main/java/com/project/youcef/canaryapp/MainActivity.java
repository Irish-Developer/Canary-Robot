package com.project.youcef.canaryapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.View;

import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


/**************************************************
 * Title: Main Activity
 * Name: Youcef O'Connor
 * Student number: x13114557
 * Date: 27/04/2017
 **************************************************/
public class MainActivity extends AppCompatActivity implements Response.Listener, Response.ErrorListener{

    private RequestQueue mQueue;
    private TextView view;
    private VideoView myVideoView;
    private ProgressDialog proDialog;
    HashMap<String, String> myHashmap = new HashMap();
    ToggleButton toggle1, toggle2, toggle3;

//    Declaring my LAN address to Robot Camera
    String VidURL = "http://192.168.0.13:1685/?action=stream";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("myTag","onCreate ");


        toggle1 = (ToggleButton) findViewById(R.id.lightsBtn);
        toggle2 = (ToggleButton) findViewById(R.id.panBtn);
        toggle3 = (ToggleButton) findViewById(R.id.tiltBtn);
        view = (TextView) findViewById(R.id.textView);


        String url = "https://dweet.io/get/latest/dweet/for/canary30_iot?";                   //this is where the dweets are received from

        mQueue = Queue.getInstance(this.getApplicationContext())                            //This code was provided my lecturer Dominic Carr
                .getRequestQueue();                                                         //It uses the Volley HTTP networking library to make calls to dweet

        final MyJSONRequest jsonRequest = new MyJSONRequest(Request.Method.GET, url,        //This code was provided my lecturer Dominic Carr
                new JSONObject(), this, this);                                              //Requests JSON data from my dweet thing
        Log.d("myTag","JSONObject " +jsonRequest);
        jsonRequest.setTag("test");
        mQueue.add(jsonRequest);


    }




    //////////////////////////////////////////  Switch On & off lights //////////////////////////////////////////////
    public void lightsBtn(View v) {
        Boolean myLight = Boolean.valueOf(myHashmap.get("lights"));                          // this simply takes the current state of the sensor (stored in myHashmap) and the toggle button.
        myHashmap.put("lights", String.valueOf(!myLight));                                   //When the toggle button is pressed the value and button state is flipped or switched to its opposite
        Log.d("myTag","myLight " +myLight);
        JSONObject outputJson = new JSONObject(myHashmap);                                  //The new state is stored in myHashmap
        Log.d("myTag","JSONObject " +outputJson);
        mQueue = Queue.getInstance(this.getApplicationContext())                            //It uses the Volley HTTP networking library to make calls to dweet
                .getRequestQueue();
        Log.d("myTag","Oueue " +mQueue);
        String url = "https://dweet.io/dweet/for/canary30_iot";                               //This is where the JSON (dweet)is sent too

        final MyJSONRequest jsonRequest = new MyJSONRequest(Request.Method.POST, url,       //Posts sensor boolean commands in JSON to my dweet thing
                outputJson, this, this);
        jsonRequest.setTag("test");
        Log.d("myTag","Lights JSONRequest " +jsonRequest);
        mQueue.add(jsonRequest);
    }

    ///////////////////////////////////////////// Pan /////////////////////////////////////////////////////////////
    public void panBtn(View v) {
        Boolean myPan = Boolean.valueOf(myHashmap.get("pan"));
        myHashmap.put("pan", String.valueOf(!myPan));

        JSONObject outputJson = new JSONObject(myHashmap);

        mQueue = Queue.getInstance(this.getApplicationContext())
                .getRequestQueue();

        String url = "https://dweet.io/dweet/for/canary30_iot";

        final MyJSONRequest jsonRequest = new MyJSONRequest(Request.Method.POST, url,
                outputJson, this, this);
        jsonRequest.setTag("test");
        mQueue.add(jsonRequest);
    }
//
//    /////////////////////////////////// Tilt Camera  ///////////////////////////
    public void tiltBtn(View v) {
        Boolean myTemp = Boolean.valueOf(myHashmap.get("tilt"));
        myHashmap.put("tilt", String.valueOf(!myTemp));

        JSONObject outputJson = new JSONObject(myHashmap);
        String url = "https://dweet.io/dweet/for/canary30_iot";

        mQueue = Queue.getInstance(this.getApplicationContext())
                .getRequestQueue();

        final MyJSONRequest jsonRequest = new MyJSONRequest(Request.Method.POST, url,
                outputJson, this, this);
        jsonRequest.setTag("test");
        mQueue.add(jsonRequest);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResponse(Object response) {                       //I had help with developing the onResponse and myToggle methods from student Rich Mangain
        Log.d("myTag","onResponse ");                               //On a previous IoT project
        view.setText(response.toString());                          //please note; he did not give me code and I did not copy any code
        String jsonString = response.toString();                    //Rich told me the direction and steps I needed to take and I coded it myself
        Log.d("myTag","onResponse " +jsonString);
        try {
            JSONObject obj1 = new JSONObject(jsonString);           //declaring an instance of JSONObject - Received Dweet
            Log.d("myTag","JSONObject: " + obj1);
            JSONArray myArray = obj1.getJSONArray("with");          //Opening the array layer named "with" of the dweet
            Log.d("myTag","Array: " + myArray);
            JSONObject layer2 = myArray.getJSONObject(0);           //layer2 is being positioned at position 0 of the array
            Log.d("myTag","Layer2: " + layer2);
            JSONObject layer3 = layer2.getJSONObject("content");    //declaring the JSONObject "content" that is in position 0 of the array. this is the data use
            Log.d("myTag","JSON: " + layer3);

            String objLight = layer3.getString("lights");            //Gets the string "lights" and it's value from the JSONObtect "content"
            Log.d("myTag","lights object: " + objLight);
            String objPan = layer3.getString("pan");                //Gets the string "pan" and it's value from the JSONObtect "content"
            String objTilt = layer3.getString("tilt");              //Gets the temp "tilt" and it's value from the JSONObtect "content"

            myHashmap.put("lights", objLight);                      //Then the string and their values are stored in a hashmap (myHashmap)
            Log.d("myTag","Hashmap lights " + myHashmap);
            myHashmap.put("pan", objPan);
            myHashmap.put("tilt", objTilt);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        myToggle();
    }
//
    public void myToggle() {
        if (myHashmap.containsKey("lights")) {                        //This is where the Toggle switch values are set
            String Temp = myHashmap.get("lights");
            if (Temp.equals("true")) {                                //If True
                toggle1.setChecked(true);                             //Set toggle1 to true
            } else {                                                  //else
                toggle1.setChecked(false);                            //Set toggle1 to false
            }
        }
        if (myHashmap.containsKey("pan")) {
            String Temp = myHashmap.get("pan");
            if (Temp.equals("true")) {
                toggle2.setChecked(true);
            } else {
                toggle2.setChecked(false);
            }
        }
        if (myHashmap.containsKey("tilt")) {
            String Temp = myHashmap.get("tilt");
            if (Temp.equals("true")) {
                toggle3.setChecked(true);
            } else {
                toggle3.setChecked(false);
            }
        }
    }
}
