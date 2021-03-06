package com.a1403.aditumall;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.a1403.aditumall.model.Venue;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    MapView mMapView;
    private GoogleMap mGoogleMap;
    private GoogleApiClient locationApi;
    private LocationRequest mLocationRequest;
    private Venue venue;
    Location mLastLocation;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflat and return the layout
        View v = inflater.inflate(R.layout.fragment_maps, container,
                false);

        locationApi = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mMapView = (MapView) v.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        MainActivity currentActivity = (MainActivity) getActivity();

        venue = currentActivity.getVenue();

        /*mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds*/

        SupportMapFragment mSupportMapFragment;

        mSupportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mSupportMapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mSupportMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.map, mSupportMapFragment).commit();
        }

        if (mSupportMapFragment != null) {
            mSupportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    LatLng dummy = new LatLng(venue.getLat(),venue.getLongt());
                    Log.i(TAG, "Lat " + Double.toString(venue.getLat()) );
                    LatLng pastLocation = getPastLocation();
                    if(mMap != null) {
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(dummy).zoom(17.0f).build();
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                            mMap.moveCamera(cameraUpdate);
                            mMap.addMarker(new MarkerOptions().position(dummy).title("Current Venue"));
                    }
                }
            });
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        locationApi.connect();
        mapSetUp();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        /*savePastLocation();
        if (locationApi.isConnected()) {
            locationApi.disconnect();
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        //setCurrentLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        buildGoogleApiClient();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    Toast.makeText(getActivity(), "Permission deny to read your location", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    private void mapSetUp(){
        if(mMap != null) {
            LatLng dummy = new LatLng(venue.getLat(),venue.getLongt());

            mMap.addMarker(new MarkerOptions().position(dummy).title("Current Venue"));
        }
    }
    private void handleNewLocation(Location location){
        Log.d(TAG, location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        LatLng dummy = new LatLng(venue.getLat(),venue.getLongt());
        MarkerOptions options = new MarkerOptions()
                .position(dummy)
                .title("I am here!");

        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(dummy));
    }
    synchronized void buildGoogleApiClient() {
        locationApi = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }
    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }
    @Override
    public void onStart() {
        super.onStart();
        locationApi.connect();
    }

    private void setCurrentLocation(){
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100); // Update location every second
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Toast.makeText(getActivity(),"TODO: LOCATION PERMISSION STATEMENT",Toast.LENGTH_LONG).show();

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            1);

                }
            }
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(locationApi);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(locationApi, mLocationRequest, this);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(locationApi);

            double lat = mLastLocation.getLatitude();
            double lon = mLastLocation.getLongitude();
            mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
            mGoogleMap.setMyLocationEnabled(true);
            LatLng currentLocation = new LatLng(lat, lon);
            CameraPosition cameraPosition = new CameraPosition.Builder().target(currentLocation).zoom(15.0f).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            mGoogleMap.moveCamera(cameraUpdate);

        }
        else {
            handleNewLocation(location);
        }

    }
    private LatLng getPastLocation(){
        SharedPreferences sp = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String returnedLongitude = sp.getString("pastLongitude","0");
        String returnedLatitude = sp.getString("pastLatitude","0");

        LatLng returnedLatLng = new LatLng(Double.valueOf(returnedLatitude),Double.valueOf(returnedLongitude));

        return returnedLatLng;
    }
    private void savePastLocation(){
        SharedPreferences sp = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Toast.makeText(getActivity(),"TODO: LOCATION PERMISSION STATEMENT",Toast.LENGTH_LONG).show();

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            1);

                }
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(locationApi, mLocationRequest, this);
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(locationApi);
        String longitude = Double.toString(mLastLocation.getLongitude());
        String latitude = Double.toString(mLastLocation.getLatitude());

        editor.putString("pastLatitude",longitude);
        editor.putString("pastLongitude",latitude);

        editor.apply();
    }
}
