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

public class new_Task extends AppCompatActivity {

    // los dos metodos de este inferiores se relacionan con la captura de imagenes por parte del usuario,
    // la cual se puede visualizar en el elemento fotoViewer.
    private static final int CAPTURA_IMAGEN = 1;

    private ImageView fotoViewer;

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

        // Vincular el ImageView
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
        Toast.makeText(this, "Nueva tarea creada con éxito", Toast.LENGTH_LONG).show();
    }

    public void cancelar_nueva_tarea(View v) {
        finish();
    }
}