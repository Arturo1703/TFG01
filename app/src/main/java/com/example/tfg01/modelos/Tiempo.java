package com.example.tfg01.modelos;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Tiempo {
    public String getTiempo(){
        String tiempo, formatoHora, formatoFecha;
        formatoHora = "HH:mm:ss";
        formatoFecha = "yyyy-MM-dd";
        tiempo = "Dia: ";
        tiempo += obtenerTiempoActual(formatoFecha) + " Hora: ";
        tiempo += obtenerTiempoActual(formatoHora);
        return tiempo;
    }
    private String obtenerTiempoActual(String formato){
        String ZonaHoraria = "GMT+1";
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat(formato);
        sdf.setTimeZone(TimeZone.getTimeZone(ZonaHoraria));
        return sdf.format(date);
    }
}
