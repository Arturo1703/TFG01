package com.example.tfg01.modelos;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Mensaje {
    String Origen, Destinatario, Fecha, Mensaje, id;

    public Mensaje() {
    }
    public Mensaje(String id,String origen, String destinatario, String fecha, String mensaje) {
        this.id = id;
        this.Origen = origen;
        this.Destinatario = destinatario;
        this.Fecha = fecha;
        this.Mensaje = mensaje;
    }

    public String getId() {return id;}

    public String getMensaje() {
        return Mensaje;
    }

    public String getDestinatario() {
        return Destinatario;
    }

    public String getFecha() {
        return Fecha;
    }

    public String getOrigen() {
        return Origen;
    }

    public void setMensaje(String mensaje) {
        Mensaje = mensaje;
    }

    public void setId(String id) {this.id = id;}

    public void setDestinatario(String destinatario) {
        Destinatario = destinatario;
    }

    public void setFecha(String fecha) {
        Fecha = fecha;
    }

    public void setOrigen(String origen) {
        Origen = origen;
    }
}