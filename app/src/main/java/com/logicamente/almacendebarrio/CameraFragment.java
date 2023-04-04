package com.logicamente.almacendebarrio;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiDetector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class CameraFragment extends Fragment implements SurfaceHolder.Callback, Detector.Processor<Barcode> {

    private static final String TAG = "BarcodeScannerFragment";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private SurfaceView mCameraView;
    private CameraSource mCameraSource;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camara, container, false);

        mCameraView = view.findViewById(R.id.camera_preview);

        // Comprueba si la cámara tiene permiso de uso
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(getActivity(), "Se requiere permiso de cámara para escanear códigos de barras.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        createCameraSource();
        mCameraView.getHolder().addCallback(this);
    }

    private void createCameraSource() {
        BarcodeDetector barcodeDetector =new BarcodeDetector.Builder(getActivity()).setBarcodeFormats(Barcode.ALL_FORMATS).build();
        CameraSource.Builder builder = new CameraSource.Builder(getActivity(), barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(1600, 1024);

        mCameraSource = builder.build();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
                return;
            }
            mCameraSource.start(mCameraView.getHolder());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCameraSource.stop();
    }

    @Override
    public void release() {

    }

    @Override
    public void receiveDetections(Detector.Detections<Barcode> detections) {
        SparseArray<Barcode> barcodes = detections.getDetectedItems();
        if (barcodes.size() != 0) {
            String barcodeValue = barcodes.valueAt(0).displayValue;
            Log.d(TAG, "Barcode detected: " + barcodeValue);
            Toast.makeText(getActivity(), "Código de barras detectado: " + barcodeValue, Toast.LENGTH_SHORT).show();
        }
    }

}