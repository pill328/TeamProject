package com.example.teamproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {


    private static final String TAG = "MainActivity";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    // widgets
    private EditText mSearchText;
    private ImageView mGps;

    // vars
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    private Boolean mLocationPermissionsGranted = false;

    // connect



    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "검색한 지역으로 이동합니다", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            init();

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Intent intent = new Intent(getBaseContext(), Scene2.class);
                    startActivity(intent);

                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSearchText = (EditText) findViewById(R.id.input_search);
        mGps = (ImageView) findViewById(R.id.ic_gps);

        getLocationPermission();
    }

    private void init() {
        Log.d(TAG,"init: initializing");

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    // execute method for searching
                    geoLocate();
                }
                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: clicked gps icon");
                getDeviceLocation();
            }
        });

        hideSoftKeyboard();
    }

    protected void geoLocate() {
        Log.d(TAG,"geoLocate: geolocating");
        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString,1);
        }catch (IOException e) {
            Log.d(TAG,"geoLocate: IOException: " + e.getMessage());
        }

        if(list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG,"geoLocate: found a location: " + address.toString());

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));
        }
    }

    protected void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if(mLocationPermissionsGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();

                            moveCamera(new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()), DEFAULT_ZOOM,"My Location");

                            moveCamera(new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()), DEFAULT_ZOOM,"hongik univ");

                            moveCamera(new LatLng(currentLocation.getLatitude(),
                                    currentLocation.getLongitude()), DEFAULT_ZOOM,"konkuk univ");

                        }else{
                            Toast.makeText(MainActivity.this,
                                    "지역을 찾을 수 없습니다",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e) {
            e.getMessage();
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG,"moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(options);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }

        if(title.equals("konkuk univ"))
            showAllRestaurantKonkuk();

        if(title.equals("hongik univ"))
            showAllRestaurantHongik();

        hideSoftKeyboard();
    }

    private void showAllRestaurantKonkuk() {
        Marker a, b, c, d, e, f, g, h;
        LatLng aPoint = new LatLng(37.543529, 127.071668);
        LatLng bPoint = new LatLng(37.543155, 127.070552);
        LatLng cPoint = new LatLng(37.540773, 127.071367);
        LatLng dPoint = new LatLng(37.539786, 127.069135);
        LatLng ePoint = new LatLng(37.541011, 127.068406);
        LatLng fPoint = new LatLng(37.538697, 127.073512);
        LatLng gPoint = new LatLng(37.541420, 127.068063);
        LatLng hPoint = new LatLng(37.541045, 127.071367);

        a = mMap.addMarker(new MarkerOptions()
                .position(aPoint)
                .title("우마이도"));
        a.showInfoWindow();
        b = mMap.addMarker(new MarkerOptions()
                .position(bPoint)
                .title("미야비"));
        b.showInfoWindow();
        c = mMap.addMarker(new MarkerOptions()
                .position(cPoint)
                .title("무스쿠스 건대점"));
        c.showInfoWindow();
        d = mMap.addMarker(new MarkerOptions()
                .position(dPoint)
                .title("일마지오(건대점)"));
        d.showInfoWindow();
        e = mMap.addMarker(new MarkerOptions()
                .position(ePoint)
                .title("로니로티 건대점"));
        e.showInfoWindow();
        f = mMap.addMarker(new MarkerOptions()
                .position(fPoint)
                .title("매드포갈릭 건대스타시티점"));
        f.showInfoWindow();
        g = mMap.addMarker(new MarkerOptions()
                .position(gPoint)
                .title("메이빌"));
        g.showInfoWindow();
        h = mMap.addMarker(new MarkerOptions()
                .position(hPoint)
                .title("TGIF 건대스타시티점"));
        h.showInfoWindow();
    }

    private void showAllRestaurantHongik() {
        Marker a, b, c, d, e, f, g, h;
        LatLng aPoint = new LatLng(37.553518, 126.924666);
        LatLng bPoint = new LatLng(37.553318, 126.923998);
        LatLng cPoint = new LatLng(37.553332, 126.923366);
        LatLng dPoint = new LatLng(37.552946, 126.923493);
        LatLng ePoint = new LatLng(37.552745, 126.923511);
        LatLng fPoint = new LatLng(37.552488, 126.923132);
        LatLng gPoint = new LatLng(37.550614, 126.923547);
        LatLng hPoint = new LatLng(37.552073, 126.921345);

        a = mMap.addMarker(new MarkerOptions()
                .position(aPoint)
                .title("알라또레"));
        a.showInfoWindow();
        b = mMap.addMarker(new MarkerOptions()
                .position(bPoint)
                .title("이뜰"));
        b.showInfoWindow();
        c = mMap.addMarker(new MarkerOptions()
                .position(cPoint)
                .title("청해루"));
        c.showInfoWindow();
        d = mMap.addMarker(new MarkerOptions()
                .position(dPoint)
                .title("Temple Food"));
        d.showInfoWindow();
        e = mMap.addMarker(new MarkerOptions()
                .position(ePoint)
                .title("다락투"));
        e.showInfoWindow();
        f = mMap.addMarker(new MarkerOptions()
                .position(fPoint)
                .title("홍대라면"));
        f.showInfoWindow();
        g = mMap.addMarker(new MarkerOptions()
                .position(gPoint)
                .title("예티"));
        g.showInfoWindow();
        h = mMap.addMarker(new MarkerOptions()
                .position(hPoint)
                .title("홍대 돈부리 본점"));
        h.showInfoWindow();
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    // initialize map
                    initMap();
                }
            }
        }
    }


    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}