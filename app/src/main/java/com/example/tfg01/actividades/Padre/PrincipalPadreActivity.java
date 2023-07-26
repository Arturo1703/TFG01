package com.example.tfg01.actividades.Padre;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tfg01.includes.ListaHijosRVAdapter;
import com.example.tfg01.R;
import com.example.tfg01.actividades.MainActivity;
import com.example.tfg01.modelos.Hijo;
import com.example.tfg01.modelos.Mensaje;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

//Esta actividad es donde se presentan todas las funcionalidades para el padre que son:
//1º: muestra a todos sus hijos vinculados con su nombre id e imagen. Al clicarlo te llevará al mapa con sus ultimas ubicaciones
//2º: manda una notificacion de alerta cuando se produce una notificacion de Alerta en la BD de uno de sus hijos vinculados
//3º: Tiene un boton que te lleva auna actividad distinta para vincualr nuevos hijos
public class PrincipalPadreActivity extends AppCompatActivity {

    Button logOut;

    android.app.AlertDialog AlertDialog;
    View  parent;
    RecyclerView hijosrecyclerView;
    ListaHijosRVAdapter recyclerAdapter;
    FloatingActionButton addButton, mailButton;
    String Parameter, TimeArray;

    SharedPreferences mPref;
    DatabaseReference mDatabase;
    FirebaseAuth auth;
    ArrayList<Hijo> listaHijos = new ArrayList<> ();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_padre);

        mPref = getApplicationContext().getSharedPreferences("typeUser", MODE_PRIVATE);

        logOut = findViewById(R.id.cerrarSesionPadre);
        addButton = findViewById(R.id.createIdTemporal);
        mailButton = findViewById(R.id.buzonButon);
        //Lo primero que hacemos es mostrar todos los hijos vinculados a este usuario en cardviews mediante esta funcion
        createRecyclerView();

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                Intent intent = new Intent(PrincipalPadreActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PrincipalPadreActivity.this, RelacionarHijo.class);
                startActivity(intent);
            }
        });
        mailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrincipalPadreActivity.this, BuzonPadre.class);
                startActivity(intent);
            }
        });
    }
//Esta funcion no sobtiene todos los hijos del usuario y se los manda al recyclerview para que los muestre todos
    private void createRecyclerView() {
        parent = findViewById(R.id.layoutPrincipalPadre);
        hijosrecyclerView = findViewById(R.id.recyclerViewPadreMain);
        recyclerAdapter= new ListaHijosRVAdapter(this);
        auth = FirebaseAuth.getInstance();
        String idUser = auth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        //Obtenemos todos los ids de hijos de la bd
        mDatabase.child("Users").child("padre").child(idUser).child("hijos").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    long Numhijos = task.getResult().getChildrenCount();
                    long CurrentHijo = 0;
                    for (DataSnapshot ds : task.getResult().getChildren()) {
                        CurrentHijo++;
                        String idNiño = ds.getValue(String.class);
                        Hijo hijo = new Hijo();
                        long finalCurrentHijo = CurrentHijo;
                        //A partir del id buscamos informacion del hijo par poder mostrarla en las Tarjetas
                        mDatabase.child("Users").child("hijo").child(idNiño).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {

                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                hijo.setId(idNiño);
                                for (DataSnapshot ds : task.getResult().getChildren()) {
                                    if (ds.getKey().equals("email"))
                                        hijo.setEmail(ds.getValue(String.class));
                                    if (ds.getKey().equals("nombre"))
                                        hijo.setNombre(ds.getValue(String.class));
                                }
                                //anadimos la informacion del hijo al arraylist de hijos para uego pasarsela al adaptador
                                listaHijos.add(hijo);
                                if (Numhijos == finalCurrentHijo) {
                                    recyclerAdapter.setListahijos(listaHijos);
                                    hijosrecyclerView.setAdapter(recyclerAdapter);
                                    //Ademas debemos crear un listener para que cuando un usuario presione en una tarjeta se abra el mapa y no puede ser
                                    //hecha desde el recyclerView porque Android Studio no puede hacer cambios de actividad desde un recyclerView
                                    recyclerAdapter.setOnItemClickListener(PrincipalPadreActivity.this::onItemClick);
                                    hijosrecyclerView.setLayoutManager(new GridLayoutManager(PrincipalPadreActivity.this, 2));
                                }

                            }
                        });
                    }
                }
            }
        });
    }
