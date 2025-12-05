package com.example.getdone;

import android.os.Bundle;

import android.content.Intent;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

public class SingupActivity extends AppCompatActivity {

    // Estas clases privadas se crean con la finalidad de realizar de manera adecuada el registro de datos y enviar la información a la base de datos
    private TextInputEditText nombreInput;
    private TextInputEditText correoInput;
    private TextInputEditText passwordInput;
    private CheckBox checkTerminos;
    private adminSqliteOpenHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_singup);

        // Inicializar base de datos (versión 2 para incluir usuarios)
        dbHelper = new adminSqliteOpenHelper(this, "usuarios", null, 2);

        // Referenciar campos del layout
        nombreInput = findViewById(R.id.nombre_usuario);
        correoInput = findViewById(R.id.nuevo_correo);
        passwordInput = findViewById(R.id.nuevo_password);
        checkTerminos = findViewById(R.id.checkBox3);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nuevo_correo_usuario), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void nueva_cuenta_creada(View v) {
        // Obtener valores de los campos
        String nombre = nombreInput.getText().toString().trim();
        String correo = correoInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validación: campos vacíos
        if (nombre.isEmpty()) {
            nombreInput.setError("Ingrese su nombre completo");
            nombreInput.requestFocus();
            return;
        }

        if (correo.isEmpty()) {
            correoInput.setError("Ingrese su correo electrónico");
            correoInput.requestFocus();
            return;
        }

        // Validación: formato de correo
        if (!correo.contains("@") || !correo.contains(".")) {
            correoInput.setError("Correo electrónico inválido");
            correoInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Ingrese una contraseña");
            passwordInput.requestFocus();
            return;
        }

        // Validación: longitud mínima de contraseña
        if (password.length() < 6) {
            passwordInput.setError("La contraseña debe tener al menos 6 caracteres");
            passwordInput.requestFocus();
            return;
        }

        // Validación: términos y condiciones
        if (!checkTerminos.isChecked()) {
            Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si el correo ya está registrado
        if (dbHelper.existeCorreo(correo)) {
            correoInput.setError("Este correo ya está registrado");
            correoInput.requestFocus();
            Toast.makeText(this, "Este correo ya tiene una cuenta. Por favor inicia sesión", Toast.LENGTH_LONG).show();
            return;
        }

        // Intentar registrar el usuario
        if (dbHelper.registrarUsuario(correo, password, nombre)) {
            Toast.makeText(this, "¡Cuenta creada con éxito! Ahora puedes iniciar sesión", Toast.LENGTH_LONG).show();

            // Volver a la pantalla de login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // Cerrar SingupActivity
        } else {
            Toast.makeText(this, "Error al crear la cuenta. Intenta nuevamente", Toast.LENGTH_SHORT).show();
        }
    }
}
