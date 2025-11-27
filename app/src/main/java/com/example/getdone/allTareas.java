package com.example.getdone;

import android.os.Bundle;

// las siguientes importaciones son necesarias para el correcto funcionamiento de la lista tareas y por ende, de los metodos UPDATE and DELETE que la base roponen desarrollar en esta activity
// las importaciones incluyen conexión con la base de datos, además de los elementos visuales necesarios para que la información se lleve desde el BackEnd hasta el FronEnd directamente desde base de datos (contenido dinamico)
// Hasta la fase 3, esto contenido se mostraba estatico con elementos .xml con las importaciones más las clases correspondientes en este archivo, ya contamos con contenido dinámico

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class allTareas extends AppCompatActivity {

    // Variables de clase para el manejo del contenedor de tareas y la conexión con la base de datos dentro de la clase allTareas
    private LinearLayout contenedorTareas;
    private adminSqliteOpenHelper admin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_all_tareas);
        bottomMenu.configurar(this, R.id.menu_tareas);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // variables necesarias para que el onCreate pueda conectar con BD y buscar tareas con id
        admin = new adminSqliteOpenHelper(this, "tareas", null, 1);
        contenedorTareas = findViewById(R.id.contenedor_tareas);
        cargarTodasLasTareas();
    }

    private void cargarTodasLasTareas(){
        contenedorTareas.removeAllViews();
        SQLiteDatabase bd = admin.getReadableDatabase();
        Cursor cursor = bd.rawQuery(
                "SELECT id, nombreActividad, descripcion, fechaLimite, prioridad, categoria, completada " + "FROM tareas ORDER BY id DESC", null
        );

        // este bucle condicional se encarga de cargar los registros desde base de datos, se puede evidenciar la relaciones con cada una de las columnas del archivo "adminSqliteOpenHelper.java
        if (cursor.moveToFirst()){
            do {
                int id = cursor.getInt(0);
                String nombreActividad = cursor.getString(1);
                String descripcion = cursor.getString(2);
                String fechaLimite = cursor.getString(3);
                String prioridad = cursor.getString(4);
                String categoria = cursor.getString(5);
                boolean completada = cursor.getInt(6) == 1;

                crearTarjeta (id, nombreActividad, descripcion,fechaLimite, prioridad, categoria, completada);
            // este while permite navegar entre todos los registros de base de datos
            } while (cursor.moveToNext());
            // else que se despliega en caso que no existan registros
        } else {
            TextView vacio = new TextView(this);
            vacio.setText("📋 No hay tareas registradas");
            vacio.setTextSize(16);
            vacio.setTextColor(getResources().getColor(android.R.color.white));
            vacio.setPadding(20, 20, 20, 20);
            contenedorTareas.addView(vacio);
        }

        cursor.close();
        bd.close();
    }

    // este metodo es necesario para poder visualizar las tareas a través de un elemento tarjeta (de manera similar como las tareas se cargan en la pantalla principal)
    // A difrerencia de otras clases de este archivo, se puede evidenciar el uso de multiples etiquetas de estilo (setTextSize, setOrientation, setpadding, etc)

    private void crearTarjeta(int id, String nombre, String descripcion,
                                      String fechaLimite, String prioridad,
                                      String categoria, boolean completada) {

        // CardView principal
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 20);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(0x5F716565);
        card.setRadius(12);

        // Layout interno
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // Nombre
        TextView nombreActividad = new TextView(this);
        nombreActividad.setText(nombre);
        nombreActividad.setTextColor(getResources().getColor(android.R.color.white));
        nombreActividad.setTextSize(16);
        nombreActividad.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(nombreActividad);

        // Categoría
        TextView categoriaView = new TextView(this);
        categoriaView.setText(categoria);
        categoriaView.setTextColor(getResources().getColor(android.R.color.white));
        layout.addView(categoriaView);

        // Fecha
        TextView fecha = new TextView(this);
        fecha.setText("📅 " + fechaLimite);
        fecha.setTextColor(0xFFD16201);
        layout.addView(fecha);

        // CheckBox
        CheckBox check = new CheckBox(this);
        check.setText("Marcar como completada");
        check.setChecked(completada);
        check.setTextColor(getResources().getColor(android.R.color.white));
        check.setOnCheckedChangeListener((v, isChecked) -> {
            actualizarEstadoCompletada(id, isChecked);
        });
        layout.addView(check);

        // Layout de botones
        LinearLayout layoutBotones = new LinearLayout(this);
        layoutBotones.setOrientation(LinearLayout.HORIZONTAL);
        layoutBotones.setGravity(android.view.Gravity.CENTER);

        // Botón EDITAR
        Button btnEditar = new Button(this);
        btnEditar.setText("Editar");
        btnEditar.setBackgroundColor(0xFF4CAF50);
        btnEditar.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams paramsEditar = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        paramsEditar.setMargins(0, 10, 5, 0);
        btnEditar.setLayoutParams(paramsEditar);
        btnEditar.setOnClickListener(v ->
            mostrarEditar(id, nombre, descripcion, fechaLimite, prioridad, categoria)
        );
        layoutBotones.addView(btnEditar);

        // Botón ELIMINAR
        Button btnEliminar = new Button(this);
        btnEliminar.setText("Eliminar");
        btnEliminar.setBackgroundColor(0xFFF44336);
        btnEliminar.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams paramsEliminar = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        paramsEliminar.setMargins(5, 10, 0, 0);
        btnEliminar.setLayoutParams(paramsEliminar);
        btnEliminar.setOnClickListener(v ->
            confirmarEliminarTarea(id, nombre)
        );
        layoutBotones.addView(btnEliminar);

        layout.addView(layoutBotones);
        card.addView(layout);
        contenedorTareas.addView(card);
    }

    // el siguiente metodo es necesario para la funcionalidad de marcar una tarea como completada a través del checkbox, es diferente del metodo para editar tareas relacionadas con el respectivo botón

    private void actualizarEstadoCompletada(int id, boolean completada) {
        SQLiteDatabase bd = admin.getWritableDatabase();
        ContentValues registro = new ContentValues();
        registro.put("completada", completada ? 1 : 0);

        bd.update("tareas", registro, "id=" + id, null);
        bd.close();

        Toast.makeText(this, completada ? "✅ Completada" : "⏳ Pendiente", Toast.LENGTH_SHORT).show();
    }

    private void mostrarEditar(int id, String nombreActividad, String descripcion, String fechaLimite, String prioridad, String categoria) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Tarea");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.editar_tarea, null);

        // se obtiene la información de los campos actuales para su edición

        EditText etNombre = dialogView.findViewById(R.id.et_editar_nombre);
        EditText etDescripcion = dialogView.findViewById(R.id.et_editar_descripcion);
        EditText etFecha = dialogView.findViewById(R.id.et_editar_fecha);
        EditText etPrioridad = dialogView.findViewById(R.id.et_editar_prioridad);
        EditText etCategoria = dialogView.findViewById(R.id.et_editar_categoria);

        // una vez obtenida la información existente en base de datos, se despliega una vista con la misma
        etNombre.setText(nombreActividad);
        etDescripcion.setText(descripcion);
        etFecha.setText(fechaLimite);
        etPrioridad.setText(prioridad);
        etCategoria.setText(categoria);

        builder.setView(dialogView);

        // Botón de guardado (metodo UPDATE)
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            SQLiteDatabase bd = admin.getWritableDatabase();

            ContentValues registro = new ContentValues();
            registro.put("nombreActividad", etNombre.getText().toString());
            registro.put("descripcion", etDescripcion.getText().toString());
            registro.put("fechaLimite", etFecha.getText().toString());
            registro.put("prioridad", etPrioridad.getText().toString());
            registro.put("categoria", etCategoria.getText().toString());

            int cant = bd.update("tareas", registro, "id=" + id, null);
            bd.close();

            if ( cant == 1) {
                Toast.makeText(this, "✅ Tarea actualizada", Toast.LENGTH_SHORT).show();
                // esta función especifica permite recargar la lista con los datos acutalizados
                cargarTodasLasTareas();
            }
        });

        // Botón Cancelar actualización
        builder.setNegativeButton("Cancelar", null);

        builder.create().show();

    }

    // La siguiente función maneja la lógica de eliminación de registros DOM
    private void confirmarEliminarTarea (int id, String nombre){
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Tarea")
                .setMessage("¿Esta seguro que desea eliminar '" + nombre + "?")
                .setPositiveButton("Eliminar", (dialog, which) ->{
                    SQLiteDatabase bd = admin.getWritableDatabase();
                    int cant = bd.delete("tareas", "id=" + id, null);
                    bd.close();

                    if (cant == 1){
                        Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show();
                        cargarTodasLasTareas();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onResume(){
        super.onResume();
        cargarTodasLasTareas();
    }
}