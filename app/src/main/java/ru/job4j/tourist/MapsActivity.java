package ru.job4j.tourist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
        if(requestCode == SHOW_MARK && resultCode == RESULT_OK) {
            int id = data.getIntExtra("mark", 0);
            Mark mark = mStore.findMarkByID(id);
            mMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(mark.getLatitude(),mark.getLongitude()), 15));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mStore = SQLStore.getInstance(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Button current = findViewById(R.id.current);
        current.setOnClickListener(this::getCurrentLocation);
        Button list = findViewById(R.id.list);
        list.setOnClickListener(this::toMarkListActivity);
    }

    private void getCurrentLocation(View view) {
        if (mLocation != null) {
            String title = "Hello Maps";
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();
            LatLng coordinates = new LatLng(latitude, longitude);
            MarkerOptions marker = new MarkerOptions().position(coordinates).title(title);
            marker.flat(true);
            mMap.addMarker(marker);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
            mStore.addMark(new Mark(latitude, longitude, title));
        }
    }

    private void setAllMarks() {
        mStore.getMarks().stream().forEach(mark -> {
            LatLng coordinates = new LatLng(mark.getLatitude(), mark.getLongitude());
            MarkerOptions marker = new MarkerOptions().position(coordinates).title(mark.getTitle());
            marker.flat(true);
            mMap.addMarker(marker);
        });
    }

    private void toMarkListActivity(View view){
        Intent intent = new Intent(this, MarkListActivity.class);
        startActivityForResult(intent, SHOW_MARK);
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
    }
}