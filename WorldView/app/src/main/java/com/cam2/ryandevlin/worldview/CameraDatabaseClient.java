package com.cam2.ryandevlin.worldview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;

/**
 * Class to manage interactions with the camera database
 * @author Rohith Ravindranath
 * @version 09/16/18
 */
public class CameraDatabaseClient {

    //GLOBAL VARIABLES
    String masterToken;
    String baseUrl ;
    String jsonUrlToken;
    Hashtable<String,Camera> cameraObjects;
    List <Marker> cameraMarkers;
    boolean firstRun;
    int maxCameras;

    public CameraDatabaseClient(){
        this.masterToken = new String();
        this.baseUrl = "https://cam2-api.herokuapp.com/";
        this.jsonUrlToken = "https://cam2-api.herokuapp.com/auth?clientID=5804518549e100481b100ac0fdfff756177ffd266fe181b6df02744ec2f0fe31befc6c67f93c844aaac3e05cce1f9087&clientSecret=b3f3eec2d6805dd2c6003003a938c068ba9ca6d5eca7cbaf258bfe18aaf989272423de536a356b32f7b48c6b6d89a460";
        this.cameraObjects = new Hashtable<>();
        this.cameraMarkers = new ArrayList<>();
        this.firstRun= true;
        this.maxCameras = 0;
    }


    /**
     * Getter Method for camera list
     * @return list of all cameras
     */
    public List<Camera> getCameras() {
        return new ArrayList<>(this.cameraObjects.values());
    }

    /**
     * Hides all the camera markers from the map
     * @return list of the camera markers
     */
    public List<Marker> hideAllCameraMarkers(){
        for(int i=0;i<this.cameraMarkers.size();i++) {
            this.cameraMarkers.get(i).remove();
        }
        this.cameraMarkers.removeAll(cameraMarkers);
        return this.cameraMarkers;
    }

    /**
     * 1. Contacts and connects to the camera database and receives the token
     * 2. Calls sub-methods that calls the databases REST API and converts the json response into camera objects and stores it
     * 3. Plots all the cameras by make a camera marker
     * @param  queue
     * @param context
     * @param map
     */
    public void initializeCameras (final RequestQueue queue, final Context context, final GoogleMap map, final LatLng coordinates, final double radius) {
        JsonObjectRequest objReq = new JsonObjectRequest(Request.Method.GET, jsonUrlToken, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    masterToken = response.getString("token");
                    Log.d("MASTER_TOKEN", masterToken);
                    if(firstRun){
                        Toast.makeText(context,"Downloading Camera Database. Please Wait....",Toast.LENGTH_LONG).show();
                        firstRun = false;
                    }
                    else{
                        Toast.makeText(context,"Updating local Cams. Please Wait....",Toast.LENGTH_LONG).show();
                    }


                    updateCameraList(masterToken, "&longitude=" + coordinates.longitude +
                                "&latitude=" + coordinates.latitude + "&radius=" + radius + "&type=ip",
                                queue, context, map);


                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    // Handles errors that occur due to Volley
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley", "Error with Token");
                        String body = error.getMessage();
                        Log.e("Volley_error", body);
                    }
                }
        );
        queue.add(objReq);
    }

    /**
     * 1. Contacts the camera database and receives the json response
     * 2. Generates camera objects from the json response
     * 3. Plots all the cameras in the list
     * @param  token
     * @param offset
     * @param queue
     * @param context
     * @param map
     */
    public void updateCameraList(String token, String offset, RequestQueue queue, final Context context, final GoogleMap map) {
        String search_url = baseUrl + "cameras/search?access_token=" + token + offset;

        JsonArrayRequest cam_arrayreq = new JsonArrayRequest(Request.Method.GET, search_url,new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                     List<Camera> list = generateCameraObjects(response);
                    plotCamerasOnMap(context, map, list);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley", "Error");
                    }
                }
        );
        queue.add(cam_arrayreq);
    }

    /**
     * From the json response, the method will convert the information into cameras and store them in a list
     * @param  response
     */
    public List<Camera> generateCameraObjects(JSONArray response) throws JSONException {
        int num_cameras = response.length();
        String temp = Integer.toString(num_cameras);
        Log.d("UPDATE", "camera updating");
        Log.d("UPDATE_MAS",masterToken);
        Log.d("UPDATE_NUM",temp);
        List<Camera> temp_list = new ArrayList<>();
        for (int i = 0; i < num_cameras; i++){
            JSONObject camera = response.getJSONObject(i);
            if (!(camera.getString("reference_url").equals("null"))) {
                Log.d("url" , camera.getString("reference_url"));
                String camera_id = camera.getString("cameraID");
                if (cameraObjects.get(camera_id) == null) {
                    double latitude = camera.getDouble("latitude");
                    double longitude = camera.getDouble("longitude");
                    String formatted_address = getFormattedAddress(latitude, longitude);
                    String source_url = camera.getString("reference_url");
                    Camera camera_obj = new Camera(camera_id, latitude, longitude, formatted_address, source_url);
                    this.cameraObjects.put(camera_obj.cameraID, camera_obj);
                    temp_list.add(camera_obj);
                    maxCameras++;
                }
            }
        }
        return temp_list;
    }

    /**
     * From the camera list, will create a marker for each camera and then plots the marker onto the map
     * @param  map
     * @param context
     */
    public void plotCamerasOnMap(Context context, GoogleMap map, List<Camera> list){
        List<Camera> cameras = new ArrayList<>(this.cameraObjects.values());
        Log.d("length", Integer.toString(cameras.size()));
        for(int i = 0; i < cameras.size(); i++) {
            Camera curr_camera = cameras.get(i);
            LatLng cam_location = new LatLng(curr_camera.latitude, curr_camera.longitude);
            Bitmap temp = BitmapFactory.decodeResource(context.getResources(),R.drawable.cam_marker);
            Bitmap custom_marker = Bitmap.createScaledBitmap(temp, 60, 100, true); //RESCALE BITMAP ICON TO PROPER SIZE
            MarkerOptions a = new MarkerOptions().position(cam_location).icon(BitmapDescriptorFactory.fromBitmap(custom_marker)).alpha(0.9f);
            Marker camera_marker = map.addMarker(a);
            camera_marker.setTag("cam");
            camera_marker.setSnippet(""+i);
            cameraMarkers.add(camera_marker);
        }
    }


    public void updateCameras(LatLng coordinates, Context context, GoogleMap mMap,final RequestQueue queue, double radius ){
        Log.d("Current Coordinates ", coordinates.toString());
        initializeCameras(queue,context,mMap,coordinates, radius);
    }


    /**
     * This function will request from google's geocoder the formatted address given a location's latitude and longitude
     * @param  lat
     * @param lng
     */
    public String getFormattedAddress(double lat, double lng) {
        String address = String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%.4f,%.4f&key=AIzaSyAIUYsMnJDb1v1gIaXZ1EIIwR2eRFjJrbw",lat,lng);
        String formatted_address = "";
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            JSONObject jsonResult = JsonReader.readJsonFromUrl(address);
            JSONArray data = jsonResult.getJSONArray("results");
            formatted_address = data.getJSONObject(0).getString("formatted_address");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return formatted_address;
    }
}