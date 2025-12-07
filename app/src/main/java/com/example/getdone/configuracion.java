package com.example.getdone;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import com.google.android.material.textfield.TextInputEditText;

public class configuracion extends AppCompatActivity {
    // Creación del canal de notificaciones
    private static final String CHANNEL_ID = "getDone_canal";
    private static final int NOTIFICATION_ID = 5;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;

    Switch switch_notificaciones;
    private boolean intentoNotificacionPendiente = false;
    private boolean esActivacion = true;

    // Referencias a campos de entrada
    private TextInputEditText inputNombre;
    private TextInputEditText inputCorreo;

    // Base de datos y correo del usuario
    private adminSqliteOpenHelper admin;
    private String correoUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);
        bottomMenu.configurar(this, R.id.menu_configuracion);

        // Inicializar base de datos
        admin = new adminSqliteOpenHelper(this, "tareas", null, 2);

        // Obtener correo del usuario logueado desde SharedPreferences (en este caso, es un archivo local xlm. que almacena en el dispositivo datos en modo clave:valor)
        SharedPreferences preferences = getSharedPreferences("sesion_getdone", MODE_PRIVATE);
        correoUsuario = preferences.getString("correo_usuario", "");

        // Referenciar campos
        inputNombre = findViewById(R.id.input_nombre_usuario);
        inputCorreo = findViewById(R.id.input_correo_usuario);

        // Cargar datos del usuario desde BD
        cargarDatosUsuario();

        // Solicitud de permisos para notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION);
            }
        }

        // Conexión con el switch de notificaciones
        switch_notificaciones = findViewById(R.id.configuracion_notificacion);

        // Listener para detectar cambios en el Switch
        switch_notificaciones.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Verificar permisos antes de mostrar notificación
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(configuracion.this,
                            android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                        intentoNotificacionPendiente = true;
                        esActivacion = isChecked;

                        ActivityCompat.requestPermissions(configuracion.this,
                                new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                                REQUEST_NOTIFICATION_PERMISSION);
                        return;
                    }
                }

                if (isChecked) {
                    mostrarNotificacionActivada();
                } else {
                    mostrarNotificacionDesactivada();
                }
            }
        });
    }

    // Cargar datos del usuario desde la base de datos
    private void cargarDatosUsuario() {
        if (correoUsuario.isEmpty()) {
            Toast.makeText(this, "Error: No se pudo obtener el usuario", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase bd = admin.getReadableDatabase();
        Cursor cursor = bd.rawQuery(
                "SELECT nombre, correo FROM usuarios WHERE correo = ?",
                new String[]{correoUsuario}
        );

        if (cursor != null && cursor.moveToFirst()) {
            String nombre = cursor.getString(0);
            String correo = cursor.getString(1);

            // Establecer valores en los campos
            inputNombre.setText(nombre != null ? nombre : "");
            inputCorreo.setText(correo != null ? correo : "");

            cursor.close();
        } else {
            Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show();
        }
        bd.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Permisos de notificación concedidos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "⚠️ Permisos denegados.\n" +
                                "Habilítalos en: Ajustes > Apps > GetDone > Notificaciones",
                        Toast.LENGTH_LONG).show();

                if (switch_notificaciones != null) {
                    switch_notificaciones.setChecked(!esActivacion);
                }

                intentoNotificacionPendiente = false;
            }
        }
    }

    private void mostrarNotificacionActivada() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            Toast.makeText(this, "Error: No se pudo acceder al servicio de notificaciones", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Canal getDone",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notificaciones de estado de GetDone");
            notificationManager.createNotificationChannel(channel);
        }

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

    private void mostrarNotificacionDesactivada() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            Toast.makeText(this, "Error: No se pudo acceder al servicio de notificaciones", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Canal getDone",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notificaciones de estado de GetDone");
            notificationManager.createNotificationChannel(channel);
        }

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

    // Metodo para actualizar datos del usuario
    public void data_actualizada(View v) {
        // Obtener valor del nombre
        String nombre = inputNombre.getText().toString().trim();

        // Validación
        if (nombre.isEmpty()) {
            inputNombre.setError("El nombre es obligatorio");
            inputNombre.requestFocus();
            return;
        }

        // Actualiza la información modificada por el usuario, en este caso, solo se ha configurado para modificar el nombre, una versión final de la aplicación permitir la actualización total de los datos del usuario
        SQLiteDatabase bd = admin.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);

        int filasActualizadas = bd.update("usuarios", values, "correo = ?", new String[]{correoUsuario});
        bd.close();

        if (filasActualizadas > 0) {
            Toast.makeText(this, "✅ Datos actualizados correctamente", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "❌ Error al actualizar datos", Toast.LENGTH_SHORT).show();
        }
    }

    public void volver_activity_main(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void cerrarSesion(View v) {
        // Limpiar SharedPreferences (eliminar sesión)
        SharedPreferences preferences = getSharedPreferences("sesion_getdone", MODE_PRIVATE);
        preferences.edit().clear().apply();

        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();

        // Volver a LoginActivity y limpiar stack de activities
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}