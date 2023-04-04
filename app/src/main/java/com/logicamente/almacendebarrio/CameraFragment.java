package com.logicamente.almacendebarrio;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class CameraFragment extends Fragment {
    private static final int REQUEST_CODE_SCAN = 0;
    private SurfaceView surfaceView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camara, container, false);

        // Obtener la vista SurfaceView del layout y configurarla como contenedor de la vista de la cámara
        surfaceView = (SurfaceView) view.findViewById(R.id.surfaceView);

        // Inicializar el objeto IntentIntegrator y configurarlo para usar la vista SurfaceView como contenedor de la vista de la cámara
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setOrientationLocked(true);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.EAN_13);
        integrator.setPrompt("Scan a barcode");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        //integrator.setRequestedPreviewSize(640, 480);
        integrator.initiateScan();

        return view;
    }


    // Manejar el resultado del escaneo del código de barras
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() != null) {
                String barcode = result.getContents();
                // Hacer algo con el resultado del escaneo del código de barras
            } else {
                // El escaneo del código de barras fue cancelado
            }
        }
    }
}
