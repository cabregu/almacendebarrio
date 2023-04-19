package com.logicamente.almacendebarrio;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;


public class CargarActivity extends AppCompatActivity {
    private Button buttoncamera;
    private String username;
    private String tipodato = "Nuevo";
    private FirebaseFirestore mfirestore;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar);
        username = getIntent().getStringExtra("username");
        mfirestore = FirebaseFirestore.getInstance();


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

    private void IngresarEan(String ean, String descrip, String precioc, String preciov, String Stock) {
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

                        Toast.makeText(getApplicationContext(), "El código EAN ya existe", Toast.LENGTH_SHORT).show();
                    } else {
                        // El documento no existe, crear un mapa con los datos a agregar
                        Map<String, Object> map = new HashMap<>();
                        map.put("descripcion", descrip);
                        map.put("Stock", Stock);
                        map.put("preciocompra", precioc);
                        map.put("precioventa", preciov);
                        map.put("tipodato", tipodato);

                        // Agregar el documento con ID "ean" y los datos en el mapa
                        docRef.set(map)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getApplicationContext(), "Agregado exitosamente", Toast.LENGTH_SHORT).show();
                                        finish();
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
        map.put("preciocompra", precioc);
        map.put("precioventa", preciov);
        map.put("tipodato", tipodato);

        // Actualizar el documento con ID "ean" y los nuevos datos en el mapa
        docRef.update(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(), "Actualizado exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
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

        // Obtener los datos del documento
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // El documento ya existe, obtener los datos y llenar los EditText
                        EditText etDescripcion = findViewById(R.id.et_descripcion);
                        EditText etStock = findViewById(R.id.et_stock);
                        EditText etPrecioC = findViewById(R.id.et_precio_compra);
                        EditText etPrecioV = findViewById(R.id.et_precio_venta);

                        String descripcion = document.getString("descripcion");
                        String stock = document.getString("Stock");
                        String precioC = document.getString("preciocompra");
                        String precioV = document.getString("precioventa");

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


