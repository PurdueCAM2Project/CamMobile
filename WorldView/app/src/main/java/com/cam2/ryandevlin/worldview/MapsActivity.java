package com.cam2.ryandevlin.worldview;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.util.Log;

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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.location.places.*;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.common.api.Status;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.MapStyleOptions;

import android.location.Address;
import java.io.IOException;
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
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;

// -- Web Imports -- //
import java.io.*;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, NavigationView.OnNavigationItemSelectedListener, RoutingListener, View.OnClickListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    // Location of Database API token requests
    RequestQueue requestQueue;

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient = null; //Google Play services API
    private UiSettings mUiSettings; //Google map UI settings
    Location mLastLocation; //Location for GPS tracking
    Marker mCurrLocationMarker; //Putting the marker down
    LocationRequest mLocationRequest; //Requesting location.
    boolean connected; //Checking if the location is enabled or suspended for the directions portion of the code
    private static final String TAG = MapsActivity.class.getSimpleName();

    Button side_menu_button;
    Button map_options_button;
    LocationManager locationManager;

    LatLng curr_lat_lng = new LatLng(0, 0);
    LatLng search_latLng = new LatLng(0, 0);

    boolean query = false;
    boolean plotCameras = false;

    List<Marker> markers = new ArrayList<Marker>();
    public Marker customMarker;
    public Circle mileCircle;

    public Polyline route = null;
    boolean hide_route_flag = false;
    boolean search_marker_hidden_flag = false;

    // -- Direction Updating -- //
    boolean directions_on = false;
    Polyline polyline;
    List<LatLng> points;

    boolean flip = true;

    CameraDatabaseClient cameraDatabaseClient;

    /**
     * When the application is first open, the method will run
     * Will first create the google map and contact the google apis
     * Will get all the cameras from the camera database and plot them on the map
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) { //CREATING THE MAP
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        cameraDatabaseClient = new CameraDatabaseClient();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        requestQueue = Volley.newRequestQueue(this);
        //initiate polylines for map
        polylines = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //Creating and enabling the Navigation View
        NavigationView navigationView =  findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Initializing the button meant for bring up the side menu (tool bar option wasn't going to work)
        side_menu_button =  findViewById(R.id.side_button);
        side_menu_button.setOnClickListener(MapsActivity.this);
        //Button in which will bring up a popup menu for the various map options
        map_options_button =  findViewById(R.id.map_options);
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
                        if (item == R.id.map_drawn) {
                            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        }
                        else if (item == R.id.map_sat) {
                            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        }
                        else if (item == R.id.map_hybrid) {
                            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
        //mileCircle.setClickable(true);
    }

    /**
     * Will connect and initiate connection with the Google API
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    /**
     * Checks whether the user has location permission, will return boolean if they have permissions or not
     */
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                //TODO: Show a reason to enable location
                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else
            return true;
    }

    /**
     * checking whether the user has permissions and grants it
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.setOnMyLocationClickListener(this); //Enable clicking on the blue dot for your location
                        mMap.setOnMyLocationButtonClickListener(this); //Enable listener to handle clicks of the My Location button
                        //mUiSettings.setCompassEnabled(false); //Allowing the Google API compass to be used
                        mUiSettings.setZoomControlsEnabled(true); //Enabling the zoom in and zoom out functionality
                    }
                }
                else {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    /**
     * When method is called to create the Google Map.
     * Calls the Google API's and sets all the parameters and configurations.
     * Finally, displays it on the application
     * @param googleMap
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) { //THE MAP IS NOW RUNNING
        mMap = googleMap; //OBJECT FOR MAP MANIPULATION
        mUiSettings = mMap.getUiSettings();
        //mMap.setPadding(0,100,0,0);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient(); //Build the Google API
                mMap.setMyLocationEnabled(true); //Enable the My location button
                mMap.setOnMyLocationClickListener(this); //Enable clicking on the blue dot for your location
                mMap.setOnMyLocationButtonClickListener(this); //Enable listener to handle clicks of the My Location button
                mUiSettings.setZoomControlsEnabled(true); //Enabling the zoom in and zoom out functionality
            }
        }
        else { //Permission was already granted.
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationClickListener(this);
            mMap.setOnMyLocationButtonClickListener(this);
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
                        directions_on = true;
                        Log.d(TAG, "onCheckedChanged: toggle is on");
                        addPolyline(curr_lat_lng, search_latLng);
                    } else if (connected == false) { //else if the user's location is not detected
                        directions_on = false;
                        Context context = getApplicationContext();
                        CharSequence text = "Route cannot be planned until your current location is known.";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        destination_plan.setChecked(false); //reset toggle
                    } else { //else the user hasn't searched anything yet
                        directions_on = false;
                        Context context = getApplicationContext();
                        CharSequence text = "Search a location to plan a route.";
                        int duration = Toast.LENGTH_SHORT;
                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                        destination_plan.setChecked(false); //reset toggle
                    }
                } else {
                    // The toggle is disabled
                    directions_on = false;
                    polylines.clear();
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
        final ToggleButton cameras = findViewById(R.id.camera_button);
        cameras.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean check) {
                if (check) {
                    Toast.makeText(getApplicationContext(),"Cameras now showing", Toast.LENGTH_SHORT);
                    //TODO: Insure plotting cameras work
                    Log.d(TAG, "onCheckedChanged: Plotting cameras");
                    cameraDatabaseClient.initializeCameras(requestQueue, getApplicationContext(), mMap, curr_lat_lng);
                    plotCameras = true;
                }
                else {
                    Toast.makeText(getApplicationContext(), "Cameras now hidden", Toast.LENGTH_SHORT);
                    //TODO: Insure hiding cameras work
                    cameraDatabaseClient.hideAllCameraMarkers();
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
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                if (query && (route != null)) {
                    route.remove();
                    destination_plan.setChecked(false); //reset toggle
                }
                String search_name = null;
                LatLngBounds search_zoom = null;
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
        mUiSettings.setCompassEnabled(false);
        //mMap.setMyLocationEnabled(true);
        ///////////////////////Todo: Not sure if you want to keep that marker, because Google's default marker is easier to handle and change///////////////////////
        /*MARKER INITIALIZATION*/
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
                    //  cameraDatabaseClient.updateCameras(curr_lat_lng,getApplicationContext(),mMap,requestQueue);
                    if(directions_on) {
                        // -- Update Polylines -- //
                        points = polyline.getPoints();
                        points.remove(0);
                        points.add(0, curr_lat_lng);
                        polyline.setPoints(points);
                    }
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                        String curr_location = addressList.get(0).getAddressLine(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (hide_route_flag && mCurrLocationMarker != null) {
                        mCurrLocationMarker.setAlpha(0.0f);
                    } else if(mCurrLocationMarker != null) {
                        //mCurrLocationMarker.setAlpha(1.0f);
                        mCurrLocationMarker.setAlpha(0.0f);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override
                public void onProviderEnabled(String provider) {}
                @Override
                public void onProviderDisabled(String provider) {}
            });
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { //USING GPS DATA FOR LOCATION TRACKING
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new android.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    curr_lat_lng = new LatLng(latitude, longitude);
                    if(directions_on) {
                        // -- Update Polylines -- //
                        points = polyline.getPoints();
                        points.remove(0);
                        points.add(0, curr_lat_lng);
                        polyline.setPoints(points);
                    }
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                        String curr_location = addressList.get(0).getAddressLine(0);
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
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override
                public void onProviderEnabled(String provider) {}
                @Override
                public void onProviderDisabled(String provider) {}
            });
        }
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if(plotCameras) {
                    Log.e("Camera Change", "updating cameras");
                    cameraDatabaseClient.updateCameras(cameraPosition.target, getApplicationContext(), mMap, requestQueue);
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
                }else if (marker.getTag() == "cam"){
                    String str;
                    int index = Integer.parseInt(marker.getSnippet());
                    Camera curr_camera = cameraDatabaseClient.getCameras().get(index);
                    str = curr_camera.sourceURL;
                    Intent intent = new Intent(MapsActivity.this,web_cam.class);
                    intent.putExtra("source",str);
                    startActivity(intent);
                }
                return false;
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng)
            {
                if (customMarker == null)
                {
                    customMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Custom Marker"));
                    customMarker.setTag(0);
                }
                else
                {
                    customMarker.remove();
                    customMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Custom Marker"));
                    customMarker.setTag(0);
                }

            }
        });
        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {
                circle.remove();
            }
        });
    }

    /**
     * Creating a new polyline from a given origin and destination
     * @param origin
     * @param destination
     */
    private void addPolyline(LatLng origin,LatLng destination) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(origin,destination)
                .build();
        routing.execute();

    }

    /**
     * Navigation menu handler. Allowing various clicks.
     * @param item
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.cam_search) {
            // Search Camera Function
            Intent i = new Intent(MapsActivity.this,Camera_List.class);
            Bundle b = new Bundle();
            // TODO : Serialize is old method of sending objects, investigate Parcelable.
            // Puts camera array into serialized bundle to be sent across activities
            b.putSerializable("cameras", (Serializable) cameraDatabaseClient.getCameras());
            i.putExtras(b);
            startActivity(i);
        }
        else if (id == R.id.nav_settings) {
            // Search Camera Function
            if(flip) {
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.night_mode));
                flip = false;
            } else{
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.standard));
            }
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    /**
     * One of the fucntions provided by the Google services API.
     * Automatically detects when a user is connected or not
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        connected = true; //HANDLER FOR THE DIRECTIONS BUTTON SO IT KNOWS IT'S CONNECTED
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY); //Balance power and GPS accuracy.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * When connection to Google Map's API is suspended, will send an error message
     */
    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
        connected = false;
    }

    /**
     * When connection to Google Map's API has failed, will send an error message
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
        connected = false;
    }

    /**
     * One of the fucntions provided by the Google services API.
     * Automatically detects if the user moves
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null)
            mCurrLocationMarker.remove();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (plotCameras)
            cameraDatabaseClient.updateCameras(latLng,getApplicationContext(), mMap,requestQueue);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        Context context = getApplicationContext();
        Bitmap temp = BitmapFactory.decodeResource(context.getResources(),//TURN THE DRAWABLE ICON INTO A BITMAP
                R.drawable.user_location);
        Bitmap custom_marker = Bitmap.createScaledBitmap(temp, 80, 80, true);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(custom_marker));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        if (mGoogleApiClient != null)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * One of the functions provided by the Google Map API.
     * The location on the top right corner is the default location button.
     * Clicking it automatically zooms in on the user
     */
    @Override
    public boolean onMyLocationButtonClick(){
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    /**
     * One of the functions provided by the Google Map API. When you click on your physical location,
     * as in the blue dot, it tells you your location
     * @param location
     */
    @Override
    public void onMyLocationClick(@NonNull Location location) {}

    /**
     * When there is a routing error, will send an error message
     * @param e
     */
    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null)
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
    }

    /**
     * REQUIRED METHODS FROM THE EXTENDED CLASSES
     */
    @Override
    public void onRoutingStart() {}
    @Override
    public void onRoutingCancelled() {}

    /**
     * When routing has been made, will create polylines and add the shortest on to the map
     * @param route
     * @param shortestRoutingIndex
     */
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRoutingIndex) {
        polylines.clear();
        for (int i = 0; i <route.size(); i++) {
            int colorIndex = i % COLORS.length;
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
        }
    }

    /**
     * When map is clicked on
     * @param view
     */
    @Override
    public void onClick(View view) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.openDrawer(GravityCompat.START);
    }


}