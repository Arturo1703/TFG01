package com.example.tfg01.proveedores;

import com.example.tfg01.modelos.Padre;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import com.example.tfg01.modelos.Hijo;

public class
HijoProvider {
    DatabaseReference mDatabase;

    public HijoProvider(){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("hijo");
    }

    public Task<Void> create(Hijo hijo) { return mDatabase.child(hijo.getId()).setValue(hijo);}
}
