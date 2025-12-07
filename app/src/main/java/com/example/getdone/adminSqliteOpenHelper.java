package com.example.getdone;

import android.content.ContentValues;
import android.database.Cursor;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class adminSqliteOpenHelper extends SQLiteOpenHelper {

    public adminSqliteOpenHelper(@Nullable Context context, @Nullable String name,
                                 @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // para la fase 5, con la finalidad de mejorar el producto infromatico, se añade la tabla usuarios para validación de login a través de credenciales validas, se conecta con tabla tareas a través de id usuario
        db.execSQL("CREATE TABLE usuarios(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "correo TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "nombre TEXT)");

        // Crear la tabla de tareas
        // Los diferentes atributos de la tabla se deben a que el metodo CREATE del CRUD se conectará con la activity y archivo java new_task; el formulario tiene campos que guardan relación con los atributos de creación de tabla
        // Se ha buscado igualmente coherencia entre la tabla de base de datos y el análisis de requisitos de la fase 2
        db.execSQL("CREATE TABLE tareas(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombreActividad TEXT NOT NULL, " +
                "descripcion TEXT, " +
                "fechaLimite TEXT, " +
                "prioridad TEXT, " +
                "categoria TEXT, " +
                "completada INTEGER DEFAULT 0, " +
                "nomArchivo TEXT)");
    }

    // Metodo necesario para actualizar la base de datos cada vez que se crea un nuevo registro
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tareas");
        onCreate(db);
    }

    //Los metodos que se encuentran a continuación son necearios para la poder registrar nuevos usuarios de manera exitosa; además, se utilizan para verificar usuarios existentes para el login

    // Meotodo para el registro de nuevos usuarios, se accede a la tabla "usuarios" de la base de datos en modo escritura (getWritableDatabase)
    // con los metodos put se añaden los datos ingresados por el usuario en los atributos
    public boolean registrarUsuario(String correo, String password, String nombre) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("correo", correo);
        values.put("password", password);
        values.put("nombre", nombre);

        long resultado = db.insert("usuarios", null, values);
        db.close();
        return resultado != -1;
    }

    // Metodo para validación del login
    public boolean validarUsuario(String correo, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM usuarios WHERE correo = ? AND password = ?",
                new String[]{correo, password}
        );

        boolean existe = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return existe;
    }

    // Metodo para verificar si correo (id usuario) ya existe en base de datos
    public boolean existeCorreo(String correo) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM usuarios WHERE correo = ?",
                new String[]{correo}
        );

        boolean existe = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return existe;
    }
}