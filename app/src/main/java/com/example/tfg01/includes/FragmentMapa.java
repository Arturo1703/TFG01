package com.example.tfg01.includes;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tfg01.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FragmentMapa extends Fragment {

    SharedPreferences m2Pref;
    public static FragmentMapa newInstance(String position) {
        FragmentMapa f = new FragmentMapa();
        Bundle args = new Bundle();
        args.putString("pos", position);
        f.setArguments(args);
        return f;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        String pos = bundle.getString("pos");
        Activity activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        String[] posAux = pos.split(",");

        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_Map);
        int Counter = Integer.valueOf(posAux[0]);

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {

                MarkerOptions markerOptions = new MarkerOptions();

                googleMap.clear();

                for (int x = 0; x < Counter; x++){
                    Double lat = Double.valueOf(posAux[1 + x*2]);
                    Double lon = Double.valueOf(posAux[2 + x*2]);
                    LatLng childLocation = new LatLng(lat, lon);
                    markerOptions.position(childLocation);
                    markerOptions.title("Marker "+ x + "location"+ childLocation.latitude + " : " + childLocation.longitude);
                    if(x == Counter -1){
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(childLocation, 12));
                    }
                    googleMap.addMarker(markerOptions);
                }


                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {
                        MarkerOptions markerOptions = new MarkerOptions();

                        LatLng sydney = new LatLng(40.4405309, -3.786044);
                        markerOptions.position(sydney);
                        markerOptions.title(latLng.latitude + " : " + latLng.longitude);

                        googleMap.clear();

                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10));
                        googleMap.addMarker(markerOptions);

                    }
                });

            }
        });

        return view;
    }
}