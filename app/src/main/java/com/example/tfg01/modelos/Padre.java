package com.example.tfg01.modelos;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.tfg01.actividades.Padre.RelacionarHijo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class Padre {
    DatabaseReference mDatabase;
    String id;
    String nombre;
    String email;

    String Token;
    ArrayList<String> hijos;

    public Padre(){
        this.hijos = new ArrayList<>();
    }

    public Padre(String id, String nombre, String email, String token){
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.hijos = new ArrayList<>();
    }
    public Padre getPadreByID(String ID){
        Padre padre = new Padre();
        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        mDatabase.child("Users").child("padre").child(ID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                int auxnum = 0;
                padre.setId(ID);
                for(DataSnapshot ds: task.getResult().getChildren()){

                    String aux = ds.getValue(String.class);
                    auxnum++;
                    if(auxnum == 1)
                        padre.setEmail(aux);
                    if(auxnum == 3)
                        padre.setNombre(aux);
                }
            }
        });
        return padre;
    }

    public void setPadre(Padre padreAux){
        this.id = padreAux.getId();
        this.email = padreAux.getEmail();
        this.nombre = padreAux.getNombre();
    }

    public void CreateHijos(){ this.hijos = new ArrayList<>();}

    public String getNombre() {
        return nombre;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {return  Token;}

    public ArrayList<String> getHijos(){
        return hijos;
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

    public void setToken(String token) {this.Token = token; }

    public void setHijos(){
        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        mDatabase.child("Users").child("padre").child(getId()).child("hijos").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    String idNiño = ds.getValue(String.class);
                    addHijo(idNiño);
                }
            }
        });
    }

    public void vincularHijo(String emailHijo){
        final String[] idNiño = new String[1];
        idNiño[0] = "0";
        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        String idUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase.child("Users").child("hijo").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    String idNiñoAux = ds.getKey();
                    Hijo hijo = new Hijo();
                    hijo = hijo.getHijoByID(idNiñoAux);
                    if(emailHijo == hijo.getEmail()){
                        idNiño[0] = idNiñoAux;
                    }
                }
                if(idNiño[0] != "0") {
                    mDatabase.child("Users").child("padre").child(idUser).child("hijos").setValue(idNiño[0]).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                        }
                    });
                }
            }
        });
    }


    public boolean HijosIsEmpty(){return hijos.isEmpty();}

    private void addHijo(String IdHijo){
        hijos.add(IdHijo);
    }


}
