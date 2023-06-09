package com.logicamente.almacendebarrio;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Button cerrarSesionBtn = findViewById(R.id.Cerrar_Sesion);
        Button cargarProductosBtn = findViewById(R.id.cargarProductosBtn);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", ""); // Recupera el nombre de usuario

        cerrarSesionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.putString("username", username); // Reemplaza "username" con el valor obtenido del inicio de sesión
                editor.apply();

                Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        cargarProductosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                String username = sharedPreferences.getString("username", ""); // obtener el username guardado en SharedPreferences

                Intent intent = new Intent(MenuActivity.this, CargarActivity.class);
                intent.putExtra("username", username); // agregar el username como un valor extra
                startActivity(intent);
            }
        });



    }
}