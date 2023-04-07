package com.example.tfg01.proveedores;

import com.example.tfg01.modelos.Padre;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PadreProvider {
    DatabaseReference mDatabase;

    public PadreProvider(){
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("padre");
    }

    public Task<Void> create(Padre padre){
        return mDatabase.child(padre.getId()).setValue(padre);
    }
}
