package ru.job4j.tourist;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.Objects;

import ru.job4j.tourist.store.SQLStore;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private Location mLocation;
    private GoogleMap mMap;
    private SQLStore mStore;
    private final int SHOW_MARK = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOW_MARK && resultCode == RESULT_OK) {
            int id = data.getIntExtra("mark", 0);
            Mark mark = mStore.findMarkByID(id);
            mMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(mark.getLatitude(), mark.getLongitude()), 15));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mStore = SQLStore.getInstance(this);
        Button current = findViewById(R.id.current);
        current.setOnClickListener(this::getCurrentLocation);
        Button list = findViewById(R.id.list);
        list.setOnClickListener(this::toMarkListActivity);
        if (isMapPermissionGranted()) {
            initMap();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setAllMarks();
        LocationListener loc = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLocation = location;
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
        };
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Objects.requireNonNull(locationManager).
                requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, loc);
        Places.initialize(this, getString(R.string.google_maps_key));
        AutocompleteSupportFragment search = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        search.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        search.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                addNewMark(place.getLatLng(), place.getName());
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i("MainActivity", "An error occurred: " + status);
            }
        });
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private boolean isMapPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            initMap();
        }
    }

    private void getCurrentLocation(View view) {
        if (mLocation != null) {
            String title = "Hello Maps";
            LatLng coordinates = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            addNewMark(coordinates, title);
        }
    }

    private void addNewMark(LatLng coordinates, String title) {
        MarkerOptions marker = new MarkerOptions().position(coordinates).title(title);
        marker.flat(true);
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
        mStore.addMark(new Mark(coordinates.latitude, coordinates.longitude, title));
    }

    private void setAllMarks() {
        mStore.getMarks().stream().forEach(mark -> {
            LatLng coordinates = new LatLng(mark.getLatitude(), mark.getLongitude());
            MarkerOptions marker = new MarkerOptions().position(coordinates).title(mark.getTitle());
            marker.flat(true);
            mMap.addMarker(marker);
        });
    }

    private void toMarkListActivity(View view) {
        Intent intent = new Intent(this, MarkListActivity.class);
        startActivityForResult(intent, SHOW_MARK);
    }
}