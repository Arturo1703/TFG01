package com.example.tfg01.actividades;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.tfg01.R;
import com.example.tfg01.actividades.Padre.PrincipalPadreActivity;
import com.example.tfg01.actividades.Padre.RegistroPadre;
import com.example.tfg01.actividades.hijo.PrincipalHijoActivity;
import com.example.tfg01.actividades.hijo.RegistroHijo;
import com.google.firebase.auth.FirebaseAuth;
/*
Esta es la Actividad en la que comienza nuestra aplicación, lo unico que hacemos aqui es diferenciar entre padre e hijo.
Independietemente de la respuesta ambas Opciones llevan a la actividad AutentificacionUsuario
 */
public class MainActivity extends AppCompatActivity {

    private Button padreButton, hijoButton;
    SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);
        SharedPreferences.Editor edit = mPref.edit();
    //Inicializamos 2 Botones para poder guardar la elección en el metodo onClick respectivo
        padreButton = findViewById(R.id.inicioPadre);
        hijoButton = findViewById(R.id.inicioHijo);

        padreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Guardamos la opcion padre en un sharedpreference que utilizaremos posteriormente
                edit.putString("user", "padre");
                edit.apply();
                Intent intent = new Intent(MainActivity.this, AutentificacionUsuario.class);
                startActivity(intent);
            }
        });

        hijoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Guardamos la opcion hijo en un sharedpreference que utilizaremos posteriormente
                edit.putString("user", "hijo");
                edit.apply();
                Intent intent = new Intent(MainActivity.this, AutentificacionUsuario.class);
                startActivity(intent);
            }
        });
    }
}