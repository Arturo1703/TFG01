package com.example.tfg01.actividades;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.tfg01.R;
import com.example.tfg01.includes.myToolbar;
import com.example.tfg01.actividades.Padre.RegistroPadre;
import com.example.tfg01.actividades.hijo.RegistroHijo;

/*
    Esta Actividad nos sirve como bifurcación entre el Inicio de Sesion y el Registro. Dependiendo de la elección del usuario
    se llevará hasta RegistroHijo/RegistroPadre o InicioDeSesionUsuario (Misma actividad independientemente del tipo de usuario)
 */
public class AutentificacionUsuario extends AppCompatActivity {
    private Button inicioSesionButton, registroButton;
    SharedPreferences mPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autentificacion_usuario);

        myToolbar.show(this,"Autentificacion del Usuario", true);
        //Instanciamos los 2 botones que nos permitirán dirigir el flujo de actividades en sus métodos onClick respectivos
        inicioSesionButton = findViewById(R.id.LogButton);
        registroButton = findViewById(R.id.RegButton);
        //Instanciamos los SharedPreferences que escribimos la actividad anterior para poderlos usuar
        mPreference = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);

        inicioSesionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //En el caso de Iniciar Sesión llevamos a la actividad InicioDeSesionUsuario directamente
                Intent intent = new Intent(AutentificacionUsuario.this, InicioDeSesionUsuario.class);
                startActivity(intent);
            }
        });

        registroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //En el caso de pulsar Registro, obtenemos la elección en la actividad anterior y según el valor, nos dirigimos a RegistroHijo o RegistroPadre
                String typeUser = mPreference.getString("user", "");
                Intent intent;
                if(typeUser.equals("hijo"))
                    intent = new Intent(AutentificacionUsuario.this, RegistroHijo.class);
                else
                    intent = new Intent(AutentificacionUsuario.this, RegistroPadre.class);
                startActivity(intent);
            }
        });
    }
}