package com.example.getdone;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class configuracion extends AppCompatActivity {
    //Creación del canal de notificaciones
    private static final String CHANNEL_ID = "getDone_canal";
    private static final int NOTIFICATION_ID = 5;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;

    Switch switch_notificaciones;
    private boolean intentoNotificacionPendiente = false;
    private boolean esActivacion = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);
        bottomMenu.configurar(this, R.id.menu_configuracion);

        // Esta parte del código es necesaria para la solicitud de permisos al dispositivo aplica posterior a Android Tiramisu (13)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        // Esta es la conexión con el switch que se encuentra en activity_configuracion.xml
        switch_notificaciones = findViewById(R.id.configuracion_notificacion);

        // Listener para detectar cambios en el Switch
        switch_notificaciones.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Verificar si tenemos permisos antes de mostrar notificación
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(configuracion.this,
                            android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                        // Guardar el estado para mostrarlo después de conceder permiso
                        intentoNotificacionPendiente = true;
                        esActivacion = isChecked;

                        // Solicitar permiso
                        ActivityCompat.requestPermissions(configuracion.this,
                                new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                                REQUEST_NOTIFICATION_PERMISSION);
                        return;
                    }
                }

                // Si tenemos permisos o es Android < 13, mostrar notificación directamente
                if (isChecked) {
                    mostrarNotificacionActivada();
                } else {
                    mostrarNotificacionDesactivada();
                }
            }
        });
    }

    // Callback para manejar respuesta de permisos, dependiendo de si el usuario otorga lo permisos o no, se mostrará
    // el primer mensaje (si concede permisos) o el segundo (en caso que el usuario no los conceda)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
                Toast.makeText(this, "✅ Permisos de notificación concedidos", Toast.LENGTH_SHORT).show();
            } else {
                // Permiso denegado
                Toast.makeText(this, "⚠️ Permisos denegados.\n" +
                                "Habilítalos en: Ajustes > Apps > GetDone > Notificaciones",
                        Toast.LENGTH_LONG).show();

                // Resetear el switch a su posición anterior
                if (switch_notificaciones != null) {
                    switch_notificaciones.setChecked(!esActivacion);
                }

                intentoNotificacionPendiente = false;
            }
        }
    }

    // Si obtenemos permiso por parte del usuario este es el metodo que se activa cuando el usuario activa
    // las notificaciones a través del switch que se encuentra en activity_configuracion.xml
    private void mostrarNotificacionActivada() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            Toast.makeText(this, "Error: No se pudo acceder al servicio de notificaciones", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear canal para Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Canal getDone",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notificaciones de estado de GetDone");
            notificationManager.createNotificationChannel(channel);
        }

        // Construir notificación expandible, a través del elemento NotificationCompact de material design de Google.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.getdonenb1)
                .setContentTitle("Notificaciones Activadas")
                .setContentText("Las notificaciones están ahora activas")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Has activado las notificaciones de GetDone. \nRecibirás alertas sobre tus tareas y recordatorios importantes."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // Este es el metodo que se activa en el evento que el usuario apague el swicth y por lo tanto, desactiva las notificaciones.
    private void mostrarNotificacionDesactivada() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            Toast.makeText(this, "Error: No se pudo acceder al servicio de notificaciones", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear canal para Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Canal getDone",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notificaciones de estado de GetDone");
            notificationManager.createNotificationChannel(channel);
        }

        // Construir notificación expandible, en esta caso cuando se desactivan las notificaciones, elemento NotificationCompact
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.getdonenb1)
                .setContentTitle("Notificaciones Desactivadas")
                .setContentText("Las notificaciones están ahora inactivas")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Has desactivado las notificaciones de GetDone. \nNo recibirás alertas hasta que las vuelvas a activar."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // metodos relacionados con los botones crear tarea
    public void data_actualizada(View v) {
        Toast.makeText(this, "Datos actualizados de forma correcta", Toast.LENGTH_LONG).show();
    }

    public void volver_activity_main(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}