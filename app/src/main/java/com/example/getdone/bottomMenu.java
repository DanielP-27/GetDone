package com.example.getdone;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class bottomMenu {
    // Clase publica para poder ser utilizada por otros componentes; a través de la estructura condicional if - else if
    // se identifica la opción seleccionada por el usuario.
    public static void configurar(Activity activity, int menuSeleccionado) {
        // A través de está variable, se identifica el id del componente material desing BottomMenu en los archivos .XML
        BottomNavigationView bottomNav = activity.findViewById(R.id.menu_navegacion);

        if (bottomNav != null) {
            bottomNav.setSelectedItemId(menuSeleccionado);

            bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    // ID's de los elementos del archivo bottom_menu.xml
                    if (itemId == R.id.menu_home) {
                        navegarA(activity, MainActivity.class);
                        return true;

                    } else if (itemId == R.id.menu_tareas) {
                        navegarA(activity, allTareas.class);
                        return true;

                    } else if (itemId == R.id.menu_calendario) {
                        navegarA(activity, calendario.class);
                        return true;

                    } else if (itemId == R.id.menu_configuracion) {
                        navegarA(activity, configuracion.class);
                        return true;
                    }

                    return false;
                }
            });
        }
    }

    private static void navegarA(Activity activity, Class<?> destino) {
        if (!activity.getClass().equals(destino)) {
            Intent intent = new Intent(activity, destino);
            activity.startActivity(intent);
            activity.finish();
        }
    }
}