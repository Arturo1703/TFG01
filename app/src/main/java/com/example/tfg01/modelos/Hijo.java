package com.example.tfg01.modelos;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.tfg01.actividades.hijo.PrincipalHijoActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Hijo {
    DatabaseReference mDatabase;
    String id;
    String nombre;
    String email;

    String token;

    public Hijo(){
    }

    public Hijo(String id, String nombre, String email, String token){
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.token = token;
    }

    public Hijo getHijoByID(String ID){
        Hijo hijo = new Hijo();
        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        mDatabase.child("Users").child("hijo").child(ID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                int auxnum = 0;
                hijo.setId(ID);
                for(DataSnapshot ds: task.getResult().getChildren()){

                    String aux = ds.getValue(String.class);
                    auxnum++;
                    if(auxnum == 1)
                        hijo.setEmail(aux);
                    if(auxnum == 3)
                        hijo.setNombre(aux);
                }
            }
        });
        return hijo;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setToken(String token) {this.token = token;}

    public String getToken() { return this.token;}

    public String getNombre() {return nombre;}

    public String getId() {return id;}

    public String getEmail() {
        return email;
    }

    /*public void refreshLocation(String IdUser){
        Geolocation geolocation = new Geolocation();
        geolocation.fn_getlocation();
        ArrayList<Double> location = new ArrayList<>();
        location = geolocation.fn_getlocation();

        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();


        mDatabase.child("Users").child("hijo").child(IdUser).child("latitud").setValue(location.get(0)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });

        mDatabase.child("Users").child("hijo").child(IdUser).child("longitud").setValue(location.get(1)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
    }*/

    /*public Double getLongitud(){

        final Double[] Longitud = new Double[1];
        Longitud[0] = 9999.0;
        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        mDatabase.child("Users").child("hijo").child(getId()).child("latitud").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                Longitud[0] = (Double) task.getResult().getValue();
            }
        });
        return Longitud[0];
    }*/
}
