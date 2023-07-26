package com.example.tfg01.actividades.hijo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tfg01.R;
import com.example.tfg01.includes.ListaMensajesRVAAdapter;
import com.example.tfg01.modelos.Mensaje;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

public class BuzonHijo extends AppCompatActivity {

    View parent;

    RecyclerView mensajerecyclerView;
    ListaMensajesRVAAdapter recyclerAdapter;
    DatabaseReference mDatabase;
    FirebaseAuth auth;
    ArrayList<Mensaje> listaMensaje = new ArrayList<> ();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buzon_hijo);
        createRecyclerView();
    }

    private void createRecyclerView() {
        parent = findViewById(R.id.layoutPrincipalPadre);
        mensajerecyclerView = findViewById(R.id.recyclerViewBuzonHijo);
        recyclerAdapter = new ListaMensajesRVAAdapter(this);
        auth = FirebaseAuth.getInstance();
        String idUser = auth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        //Obtenemos todos los ids de hijos de la bd
        mDatabase.child("Users").child("hijo").child(idUser).child("Mensajes").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    long Nummensajes = task.getResult().getChildrenCount();
                    long CurrentMensaje = 0;
                    for (DataSnapshot ds : task.getResult().getChildren()) {
                        CurrentMensaje++;
                        String idMensaje = ds.getKey();
                        Mensaje mensaje = new Mensaje();
                        long finalCurrentMensaje = CurrentMensaje;
                        //A partir del id buscamos informacion del hijo par poder mostrarla en las Tarjetas
                        mDatabase.child("Users").child("hijo").child(idUser).child("Mensajes").child(idMensaje).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {

                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if(task.isSuccessful()) {
                                    mensaje.setId(idMensaje);
                                    for (DataSnapshot ds : task.getResult().getChildren()) {
                                        String Origen, Destinatario, Fecha, Mensaje;
                                        if (ds.getKey().equals("origen"))
                                            mensaje.setOrigen(ds.getValue(String.class));
                                        else if (ds.getKey().equals("destinatario"))
                                            mensaje.setDestinatario(ds.getValue(String.class));
                                        else if (ds.getKey().equals("fecha"))
                                            mensaje.setFecha(ds.getValue(String.class));
                                        else if (ds.getKey().equals("mensaje"))
                                            mensaje.setMensaje(ds.getValue(String.class));
                                    }
                                    //anadimos la informacion del hijo al arraylist de hijos para uego pasarsela al adaptador
                                    listaMensaje.add(mensaje);
                                    if (Nummensajes == finalCurrentMensaje) {
                                        recyclerAdapter.setListaMensajes(listaMensaje);
                                        mensajerecyclerView.setAdapter(recyclerAdapter);
                                        //Ademas debemos crear un listener para que cuando un usuario presione en una tarjeta se abra el mapa y no puede ser
                                        //hecha desde el recyclerView porque Android Studio no puede hacer cambios de actividad desde un recyclerView
                                        recyclerAdapter.setOnItemClickListener(com.example.tfg01.actividades.hijo.BuzonHijo.this::onItemClick);
                                        mensajerecyclerView.setLayoutManager(new LinearLayoutManager(com.example.tfg01.actividades.hijo.BuzonHijo.this));
                                    }
                                }

                            }
                        });
                    }
                }
            }
        });
    }

    public void onItemClick(String id, Integer num) {
        if(num == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(com.example.tfg01.actividades.hijo.BuzonHijo.this);
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
                        Mensaje mensaje = new Mensaje();
                        //A partir del id buscamos informacion del hijo par poder mostrarla en las Tarjetas
                        mDatabase.child("Users").child("hijo").child(idUser).child("Mensajes").child(id).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {

                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if(task.isSuccessful()) {
                                    mensaje.setId(idMensaje);
                                    mensaje.setFecha(fecha);
                                    mensaje.setMensaje(text.getText().toString());
                                    String IdPadre = null;
                                    for (DataSnapshot ds : task.getResult().getChildren()) {
                                        String Origen, Destinatario, Fecha, Mensaje;
                                        if (ds.getKey().equals("origen")){
                                            mensaje.setDestinatario(ds.getValue(String.class));
                                            IdPadre = ds.getValue(String.class);
                                        }
                                        else if (ds.getKey().equals("destinatario"))
                                            mensaje.setOrigen(ds.getValue(String.class));
                                    }
                                    mDatabase.child("Users").child("padre").child(IdPadre).child("Mensajes").child(idMensaje).setValue(mensaje).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                                Toast.makeText(com.example.tfg01.actividades.hijo.BuzonHijo.this, "Mensaje enviado", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                            }
                        });
                    }
                    dialogInterface.dismiss();


                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        if(num == 2){
            auth = FirebaseAuth.getInstance();
            String idUser = auth.getCurrentUser().getUid();
            mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
            mDatabase.child("Users").child("hijo").child(idUser).child("Mensajes").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(com.example.tfg01.actividades.hijo.BuzonHijo.this, "Mensaje borrado con exito", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(BuzonHijo.this, BuzonHijo.class);
                    startActivity(intent);
                    finish();
                }
            });

        }

    }
}