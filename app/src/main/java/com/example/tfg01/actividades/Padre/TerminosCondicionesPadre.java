package com.example.tfg01.actividades.Padre;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.tfg01.R;
import com.example.tfg01.actividades.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//En terminos y condiciones del padre, se presetan una serie de campos a aceptar por el usuario
public class TerminosCondicionesPadre extends AppCompatActivity {
    CheckBox Condicion1, Condicion2;
    Button AcceptTerm, CancelTerm;

    DatabaseReference mDatabase;
    FirebaseAuth authp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminos_condiciones_padre);
        Condicion1 = findViewById(R.id.Condicion1);
        Condicion2 = findViewById(R.id.Condicion2);
        AcceptTerm = findViewById(R.id.AcceptTermButton);
        CancelTerm = findViewById(R.id.CancelTermButton);

        authp = FirebaseAuth.getInstance();

        AcceptTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickAccept();
            }
        });

        CancelTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { clickCancel();}
        });
    }
    //En caso de Aceptar, se chequea que ambas condiciones sean aceptados, se marca que la base de datos sea aceptadas
    // y se lleva al usuario a la actividad principal del hijo
    private void clickAccept(){

        if(Condicion1.isChecked() && Condicion2.isChecked()){
            mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
            mDatabase.child("Users").child("padre").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()).child("Term").setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent intent = new Intent(TerminosCondicionesPadre.this, PrincipalPadreActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
        }
        else
            Toast.makeText(this, "Por favor, aprueba todos las condiciones para continuar", Toast.LENGTH_LONG).show();

    }
    //En caso de Cancelar, se marca la base de datos como no aceptada y se devuelve a la actividad inicial
    private void clickCancel(){
        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        String Uid = authp.getCurrentUser().getUid().toString();
        mDatabase.child("Users").child("padre").child(Uid).child("Term").setValue("2").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                authp.signOut();
                Intent intent = new Intent(TerminosCondicionesPadre.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}