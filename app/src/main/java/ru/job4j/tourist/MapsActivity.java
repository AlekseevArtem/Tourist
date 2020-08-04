package ru.job4j.tourist;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import ru.job4j.tourist.store.MemStoreTrack;
import ru.job4j.tourist.store.SQLStoreMarks;
import ru.job4j.tourist.store.SQLStoreTrack;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        SingleChoiceDialogFragment.SingleChoiceListener,
        GoogleMap.OnInfoWindowClickListener{
    private final int SHOW_MARK = 1;
    private final String LOG = "MainActivity";

    private SQLStoreMarks mMarkStore;
    private SQLStoreTrack mTrackStore;
    private MemStoreTrack mTempTrackStore;
    private Location mLocation;
    private GoogleMap mMap;
    private int mMode;
    private Disposable sbr;
    private boolean sbrInProcess = false;
    private GeoApiContext mGeoApiContext;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mode", mMode);
        outState.putBoolean("sbrInProcess", sbrInProcess);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOW_MARK && resultCode == RESULT_OK) {
            int id = data.getIntExtra("mark", 0);
            Mark mark = mMarkStore.findMarkByID(id);
            mMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(mark.getLatitude(), mark.getLongitude()), 15));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if(savedInstanceState!= null) {
            mMode = savedInstanceState.getInt("mode");
            if(savedInstanceState.getBoolean("sbrInProcess")) startTrack();
        }
        mMarkStore = SQLStoreMarks.getInstance(this);
        mTrackStore = SQLStoreTrack.getInstance(this);
        setDynamicButtons();
        Button mode = findViewById(R.id.mode);
        mode.setOnClickListener(v -> swapMode());
        if (isMapPermissionGranted()) {
            initMap();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(mMode == 0) setAllMarks(); else setAllTracks();
        initPlacesAPI();
        initLocListener();
    }

    @SuppressLint("MissingPermission")
    private void initLocListener() {
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

    private void initPlacesAPI() {
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
                Log.i(LOG, "An error occurred: " + status);
            }
        });
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if(mGeoApiContext == null){
            mGeoApiContext = new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();
        }
    }

    private void calculateDirections(Marker marker){
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mLocation.getLatitude(),
                        mLocation.getLongitude()
                )
        );
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(LOG, "onResult: successfully retrieved directions.");
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(LOG, "onFailure: " + e.getMessage() );

            }
        });
    }

    private void addPolylinesToMap(final DirectionsResult result){
        runOnUiThread(() -> {
            for(DirectionsRoute route: result.routes){
                List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                List<LatLng> newDecodedPath = new ArrayList<>();
                for(com.google.maps.model.LatLng latLng: decodedPath){
                    newDecodedPath.add(new LatLng(
                            latLng.lat,
                            latLng.lng
                    ));
                }
                Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                polyline.setColor(getResources().getColor(R.color.colorPrimaryDark));
                polyline.setClickable(true);

            }
        });
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

    private void swapMode() {
        DialogFragment singleChoiceDialog = SingleChoiceDialogFragment.newInstance(mMode);
        singleChoiceDialog.show(getSupportFragmentManager(), "Single choice dialog");
    }

    private void addNewMark(LatLng coordinates, String title) {
        MarkerOptions marker = new MarkerOptions().position(coordinates).title(title);
        marker.flat(true);
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
        mMarkStore.addMark(new Mark(coordinates.latitude, coordinates.longitude, title));
    }

    private void setAllMarks() {
        mMarkStore.getMarks().stream().forEach(mark -> {
            LatLng coordinates = new LatLng(mark.getLatitude(), mark.getLongitude());
            MarkerOptions marker = new MarkerOptions().position(coordinates).title(mark.getTitle());
            marker.flat(true);
            mMap.addMarker(marker);
        });
        mMap.setOnInfoWindowClickListener(this);
    }

    private void setAllTracks() {
        mTrackStore.getTracks().stream().forEach(track -> {
            Polyline line = mMap.addPolyline(new PolylineOptions().addAll(track.getCoordinates()));
            line.setColor(track.getColor());
            line.setWidth(track.getWidth());
        });
    }

    private void toMarkListActivity(View view) {
        Intent intent = new Intent(this, MarkListActivity.class);
        startActivityForResult(intent, SHOW_MARK);
    }

    public void startTrack() {
        if(mTempTrackStore == null) {
            Track track = new Track("track", getResources().getColor(R.color.colorPrimaryDark), 3, new ArrayList<>());
            mTempTrackStore = MemStoreTrack.getInstance(track);
        }
        Track track = mTempTrackStore.getTrack();
        if(sbr == null || sbr.isDisposed()) {
            if(track.getCoordinates().size() == 0){
                track.getCoordinates().add(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
            }
            this.sbr = Observable.interval(5, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(v -> track.getCoordinates().add(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())));
            sbrInProcess = true;
            setDynamicButtons();
        }
    }

    public void stopAndAddTrack() {
        this.sbr.dispose();
        Track track = mTempTrackStore.getTrack();
        mTempTrackStore = null;
        track.getCoordinates().add(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
        mTrackStore.addTrack(track);
        sbrInProcess = false;
        Polyline line = mMap.addPolyline(new PolylineOptions().addAll(track.getCoordinates()));
        line.setColor(track.getColor());
        line.setWidth(track.getWidth());
        setDynamicButtons();
    }

    public void setDynamicButtons() {
        Button action = findViewById(R.id.current);
        Button list = findViewById(R.id.list);
        list.setOnClickListener(this::toMarkListActivity);
        if(mMode == 0) {
            action.setText(R.string.i_am_here);
            action.setOnClickListener(this::getCurrentLocation);
            list.setEnabled(true);
            list.setText(R.string.list_of_marks);
        } else {
            list.setEnabled(false);
            list.setText(R.string.list_of_tracks);
            if (!sbrInProcess) {
                action.setText(R.string.start_track);
                action.setOnClickListener(v -> startTrack());
            } else {
                action.setText(R.string.finish_track);
                action.setOnClickListener(v -> stopAndAddTrack());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.sbr != null) {
            this.sbr.dispose();
        }
    }

    @Override
    public void onPositiveSwapMode(int mode) {
        if (mMode != mode) {
            mMode = mode;
            mMap.clear();
            setDynamicButtons();
            if(mMode == 0) setAllMarks(); else setAllTracks();
        }
    }

    @Override
    public void onNegativeSwapMode() {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you wanna build a path to " + marker.getTitle() + "?")
                .setCancelable(true)
                .setPositiveButton("Yes", (dialog, id) -> {
                    calculateDirections(marker);
                    dialog.dismiss();
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }
}