package com.example.tfg01.includes;

/*
    Clase que contiene las rutas de las carpetas a explorar por parte de la aplicacion en busca
    de videos. Contiene las rutas de las carpetas así como métodos para obtener si el dispositivo
    dispone de almacenamiento interno externo.
    Tambien se puede usar el método [TODO] que devuelve todas las rutas de carpetas a analizar.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

public class FolderHelper {

    private static final int EXTERNAL_STORAGE_PERMISSION_CODE = 23;
    private Context c;
    private Activity a;

    //ruta de versiones de telegram por debajo de la versión 7.5
    public static final String TELEGRAM_FOLDER_OLD_VERSION =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/Telegram/Telegram Video/";

    //ruta de versiones más recientes de telegram
    public static final String TELEGRAM_FOLDER =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Telegram/Telegram Video/";

    //ruta de la carpeta de descargas
    public static final String DOWNLOADS_FOLDER =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/";

    //ruta de la carpeta DCIM para videos captados con la camara
    public static final String DCIM_CAMERA_FOLDER =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera/";

    public static FolderHelper getInstance(Activity a, Context c){
        //TODO poner los .nomedia
        return new FolderHelper(a,c);
    }

    //Constructor por defecto privado porque se necesita el contexto
    private FolderHelper(){}

    private FolderHelper(Activity a, Context c){
        this.a = a;
        this.c = c;
    }

    /*
        Obtenemos la dirección de almacenamiento externa (si existe) o interna para guardar los archivos
        A partir de Android 11, para acceder al directorio que proporciona el sistema a tu app,
        se llama al método getExternalFilesDirs().
        @return AbsolutePath String
     */
    public String getStorageDirPath(){
        solicitarPermiso();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11 y posteriores
            File externalFilesDir = this.c.getExternalFilesDir(null);
            if (externalFilesDir != null) {
                return externalFilesDir.getAbsolutePath();
            }else{
                File file = this.c.getFilesDir();

                return file.getAbsolutePath();
            }
        }else{ // Versiones anteriores a Android 11
            if (isExternalStorageWritable()) {
                File[] externalStorageVolumes =
                        ContextCompat.getExternalFilesDirs(this.c, null);
                File primaryExternalStorage = externalStorageVolumes[0];

                return primaryExternalStorage.getAbsolutePath();
            }else{
                File file = this.c.getFilesDir();

                return file.getAbsolutePath();
            }
        }
    }


    public static boolean directoryExists(final String path){
        File dir = new File(path);

        return(dir.exists()) && (dir.isDirectory());
    }

    public static String obtenerRutaVideo(String rutaImagen, String rutaCarpetaPadre) {
        File imagenFile = new File(rutaImagen);
        StringBuilder rutaVideo = new StringBuilder(rutaCarpetaPadre);

        // Subir en la jerarquía de carpetas desde la imagen hasta encontrar la "rutaCarpetaPadre"
        File carpetaActual = imagenFile.getParentFile();
        while (carpetaActual != null && !carpetaActual.getAbsolutePath().equals(rutaCarpetaPadre)) {
            rutaVideo.insert(rutaCarpetaPadre.length(), "/" + carpetaActual.getName());
            carpetaActual = carpetaActual.getParentFile();
        }

        if (carpetaActual == null) {
            return null; // "rutaCarpetaPadre" no fue encontrada en la jerarquía de la "rutaImagen"
        }

        return rutaVideo.toString();
    }




    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /*
        Compruba o solicita permidos de acceso y escitura a la aplicación
     */
    private void solicitarPermiso() {
        if (ContextCompat.checkSelfPermission(this.a,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Explicamos porque necesitamos el permiso
            if (ActivityCompat.shouldShowRequestPermissionRationale(this.a,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE)) {

            } else {

                // El usuario no necesitas explicación, puedes solicitar el permiso:
                ActivityCompat.requestPermissions(this.a,
                        new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_PERMISSION_CODE);


            }
        }
    }








}