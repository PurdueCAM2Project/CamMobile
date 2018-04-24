package com.cam2.ryandevlin.worldview;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
import android.widget.CompoundButton; //for button code
import android.widget.*; //for button code
import android.view.*; //for button code
import android.graphics.*;
import android.support.v4.widget.DrawerLayout;
import java.util.ArrayList;


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
import java.util.concurrent.TimeUnit;

//import org.json.JSONObject;
//import org.json.JSONArray;
//import org.json.JSONException;

import android.graphics.Color;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;

// -- Web Imports -- //
import java.net.*;
import java.io.*;
import org.json.*;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, NavigationView.OnNavigationItemSelectedListener, RoutingListener, View.OnClickListener {

    // Location of Database API token requests
    String data = "";
    RequestQueue requestQueue;
    TextView results;

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient = null; //Google Play services API
    private UiSettings mUiSettings; //Google map UI settings
    Location mLastLocation; //Location for GPS tracking
    Marker mCurrLocationMarker; //Putting the marker down
    LocationRequest mLocationRequest; //Requesting location.
    boolean connected; //Checking if the location is enabled or suspended for the directions portion of the code
    private static final String TAG = MapsActivity.class.getSimpleName();


    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    Button side_menu_button;
    Button map_options_button;
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

    private ArrayList<String> mList;
    private ListAdapter editList;
    private ArrayAdapter<String> mAdapter;

    List<Marker> markers = new ArrayList<Marker>();
    List<Marker> cam_markers = new ArrayList<Marker>();
    ArrayList<Camera> cam_objects = new ArrayList<Camera>();

    public Polyline route = null;
    boolean hide_route_flag = false;
    boolean search_marker_hidden_flag = false;
    boolean curr_marker_hidden_flag = true;
    boolean cams_hidden_flag = true;
    Camera camera_obj = null;
    Marker camera_marker = null;
    int num_cameras = 1;
    int curr_max_cams = 0;
    String str;
    Camera hard = null;

    // -- New Camera Parsing -- //
    String master_token = new String();
    String base_URL = "https://cam2-api.herokuapp.com/";
    String JsonURL_token = "https://cam2-api.herokuapp.com/auth?clientID=5804518549e100481b100ac0fdfff756177ffd266fe181b6df02744ec2f0fe31befc6c67f93c844aaac3e05cce1f9087&clientSecret=b3f3eec2d6805dd2c6003003a938c068ba9ca6d5eca7cbaf258bfe18aaf989272423de536a356b32f7b48c6b6d89a460";
    boolean cam_exists = false; //Camera array flag
    boolean first_run = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) { //CREATING THE MAP
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        requestQueue = Volley.newRequestQueue(this);
        //token_req();

        //initiate polylines for map
        polylines = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //Creating and enabling the Navigation View
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Initializing the button meant for bring up the side menu (tool bar option wasn't going to work)
        side_menu_button = (Button) findViewById(R.id.side_button);
        side_menu_button.setOnClickListener(MapsActivity.this);
        //Button in which will bring up a popup menu for the various map options
        map_options_button = (Button) findViewById(R.id.map_options);
        map_options_button.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View view) {
                                                      PopupMenu popup = new PopupMenu(MapsActivity.this, map_options_button);
                                                      popup.getMenuInflater().inflate(R.menu.popup_map_options, popup.getMenu());
                                                      popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                                          @Override
                                                          public boolean onMenuItemClick(MenuItem menuItem)
                                                          {
                                                              int item = menuItem.getItemId();
                                                              if (item == R.id.map_drawn)
                                                              {
                                                                  mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                                              }
                                                              else if (item == R.id.map_sat)
                                                              {
                                                                  mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                                              }
                                                              else if (item == R.id.map_hybrid)
                                                              {
                                                                  mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                                              }
                                                              return true;
                                                          }
                                                      });
                                                    popup.show();
                                                  }
                                              });

        // -- JSON RESP PARSING HERE -- //

        // ---- OBTAINING TOKENS ---- //


        // ---- DONE OBTAINING TOKENS ---- //


        // -- END JSON RESP PARSING HERE -- //

        // Adds the JSON object request "obreq" to the request queue
        //requestQueue.add(cam_arrayreq);
        Log.d(TAG, "onCreate: hardcoding a camera");


        /*
        //hardcoding for demo
        String formatted_addressk = null;
        try {
            formatted_addressk = getFormattedAddress(36.1451,-115.155);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        hard = new Camera( 3);
        //hard.des("Las Vegas Strip: The Stratosphere");
        hard.cam_type("IP");
        hard.lat(36.1451);
        hard.lng(-115.155);
        hard.cam_address(formatted_addressk);
        hard.cam_url("https://www.skylinewebcams.com/en/webcam/united-states/nevada/las-vegas/las-vegas.html");
        hard.cam_country("USA");
        hard.cam_city("Las Vegas");
        cam_objects.add(0,hard);
        hard = new Camera(2);
        hard.lat(18.1825);
        hard.lng(-63.137553);
        try {
            hard.cam_address(getFormattedAddress(hard.latitude,hard.longitude));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        hard.cam_url("https://www.earthcam.com/world/anguilla/meadsbay/?cam=meadsbay_hd");
        cam_objects.add(1,hard);
        hard = new Camera(2);
        hard.lat(40.731414);
        hard.lng(-73.9969);
        try {
            hard.cam_address(getFormattedAddress(hard.latitude,hard.longitude));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        hard.cam_url("https://www.earthcam.com/usa/newyork/fifthave/?cam=nyc5th_str");
        cam_objects.add(2,hard);
        hard = new Camera(2);
        hard.lat(-8.391231);
        hard.lng(115.283947);
        try {
            hard.cam_address(getFormattedAddress(hard.latitude,hard.longitude));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        hard.cam_url("https://www.earthcam.com/world/indonesia/bali/?cam=bali1");
        cam_objects.add(3,hard);
        hard = new Camera(2);
        hard.lat(51.537027);
        hard.lng(-0.183218);
        try {
            hard.cam_address(getFormattedAddress(hard.latitude,hard.longitude));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        hard.cam_url("https://www.earthcam.com/world/england/london/abbeyroad/?cam=abbeyroad_uk");
        cam_objects.add(4,hard);

        hard = new Camera(2);
        hard.lat(34.101393);
        hard.lng(-118.3389);
        try {
            hard.cam_address(getFormattedAddress(hard.latitude,hard.longitude));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        hard.cam_url("https://www.earthcam.com/usa/california/losangeles/hollywoodblvd/?cam=hollywoodblvd");
        cam_objects.add(5,hard);
        */

        //end of hardcoding


        // -- Obtain new auth token -- //

        /*
        URL temp_url = null;
        try {
            temp_url = new URL("https://cam2-api.herokuapp.com/auth?clientID=5804518549e100481b100ac0fdfff756177ffd266fe181b6df02744ec2f0fe31befc6c67f93c844aaac3e05cce1f9087&clientSecret=b3f3eec2d6805dd2c6003003a938c068ba9ca6d5eca7cbaf258bfe18aaf989272423de536a356b32f7b48c6b6d89a460");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) temp_url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            // Use Java JSON Parsing to Extract Token -- //
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            //String temp = readStream(in);
            JSONObject resp = new JSONObject(readStream(in));
            //String pageName = resp.getJSONObject("pageInfo").getString("pageName");
            //Log.d("page_name", pageName);
            JSONArray arr = resp.getJSONArray("token");

            for (int i = 0; i < arr.length(); i++)
            {
                String post_id = arr.getJSONObject(i).getString("token");
                Log.d("TEST1", post_id);
            }
            Log.d("worked", "Complete");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
        */
        // Use Java JSON Parsing to Extract Token -- //

    }

    // -- Handle HTML GET Requests -- //
    public String readStream(InputStream in) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }
        Log.d("DATA", total.toString());
        return(total.toString());
    }


    String faddress;
    // This function will request from google's geocoder the formatted address given a location's latitude and longitude
    public String getFormattedAddress(double lat, double lng) throws JSONException {
        //addressQueue= Volley.newRequestQueue(this);
        //The string below will send a request to google to get formatted address in JSONObject form. In the JSONObject the tag for formatted address
        // is "formatted_address"
        String address = String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng=%.4f,%.4f&key=AIzaSyAIUYsMnJDb1v1gIaXZ1EIIwR2eRFjJrbw",lat,lng);
        String formatted_address;
        try {
            //without this there will be "android.os.NetworkOnMainThreadException exception" error however must be used only in development environment
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //// the mode above causes app to be problematic around area with spotty wifi

            // reads JSONObject from url
            JSONObject jsonResult = JsonReader.readJsonFromUrl(address);
            JSONArray data = jsonResult.getJSONArray("results");
            // getformatted address
            faddress = data.getJSONObject(0).getString("formatted_address");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        formatted_address = faddress;
        return formatted_address;
    }

    ///////////////////////Google API client that allows various functionality////////////////
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    ///////////////////////Requesting permission and actually doing something with the permission///////////////////////
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                //TODO: Show a reason to enable location

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        //mMap.setMyLocationEnabled(true);
                        mMap.setOnMyLocationClickListener(this); //Enable clicking on the blue dot for your location
                        mMap.setOnMyLocationButtonClickListener(this); //Enable listener to handle clicks of the My Location button
                        //mUiSettings.setCompassEnabled(false); //Allowing the Google API compass to be used
                        mUiSettings.setZoomControlsEnabled(true); //Enabling the zoom in and zoom out functionality
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    ///////////////////////Google map creation///////////////////////
    @Override
    public void onMapReady(final GoogleMap googleMap) { //THE MAP IS NOW RUNNING

        mMap = googleMap; //OBJECT FOR MAP MANIPULATION
        mUiSettings = mMap.getUiSettings();
        //mMap.setPadding(0,100,0,0);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient(); //Build the Google API
                //mMap.setMyLocationEnabled(true); //Enable the My location button
                mMap.setOnMyLocationClickListener(this); //Enable clicking on the blue dot for your location
                mMap.setOnMyLocationButtonClickListener(this); //Enable listener to handle clicks of the My Location button
                //mUiSettings.setCompassEnabled(true); //Allowing the Google API compass to be used
                mUiSettings.setZoomControlsEnabled(true); //Enabling the zoom in and zoom out functionality
            }
        }
        else { //Permission was already granted.
            buildGoogleApiClient();
            //mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationClickListener(this);
            mMap.setOnMyLocationButtonClickListener(this);
            //mUiSettings.setCompassEnabled(true);
            mUiSettings.setZoomControlsEnabled(true);

        }
        ///////////////////////Toggle buttons for BOTH location and camera plotting and hiding///////////////////////
        /* button to find a route between two locations */
        final ToggleButton destination_plan = (ToggleButton) findViewById(R.id.directionsbutton);
        destination_plan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {

                    // The toggle is enabled
                    if (query && (!search_marker_hidden_flag)) { //if the user has already searched a location
                        Log.d(TAG, "onCheckedChanged: toggle is on");
                        addPolyline(curr_lat_lng, search_latLng);
                        Context things = getApplicationContext();
                        CharSequence text = "Path Calculated.";
                        int duration = Toast.LENGTH_SHORT;


                    } else if (connected == false) { //else if the user's location is not detected
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
                    erasePolylines();
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

        /* button to plot cameras or hide them*/
        final ToggleButton cameras = (ToggleButton) findViewById(R.id.camera_button);
        cameras.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                //cam_update();
                if (check)
                {
                    Toast.makeText(getApplicationContext(),"Cameras now showing", Toast.LENGTH_SHORT);
                    //TODO: Insure plotting cameras work
                    Log.d(TAG, "onCheckedChanged: Plotting cameras");
                    //cam_update();
                    //plot_cameras();
                    token_req();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Cameras now hidden", Toast.LENGTH_SHORT);
                    //TODO: Insure hiding cameras work
                    hide_cameras();
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
                Log.d(TAG, "onPlaceSelected: setting search lat and long");
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

        // Initializing proper UI settings
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        ///////////////////////Todo: Not sure if you want to keep that marker, because Google's default marker is easier to handle and change///////////////////////
        /*MARKER INITIALIZATION*/

        Context context = getApplicationContext();
        //Bitmap temp = BitmapFactory.decodeResource(context.getResources(),//TURN THE DRAWABLE ICON INTO A BITMAP
                //R.drawable.user_location);
        //Bitmap custom_marker = Bitmap.createScaledBitmap(temp, 80, 80, true); //RESCALE BITMAP ICON TO PROPER SIZE


        /*MarkerOptions a = new MarkerOptions()
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new android.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //get coordinates
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    //create latlng class
                    Log.d(TAG, "onLocationChanged: setting current latitude and longitude");
                    curr_lat_lng = new LatLng(latitude, longitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                        curr_location = addressList.get(0).getAddressLine(0);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (hide_route_flag && mCurrLocationMarker != null) {
                        mCurrLocationMarker.setAlpha(0.0f);
                    } else if(mCurrLocationMarker != null) {
                        mCurrLocationMarker.setAlpha(1.0f);
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
                        curr_location = addressList.get(0).getAddressLine(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (hide_route_flag && mCurrLocationMarker != null) {
                        mCurrLocationMarker.setAlpha(0.0f);
                    } else if(mCurrLocationMarker != null) {
                        mCurrLocationMarker.setAlpha(1.0f);
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
                }else if (marker.getTag() == "cam"){
                    // If the marker clicked is a camera ie. tagged 'cam', we get its index via snippet of the marker.
                    // The url is extracted from the camera array and sent to web_cam.java to launch the website associated with the camera
                    // The url is sent using intent.putExtra
                    int index = Integer.parseInt(marker.getSnippet());
                    Camera curr_camera = cam_objects.get(index);
                    str = curr_camera.source_url;
                    Intent intent = new Intent(MapsActivity.this,web_cam.class);
                    intent.putExtra("source",str);
                    startActivity(intent);
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

    private void addPolyline(LatLng origin,LatLng destination) {
        //This function uses external library to build its polylines on the map. The library is located in build.gradle
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(origin,destination)
                .build();
        routing.execute();

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
        for(int i = 0; i < curr_max_cams; i++) {
            //Log.d(TAG, "plot_cameras: plotting camera "+i);
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
            //camera_marker.setTitle(curr_camera.description);
            camera_marker.setTag("cam");
            camera_marker.setSnippet(""+i);
            cam_markers.add(camera_marker);
        }
    }
    public void hide_cameras(){
        for(int i=0;i<cam_markers.size();i++) {
            cam_markers.get(i).remove();
        }
        cam_markers.removeAll(cam_markers);
    }

    //// -- REQUEST NEW TOKEN -- ////
    // Sets master_token with new token val
    public void token_req() {
        final Context token_con = getApplicationContext();
        JsonObjectRequest objreq = new JsonObjectRequest(Request.Method.GET, JsonURL_token,
                // The second parameter Listener overrides the method onResponse() and passes
                //JSONArray as a parameter
                new Response.Listener<JSONObject>() {

                    // Takes the response from the JSON request
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            master_token = response.getString("token");
                            Log.d("MASTER_TOKEN", master_token);
                            if(first_run){
                                Toast.makeText(token_con,"Downloading Camera Database. Please Wait....",Toast.LENGTH_LONG).show();
                                first_run = false;
                            }
                            else{
                                Toast.makeText(token_con,"Updating local Cams. Please Wait....",Toast.LENGTH_LONG).show();
                            }
                            cam_update(master_token, "");
                            cam_update(master_token, "&offset=100");
                            cam_update(master_token, "&offset=200");
                            cam_update(master_token, "&offset=300");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }

                },
                // The final parameter overrides the method onErrorResponse() and passes VolleyError
                // as a parameter
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
        requestQueue.add(objreq);
    }



    //// -- Update Cameras -- ////
    // Updates the list of camera objects
    public void cam_update(String token, String offset) {
        //token_req();
        String search_url = base_URL + "cameras/search?access_token=" + token + offset;
        JsonArrayRequest cam_arrayreq = new JsonArrayRequest(Request.Method.GET, search_url,
                // The second parameter Listener overrides the method onResponse() and passes
                //JSONArray as a parameter
                new Response.Listener<JSONArray>() {

                    // Takes the response from the JSON request
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            cam_obj_gen(response);
                            plot_cameras();
                        } catch (JSONException e) {
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
        requestQueue.add(cam_arrayreq);
    }


    public void cam_obj_gen(JSONArray response) throws JSONException {
        //JSONObject camera = response.getJSONObject(0);
        //int size = camera.length();
        int i = 0;
        num_cameras = response.length();
        String temp = Integer.toString(num_cameras);
        Log.d("UPDATE", "camera updating");
        Log.d("UPDATE_MAS",master_token);
        Log.d("UPDATE_NUM",temp);
        int j = 0;
        while (i < num_cameras) {
            JSONObject camera = response.getJSONObject(i);
            i++;
            if (!(camera.getString("reference_url").equals("null"))) {
                cam_exists = false;
                int q = 0;
                int camera_id = camera.getInt("cameraID");
                if (!first_run) {
                    while (q < curr_max_cams) { //loop through all cams in the current array
                        if (cam_objects.get(q).camera_id == camera_id) {
                            //Log.d("CHECKER", "Camera already exists");
                            cam_exists = true;
                            break;
                        }
                        q++;
                    }
                    if (cam_exists == false) {
                        //Log.d("URL_TYPE", camera.getString("reference_url"));
                        //String description = camera.getString("description");
                        //String camera_type = camera.getString("type");
                        //int camera_id = camera.getInt("cameraID");
                        double latitude = camera.getDouble("latitude");
                        double longitude = camera.getDouble("longitude");
                        String formatted_address = getFormattedAddress(latitude, longitude);
                        String source_url = camera.getString("reference_url");
                        //String country = camera.getString("country");
                        //String city = camera.getString("city");

                        // create new Camera object //
                        camera_obj = new Camera(camera_id);
                        //camera_obj.des(description);
                        //camera_obj.cam_type(camera_type);
                        camera_obj.lat(latitude);
                        camera_obj.lng(longitude);
                        camera_obj.cam_address(formatted_address);
                        camera_obj.cam_url(source_url);
                        //camera_obj.cam_country(country);
                        //camera_obj.cam_city(city);
                        //cam_objects.add(i - 1, camera_obj); // add the camera object to the list
                        cam_objects.add(curr_max_cams, camera_obj); // add the camera object to the list
                        curr_max_cams++; //camera is valid so increase num var
                    }

                }
            }
            //Log.d("UPDATE_COMPLETE", "done");
        }
    }

    ///////////////////////Navigation menu handler. Allowing various clicks.///////////////////////
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.cam_search)
        {
            // Search Camera Function
            Intent i = new Intent(MapsActivity.this,Camera_List.class);
            Bundle b = new Bundle();
            // TODO : Serialize is old method of sending objects, investigate Parcelable.
            // Puts camera array into serialized bundle to be sent across activities
            b.putSerializable("cameras", cam_objects);
            i.putExtras(b);
            startActivity(i);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }
    ///////////////////////One of the fucntions provided by the Google services API. Automatically detects when a user is connected or not///////////////////////
    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        connected = true; //HANDLER FOR THE DIRECTIONS BUTTON SO IT KNOWS IT'S CONNECTED

        //Setting new request, and setting the time interval to update every 1000 millasecons.
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); //Balance power and GPS accuracy.
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
        connected = false; //HANDLER FOR THE DIRECTIONS BUTTON SO IT KNOWS IT'S DISCONNECTED
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
        connected = false;
    }
    ///////////////////////One of the fucntions provided by the Google services API. Automatically detects if the user moves///////////////////////
    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        Context context = getApplicationContext();
        Bitmap temp = BitmapFactory.decodeResource(context.getResources(),//TURN THE DRAWABLE ICON INTO A BITMAP
                R.drawable.user_location);
        Bitmap custom_marker = Bitmap.createScaledBitmap(temp, 80, 80, true);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(custom_marker));

        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }
    ///////////////////////One of the functions provided by the Google Map API. The location on the top right corner is the default location button. Clicking it automatically zooms in on the user///////////////////////
    @Override
    public boolean onMyLocationButtonClick()
    {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }
    ///////////////////////One of the functions provided by the Google Map API. When you click on your physical location, as in the blue dot, it tells you your location///////////////////////
    @Override
    public void onMyLocationClick(@NonNull Location location)
    {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }


    ////////Calculating Polylines to be used on Map///////////////////////////////////////////////////////////////////
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRoutingIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }
    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }

    @Override
    public void onClick(View view)
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }
}