package com.example.tfg01.actividades.Padre;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tfg01.R;
import com.example.tfg01.actividades.hijo.PrincipalHijoActivity;
import com.example.tfg01.actividades.hijo.RegistroHijo;
import com.example.tfg01.modelos.Hijo;
import com.example.tfg01.modelos.Padre;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
//Esta funcion nos permite relacionar un hijo con su padre, al introducir el email de un hijo en el cuador de texto, su id se
//vinculara con padre en su array hijos en la BD y a partir de entonces saldra en la paginna principal del usuario
//tambien chequea que no se introduce un hijo ya relacionado.
public class RelacionarHijo extends AppCompatActivity {

    TextView emailHijo;
    Button vincularbut, terminarbut;

    DatabaseReference mDatabase;
    FirebaseAuth auth;

    String idNiño;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relacionar_hijo);

        emailHijo = findViewById(R.id.emailHijoVin);
        vincularbut = findViewById(R.id.VincularHijo);
        terminarbut = findViewById(R.id.TerminarVincular);
        idNiño = "";

        vincularbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = emailHijo.getText().toString();
                if(!email.isEmpty()){
                    vincular(email);
                }
                else
                    Toast.makeText(RelacionarHijo.this, "EmailBox Empty", Toast.LENGTH_SHORT).show();
            }
        });
        terminarbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RelacionarHijo.this, PrincipalPadreActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
    //esta funcion se encarga de relacionar ese nuevo hijo con el padre, primero encuentra el id del hijo a oartir del gmail y luego lo
    //añade en la base de datos del padre
    private void vincular(String emailHijo){
        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        auth = FirebaseAuth.getInstance();
        String idUser = auth.getCurrentUser().getUid();
        mDatabase.child("Users").child("hijo").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    String idNiñoAux = ds.getKey();
                    mDatabase.child("Users").child("hijo").child(idNiñoAux).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            int auxNum = 0;
                            for (DataSnapshot ds : task.getResult().getChildren()) {
                                if (ds.getKey().equals("email")){
                                    String aux = ds.getValue(String.class);
                                    if(aux.equals(emailHijo)) {
                                        mDatabase.child("Users").child("padre").child(idUser).child("hijos").push().setValue(idNiñoAux).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(RelacionarHijo.this, "Hijo Añadido: " + aux, Toast.LENGTH_LONG).show();
                                                mDatabase.child("Users").child("hijo").child(idNiñoAux).child("padres").push().setValue(idUser);
                                                //TODO añadas al padre al grupo del id del hijo

                                            }
                                        });
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });

    }
}