//Esta funcion nos permite obtener las localizaciones de un usuario de la base de datos, Es un poco complicada dado que hay que obtener
    //Varias localizaciones en el orden establecido por la BD, una vez terminado se pasan las localizaciones en un string como Bundle
    //A nuestra nueva actividad MapaPadre para poder visualizar las localizaciones
    public void onItemClick(String id, Integer num) {
        if(num == 1) {
            Toast.makeText(PrincipalPadreActivity.this, "tarjeta Clickeada", Toast.LENGTH_SHORT).show();
            SharedPreferences.Editor edit = mPref.edit();

            mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
            mDatabase.child("Users").child("hijo").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DataSnapshot ds : task.getResult().getChildren()) {
                            if (ds.getKey().equals("locationNum")) {
                                int counter = ds.getValue(int.class);
                                if (counter < 10) {
                                    Parameter = counter + "";
                                    TimeArray = counter + "";
                                    for (int x = counter - 1; x >= 0; x--) {
                                        int finalX = x;
                                        mDatabase.child("Users").child("hijo").child(id).child("location").child(x + "").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    String lat = "";
                                                    String lon = "";
                                                    String com = "";
                                                    for (DataSnapshot ds : task.getResult().getChildren()) {
                                                        if (ds.getKey().equals("lat")) {
                                                            lat = ds.getValue(String.class);
                                                        }
                                                        if (ds.getKey().equals("lon")) {
                                                            lon = ds.getValue(String.class);
                                                        }
                                                        if (ds.getKey().equals("com")) {
                                                            com = ds.getValue(String.class);
                                                        }
                                                    }
                                                    Parameter = Parameter + "," + lat + "," + lon;
                                                    TimeArray = TimeArray + "," + com;
                                                    if (finalX == 0) {
                                                        edit.putString("pos", Parameter);
                                                        edit.putString("com", TimeArray);
                                                        edit.apply();
                                                        Intent intent = new Intent(PrincipalPadreActivity.this, MapaPadre.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            }
                                        });
                                    }
                                } else {
                                    Parameter = 10 + "";
                                    TimeArray = 10 + "";
                                    int aux;
                                    for (int x = 0; x > -10; x--) {
                                        aux = x + counter;
                                        if (aux >= 10)
                                            aux -= 10;
                                        int finalX = x;
                                        mDatabase.child("Users").child("hijo").child(id).child("location").child(aux + "").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    String lat = "";
                                                    String lon = "";
                                                    String com = "";
                                                    for (DataSnapshot ds : task.getResult().getChildren()) {
                                                        if (ds.getKey().equals("lat")) {
                                                            lat = ds.getValue(String.class);
                                                        }
                                                        if (ds.getKey().equals("lon")) {
                                                            lon = ds.getValue(String.class);
                                                        }
                                                        if (ds.getKey().equals("com")) {
                                                            com = ds.getValue(String.class);
                                                        }
                                                    }
                                                    Parameter = Parameter + "," + lat + "," + lon;
                                                    TimeArray = TimeArray + "," + com;
                                                    if (finalX == -9) {
                                                        edit.putString("pos", Parameter);
                                                        edit.putString("com", TimeArray);
                                                        edit.apply();
                                                        Intent intent = new Intent(PrincipalPadreActivity.this, MapaPadre.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            }

                        }
                    }
                }
            });
        }
        if(num == 2){
            AlertDialog.Builder builder = new AlertDialog.Builder(PrincipalPadreActivity.this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogo = inflater.inflate(R.layout.dialog_enviar_mensaje, null);
            builder.setView(dialogo);
            EditText text = dialogo.findViewById(R.id.textDialog);
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            builder.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (text.getText().toString().isEmpty()) {
                        Toast.makeText(builder.getContext(), "No se puede enviar un mensaje vacio", Toast.LENGTH_LONG).show();
                    } else {
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        String fecha = day + "/" + (month + 1) + "/" + year;
                        UUID uuid = UUID.randomUUID();
                        String idMensaje = uuid.toString();
                        auth = FirebaseAuth.getInstance();
                        String idUser = auth.getCurrentUser().getUid();
                        Mensaje mensaje = new Mensaje(idMensaje, idUser, id, fecha, text.getText().toString());
                        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
                        mDatabase.child("Users").child("hijo").child(id).child("Mensajes").child(idMensaje).setValue(mensaje).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                    Toast.makeText(PrincipalPadreActivity.this, "Mensaje enviado", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    dialogInterface.dismiss();


                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}