package com.example.getdone;

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
        // Crear la tabla de tareas
        // Los diferentes atributos de la tabla se deben a que el metodo CREATE del CRUD se conectará con la activity y archivo java new_task; el formulario tiene campos que guardan relación con los atributos de creación de tabla
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Si actualizas la versión de la BD, puedes gestionar migraciones aquí
        db.execSQL("DROP TABLE IF EXISTS tareas");
        onCreate(db);
    }
}