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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.util.List;

///////////////////////
import android.widget.Toast;
import android.content.Context;

/////////////////


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient = null; //new
    private static final String TAG = MapsActivity.class.getSimpleName();

    //private GoogleApiClient mGoogleApiClient;

    // The entry points to the Places API.
    //private GeoDataClient mGeoDataClient;
    //private PlaceDetectionClient mPlaceDetectionClient;
    LocationManager locationManager;
    String curr_location = null;
    LatLng latLng = new LatLng(0, 0);

    @Override
    protected void onCreate(Bundle savedInstanceState) { //CREATING THE MAP
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*FUTURE USE FOR BETTER LOCATION FUNCTIONALITY*/
        /*
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }*/

    }
    /*FUTURE USE FOR BETTER LOCATION FUNCTIONALITY*/
    /*
    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }
*/
    /////////////////////////////////////////////////////
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123; //REQUEST CODE USED IN THE PERMISSION REQUEST.  STILL NOT SURE IF THIS NUMBER MATTERS. "123" IS A RANDOM NUMBER.



    @Override
    public void onMapReady(GoogleMap googleMap) { //THE MAP IS NOW RUNNING

        /*THIS IS TO GRAB THE PROPER MAP STYLE JSON FILE. SEE THE APP/RES/RAW FOLDER FOR THE JSON FILENAME OPTIONS*/

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.standard)); //THIS IS THE STANDARD OPTION. FUTURE UPDATES WILL ALLOW FOR DYNAMICALLY CHANGING THE MAP STYLE.
                            // THIS WOULD ALLOW SOMETHING LIKE CLICKING A BUTTON TO CHANGE FROM STANDARD OVERVIEW TO A NIGHT MODE.
                            //FOR ARTISTIC PURPOSES ONLY
                            //I WOULD ALSO LIKE TO ADD IN A WAY TO DISPLAY THE MAP IN SATELLITE MODE

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        //INITIALIZATION
        int permissionCheck = ContextCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION);

        //ASK THE USER IF WORLDVIEW CAN TRACK THEIR LOCATION
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
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

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

        }
        else{
            Context context = getApplicationContext();
            CharSequence text = "Location Services Enabled."; //WE HAVE PERMISSION TO TRACK THEM
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }


        mMap = googleMap; //OBJECT FOR MAP MANIPULATION

        // Initializing proper UI settings
        UiSettings map_settings = mMap.getUiSettings();
        map_settings.setZoomControlsEnabled(true);
        map_settings.setCompassEnabled(true);


        /*START OF LOCATION TRACKING CODE*/
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        MarkerOptions a = new MarkerOptions().position(latLng); //CREATE A MARKER FOR THE USER'S LOCATION
        final Marker user_location = mMap.addMarker(a);

        //check whether the network provider is enabled
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){ //USING THE NETWORK PROVIDER FOR LOCATION TRACKING
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new android.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    //get coordinates
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    //create latlng class
                    latLng = new LatLng(latitude, longitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                        curr_location = addressList.get(0).getAddressLine(0);
                        user_location.setPosition(latLng); //UPDATE THE MARKER AS THEY MOVE AROUND
                        user_location.setTitle(curr_location);
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f)); //JUST FOR DEBUGGING. THIS LINE CAUSES THE CAMERA TO RESET TOO OFTEN

                    } catch (IOException e) {
                        e.printStackTrace();
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
        else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { //USING GPS DATA FOR LOCATION TRACKING
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
                        curr_location = addressList.get(0).getAddressLine(0);//+", ";
                        //curr_location += addressList.get(0).getLocality()+", ";
                        //curr_location += addressList.get(0).getCountryName();
                        mMap.addMarker(new MarkerOptions().position(latLng).title(curr_location));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                    } catch (IOException e) {
                        e.printStackTrace();
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

    }

    /*FUTURE USE FOR BETTER LOCATION FUNCTIONALITY*/
    /*
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }*/

}
