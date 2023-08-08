package com.example.tfg01.actividades.hijo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.tfg01.R;
import com.example.tfg01.actividades.MainActivity;
import com.example.tfg01.includes.FolderHelper;
import com.example.tfg01.includes.KeyFrames;
import com.example.tfg01.includes.LocationUpdate;
import com.example.tfg01.includes.MyFirebaseMessagingService;
import com.example.tfg01.includes.ServicioGeolocalizacion;
import com.example.tfg01.modelos.Tiempo;
import com.example.tfg01.modelos.Video;
import com.example.tfg01.proveedores.AuthProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.ArrayList;

//Esta es la actividad principal del Hijo, aqui se porducirán dos servicios princiaples:
//1º: Servicio de geolocalización en el que cada 10 minutos se haga una actualización de la ubicación del ussuario en la base de datos
//2º: Servicio/trigger, en el que cada vez que se actualice la carpeta de videos de whatsapp/messenger... se analice el video
public class PrincipalHijoActivity extends AppCompatActivity {

    Button logOut, cancelJob;

    FloatingActionButton mailButton;
    AuthProvider authp;
    Intent mapIntent = new Intent();
    LocationUpdate update = new LocationUpdate();
    private static final String TAG = "MainActivity";

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    DatabaseReference mDatabase;
    FirebaseAuth mAuth;
    AlarmManager alarmManager;
    String mensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_hijo);
        //Inicializamos los distintos botones y secciones que veremos en esta actividad
        logOut = findViewById(R.id.cerrarSesionHijo);
        cancelJob = findViewById(R.id.cancelarScehdule);
        drawerLayout = findViewById(R.id.DrawerLayout);
        navigationView = findViewById(R.id.Navigation_View);
        mailButton = findViewById(R.id.buzonButonHijo);
        toolbar = findViewById(R.id.toolbar);
        authp = new AuthProvider();

        String idUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        MyFirebaseMessagingService myFirebaseMessagingService = new MyFirebaseMessagingService();
        myFirebaseMessagingService.mandarAlerta("pedro Antonio");
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.OpenDrawer, R.string.CloseDrawer);
        //Esta es la funcion inicial que activa la localizacion
        permisosGeolocalizacion();
        analisisdeVideos();
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (android.os.Build.VERSION.SDK_INT <= 10) {
                    update.onStartCommand(mapIntent, 1);
                }
                authp.logout();
                Intent intent = new Intent(PrincipalHijoActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        cancelJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelarServicioGeolocalizacion();
            }
        });
        mailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PrincipalHijoActivity.this, BuzonHijo.class);
                startActivity(intent);
                finish();
            }
        });

        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.MenuInicio) {

                } else if (id == R.id.MenuAjustes) {

                } else {
                    //geolocalización
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }
    //En esta funcion se chequean los permisos iniciales de geolocalizacion y en caso de no tenerlos los pide, una vez obtenidos
    //incia una Geolocalizacion inmediatamente iniciarGeolocalizacion() e inicializa el servicio iniciarServicioGeolocalizacion()
    //En caso de que algo salga mal se crea una geolocalizacion falsa en el centro de Madrid
    private void permisosGeolocalizacion(){
        Location location;
        //Debemos diferenciar la geolocalizacion segun su version de SDK dado que previo a la 10 hay muchas funciones que no existen
        //En caso de ser mas vieja se llamara al fragmento mapIntent que se encargará de su localizacion
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            update.onStartCommand(mapIntent, 1);
        } else {

            //Chequeamos los permisos de geolocalizacion (ACCESS_FINE_LOCATION y ACCESS_COARSE_LOCATION)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //En el caso de no tenerlos los pedimos con la funncion requestPermissions
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

                //Volvemos a chequear por si nos los han aceptado una vez hecho el request
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    //Una vez tengamos los permisos buscamos un provider que no devuelva un nulo, (lo hacemos asi porque algunos providers
                    //devuelven nulo aun indicando que son usables).
                    //Una vez tengamos una localizacion llamamamos a nuestras dos funciones  iniciarGeolocalizacion e iniciarServicioGeolocalizacion()
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                    if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null /*isProviderEnabled*/) {
                        /*updateBestLocation(locationHandler.getLastKnow...)*/
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        iniciarGeolocalizacion(location);
                        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 60000, 20, locationListener);
                        //iniciarServicioGeolocalizacion();
                    } else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        iniciarGeolocalizacion(location);
                        locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 60000, 20, locationListener);
                        //iniciarServicioGeolocalizacion();
                    }
                    //En el caso de que no haya un provider crea una localizacion falsa en madrid
                    else {
                        geolocalizaciónFalsa();
                    }
                }
            }
            //En el caso de si tener los permisos hacemos el mismo recorrido que previamente
            else {
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null){
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    iniciarGeolocalizacion(location);
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 60000, 20, locationListener);
                    //iniciarServicioGeolocalizacion();
                }
                else if(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null){
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    iniciarGeolocalizacion(location);
                    locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 60000, 20, locationListener);
                    //iniciarServicioGeolocalizacion();
                }
                else{
                    geolocalizaciónFalsa();
                }
            }
        }
    }
    //Esta funcion se llama para obtener la latitud y longitud de la localizacion y guardarla en nuestra BD
    private void iniciarGeolocalizacion(Location location){
        String tiempo;
        //Obtenemos la fecha y hora para poder ponerla en nuestro mapa
        Tiempo tiempo1 = new Tiempo();
        tiempo = tiempo1.getTiempo();
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        mDatabase.child("Users").child("hijo").child(uid).child("locationNum").setValue(1);
        double latitud = location.getLatitude();
        mDatabase.child("Users").child("hijo").child(uid).child("location").child ("0").child("lat").setValue(latitud + "");
        double longitud = location.getLongitude();
        mDatabase.child("Users").child("hijo").child(uid).child("location").child ("0").child("lon").setValue(longitud + "");
        mDatabase.child("Users").child("hijo").child(uid).child("location").child ("0").child("com").setValue(tiempo);
    }

    //Esta es una implementacion temporal para ir actializando la geolocalizacion cada x tiempo pero el movild el menor debe estar encendido
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            ActualizarUbicacion(location);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras){}
        @Override
        public void onProviderEnabled( String s){
            mensaje = "GPS ACTIVADO";
            Mensaje();
        }
        @Override
        public void onProviderDisabled( String s){
            mensaje = "GPS DESACTIVADO";
            locationSart();
            Mensaje();
        }
    };

    private void locationSart(){
        LocationManager mlocManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!gpsEnabled){
            gpsEnabled = mlocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(!gpsEnabled){
                Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(settingsIntent);
            }
        }
    }
    public void ActualizarUbicacion (Location location) {
        //Obtenemos el uid del usuario, la altitud y la longitud
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        double latitud = location.getLatitude();
        double longitud = location.getLongitude();
        //Obtenemos el valor locationNum para saber cuantas localizaciones hemos guardado
        mDatabase.child("Users").child("hijo").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                String tiempo;
                //Obtenemos la fecha y hora para poder ponerla en nuestro mapa
                Tiempo tiempo1 = new Tiempo();
                tiempo = tiempo1.getTiempo();
                if (task.isSuccessful()) {
                    for (DataSnapshot ds : task.getResult().getChildren()) {
                        if (ds.getKey().equals("locationNum")) {
                            int Counter = ds.getValue(int.class);
                            int AuxCounter = Counter;
                            if(Counter >= 10)
                                AuxCounter = Counter - 10;
                            //Escribimos en el ultimo espacio o si estan todos llenos en el mas antiguo nuestra localizacion
                            mDatabase.child("Users").child("hijo").child(uid).child("location").child(AuxCounter+"").child("lat").setValue(latitud + "");
                            mDatabase.child("Users").child("hijo").child(uid).child("location").child(AuxCounter+"").child("lon").setValue(longitud + "");
                            mDatabase.child("Users").child("hijo").child(uid).child("location").child(AuxCounter+"").child("com").setValue(tiempo);
                            //Aumentamos nuestro contador y lo guardamos en la BD
                            Counter++;
                            if (Counter == 20)
                                Counter = 10;
                            mDatabase.child("Users").child("hijo").child(uid).child("locationNum").setValue(Counter);
                        }
                    }
                }
            }
        });
    }

    public void Mensaje(){
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG);
    }


    //Esta funcion se encarga de crear un servicio que geolocalice el movil cada x tiempo independientemente de si la aplicacion esta encendida o apagada
    /*private void iniciarServicioGeolocalizacion(){
        ComponentName componentName = new ComponentName(this, ServicioGeolocalizacion.class);
        //En este builder instanciamos los parametros de nuestro servicio que son
        //1º setPersisted: Hace que el servicio continua aun cerrando la aplicacion totalmente
        //2º setPeriodic: Instancia un periodo en el que el servicio se va a volver a lanzar (en ms)
        //3º setRequiresDeviceIdle: Esta opcion hace que nuestro servicio sea mas amigable con el dispositivo del usuario y no se
        //                          lance mientras el telefono esta bajo mucha carga
        JobInfo.Builder info = new JobInfo.Builder(123489024, componentName)
                .setPersisted(true)
                .setPeriodic(20 * 1000)
                .setRequiresDeviceIdle(true);
        JobInfo myJobInfo = info.build();
        //Esta seccion se encarga de lanzar el servicio
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(myJobInfo);
        //https://gist.github.com/sivabe35/d3b56b37c8296a786b21fff87bf1ecba
        if(resultCode == JobScheduler.RESULT_SUCCESS)
            Log.d(TAG, "Job Scehdule");
        else
            Log.d(TAG, "Job not Schedule");
    }*/

    //Esta funcion se encarga de mandar a la base de datos una geolocalizacion falsa en el centro de Madrid, se usa en caso de error para no tener fallos en la BD
    private void geolocalizaciónFalsa() {
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        mDatabase.child("Users").child("hijo").child(uid).child("locationNum").setValue(1);
        mDatabase.child("Users").child("hijo").child(uid).child("location").child ("0").child("lat").setValue(40.1465 + "");
        mDatabase.child("Users").child("hijo").child(uid).child("location").child ("0").child("lon").setValue(-3.70256 + "");
        String tiempo;
        //Obtenemos la fecha y hora para poder ponerla en nuestro mapa
        Tiempo tiempo1 = new Tiempo();
        tiempo = tiempo1.getTiempo();
        mDatabase.child("Users").child("hijo").child(uid).child("location").child ("0").child("com").setValue(tiempo);
    }

    //Esta funcion cancela el servicio, esto no se presentará en el modelo final pero sirve para testear y no tener un servicio continuo en tu dispositivo
    private void cancelarServicioGeolocalizacion(){
        Toast.makeText(PrincipalHijoActivity.this, "tarjeta Clickeada", Toast.LENGTH_SHORT).show();
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(123489024);
    }

    private void analisisdeVideos() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        mDatabase.child("Users").child("hijo").child(uid).child("Padres").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                ArrayList<String> padres = new ArrayList<>();
                if(task.isSuccessful()){
                    for (DataSnapshot ds : task.getResult().getChildren()) {
                        padres.add(ds.getKey().toString());
                    }
                    if(!padres.isEmpty()) {
                        //Analizar videos del hijo
                        //Bloquear video y mandar alerta a los padres
                        //Instancia del helper de las carpetas que se analizan en el dispositivo

                        FolderHelper folderHelper = FolderHelper.getInstance(PrincipalHijoActivity.this,PrincipalHijoActivity.this);

                        //Directorio de la aplicacion donde se guardarán los keyframes
                        String appFolder = folderHelper.getStorageDirPath();

                        //TODO quitar la mayoria de los videos de appFolder y anañirlos en las appFolder + rutas de las carpetas originales
                        ArrayList<String> folder_revisar = new ArrayList();

                        //Analisis de la carpeta de la camara en DCIM
                        analizarCarpeta(FolderHelper.DCIM_CAMERA_FOLDER, appFolder);

                        //Analisis de la carpeta de descargas
                        analizarCarpeta(FolderHelper.DOWNLOADS_FOLDER,appFolder);
                        //Analisis de la carpeta de telegram
                        //  Para versiones antiguas de telegram la ruta es distinta,
                        analizarCarpeta(FolderHelper.TELEGRAM_FOLDER_OLD_VERSION,appFolder);
                        analizarCarpeta(FolderHelper.TELEGRAM_FOLDER,appFolder);


                        //Borrar video si es explicito y mandar alerta a los padres
                    }
                }
            }
        });
    }

    //función recursiva que busca en una carpeta y subcarpetas, los archivos que son videos para
    //su posterior extracción de KeyFrames

    private void analizarCarpeta(final String origFolder,final String destFolder) {


        //Utilizamos hilos para mejorar el rendimiento
        //TODO mirar si este hilo empeora el rendimiento por el acceso silmultaneo de lectura en archivos
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    File f = new File(origFolder);

                    File[] archivos = f.listFiles();


                    // Utilizamos la clase MediaMetadataRetriever para comprobar que efectivamente el
                    // archivo que se borra es un video independientemente de su extensión
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();

                    for (File file : archivos) {
                        if (file.isFile()) {
                            try {
                                retriever.setDataSource(file.getAbsolutePath());
                                String mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
                                if (mime != null && mime.startsWith("video/")) {
                                    Log.v("HijoActivity", "Fichero ejecutado por comando:\n\n\n       "+file.getName()+"\n");
                                    KeyFrames.executeComandoKeyFrames(file.getAbsolutePath(), destFolder, file.getName());
                                }
                            } catch (Exception e) {
                                Toast.makeText(PrincipalHijoActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }else if(file.isDirectory()){
                            analizarCarpeta(file.getAbsolutePath(),destFolder);
                        }
                    }

                    retriever.release();

                }catch (Exception e){
                    Log.e("HijoActivity", "Error al ejecutar analizarCarpeta en la ruta "
                            +origFolder+": \n"+e.getLocalizedMessage());
                    e.fillInStackTrace();
                }
            }
        }).start();

    }
}