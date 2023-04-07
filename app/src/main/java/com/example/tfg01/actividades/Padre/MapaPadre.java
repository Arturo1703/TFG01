package com.example.tfg01.actividades.Padre;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.tfg01.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

//Esta funcion se encarga de mostrar el mapa del padre
public class MapaPadre extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap mMap;
    SharedPreferences mPreference;
    String position, commentary;
    //Configura el mapa que se va a ver
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_padre);
        mPreference = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.FragmentoMapaExtra);
        mapFragment.getMapAsync(this);
    }

    //Muestra las localizaciones con su fecha asignada en nuestro mapa (tantas como se le hayan pasado por el bundle)
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        position = mPreference.getString("pos", "");
        commentary = mPreference.getString("com", "");
        LatLng madrid;
        if(position.isEmpty()){
            madrid = new LatLng(40.30, 3.40);
            mMap.addMarker(new MarkerOptions().position(madrid)).setTitle("No location retrieved");
            mMap.moveCamera(CameraUpdateFactory.newLatLng(madrid));
        }else{
            String[] posAux = position.split(",");
            String[] comAux = commentary.split(",");
            int Counter = Integer.valueOf(posAux[0]);

            for (int x = 0; x < Counter; x++){
                Double lat = Double.valueOf(posAux[1 + x*2]);
                Double lon = Double.valueOf(posAux[2 + x*2]);
                LatLng childLocation = new LatLng(lat, lon);

                mMap.addMarker(new MarkerOptions().position(childLocation)).setTitle("Marker " + x +": " + comAux[x+1]);
                if(x == Counter -1){
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(childLocation));
                }
            }
        }
    }
}