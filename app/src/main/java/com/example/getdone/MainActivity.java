package com.example.getdone;

import android.content.Intent;
// importanciones necesarias para conexión con base de datos (2 de abajo)
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
// importancion necesaria para poder visualizar la información desde BD; hasta fase 3 el contenido estaba estatico
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {

    // los atributos de clase que se visualizan a continuación, son necesarios para poder visualizar contenido desde bd (tareas pendientes - READ CRUD)
    private LinearLayout contenedorTareasProximas;
    private adminSqliteOpenHelper admin;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        bottomMenu.configurar(this, R.id.menu_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nuevo_correo_usuario), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Variable para inicializar BD
        admin = new adminSqliteOpenHelper(this, "tareas", null, 2);

        contenedorTareasProximas = findViewById(R.id.card_actividades1);

        cargarTareas();
    }

    private void cargarTareas(){


        contenedorTareasProximas.removeAllViews();
        
        // Las líneas de código subsiguientes corresponden a la Query para cargar tareas pendientes desde BD hasta la fase 3, este contenido se estaba mostrando estatico
        SQLiteDatabase bd = admin.getWritableDatabase();
        Cursor cursor = bd.rawQuery(
                "SELECT id, nombreActividad, categoria, fechaLimite " +
                    "FROM tareas WHERE completada = 0 ORDER BY id DESC LIMIT 3", null);

        Log.d("DEBUG_TAREAS", "Total registros encontrados: " + cursor.getCount());

        // El ciclo if de abajo permite determinar si existen tareas pendientes para mostrar en consola
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String nombreActividad = cursor.getString(1);
                String categoria = cursor.getString(2);
                String fechaLimite = cursor.getString(3);

                Log.d("DEBUG_TAREAS", "Tarea encontrada: " + nombreActividad);

                // tarjeta de visualización de tareas pendientes
                crearTarjeta(id, nombreActividad, categoria, fechaLimite);
            }while (cursor.moveToNext());
        } else {
            Log.d("DEBUG_TAREAS", "No se encontraron tareas pendientes");
            // Si no hay tareas, mostrar mensaje
            TextView tvVacio = new TextView(this);
            tvVacio.setText("¡No tienes tareas pendientes!");
            tvVacio.setTextSize(16);
            tvVacio.setTextColor(getResources().getColor(android.R.color.white));
            tvVacio.setPadding(20, 40, 20, 20);
            contenedorTareasProximas.addView(tvVacio);
        }

        // Cerrar cursor y BD
        cursor.close();
        bd.close();
        }

    private void crearTarjeta(int id, String nombre, String categoria, String fechaLimite) {

        // Crear CardView principal
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 10);
        card.setLayoutParams(params);
        card.setCardBackgroundColor(0x5F716565);
        card.setRadius(8);
        card.setPadding(12, 12, 12, 12);

        // Layout interno vertical
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Nombre
        TextView tvNombre = crearTextView(nombre, 14, true);
        layout.addView(tvNombre);

        // Categoría
        TextView tvCategoria = crearTextView(categoria, 12, false);
        layout.addView(tvCategoria);

        // Fecha
        TextView tvFecha = crearTextView("📅 " + fechaLimite, 12, true);
        tvFecha.setTextColor(0xFFD16201);
        layout.addView(tvFecha);

        // CheckBox completada
        CheckBox check = new CheckBox(this);
        check.setText("Marcar como completada");
        check.setTextColor(getResources().getColor(android.R.color.white));
        check.setTextSize(12);
        check.setOnCheckedChangeListener((v, isChecked) -> {
            actualizarEstadoCompletada(id, isChecked);
        });
        layout.addView(check);

        // Ensamblar y agregar al contenedor
        card.addView(layout);
        contenedorTareasProximas.addView(card);
    }

    // este metodo se utiliza para crear un aspectos visual consistente entre las diversas tareas
    private TextView crearTextView(String texto, int tamano, boolean negrita) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextColor(getResources().getColor(android.R.color.white));
        tv.setTextSize(tamano);
        if (negrita) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setPadding(4, 4, 4, 4);
        return tv;
    }
    private void actualizarEstadoCompletada(int id, boolean completada) {
        SQLiteDatabase bd = admin.getWritableDatabase();
        bd.execSQL("UPDATE tareas SET completada = " + (completada ? 1 : 0) + " WHERE id = " + id);
        bd.close();
        cargarTareas();
    }
    public void ir_nueva_tarea (View v) {
        Intent intent = new Intent( this, new_Task.class);
        startActivity(intent);
    }
}

