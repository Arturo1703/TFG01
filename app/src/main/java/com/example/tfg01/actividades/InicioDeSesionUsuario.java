package com.example.tfg01.actividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tfg01.actividades.Padre.MapaPadre;
import com.example.tfg01.actividades.Padre.PrincipalPadreActivity;
import com.example.tfg01.actividades.Padre.RegistroPadre;
import com.example.tfg01.actividades.Padre.TerminosCondicionesPadre;
import com.example.tfg01.actividades.hijo.PrincipalHijoActivity;
import com.example.tfg01.actividades.hijo.RegistroHijo;
import com.example.tfg01.actividades.hijo.TerminosCondicionesHijo;
import com.example.tfg01.modelos.Hijo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.tfg01.includes.myToolbar;
import com.example.tfg01.R;
/*
    En esta Actividad utilizamos FirebaseAuth para Autentificar al usuario, leyendo los campos introducidos en emailText y passwordText
    Si el usuario es autentificado correctamente, miramos la variable Term en nuestra BD para chequear que los terminos y condiciones hayan
    sido aceptados. En caso afirmativo, se lleva al usuario a su página principal, en caso negativo, se les lelva de nuevo a
    TerminosCondicionesHijo/TerminosCondicionesPadre
 */
public class InicioDeSesionUsuario extends AppCompatActivity {

    EditText emailText, passwordText;
    Button finishInitButton;
    SharedPreferences mPreference;

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_de_sesion_usuario);
        emailText = findViewById(R.id.emailInicioSes);
        passwordText = findViewById(R.id.passwdInicioSes);
        finishInitButton = findViewById(R.id.ContinuarInicioSes);

        mPreference = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);
        //Inicializamos el autentificador y una instancia de nuestra base de datos
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();

        finishInitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }
    //Esta es la función que se encarga de hacer el login completo
    private void login(){
        String email = emailText.getText().toString();
        String passwd = passwordText.getText().toString();
        //Chequeamos que tanto el email como la contraseña sean válidos para disminuir el numero de llamadas erroneas a nuestro servidor
        if(!email.isEmpty() && (!passwd.isEmpty() && passwd.length() >= 6)){
            //Esta función se encarga de la autentificación del usuario
            mAuth.signInWithEmailAndPassword(email, passwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    //En el caso de que la autentificación sea erronea se le manda un mensaje al usuario mediante un Toast
                    if(!task.isSuccessful()) {
                        Toast.makeText(InicioDeSesionUsuario.this, "Email o password incrrectos", Toast.LENGTH_SHORT).show();
                    }else {
                        String usuario = mPreference.getString("user", "");
                        String idUser = mAuth.getCurrentUser().getUid();
                        if (usuario.equals("hijo")) {
                            //Comprobamos que el hijo que ha sido elegido rpeviamente y el que se presenta en la base de datos son el mismo
                            mDatabase.child("Users").child("hijo").child(idUser).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        mDatabase.child("Users").child("hijo").child(idUser).child("Term").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                String Acceptance = task.getResult().getValue(String.class);
                                                //Comprobamos si los términos han sido aceptados, si han sido (1), el usuario va directamente a La Actividad INicial del Usuario,
                                                // si no, se le lleva de uevo a Terminos y condiciones para que los acepte
                                                if (Acceptance.equals("1")) {
                                                    Intent intent = new Intent(InicioDeSesionUsuario.this, PrincipalHijoActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                } else {
                                                    Intent intent = new Intent(InicioDeSesionUsuario.this, TerminosCondicionesHijo.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }
                                            }
                                        });
                                    } else {
                                        Toast.makeText(InicioDeSesionUsuario.this, "Por favor identificate como padre para poder Iniciar Sesión correctamente", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            //Al igual que previamente comprobamos que los tipos de usuario coincidan esta vez para padre
                            mDatabase.child("Users").child("hijo").child(idUser).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        mDatabase.child("Users").child("padre").child(idUser).child("Term").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                String Acceptance = task.getResult().getValue(String.class);
                                                if (Acceptance.equals("1")) {
                                                    Intent intent = new Intent(InicioDeSesionUsuario.this, PrincipalPadreActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                } else {
                                                    Intent intent = new Intent(InicioDeSesionUsuario.this, TerminosCondicionesPadre.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                }
                                            }
                                        });
                                    }
                                    else{
                                        Toast.makeText(InicioDeSesionUsuario.this, "Por favor identificate como hijo para poder Iniciar Sesión correctamente", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
        else
            Toast.makeText(this, "Campos no validos", Toast.LENGTH_SHORT).show();
    }
}