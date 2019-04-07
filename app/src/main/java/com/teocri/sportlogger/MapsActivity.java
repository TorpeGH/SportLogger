package com.teocri.sportlogger;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.teocri.sportlogger.MainActivity.db;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnStreetViewPanoramaReadyCallback, GoogleMap.OnCameraMoveListener, StreetViewPanorama.OnStreetViewPanoramaCameraChangeListener, View.OnTouchListener, ConnectionCallbacks, OnConnectionFailedListener,
        LocationListener {

    private static StreetViewPanorama mStreet;
    private static GoogleMap mMap;
    SupportStreetViewPanoramaFragment streetFragment;
    SupportMapFragment mapFragment;
    ArrayList<Marker> markers = new ArrayList<>();
    FrameLayout fl;
    boolean upDownSide = true;
    public static boolean newOptions = false;
    boolean reconnecting = false;

    static public long updateTime = 60000; //1 minute
    static public long updateGap = 50;     //50 meter

    static final int[] priority = {
            LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            LocationRequest.PRIORITY_LOW_POWER,
            LocationRequest.PRIORITY_NO_POWER};

    // variables related to geo location
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //  MENU
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_gps_off);//icon

        //  MAPS
        streetFragment = (SupportStreetViewPanoramaFragment) getSupportFragmentManager().findFragmentById(R.id.streetView);
        streetFragment.getStreetViewPanoramaAsync(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fl = (FrameLayout) findViewById(R.id.frameLayout);
        fl.setOnTouchListener(this);

        // CONNECTION
        // do not even start if Google services are not available
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int check = googleAPI.isGooglePlayServicesAvailable(this);
        if (check != ConnectionResult.SUCCESS) {
            googleAPI.showErrorNotification(this, check);
            Toast.makeText(this, "Play Services Error: " + check, Toast.LENGTH_SHORT).show();
            Dialog d = googleAPI.getErrorDialog(this, check, 777);
            d.show();
            return;
        }
        // init ApiClient
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        // create a default locationRequest
        locationRequest = new LocationRequest();

    }


    /**********_MENU_**********/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_1:
                startPreferencesRequest(getCurrentFocus());
                return true;

            case R.id.item_2:
                if (googleApiClient.isConnected()) {
                    Toast.makeText(this, R.string.toast_alrady_connected, Toast.LENGTH_SHORT).show();
                }else{
                    googleApiClient.connect();
                }
                return true;

            case R.id.item_3:
                if (googleApiClient.isConnected()) {
                    googleApiClient.disconnect();
                }
                if (googleApiClient.isConnected())
                    Toast.makeText(getApplicationContext(), R.string.toast_errpr_during_disconnection, Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(getApplicationContext(), R.string.toast_disconnected, Toast.LENGTH_SHORT).show();
                    getSupportActionBar().setIcon(R.mipmap.ic_launcher_gps_off);
                }
                return true;

            case R.id.item_4:
                startReportLocations(getCurrentFocus());
                return true;

            case R.id.item_5:
                showAlert();
                return true;

            case R.id.item_6:
                streetViewVisibility(0);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAlert() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(R.string.delete_db)
                .setMessage(R.string.message_sure_to_delete)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        streetViewVisibility(0);
                        db.onUpgrade(db.getWritableDatabase(), 1, 1);
                        removeMarkers();
                        Toast.makeText(getApplicationContext(), R.string.toast_db_cleared, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {  }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**********_INITIAL_SETUP_**********/
    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        mStreet = streetViewPanorama;
        mStreet.setOnStreetViewPanoramaCameraChangeListener(this);
        streetViewVisibility(0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setUpZoom();
        loadDB();

        mMap.setOnCameraMoveListener(this);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                moveCameraPos(marker);
                return false;
            }
        });
    }

    private int loadDB() {
        ArrayList<String> locations = db.getRows();
        setMarkers(locations);
        return locations.size();
    }

    private void initialPos(double lat, double lon) {
        LatLng position = new LatLng(lat, lon);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14.0f));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
    }

    private void setUpZoom() {
        mMap.setMinZoomPreference(0.0f);
        mMap.setMaxZoomPreference(30.0f);
    }

    /**********_START_ACTIVITIES_**********/
    public void startReportLocations(View v) {
        Intent intent = new Intent(this, ReportLocations.class);
        startActivity(intent);
    }

    public void startPreferencesRequest(View v) {
        Intent intent = new Intent(this, PreferencesRequestActivity.class);
        startActivity(intent);
    }

    /**********_ON_EVENTS_**********/
    @Override
    public void onStreetViewPanoramaCameraChange(StreetViewPanoramaCamera streetViewPanoramaCamera) {
        if (upDownSide == false)
            mMap.moveCamera(
                    CameraUpdateFactory.newCameraPosition
                            (new CameraPosition(mMap.getCameraPosition().target,
                                                mMap.getCameraPosition().zoom,
                                                mMap.getCameraPosition().tilt,
                                                mStreet.getPanoramaCamera().bearing)));
    }

    @Override
    public void onCameraMove() {
        if (upDownSide == true)
            mStreet.animateTo(
                    new StreetViewPanoramaCamera(
                            mStreet.getPanoramaCamera().zoom,
                            mStreet.getPanoramaCamera().tilt,
                            mMap.getCameraPosition().bearing), -1);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getY() > streetFragment.getView().getHeight()) {
            upDownSide = true;
            return false;
        }
        upDownSide = false;
        return false;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    protected void onResume() {
        super.onResume();
        if (newOptions == true) {
            reconnect();
            newOptions = false;
        }
    }

    /**********_CAMERA_METHODS_**********/
    private void moveCameraPos(Marker marker) {
        //mMap.moveCamera(CameraUpdateFactory.zoomTo(13.5f));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        mStreet.setPosition(marker.getPosition());
        streetViewVisibility(1);
    }

    static public void moveCamLatLon(Double lat, Double lon) {
        LatLng pos = new LatLng(lat, lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        mStreet.setPosition(pos);
    }

    /**********_MARKER_METHODS_**********/
    static public Marker addMarkers(double lat, double lon, String name) {
        return mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(name));
    }

    private int removeMarkers() {
        int ret = markers.size();
        for (int i = 0; i < markers.size(); i++)
            markers.remove(i);
        mMap.clear();
        return ret;
    }

    private void setMarkers(ArrayList<String> locations) {
        if (locations.size() > 0) {
            markers.add(0, addMarkers(Double.parseDouble(db.readLatitude(locations.get(0))), Double.parseDouble(db.readLongitude(locations.get(0))), db.readDate(locations.get(0)) + " - " + db.readTime(locations.get(0))));
            initialPos(Double.parseDouble(db.readLatitude(locations.get(0))), Double.parseDouble(db.readLongitude(locations.get(0))));
            for (int i = 1; i < locations.size(); i++)
                markers.add(i, addMarkers(Double.parseDouble(db.readLatitude(locations.get(i))), Double.parseDouble(db.readLongitude(locations.get(i))), db.readDate(locations.get(i)) + " - " + db.readTime(locations.get(i))));
        }
    }

    private void addLocation(Location location) {
        if (reconnecting == true) {
            reconnecting = false;
            return;
        }
        if (location == null) {
            Toast.makeText(this, R.string.toast_location_null, Toast.LENGTH_SHORT).show();
            return;
        }

        double lat = location.getLatitude();
        double lon = location.getLongitude();

        Date date = new Date(location.getTime());
        DateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String day = dayFormat.format(date);
        String time = timeFormat.format(date);

        db.addLocation(lat, lon, day, time, priority[1]);
        addMarkers(lat, lon, day + " - " + time);
        moveCamLatLon(lat, lon);
        Toast.makeText(this, R.string.toast_new_marker_placed, Toast.LENGTH_SHORT).show();
    }

    /***********_LocationListener_METHODS_**********/
    // method called when the connection is established
    @Override
    public void onConnected(Bundle connectionHint) {
        // permission check
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        if (checkFineLocationPermission() & checkCoarseLocationPermission() == true){
            Toast.makeText(this, R.string.toast_connected, Toast.LENGTH_SHORT).show();
            getSupportActionBar().setIcon(R.mipmap.ic_launcher_gps_on);
            // show last known location
            LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            //addLocation(location);
            locationRequest.setPriority(priority[1])
                    .setInterval(updateTime)
                    .setFastestInterval(updateTime)
                    .setSmallestDisplacement(updateGap);
            // register for updates
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }else{
            Toast.makeText(this, R.string.toast_permission_error, Toast.LENGTH_SHORT).show();
        }
    }

    // receives location updates
    public void onLocationChanged(Location location) {
        addLocation(location);
    }

    // this is required by the interface CollectionCallback but we have nothing to do
    public void onConnectionSuspended(int cause) {
        Toast.makeText(this, getString(R.string.toast_connection_suspended) + cause, Toast.LENGTH_LONG).show();
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_gps_off);
    }

    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(this, getString(R.string.toast_connection_failed) + result, Toast.LENGTH_LONG).show();
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_gps_off);
    }

    /**********_PERMISSIONS_CHECK_**********/
    public boolean checkFineLocationPermission(){
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }
    public boolean checkCoarseLocationPermission(){
        String permission = "android.permission.ACCESS_COARSE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    /**********_UTILITIES_**********/
    private void streetViewVisibility(int i) {
        mapFragment.getView().setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, i));
    }

    private void reconnect() {
        if (!googleApiClient.isConnected())
            return;
        Toast.makeText(this, R.string.toast_reconnectiong_for_changes, Toast.LENGTH_SHORT).show();
        reconnecting = true;
        googleApiClient.disconnect();
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_gps_off);
        googleApiClient.connect();
    }
}
