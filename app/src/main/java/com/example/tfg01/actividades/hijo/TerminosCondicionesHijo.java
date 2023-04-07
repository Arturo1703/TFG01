package com.example.tfg01.actividades.hijo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.Toast;

import com.example.tfg01.R;
import com.example.tfg01.actividades.MainActivity;
import com.example.tfg01.actividades.Padre.PrincipalPadreActivity;
import com.example.tfg01.actividades.Padre.TerminosCondicionesPadre;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//En terminos y condiciones del hijo, se presetan una serie de campos a aceptar por el usuario
public class TerminosCondicionesHijo extends AppCompatActivity {
    CheckBox Condicion1, Condicion2;
    Switch Edad;
    Button AcceptTerm, CancelTerm;

    DatabaseReference mDatabase;
    FirebaseAuth authp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminos_condiciones_hijo);
        Condicion1 = findViewById(R.id.Condicion1);
        Condicion2 = findViewById(R.id.Condicion2);
        Edad = findViewById(R.id.EdadSwitch);
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
            mDatabase.child("Users").child("hijo").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()).child("Term").setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Intent intent = new Intent(TerminosCondicionesHijo.this, PrincipalHijoActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
        }
        else
            Toast.makeText(this, "Por favor, aprueba todos las condiciones para continuar", Toast.LENGTH_LONG).show();

    }
    //En caso de Cancelar, se chequea que la edad sea apropiada para poder cancelar los términos, en el caso de tener suficiente edad
    //se lleva a la actividad inicial y se marca la base de datos como no aceptada. En caso negativo no se deja cancelar los términos
    private void clickCancel(){
        if(Edad.isChecked()){
            mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
            String Uid = authp.getCurrentUser().getUid().toString();
            mDatabase.child("Users").child("padre").child(Uid).child("Term").setValue("2").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    authp.signOut();
                    Intent intent = new Intent(TerminosCondicionesHijo.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
        else
            Toast.makeText(this, "Para cancelar los terminos y condiciones necesita ser mayor de edad" +
                    "", Toast.LENGTH_LONG).show();
    }
}