package com.example.tfg01.includes;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.tfg01.R;
import com.example.tfg01.actividades.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.RemoteMessageCreator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    DatabaseReference mDatabase;
    FirebaseAuth mAuth;

    String TAG = "MyFirebasMessagingService";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.e("token", "mi token es: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //sendNotification(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if(remoteMessage.getData().size()>0){
            // Check if message contains a data payload.
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            sendNotification(remoteMessage);
        }
    }
    private void sendNotification(RemoteMessage remoteMessage) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "mensaje");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("mensaje", "Alerta", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setShowBadge(true);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Alerta de Grooming en el usuario: " + remoteMessage.getNotification().getBody())
                .setSound(defaultSoundUri)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_alert)
                .setContentText("Cliquee la notificacion para empezar el procedimiento")
                .setContentIntent(clicknoti());
        Random random = new Random();
        int idNotify = random.nextInt(8000);
        assert notificationManager != null;
        notificationManager.notify(idNotify,builder.build());
    }


    public void mandarAlerta(String NombreHijo){
        mAuth = FirebaseAuth.getInstance();
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance("https://tfg01-aa25e-default-rtdb.europe-west1.firebasedatabase.app").getReference();
        mDatabase.child("Users").child("hijo").child(uid).child("padres").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                String padreId;
                for (DataSnapshot ds : task.getResult().getChildren()) {
                    padreId = ds.getValue(String.class);
                    //A partir del id buscamos informacion del hijo par poder mostrarla en las Tarjetas
                    mDatabase.child("Users").child("padre").child(padreId).child("token").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {

                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            String PadreToken;
                            PadreToken = task.getResult().getValue(String.class);
                            FirebaseMessaging.getInstance().send(
                                    RemoteMessage.(PadreToken)
                                            .setMessageId(UUID.randomUUID().toString())
                                            .setNotification(new Notification.Builder()
                                                    .setTitle("alerta de grooming en " + uid)
                                                    .setBody(uid)
                                                    .build())
                                            .build()
                            );
                            /*JSONObject json = new JSONObject();
                            try {
                                json.put("to", PadreToken);
                                JSONObject notification = new JSONObject();
                                notification.put("title", "Alerta");
                                notification.put("body", NombreHijo);
                                json.put("data", notification);
                                String URL = "https://fcm.googleapis.com/fcm/send";
                                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, json, null, null){
                                    @Override
                                    public Map<String, String> getHeaders(){
                                        Map <String, String> header = new HashMap<>();
                                        header.put("content-type", "application/json");
                                        header.put("authorization", "BNuJ4i9lnV8rwSAYFP7GI24ahww3CKcSPqjnTJiGcuQDTJQAbiyuvsINlM8czAo6vvFm-X3njOBN2T76apmoPW4");
                                        return header;
                                    }
                                };
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }*/
                        }
                    });
                }
            }
        });
    }
    public PendingIntent clicknoti(){
        Intent nf = new Intent(getApplicationContext(), MainActivity.class);
        return PendingIntent.getActivity(this, 0, nf, 0);
    }
    //https://medium.com/nybles/sending-push-notifications-by-using-firebase-cloud-messaging-249aa34f4f4c
    //https://firebase.google.com/docs/cloud-messaging/android/first-message?hl=es-419
}

