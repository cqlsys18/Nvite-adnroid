package com.nvite.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nvite.R;

public class EventlocationActivity extends BaseActivity implements OnMapReadyCallback {
    ImageView back;
    GoogleMap mGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eventlocation);

        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        setUpUI();
    }


    private void setUpUI() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        try {
            mGoogleMap.setMyLocationEnabled(false);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }

        LatLng PlaceLatLng = new LatLng(Double.parseDouble(getIntent().getStringExtra("lat")), Double.parseDouble(getIntent().getStringExtra("long")));
        Marker marker = googleMap.addMarker(new MarkerOptions().position(PlaceLatLng).title(getIntent().getStringExtra("event_name")).icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        marker.showInfoWindow();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PlaceLatLng, 14.0f));
    }
}

