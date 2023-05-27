package com.example.parcial_maps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.example.parcial_maps.databinding.ActivityMainBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements GoogleMap.OnMapLongClickListener, OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GoogleMap mMap;
    private Marker startMarker;
    private Marker endMarker;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Los permisos ya están concedidos, puedes realizar las acciones relacionadas con la ubicación aquí
            binding.mapview.onCreate(savedInstanceState);
            binding.mapview.onResume();
            binding.mapview.getMapAsync(googleMap -> {
                mMap = googleMap;

            mMap.setOnMapLongClickListener(this); // Agregar el listener al mapa
            binding.reset.setOnClickListener(v -> {
                if (startMarker != null) {
                    startMarker.remove();
                    startMarker = null;
                }
                if (endMarker != null) {
                    endMarker.remove();
                    endMarker = null;
                }
                mMap.clear();
                showRequiredLocation();
            });
            binding.zoomInButton.setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.zoomIn()));
            binding.zoomOutButton.setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.zoomOut()));
            showRequiredLocation();
            });
        } else {
            // Los permisos no están concedidos, solicitarlos al usuario
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this); // Agregar el listener al mapa
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        if (startMarker != null && endMarker != null) {
            Toast.makeText(this, "You can only add two markers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startMarker == null) {
            startMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        } else {
            endMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("End")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

            // Calcula la distancia entre los dos marcadores
            double distance = Math.round(calculateDistance(startMarker.getPosition(), endMarker.getPosition()) * 100.0) / 100.0;

            // Muestra la distancia en el snippet del marcador "End"
            String distanceText = String.format("Distance " + distance + " kms.");
            endMarker.setSnippet(distanceText);

            // Determina el color de la Polyline basado en la distancia
            int polylineColor = Color.RED;
            if (distance >= 1 && distance <= 5) {
                polylineColor = Color.YELLOW;
            } else if (distance > 5) {
                polylineColor = Color.BLUE;
            }

            // Dibuja la Polyline entre los dos marcadores
            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(startMarker.getPosition(), endMarker.getPosition())
                    .width(10)
                    .color(polylineColor);
            mMap.addPolyline(polylineOptions);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        }
    }

    private double calculateDistance(LatLng start, LatLng end) {
        double lat1 = start.latitude;
        double lon1 = start.longitude;
        double lat2 = end.latitude;
        double lon2 = end.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Radio de la Tierra en kilómetros
       double RADIUS_OF_EARTH = 6371.0;

        // Distancia en kilómetros
        return RADIUS_OF_EARTH * c;
    }

    private void showRequiredLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LatLng initialLatLng = new LatLng(4.627835831777713, -74.06409737200865);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(initialLatLng)
                .zoom(14)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showRequiredLocation();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapview.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.mapview.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        binding.mapview.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapview.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.mapview.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapview.onLowMemory();
    }
}