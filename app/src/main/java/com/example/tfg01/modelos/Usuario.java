package com.example.tfg01.modelos;

public class Usuario {
    String id;
    String nombre;
    String email;

    public Usuario(){
    }

    public Usuario(String id, String nombre, String email){
        this.id = id;
        this.nombre = nombre;
        this.email = email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
