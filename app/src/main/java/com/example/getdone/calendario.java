package com.example.getdone;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class calendario extends AppCompatActivity {

    private CalendarView calendarView;
    private TextView textoFechaSeleccionada;
    private LinearLayout contenedorTareas;
    private adminSqliteOpenHelper admin;
    private String fechaSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendario);
        bottomMenu.configurar(this, R.id.menu_calendario);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar base de datos
        admin = new adminSqliteOpenHelper(this, "tareas", null, 2);

        // Referenciar elementos
        calendarView = findViewById(R.id.calendarView);
        textoFechaSeleccionada = findViewById(R.id.texto_fecha_seleccionada);
        contenedorTareas = findViewById(R.id.contenedor_tareas_calendario);

        // Establecer fecha actual por defecto
        Calendar calendar = Calendar.getInstance();
        fechaSeleccionada = formatearFecha(calendar);
        textoFechaSeleccionada.setText("Tareas del " + fechaSeleccionada);

        // Cargar tareas de hoy al inicio
        cargarTareasPorFecha(fechaSeleccionada);

        // Listener para cambio de fecha
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // Formatear fecha seleccionada (mes +1 porque Calendar cuenta desde 0)
                fechaSeleccionada = String.format(Locale.US, "%02d/%02d/%04d",
                        dayOfMonth, month + 1, year);

                textoFechaSeleccionada.setText("Tareas del " + fechaSeleccionada);
                cargarTareasPorFecha(fechaSeleccionada);
            }
        });
    }

    private void cargarTareasPorFecha(String fecha) {
        // Limpiar contenedor
        contenedorTareas.removeAllViews();

        // Consultar BD filtrando por fecha
        SQLiteDatabase bd = admin.getReadableDatabase();
        Cursor cursor = bd.rawQuery(
                "SELECT id, nombreActividad, categoria, fechaLimite, completada " +
                        "FROM tareas WHERE fechaLimite = ? ORDER BY completada ASC, id DESC",
                new String[]{fecha}
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String nombreActividad = cursor.getString(1);
                String categoria = cursor.getString(2);
                String fechaLimite = cursor.getString(3);
                int completada = cursor.getInt(4);

                // Crear tarjeta para cada tarea
                crearTarjeta(id, nombreActividad, categoria, fechaLimite, completada == 1);
            } while (cursor.moveToNext());
        } else {
            // Mensaje si no hay tareas para esa fecha
            TextView tvVacio = new TextView(this);
            tvVacio.setText("No hay tareas para esta fecha");
            tvVacio.setTextSize(16);
            tvVacio.setTextColor(getResources().getColor(android.R.color.white));
            tvVacio.setPadding(20, 40, 20, 20);
            tvVacio.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            contenedorTareas.addView(tvVacio);
        }

        cursor.close();
        bd.close();
    }

    private void crearTarjeta(int id, String nombre, String categoria, String fechaLimite, boolean estaCompletada) {
        // Crear CardView principal
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);
        card.setCardBackgroundColor(0x5F716565);
        card.setRadius(8);
        card.setPadding(16, 16, 16, 16);

        // Layout interno vertical
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Nombre de la tarea
        TextView tvNombre = crearTextView(nombre, 16, true);
        layout.addView(tvNombre);

        // Categoría
        TextView tvCategoria = crearTextView("📂 " + categoria, 14, false);
        tvCategoria.setTextColor(0xFFFFFFFF);
        layout.addView(tvCategoria);

        // Fecha límite
        TextView tvFecha = crearTextView("📅 " + fechaLimite, 14, true);
        tvFecha.setTextColor(0xFFD16201);
        layout.addView(tvFecha);

        // CheckBox completada
        CheckBox check = new CheckBox(this);
        check.setText("Marcar como completada");
        check.setTextColor(0xFFD16201);
        check.setTextSize(14);
        check.setButtonTintList(android.content.res.ColorStateList.valueOf(0xFFD16201));
        check.setChecked(estaCompletada);
        check.setOnCheckedChangeListener((v, isChecked) -> {
            actualizarEstadoCompletada(id, isChecked);
        });
        layout.addView(check);

        // Ensamblar y agregar al contenedor
        card.addView(layout);
        contenedorTareas.addView(card);
    }

    private TextView crearTextView(String texto, int tamano, boolean negrita) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextColor(getResources().getColor(android.R.color.white));
        tv.setTextSize(tamano);
        if (negrita) tv.setTypeface(null, android.graphics.Typeface.BOLD);
        tv.setPadding(4, 8, 4, 4);
        return tv;
    }

    private void actualizarEstadoCompletada(int id, boolean completada) {
        SQLiteDatabase bd = admin.getWritableDatabase();
        bd.execSQL("UPDATE tareas SET completada = " + (completada ? 1 : 0) + " WHERE id = " + id);
        bd.close();

        // Recargar tareas para reflejar el cambio
        cargarTareasPorFecha(fechaSeleccionada);
    }

    private String formatearFecha(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        return sdf.format(calendar.getTime());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar tareas cuando se vuelve a la actividad
        if (fechaSeleccionada != null) {
            cargarTareasPorFecha(fechaSeleccionada);
        }
    }
}