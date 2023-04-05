package com.logicamente.almacendebarrio;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.jetbrains.annotations.NotNull;

public class CameraFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentIntegrator integrator = new IntentIntegrator(getActivity());
        integrator.setDesiredBarcodeFormats(IntentIntegrator.CODE_39, IntentIntegrator.EAN_13);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    // Este método se llama cuando se obtiene el resultado de la lectura del código de barras
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Aquí puedes procesar los datos del código de barras
                String barcode = result.getContents();
                Toast.makeText(getActivity(), "Código de barras: " + barcode, Toast.LENGTH_SHORT).show();
            } else {
                // Si la lectura del código de barras falla, aquí puedes manejar el error
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
