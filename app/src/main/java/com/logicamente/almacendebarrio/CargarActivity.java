package com.logicamente.almacendebarrio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CargarActivity extends AppCompatActivity {

    private Button buttoncamera;
    private CameraFragment camfragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar);

        buttoncamera = findViewById(R.id.button_scan);

        buttoncamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fragmentManager = getSupportFragmentManager();
                CameraFragment cameraFragment = new CameraFragment();
                fragmentManager.beginTransaction()
                        .add(R.id.container, cameraFragment)
                        .commit();

            }
        });
    }
}
