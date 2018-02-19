package com.cam2.ryandevlin.worldview;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
//import android.support.v4.content.PermissionChecker;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.location.places.*;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.common.api.Status;


import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlaceDetectionClient;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import android.location.Address;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

///////////////////////
import android.widget.Toast;
import android.content.Context;
import android.widget.Button; //for button code
import android.widget.CompoundButton; //for button code
import android.widget.*; //for button code
import android.view.*; //for button code
import android.graphics.*;
import android.graphics.drawable.LevelListDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.view.animation.*;
import android.widget.SearchView;
import android.support.v4.widget.DrawerLayout;
import android.widget.Adapter;
import android.view.View;
import java.util.ArrayList;
import java.util.Arrays;

/* Directions */
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
//import com.google.maps.model.LatLng;

import org.joda.time.DateTime;

import java.io.IOException;
import java.lang.Object;
import java.util.concurrent.TimeUnit;

import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.Response;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import com.android.volley.VolleyLog;


import java.net.*;
import java.io.*;

import android.os.AsyncTask;

import android.graphics.Color;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;


//////////////////

//////////////////


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    String JsonURL = "https://tirtha.loyolachicagocs.org/cam2/database/api/cameras.json";
    String data = "";
    RequestQueue requestQueue;
    TextView results;

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient = null; //new
    private static final String TAG = MapsActivity.class.getSimpleName();


    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;


    LocationManager locationManager;
    String curr_location = null;
    LatLng curr_lat_lng = new LatLng(0, 0);
    LatLng search_latLng = new LatLng(0, 0);
    String search_name = null;
    LatLngBounds search_zoom = null;
    boolean query = false;
    int complement0 = 0;
    int complement1 = 0;
    int complement2 = 0;

    //Polyline route;

    private ListView mDrawerList;
    private ArrayList<String> mList;
    private ListAdapter editList;
    private ArrayAdapter<String> mAdapter;

    List<Marker> markers = new ArrayList<Marker>();
    List<Marker> cam_markers = new ArrayList<Marker>();
    List<Camera> cam_objects = new ArrayList<Camera>();

    public Polyline route = null;
    boolean hide_route_flag = false;
    boolean search_marker_hidden_flag = false;
    boolean curr_marker_hidden_flag = true;
    boolean cams_hidden_flag = true;

    Camera camera_obj = null;
    Marker camera_marker = null;
    int num_cameras = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //CREATING THE MAP
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        mDrawerList = (ListView) findViewById(R.id.navList);
        defaultDrawerItems();

        //String sUrl = "www.google.com";
        //new GetUrlContentTask().execute(sUrl);
        requestQueue = Volley.newRequestQueue(this);
        //results = (TextView) findViewById(R.id.jsonData);

        // Creating the JsonObjectRequest class called obreq, passing required parameters:
        //GET is used to fetch data from the server, JsonURL is the URL to be fetched from.
        JsonArrayRequest arrayreq = new JsonArrayRequest(JsonURL,
                // The second parameter Listener overrides the method onResponse() and passes
                //JSONArray as a parameter
                new Response.Listener<JSONArray>() {

                    // Takes the response from the JSON request
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            //JSONObject camera = response.getJSONObject(0);
                            //int size = camera.length();
                            int i = 0;
                            num_cameras = response.length();
                            while(i < num_cameras){
                                JSONObject camera = response.getJSONObject(i);
                                i++;
                                // Retrieves first JSON object in outer array
                                //JSONObject colorObj = response.getJSONObject(0);
                                // Retrieves "colorArray" from the JSON object
                                //JSONArray colorArry = response.getJSONArray(0);
                                // Iterates through the JSON Array getting objects and adding them
                                //to the list view until there are no more objects in colorArray

                            /*
                            for (int i = 0; i < colorArry.length(); i++) {
                                //gets each JSON object within the JSON array
                                JSONObject jsonObject = colorArry.getJSONObject(i);

                                // Retrieves the string labeled "colorName" and "hexValue",
                                // and converts them into javascript objects
                                String color = jsonObject.getString("colorName");
                                String hex = jsonObject.getString("hexValue");

                                // Adds strings from the current object to the data string
                                //spacing is included at the end to separate the results from
                                //one another
                                data += "Color Number " + (i + 1) + "nColor Name: " + color +
                                        "nHex Value : " + hex + "nnn";
                            }*/
                                String description = camera.getString("description");
                                String camera_type = camera.getString("camera_type");
                                int camera_id = camera.getInt("camera_id");
                                double latitude = camera.getDouble("lat");
                                double longitude = camera.getDouble("lng");
                                String source_url = camera.getString("source_url");
                                String country = camera.getString("country");
                                String city = camera.getString("city");

                            /* create new Camera object */
                                camera_obj = new Camera(camera_id);
                                camera_obj.des(description);
                                camera_obj.cam_type(camera_type);
                                camera_obj.lat(latitude);
                                camera_obj.lng(longitude);
                                camera_obj.cam_url(source_url);
                                camera_obj.cam_country(country);
                                camera_obj.cam_city(city);

                                cam_objects.add(i - 1, camera_obj); // add the camera object to the list
                            }
                            //num_cameras = i;

                            //data = description;

                            // Adds the data string to the TextView "results"
                            //results.setText(data);

                        }
                        // Try and catch are included to handle any errors due to JSON
                        catch (JSONException e) {
                            // If an error occurs, this prints the error to the log
                            e.printStackTrace();
                        }
                    }
                },
                // The final parameter overrides the method onErrorResponse() and passes VolleyError
                //as a parameter
                new Response.ErrorListener() {
                    @Override
                    // Handles errors that occur due to Volley
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley", "Error");
                    }
                }
        );
        // Adds the JSON object request "obreq" to the request queue
        requestQueue.add(arrayreq);

    }

    private void defaultDrawerItems() {
        String[] menuArray = {"Standard", "Drawn", "Hide Markers", "Display Cameras"};
        mList = new ArrayList<>(Arrays.asList(menuArray));
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mList);
        mDrawerList.setAdapter(mAdapter);
    }

    /////////////////////////////////////////////////////
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123; //REQUEST CODE USED IN THE PERMISSION REQUEST.  STILL NOT SURE IF THIS NUMBER MATTERS. "123" IS A RANDOM NUMBER.


    @Override
    public void onMapReady(final GoogleMap googleMap) { //THE MAP IS NOW RUNNING

        mMap = googleMap; //OBJECT FOR MAP MANIPULATION


        //INITIALIZATION OF PERMISSION CHECK
        int permissionCheck = ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        //ASK THE USER IF WORLDVIEW CAN TRACK THEIR LOCATION
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Context context = getApplicationContext();
                CharSequence text = "WorldView needs to access your location to enable all features."; //WE NEED TO EXPLAIN WHY WE MUST TRACK THEM
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
                return;

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);

                // REQUEST_CODE_ASK_PERMISSIONS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

        } else {
            Context context = getApplicationContext();
            CharSequence text = "Location Services Enabled."; //WE HAVE PERMISSION TO TRACK THEM
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        /* button to find a route between two locations */
        final ToggleButton destination_plan = (ToggleButton) findViewById(R.id.button1);
        destination_plan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {

                    // The toggle is enabled
                    if (query && (!search_marker_hidden_flag)) { //if the user has already searched a location
                        try {
                            com.google.maps.model.LatLng origin = LatLng_Convert(curr_lat_lng); //convert origin to correct class
                            com.google.maps.model.LatLng destination = LatLng_Convert(search_latLng); //convert destination to correct class

                            DirectionsResult curr_directions = setDirections(mMap, origin, destination);

                            addPolyline(curr_directions, mMap);

                            Context things = getApplicationContext();
                            CharSequence text = "Path Calculated.";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(things, text, duration);
                            toast.show();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ApiException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else if (curr_lat_lng.latitude == 0 && curr_lat_lng.longitude == 0) { //else if the user's location is not detected
                        Context context = getApplicationContext();
                        CharSequence text = "Route cannot be planned until your current location is known.";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        destination_plan.setChecked(false); //reset toggle
                    } else { //else the user hasn't searched anything yet
                        Context context = getApplicationContext();
                        CharSequence text = "Search a location to plan a route.";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        destination_plan.setChecked(false); //reset toggle
                    }
                } else {
                    // The toggle is disabled
                    if (query && (route != null)) {
                        route.remove();
                        Context context = getApplicationContext();
                        CharSequence text = "Route removed.";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                }
            }
        });


        MarkerOptions temp_search = new MarkerOptions()
                .position(search_latLng) //CREATE A MARKER FOR THE USER'S LOCATION
                .alpha(0.0f); //weird fix for marker issues. When the app loads the marker is placed at 0,0
        // until the device finds the user location. This code makes the marker transparent
        // until later when the user location is found.
        final Marker search_location = mMap.addMarker(temp_search);

        markers.add(search_location);

        /*ENTRY POINT FOR PLACES API*/

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                if (query && (route != null)) {
                    route.remove();
                    destination_plan.setChecked(false); //reset toggle
                }
                query = true;
                search_marker_hidden_flag = false;
                search_latLng = place.getLatLng();
                search_name = (String) place.getName();
                search_zoom = place.getViewport();
                search_location.setTitle(search_name);
                search_location.setPosition(search_latLng);
                search_location.setAlpha(0.75f);
                search_location.setTitle(search_name);
                search_location.setVisible(true);
                markers.add(search_location);

                if (search_zoom != null) {
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(search_zoom, 0);
                    mMap.animateCamera(cu);
                    //mMap.animateCamera(CameraUpdateFactory.newCameraPosition(search_Position));
                } else {
                    CameraPosition search_Position = new CameraPosition.Builder()
                            .target(search_latLng)
                            .zoom(15)                   // Sets the zoom
                            .bearing(0)                // Sets the orientation of the camera to east
                            .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(search_Position));
                }
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        final Button button = findViewById(R.id.location_zoom);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Context context = getApplicationContext();
                Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
                v.startAnimation(shake);
                //mMap.animateCamera(CameraUpdateFactory.newLatLng(curr_lat_lng));
                if (curr_lat_lng.latitude != 0 && curr_lat_lng.longitude != 0) {
                    CameraPosition default_Position = new CameraPosition.Builder()
                            .target(curr_lat_lng)
                            .zoom(15)                   // Sets the zoom
                            .bearing(0)                // Sets the orientation of the camera to east
                            .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(default_Position));
                } else {
                    Context contexts = getApplicationContext();
                    CharSequence text = "Cannot determine your location.";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(contexts, text, duration);
                    toast.show();
                }
            }
        });


        // Initializing proper UI settings
        UiSettings map_settings = mMap.getUiSettings();
        map_settings.setZoomControlsEnabled(true);
        map_settings.setCompassEnabled(true);

        /*MARKER INITIALIZATION*/

        Context context = getApplicationContext();
        Bitmap temp = BitmapFactory.decodeResource(context.getResources(),//TURN THE DRAWABLE ICON INTO A BITMAP
                R.drawable.user_location);
        Bitmap custom_marker = Bitmap.createScaledBitmap(temp, 80, 80, true); //RESCALE BITMAP ICON TO PROPER SIZE


        MarkerOptions a = new MarkerOptions()
                .position(curr_lat_lng) //CREATE A MARKER FOR THE USER'S LOCATION
                .icon(BitmapDescriptorFactory.fromBitmap(custom_marker))
                .alpha(0.0f); //weird fix for marker issues. When the app loads the marker is placed at 0,0
        // until the device finds the user location. This code makes the marker transparent
        // until later when the user location is found.
        final Marker user_location = mMap.addMarker(a);

        markers.add(user_location);

        /*START OF LOCATION TRACKING CODE*/

        //check whether the network provider is enabled
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) { //USING THE NETWORK PROVIDER FOR LOCATION TRACKING
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new android.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //get coordinates
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    //create latlng class
                    curr_lat_lng = new LatLng(latitude, longitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                        //curr_location = addressList.get(0).getAddressLine(0);
                        user_location.setPosition(curr_lat_lng); //UPDATE THE MARKER AS THEY MOVE AROUND
                        user_location.setTitle("Current Location");
                        user_location.setTag("user_location");
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f)); //JUST FOR DEBUGGING. THIS LINE CAUSES THE CAMERA TO RESET TOO OFTEN

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (hide_route_flag) {
                        user_location.setAlpha(0.0f);
                    } else {
                        user_location.setAlpha(1.0f);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { //USING GPS DATA FOR LOCATION TRACKING
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new android.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //get coordinates
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    //create latlng class
                    LatLng latLng = new LatLng(latitude, longitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                        //curr_location = addressList.get(0).getAddressLine(0);
                        user_location.setPosition(latLng); //UPDATE THE MARKER AS THEY MOVE AROUND
                        user_location.setTitle("Current Location");
                        user_location.setTag("user_location");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (hide_route_flag) {
                        user_location.setAlpha(0.0f);
                    } else {
                        user_location.setAlpha(1.0f);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }


        /* THIS CODE IS FOR THE SIDE MENU */

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MapsActivity.this, "Position = " + position, Toast.LENGTH_SHORT).show();
                if (position == 0) {
                    complement0 = 1 ^ complement0;
                    if (complement0 == 1) {
                        Context context = getApplicationContext(); //TOGGLES THE MAP THEME TO NIGHTMODE
                        boolean success = googleMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(
                                        context, R.raw.night_mode));
                        mList.set(position, "NightMode");
                        mAdapter.notifyDataSetChanged();

                    } else {
                        // The toggle is disabled
                        Context context = getApplicationContext(); //TOGGLES THE MAP THEME TO STANDARD MODE
                        boolean success = googleMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(
                                        context, R.raw.standard));
                        mList.set(position, "Standard");
                        mAdapter.notifyDataSetChanged();
                    }
                } else if (position == 1) {
                    complement1++;
                    if (complement1 == 1) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        mList.set(position, "Satellite");
                        mAdapter.notifyDataSetChanged();
                    } else if (complement1 == 2) {
                        // The toggle is disabled
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        mList.set(position, "Hybrid");
                        mAdapter.notifyDataSetChanged();
                    } else if (complement1 == 3) {
                        // The toggle is disabled
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        mList.set(position, "Terrain");
                        mAdapter.notifyDataSetChanged();
                    } else {
                        // The toggle is disabled
                        complement1 = 0;
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        mList.set(position, "Drawn");
                        mAdapter.notifyDataSetChanged();
                    }
                } else if (position == 2) {
                    complement0 = 1 ^ complement0;
                    if (complement0 == 1) { //ADD ALL MARKERS HERE!!!!
                        int len = markers.size();
                        len--;
                        while (len != 0) {
                            Marker temp = markers.get(len);
                            temp.setAlpha(0.0f);
                            len--;
                        }
                        mList.set(position, "Reveal Markers");
                        mAdapter.notifyDataSetChanged();
                        hide_route_flag = true;
                    } else {
                        // The toggle is disabled
                        int len = markers.size();
                        len--;
                        while (len != 0) {
                            Marker temp = markers.get(len);
                            temp.setAlpha(0.75f);
                            len--;
                        }
                        mList.set(position, "Hide Markers");
                        mAdapter.notifyDataSetChanged();
                        hide_route_flag = false;
                    }
                    if (query) {
                        route.setVisible(!hide_route_flag);
                    }
                } else if (position == 3) {
                    //camera_locate();
                    //makeJsonArrayRequest();
                    cams_hidden_flag = !cams_hidden_flag;
                    if(!cams_hidden_flag) {
                        plot_cameras();
                        mList.set(position, "Hide Cameras");
                    }
                    else{
                        hide_cameras();
                        mList.set(position, "Display Cameras");
                    }
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if ((marker.getTag() != "user_location") && (marker.getTag() != "cam")) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(MapsActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(MapsActivity.this);
                    }
                    builder.setTitle("Delete Marker")
                            .setMessage("Are you sure you want to delete this marker?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                    search_location.setVisible(false);
                                    search_marker_hidden_flag = true;
                                    dialog.cancel();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                    dialog.cancel();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                return false;
            }
        });


    }

    private GeoApiContext getGeoContext() {

        //connection timeout : default connection timeout for new connections
        //query rate: max number of queries that will be executed in 1 second intervals
        //the default read timeout for new connections
        //the default write timeout for new connections

        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3).setApiKey("AIzaSyBUk43bX4UmObgrUZooRrsS-86PxSYelbU")
                .setConnectTimeout(20, TimeUnit.SECONDS).setReadTimeout(20, TimeUnit.SECONDS)
                .setWriteTimeout(20, TimeUnit.SECONDS);
    }

    public DirectionsResult setDirections(GoogleMap mMap, com.google.maps.model.LatLng origin, com.google.maps.model.LatLng destination) throws InterruptedException, ApiException, IOException {
        DateTime now = new DateTime();

        //mode = travelmode which can be walking, driving, etc...
        //origin is where you start
        //destination is where you want to go.
        //departure time is when you want to depart.

        DirectionsResult result = DirectionsApi.newRequest(getGeoContext())
                .mode(TravelMode.DRIVING).origin(origin)
                .destination(destination).departureTime(now)
                .await();

        return result;
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[0].overviewPolyline.getEncodedPath());
        route = mMap.addPolyline(new PolylineOptions().addAll(decodedPath).width(12).color(Color.argb(200, 255, 102, 102)).geodesic(true));
        route.setVisible(true);
    }

    public com.google.maps.model.LatLng LatLng_Convert(LatLng prev) { //small function to handle latlng class conversions because Google decided to make conflicting classes
        com.google.maps.model.LatLng result = new com.google.maps.model.LatLng(0, 0);

        double lat = prev.latitude;
        double lon = prev.longitude;
        result.lat = lat;
        result.lng = lon;
        return (result);
    }

    public void plot_cameras(){
        for(int i = 0; i < num_cameras; i++) {
            Camera curr_camera = cam_objects.get(i);

            double lat = curr_camera.latitude;
            double lng = curr_camera.longitude;
            LatLng cam_location = new LatLng(lat, lng);

            Context context = getApplicationContext();
            Bitmap temp = BitmapFactory.decodeResource(context.getResources(),//TURN THE DRAWABLE ICON INTO A BITMAP
                    R.drawable.cam_marker);
            Bitmap custom_marker = Bitmap.createScaledBitmap(temp, 60, 100, true); //RESCALE BITMAP ICON TO PROPER SIZE


            MarkerOptions a = new MarkerOptions()
                    .position(cam_location) //CREATE A MARKER FOR THE USER'S LOCATION
                    .icon(BitmapDescriptorFactory.fromBitmap(custom_marker))
                    .alpha(0.9f);
            camera_marker = mMap.addMarker(a);

            camera_marker.setTitle(camera_obj.description);
            camera_marker.setTag("cam");

            cam_markers.add(camera_marker);
        }
    }
    public void hide_cameras(){
        cam_markers.get(cam_markers.indexOf(camera_marker)).remove();
        cam_markers.removeAll(cam_markers);
    }

}



