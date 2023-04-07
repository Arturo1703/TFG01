package com.example.tfg01.includes;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

//Esta clase se encarga de manejar el servicio de geolocalizacion que se ejecutará en el background de forma periodica
public class ServicioGeolocalizacion extends JobService {
    private static final String Tag = "ExampleJobService";
    private boolean JobCanceled = false;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

//onStartJob es una funcion que viene de JobService para ejecutar al llamarse al servicio
//hacemos un Log para asegurarnos que esta funcionando bien y luego llamammos a LocationChanged
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(Tag, "Job started");
        LocationChanged(params);
        return false;
    }
//Esta funcion nos permite añadir una localizacion a nuestra base de datos, dado que tenemos que manejar mas de una localizacion, utilizamos
    //una variable counter y jugamos con sus valores para poder guardarlos todos en "orden" en la base de datos y que luego podamos ordenarlos
    //correctamente al verlo el padre
    public boolean LocationChanged (JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (JobCanceled)
                    return;
                //Obtenemos el uid del usuario, la altitud y la longitud
                mAuth = FirebaseAuth.getInstance();
                String uid = mAuth.getCurrentUser().getUid();
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                double latitud = location.getLatitude();
                double longitud = location.getLongitude();
                //Obtenemos el valor locationNum para saber cuantas localizaciones hemos guardado
                mDatabase.child("Users").child("hijo").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        String formatoHora, formatoFecha, tiempo;
                        //Obtenemos la fecha y hora para poder ponerla en nuestro mapa
                        formatoHora = "HH:mm:ss";
                        formatoFecha = "yyyy-MM-dd";
                        tiempo = "Dia: ";
                        tiempo += obtenerTiempoActual(formatoFecha) + ", Hora: ";
                        tiempo += obtenerTiempoActual(formatoHora);
                        if (task.isSuccessful()) {
                            for (DataSnapshot ds : task.getResult().getChildren()) {
                                if (ds.getKey().equals("locationNum")) {
                                    int Counter = ds.getValue(int.class);
                                    int AuxCounter = Counter;
                                    if(Counter >= 10)
                                        AuxCounter = Counter - 10;
                                    //Escribimos en el ultimo espacio o si estan todos llenos en el mas antiguo nuestra localizacion
                                    mDatabase.child("Users").child("hijo").child(uid).child("location").child(AuxCounter+"").child("lat").setValue(latitud + "");
                                    mDatabase.child("Users").child("hijo").child(uid).child("location").child(AuxCounter+"").child("lon").setValue(longitud + "");
                                    mDatabase.child("Users").child("hijo").child(uid).child("location").child(AuxCounter+"").child("com").setValue(tiempo);
                                    //Aumentamos nuestro contador y lo guardamos en la BD
                                    Counter++;
                                    if (Counter == 20)
                                        Counter = 10;
                                    mDatabase.child("Users").child("hijo").child(uid).child("locationNum").setValue(Counter);
                                    jobFinished(params, false);
                                }
                            }
                        }
                    }



                });
                jobFinished(params, false);
            }
        }).start();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(Tag, "Job Canceled");
        JobCanceled = true;
        return false;
    }

    private String obtenerTiempoActual(String formato){
        String ZonaHoraria = "GMT+1";
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat(formato);
        sdf.setTimeZone(TimeZone.getTimeZone(ZonaHoraria));
        return sdf.format(date);
    }
}
