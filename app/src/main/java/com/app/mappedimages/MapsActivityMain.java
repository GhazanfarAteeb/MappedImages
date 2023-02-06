package com.app.mappedimages;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.app.mappedimages.databinding.ActivityMapsMainBinding;
import com.app.mappedimages.databinding.ItemDialogBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivityMain extends AppCompatActivity implements OnMapReadyCallback {
    private static Location currentLocation;
    private GoogleMap mMap;
    private ActivityMapsMainBinding binding;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient client;
    private static List<MarkerOptions> markersList = new ArrayList<>();
    private static final String[] PERMISSIONS = {
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    LocationCallback callback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            currentLocation = locationResult.getLastLocation();
            Uri data = getIntent().getData();
            getIntent().setData(null);
            if (data != null) {
                MarkerOptions marker = new MarkerOptions();
                marker.draggable(false);
                marker.position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                marker.title(data.toString());
                marker.visible(true);
                markersList.add(marker);
                if (mMap != null) {
                    for (MarkerOptions m: markersList) {
                        mMap.addMarker(m);
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 18));
                }
            }
        }
    };

    private void startLocationUpdates() {
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        client.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper());
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(0)
                .setFastestInterval(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (PermissionUtils.checkPermission(MapsActivityMain.this, android.Manifest.permission.ACCESS_FINE_LOCATION) && PermissionUtils.checkPermission(MapsActivityMain.this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
            enableLocationSettings();
        } else {
            ActivityResultLauncher<String[]> permissionRequest = registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                        if (fineLocationGranted != null && fineLocationGranted && coarseLocationGranted != null && coarseLocationGranted) {
                            Log.d("LoginActivity", "Location permissions granted");
                            enableLocationSettings();
                        }
                    });
            permissionRequest.launch(PERMISSIONS);
        }
//        enableLocationSettings();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        createLocationRequest();
        startLocationUpdates();
        binding.fabCaptureImage.setOnClickListener(view -> {
            startActivity(new Intent(MapsActivityMain.this, CameraActivity.class));
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
//                marker.hideInfoWindow();
                AlertDialog dialog = new AlertDialog.Builder(MapsActivityMain.this)
                        .create();
                ItemDialogBinding itemDialogBinding = ItemDialogBinding.inflate(LayoutInflater.from(MapsActivityMain.this));
                dialog.setView(itemDialogBinding.getRoot());
                Glide.with(MapsActivityMain.this).load(marker.getTitle()).into(itemDialogBinding.ivImage);
                dialog.show();
                return true;
            }
        });

    }

    public void enableLocationSettings() {
        if (!isLocationEnabled(this)) {

            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(0)
                    .setFastestInterval(0)
                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
                    .addOnSuccessListener(this, (LocationSettingsResponse response) -> {

                    }).addOnFailureListener(this, ex -> {
                        if (ex instanceof ResolvableApiException) {
                            // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                            try {
//                                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                                ResolvableApiException resolvable = (ResolvableApiException) ex;
                                resolvable.startResolutionForResult(MapsActivityMain.this, 0);
                            } catch (IntentSender.SendIntentException sendEx) {
                                // Ignore the error.
                            }
                        }
                    });

        }
    }

    public static boolean isLocationEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        boolean gps_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (NullPointerException ex) {
            ex.getLocalizedMessage();
        }

        boolean network_enabled = false;
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (NullPointerException ex) {
            ex.getLocalizedMessage();
        }

        return (gps_enabled || network_enabled);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            enableLocationSettings();
        }

    }
}