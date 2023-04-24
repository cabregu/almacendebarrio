package com.logicamente.almacendebarrio;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import android.Manifest;



public class CargarActivity extends AppCompatActivity {
    private Button buttoncamera;
    private String username;
    private String tipodato = "Nuevo";
    private FirebaseFirestore mfirestore = FirestoreConfig.getFirestoreInstance();
    private CameraDevice cameraDevice;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 123;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar);
        username = getIntent().getStringExtra("username");
        username = username.toUpperCase();

        mfirestore = FirebaseFirestore.getInstance();
        loadingProgressBar = findViewById(R.id.loadingProgressBar1);

        buttoncamera = findViewById(R.id.button_scan);
        buttoncamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(CargarActivity.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.EAN_13);
                integrator.setPrompt("Escanea el código de barras");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(true);
                integrator.setOrientationLocked(true);
                integrator.initiateScan();
            }
        });

        Button buttonGuardar = findViewById(R.id.button_guardar);
        buttonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText etEan = findViewById(R.id.et_ean);
                EditText etDescripcion = findViewById(R.id.et_descripcion);
                EditText etStock = findViewById(R.id.et_stock);
                EditText etPrecioC = findViewById(R.id.et_precio_compra);
                EditText etPrecioV = findViewById(R.id.et_precio_venta);

                String ean = etEan.getText().toString();
                String descripcion = etDescripcion.getText().toString();
                String stock = etStock.getText().toString();
                String precioC = etPrecioC.getText().toString();
                String precioV = etPrecioV.getText().toString();

                IngresarEan(ean,descripcion,precioC,precioV,stock);

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Obtén una instancia del servicio de la cámara
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            // Selecciona la cámara trasera
            String cameraId = manager.getCameraIdList()[0];

            // Obtén las características de la cámara
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

            // Configura la resolución de la vista previa
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
            Size previewSize = sizes[0];
            for (Size size : sizes) {
                if (size.getWidth() < previewSize.getWidth()) {
                    previewSize = size;
                }
            }

            // Configura la vista previa de la cámara
            SurfaceTexture surfaceTexture = new SurfaceTexture(0);
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);

            // Configura el enfoque automático
            int[] afModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
            if (afModes != null && Arrays.asList(afModes).contains(CaptureRequest.CONTROL_AF_MODE_AUTO)) {
                CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(CameraDevice camera) {
                        cameraDevice = camera;

                        try {
                            // Configura el modo de captura de la cámara
                            CaptureRequest.Builder builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                            builder.addTarget(previewSurface);
                            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);

                            // Crea una sesión de captura para la vista previa
                            camera.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                                @Override
                                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                                    try {
                                        CaptureRequest request = builder.build();
                                        cameraCaptureSession.setRepeatingRequest(request, null, null);
                                    } catch (CameraAccessException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                                    Toast.makeText(getApplicationContext(), "Unable to setup camera preview", Toast.LENGTH_LONG).show();
                                }
                            }, null);

                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onDisconnected(CameraDevice camera) {
                        camera.close();
                        cameraDevice = null;
                    }

                    @Override
                    public void onError(CameraDevice camera, int error) {
                        camera.close();
                        cameraDevice = null;
                    }
                };

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted
                    // Call the function that requires the permission
                    String[] cameraIdList = manager.getCameraIdList();
                } else {
                    // Permission is not granted, request it
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }

                // Abre la cámara en segundo plano
                manager.openCamera(cameraId, stateCallback, null);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private void showLoadingScreen() {
        loadingProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoadingScreen() {
        loadingProgressBar.setVisibility(View.GONE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show();
            } else {
                String barcode = result.getContents();
                obtenerDatosSiExisteEan(barcode);
            }
        }
    }

    private void hacerSolicitudHttpDeEan(String barcode) {
        String url = "http://www.logicamente.com.ar/eanbarrcode.php?ean=" + barcode;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Procesar respuesta JSON

                        try {
                            JSONArray productos = response.getJSONArray("productos");
                            if (productos.length() > 0) {
                                JSONObject producto = productos.getJSONObject(0);
                                String descripcion = producto.optString("descripcion", null);
                                String ean = producto.optString("ean", null);
                                EditText etEan = findViewById(R.id.et_ean);
                                EditText etDescripcion = findViewById(R.id.et_descripcion);
                                tipodato="EnBase";

                                etEan.setText(ean);
                                etDescripcion.setText(descripcion);
                                etEan.setEnabled(false);
                                etEan.setFocusable(false);

                            } else {
                                Toast.makeText(CargarActivity.this, "No se encontró ningún producto", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        EditText etCodigo = findViewById(R.id.et_ean);
                        etCodigo.setText(barcode);
                        tipodato="Nuevo";
                        error.printStackTrace();
                    }
                });
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void IngresarEan(String ean, String descrip, String precioc, String preciov, String stock) {
        // Verificar que ean no está vacío
        if (TextUtils.isEmpty(ean)) {
            Toast.makeText(getApplicationContext(), "El código EAN no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener la referencia al documento con ID "ean"
        DocumentReference docRef = mfirestore.collection(username).document(ean);

        // Obtener los datos del documento
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // El documento ya existe
                        actualizarEan(ean,descrip,precioc,preciov,stock);
                 //Toast.makeText(getApplicationContext(), "El código EAN ya existe", Toast.LENGTH_SHORT).show();
                    } else {
                        // El documento no existe, crear un mapa con los datos a agregar
                        Map<String, Object> map = new HashMap<>();
                        map.put("descripcion", descrip);
                        map.put("Stock", stock);
                        map.put("preciocompra", Double.parseDouble(precioc));
                        map.put("precioventa", Double.parseDouble(preciov));
                        map.put("tipodato", tipodato);


                        // Agregar el documento con ID "ean" y los datos en el mapa
                        docRef.set(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        EditText etEan = findViewById(R.id.et_ean);
                                        EditText etDescripcion = findViewById(R.id.et_descripcion);
                                        EditText etStock = findViewById(R.id.et_stock);
                                        EditText etPrecioC = findViewById(R.id.et_precio_compra);
                                        EditText etPrecioV = findViewById(R.id.et_precio_venta);
                                        etEan.setText("");
                                        etDescripcion.setText("");
                                        etStock.setText("");
                                        etPrecioC.setText("");
                                        etPrecioV.setText("");

                                        // Ocultar el teclado
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        if (imm != null) {
                                            imm.hideSoftInputFromWindow(etEan.getWindowToken(), 0);
                                        }

                                        Toast.makeText(getApplicationContext(), "Agregado exitosamente", Toast.LENGTH_SHORT).show();

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "Error al agregar", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

               }
            }
        });
    }

    private void actualizarEan(String ean, String descrip, String precioc, String preciov, String Stock) {
        // Verificar que ean no está vacío
        if (TextUtils.isEmpty(ean)) {
            Toast.makeText(getApplicationContext(), "El código EAN no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener la referencia al documento con ID "ean"
        DocumentReference docRef = mfirestore.collection(username).document(ean);

        // Crear un mapa con los nuevos datos a actualizar
        Map<String, Object> map = new HashMap<>();
        map.put("descripcion", descrip);
        map.put("Stock", Stock);
        map.put("preciocompra", Double.parseDouble(precioc));
        map.put("precioventa", Double.parseDouble(preciov));
        map.put("tipodato", tipodato);

        // Actualizar el documento con ID "ean" y los nuevos datos en el mapa
        docRef.update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        EditText etEan = findViewById(R.id.et_ean);
                        EditText etDescripcion = findViewById(R.id.et_descripcion);
                        EditText etStock = findViewById(R.id.et_stock);
                        EditText etPrecioC = findViewById(R.id.et_precio_compra);
                        EditText etPrecioV = findViewById(R.id.et_precio_venta);
                        etEan.setText("");
                        etDescripcion.setText("");
                        etStock.setText("");
                        etPrecioC.setText("");
                        etPrecioV.setText("");


                        // Ocultar el teclado
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (imm != null) {
                            imm.hideSoftInputFromWindow(etEan.getWindowToken(), 0);
                        }

                        Toast.makeText(getApplicationContext(), "Agregado exitosamente", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Error al actualizar", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void obtenerDatosSiExisteEan(String ean) {
        // Verificar que ean no está vacío
        if (TextUtils.isEmpty(ean)) {
            Toast.makeText(getApplicationContext(), "El código EAN no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obtener la referencia al documento con ID "ean"
        DocumentReference docRef = mfirestore.collection(username).document(ean);

        Toast.makeText(getApplicationContext(), " " + ean + " " + username + " ", Toast.LENGTH_SHORT).show();

        // Obtener los datos del documento
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // El documento ya existe, obtener los datos y llenar los EditText

                        EditText etEan = findViewById(R.id.et_ean);
                        EditText etDescripcion = findViewById(R.id.et_descripcion);
                        EditText etStock = findViewById(R.id.et_stock);
                        EditText etPrecioC = findViewById(R.id.et_precio_compra);
                        EditText etPrecioV = findViewById(R.id.et_precio_venta);

                        String descripcion = document.getString("descripcion");
                        String stock = document.getString("Stock");
                        String precioC = document.getString("preciocompra");
                        String precioV = document.getString("precioventa");

                        etEan.setText(ean);
                        etDescripcion.setText(descripcion);
                        etStock.setText(stock);
                        etPrecioC.setText(precioC);
                        etPrecioV.setText(precioV);
                    } else {
                        // El documento no existe

                        hacerSolicitudHttpDeEan(ean);
                        Toast.makeText(getApplicationContext(), "El código EAN no existe", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Error al obtener los datos del documento
                    Toast.makeText(getApplicationContext(), "Error al obtener los datos del código EAN", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



}


