package com.example.getdone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// importaciomes necesarias para el funcionamiento correcto funcionamiento del metodo CRUD CREATE
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.graphics.Matrix;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class new_Task extends AppCompatActivity {

    // los dos metodos de este inferiores se relacionan con la captura de imagenes por parte del usuario,
    // la cual se puede visualizar en el elemento fotoViewer.
    private static final int CAPTURA_IMAGEN = 1;

    private ImageView fotoViewer;
    private EditText etnombreActividad, etdescripcion, etfechaLimite, etprioridad, etcategoria;
    private adminSqliteOpenHelper admin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_task);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Este apartado del código ha sido modificado respecto de la fase 3; en la cual solo se contaba con el código para que el service de camara funcionara; ahora se crea la estructura de conexión con la BD

        // inicizalización de la base de datos
        admin = new adminSqliteOpenHelper(this, "tareas", null, 1);

        etnombreActividad = findViewById(R.id.nombre_actividad);
        etdescripcion = findViewById(R.id.descripcion_actividad);
        etfechaLimite = findViewById(R.id.fecha_actividad);
        etprioridad = findViewById(R.id.prioridad_actividad);
        etcategoria = findViewById(R.id.categoria_actividad);


        fotoViewer = findViewById(R.id.foto_viewer);

        bottomMenu.configurar(this, R.id.menu_home);
    }

    public void tomarFoto(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAPTURA_IMAGEN);
        } else {
            Toast.makeText(this, "No hay cámara disponible", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURA_IMAGEN && resultCode == RESULT_OK && data != null) {
            Bitmap foto = (Bitmap) data.getExtras().get("data");

            if (foto != null) {
                fotoViewer.setImageBitmap(foto);
                fotoViewer.setVisibility(View.VISIBLE);
                Toast.makeText(this, "✅ Foto capturada", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void nueva_tarea_creada(View v) {
        // validación del unico campo obligatorio: nombre actividad
        if (etnombreActividad.getText().toString().trim().isEmpty()){
            Toast.makeText(this, "⚠️ El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        // con este comando se accede a la base de datos
        SQLiteDatabase bd = admin.getWritableDatabase();

        //Se utiliza el metodo.put para la preparación de los datos y su envío a la base de datos, es muy importante validar que los nombres de los campos esten bien ingresados, no solo en este apartado, en este metodo CREATE es importante (y en todas las otras funciones)
        ContentValues registro = new ContentValues(0);
        registro.put("nombreActividad", etnombreActividad.getText().toString());
        registro.put("descripcion", etdescripcion.getText().toString());
        registro.put("fechaLimite", etfechaLimite.getText().toString());
        registro.put("prioridad", etprioridad.getText().toString());
        registro.put("categoria", etcategoria.getText().toString());

        //Código para añadir la imagen a la db

        String nombreArchivo = "tarea_" + System.currentTimeMillis();
        registro.put("nomArchivo", nombreArchivo);

        // Insertar en BD
        long resultado = bd.insert("tareas", null, registro);

        if (resultado != -1) {
            // Guardar imagen si existe
            if (fotoViewer.getDrawable() != null) {
                fotoViewer.setDrawingCacheEnabled(true);
                Bitmap imagenTarea = Bitmap.createBitmap(fotoViewer.getDrawingCache());
                guardarImagenEnMemoria(imagenTarea, nombreArchivo);
                fotoViewer.setDrawingCacheEnabled(false);
            }

            limpiarCampos();
            Toast.makeText(this, "✅ Tarea creada", Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "❌ Error al crear", Toast.LENGTH_SHORT).show();
        }
        bd.close();

    }

    public void cancelar_nueva_tarea(View v) {
        limpiarCampos();
        finish();
    }

    // Este metodo se utiliza para acceder a la memoria del dispositivo y gardar la imagen
    private void guardarImagenEnMemoria(Bitmap imagenBitmap, String nombreArchivo) {
        File directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File archivoImagen = new File(directorio, nombreArchivo + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(archivoImagen)) {
            imagenBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar imagen", Toast.LENGTH_SHORT).show();
        }
    }

    // metodo para limpiar los campos del formulario despúes de creada una tarea
    private void limpiarCampos() {
        etnombreActividad.setText("");
        etdescripcion.setText("");
        etfechaLimite.setText("");
        etprioridad.setText("");
        etcategoria.setText("");
        fotoViewer.setImageDrawable(null);
        fotoViewer.setVisibility(View.GONE);
    }
}