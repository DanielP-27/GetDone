package com.example.getdone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    // se crean clases privadas para el manejo de los campos correo y contraseña en Login para manejo desde base de datos
    private TextInputEditText correoInput;
    private TextInputEditText passwordInput;
    private adminSqliteOpenHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // incializacion del helper de la base de datos
        dbHelper = new adminSqliteOpenHelper(this, "tareas", null, 2);

        // referencia a los campos de entrada para comparar información registrada con datos contenidos en base de datos
        correoInput = findViewById(R.id.correo_usuario);
        passwordInput = findViewById(R.id.password_usuario);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nuevo_correo_usuario), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void ir_activity_main(View v) {
        // Obtener los valores de los campos
        String correo = correoInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validar que los campos no estén vacíos
        if (correo.isEmpty()) {
            correoInput.setError("Ingrese su correo electrónico");
            correoInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Ingrese su contraseña");
            passwordInput.requestFocus();
            return;
        }

        // Validar formato de correo básico
        if (!correo.contains("@") || !correo.contains(".")) {
            correoInput.setError("Correo electrónico inválido");
            correoInput.requestFocus();
            return;
        }

        // Validar credenciales con la base de datos
        if (dbHelper.validarUsuario(correo, password)) {
            // Login exitoso - guardar sesión
            guardarSesion(correo);

            Toast.makeText(this, "Bienvenido a GetDone", Toast.LENGTH_SHORT).show();

            // Ir a MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("correo_usuario", correo);
            startActivity(intent);
            finish();
        } else {
            // Credenciales incorrectas
            Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
            passwordInput.setText("");
            passwordInput.requestFocus();
        }
    }

    private void guardarSesion(String correo) {
        SharedPreferences preferences = getSharedPreferences("sesion_getdone", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("correo_usuario", correo);
        editor.putBoolean("sesion_activa", true);
        editor.apply();
    }
    public void ir_activity_singup (View v) {
        Intent f = new Intent(this, SingupActivity.class);
        startActivity(f);
    }

}