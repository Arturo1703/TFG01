package com.example.tfg01.actividades.Padre;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tfg01.actividades.hijo.PrincipalHijoActivity;
import com.example.tfg01.actividades.hijo.RegistroHijo;
import com.example.tfg01.modelos.Hijo;
import com.example.tfg01.modelos.Padre;
import com.example.tfg01.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.example.tfg01.includes.myToolbar;
import com.example.tfg01.proveedores.AuthProvider;
import com.example.tfg01.proveedores.PadreProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

//Esta actividad nos permite registrar a un nuevo padre a la BD una vez resgistrado se le lleva a TerminosConicionesPadre para aceptar los términos
public class RegistroPadre extends AppCompatActivity {

    TextView nombreTxt, emailTxt, passwdTxt;
    Button registroSig;

    DatabaseReference mDatabase;
    FirebaseAuth authp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_padre);

        myToolbar.show(this,"Registro del Padre", true);

        nombreTxt = findViewById(R.id.nombrePadreReg);
        emailTxt = findViewById(R.id.emailPadreReg);
        passwdTxt = findViewById(R.id.passwdPadreReg);
        registroSig = findViewById(R.id.conRegistroPadre);

        authp = FirebaseAuth.getInstance();

        registroSig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickRegistro();
            }
        });
    }
    //Esta funcion se encarga de denegar el registro en el caso de que algun campo esté vacio o la contraseña sea muy corta
    private void clickRegistro(){
        final String name = nombreTxt.getText().toString();
        final String email = emailTxt.getText().toString();
        final String passwd = passwdTxt.getText().toString();

        if(!name.isEmpty() && !email.isEmpty() && (!passwd.isEmpty() &&passwd.length() > 6)){
            registro(name, email, passwd);
        }
        else
            Toast.makeText(this, "Campos vacios o contraseña muy corta", Toast.LENGTH_SHORT).show();

    }
    //Esta funncion se encarga de crear el nuevo usuario y de añadir ese usuario a nuestra BD
    private void registro(String name, String email, String passwd){
        authp.createUserWithEmailAndPassword(email, passwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Padre padre = new Padre();
                    padre.setNombre(name);
                    padre.setEmail(email);
                    padre.setId(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
                    padre.CreateHijos();
                    mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if (!task.isSuccessful()) {
                                        Log.w("REGISTRO_PADRE", "Fetching FCM registration token failed", task.getException());
                                    }

                                    // Get new FCM registration token
                                    String token = task.getResult();
                                    padre.setToken(token);
                                    mDatabase.child("Users").child("padre").child(padre.getId()).setValue(padre).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Intent intent = new Intent(RegistroPadre.this, TerminosCondicionesPadre.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        }
                                    });
                                }
                            });
                }else
                    Toast.makeText( RegistroPadre.this, "Could not register succesfully", Toast.LENGTH_SHORT).show();
            }
        });
    }
}