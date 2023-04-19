package com.logicamente.almacendebarrio;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


public class CargarActivity extends AppCompatActivity {
    private Button buttoncamera;
    private String username;
    private String tipodato = "Nuevo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar);
        username = getIntent().getStringExtra("username");

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
                hacerSolicitudHttpDeEan(barcode);
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




}


