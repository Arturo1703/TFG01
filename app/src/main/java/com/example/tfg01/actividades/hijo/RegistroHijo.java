package com.example.tfg01.actividades.hijo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tfg01.proveedores.AuthProvider;
import com.example.tfg01.proveedores.HijoProvider;
import com.example.tfg01.includes.myToolbar;
import com.example.tfg01.modelos.Hijo;
import com.example.tfg01.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
//Esta actividad nos permite registrar a un nuevo hijo a la BD una vez resgistrado se le lleva a TerminosConicionesHijo
public class RegistroHijo extends AppCompatActivity {

    TextView nombreTxt, emailTxt, passwdTxt;
    Button registroSig;

    DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_hijo);

        myToolbar.show(this,"Registro de Hijo", true);

        nombreTxt = findViewById(R.id.nombreHijoReg);
        emailTxt = findViewById(R.id.emailHijoReg);
        passwdTxt = findViewById(R.id.passwdHijoReg);
        registroSig = findViewById(R.id.conRegistroHijo);

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
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, passwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Hijo hijo = new Hijo();
                    hijo.setNombre(name);
                    hijo.setEmail(email);
                    hijo.setId(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
                    mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
                    mDatabase.child("Users").child("hijo").child(hijo.getId()).setValue(hijo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(RegistroHijo.this, TerminosCondicionesHijo.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                    });
                }else
                    Toast.makeText(RegistroHijo.this, "No se pudo registrar al hijo", Toast.LENGTH_SHORT).show();
            }
        });
    }
}