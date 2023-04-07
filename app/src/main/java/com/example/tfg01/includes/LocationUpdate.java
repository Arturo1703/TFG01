package com.example.tfg01.includes;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.tfg01.actividades.hijo.PrincipalHijoActivity;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

public class LocationUpdate extends Service {

    DatabaseReference mDatabase;
    FirebaseAuth mAuth;




    private LocationCallback locationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            if (locationResult != null && locationResult.getLastLocation() != null) {

                //Current Location
                double latitud = locationResult.getLastLocation().getLatitude();
                double longitud = locationResult.getLastLocation().getLongitude();
                mAuth = FirebaseAuth.getInstance();
                //////////////////////////////

                String uid = mAuth.getCurrentUser().getUid();

                //Se define la instancia de la Base de datos de Firebase
                mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();

                ArrayList<String> ListLocation = new ArrayList<>();

                // Se almacenan los valores de la localizacion en los campos creados cuando fueron generadas las
                // cuentas de los niños (lat y lon), de deben almacenar teniendo en cuenta los uid del hijo. Para almacenar los datos
                // nos dirigimos a los nodos /users/nino/"UID"/lat y /users/nino/"UID"/lon. se deben de cambiar de double a String
                // se puede solo añadiendo un caracter y cambia el típo de dato (ejemp: latitud+"")

                mDatabase.child("Users").child("hijo").child(uid).child("lat").setValue(latitud+"");
                mDatabase.child("Users").child("hijo").child(uid).child("lon").setValue(longitud+"");

            }

        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void startLocationService() {

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (android.os.Build.VERSION.SDK_INT <= 10){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void stopLocationService(){

        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();

    }

    //@Override
    public int onStartCommand(Intent intent, int action) {

        if(intent != null){
            if (action != -1){
                if (action == 1){

                    startLocationService();

                }else if(action == 0){

                    stopLocationService();

                }

            }

        }

        return START_STICKY;

    }

}

    /*ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION,false);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            // Precise location access granted.
                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            // Only approximate location access granted.
                        } else {
                            // No location access granted.
                        }
                    }
            );

// ...

// Before you perform the actual permission request, check whether your app
// already has the permissions, and whether your app needs to show a permission
// rationale dialog. For more details, see Request permissions.
locationPermissionRequest.launch(new String[] {
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
        });*/

/*fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
@Override
public void onSuccess(Location location) {
        // Got last known location. In some rare situations this can be null.
        if (location != null) {
            // Logic to handle location object
        }
    }
});*/
