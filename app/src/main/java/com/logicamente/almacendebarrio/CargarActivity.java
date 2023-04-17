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
                EditText etEan = findViewById(R.id.et_ean);
                String ean = etEan.getText().toString();
                etEan.setText(ean);
                etEan.setEnabled(true);
                etEan.setFocusable(true);
                hacerSolicitudHttpSiExisteEnStock(ean);

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
                hacerSolicitudHttpDeStock(barcode);
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
    private void hacerSolicitudHttpDeStock(String barcode) {
        String url = "http://www.logicamente.com.ar/eanstock.php?codigo=" + barcode + "&usuario=" + username;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Procesar respuesta JSON
                        try {
                            JSONArray stockArray = response.getJSONArray("stock");
                            if (stockArray.length() > 0) {
                                JSONObject stockObject = stockArray.getJSONObject(0);
                                String codigo = stockObject.optString("codigo", null);
                                String descripcion = stockObject.optString("descripcion", null);
                                String preciocompra = stockObject.optString("preciocompra", null);
                                String precioventa = stockObject.optString("precioventa", null);
                                String cantidadstock = stockObject.optString("cantidadstock", null);

                                EditText etCodigo = findViewById(R.id.et_ean);
                                EditText etDescripcion = findViewById(R.id.et_descripcion);
                                EditText etPrecioCompra = findViewById(R.id.et_precio_compra);
                                EditText etPrecioVenta = findViewById(R.id.et_precio_venta);
                                EditText etCantidadStock = findViewById(R.id.et_stock);

                                etCodigo.setText(codigo);
                                etDescripcion.setText(descripcion);
                                etPrecioCompra.setText(preciocompra);
                                etPrecioVenta.setText(precioventa);
                                etCantidadStock.setText(cantidadstock);
                            } else {



                                Toast.makeText(CargarActivity.this, "No se encontró ningún stock", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        // Si la respuesta es un error 404, llamar a otra función
                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            hacerSolicitudHttpDeEan(barcode);
                        }
                    }
                });
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    private void enviardatos() {
        // Get data from EditTexts
        EditText etEan = findViewById(R.id.et_ean);
        EditText etDescripcion = findViewById(R.id.et_descripcion);
        EditText etPrecioCompra = findViewById(R.id.et_precio_compra);
        EditText etPrecioVenta = findViewById(R.id.et_precio_venta);
        EditText etStock = findViewById(R.id.et_stock);

        String ean = etEan.getText().toString();
        String descripcion = etDescripcion.getText().toString();
        String precioCompra = etPrecioCompra.getText().toString();
        String precioVenta = etPrecioVenta.getText().toString();
        String cantidadStock = etStock.getText().toString();
        String usuario = username; // Get username from variable

        // Validate that all fields are filled
        if (ean.isEmpty() || descripcion.isEmpty() || precioCompra.isEmpty() || precioVenta.isEmpty() || cantidadStock.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please fill all the fields", Toast.LENGTH_LONG).show();
            return;
        }

        // Build request body with parameters
        HashMap<String, String> params = new HashMap<>();
        params.put("codigo", ean);
        params.put("descripcion", descripcion);
        params.put("preciocompra", precioCompra);
        params.put("precioventa", precioVenta);
        params.put("cantidadstock", cantidadStock);
        params.put("tipodato", tipodato);
        params.put("usuario", usuario); // Add username to request body

        // Send a POST request with Volley
        String url = "http://www.logicamente.com.ar/ingresarstock.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Process response if received
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();

                // Clear EditTexts
                etEan.setText("");
                etDescripcion.setText("");
                etPrecioCompra.setText("");
                etPrecioVenta.setText("");
                etStock.setText("");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error sending data", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
    private void hacerSolicitudHttpSiExisteEnStock(String barcode) {
        String url = "http://www.logicamente.com.ar/eanexisteenstock.php?codigo=" + barcode + "&usuario=" + username;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("codigo_encontrado")) {
                            // código ya existe en la base de datos, actualizar stock
                            actualizarstock();
                        } else {
                            // código no existe en la base de datos, agregar al stock
                            enviardatos();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Mostrar mensaje de error de red
                        Toast.makeText(CargarActivity.this, "Error de red", Toast.LENGTH_LONG).show();
                    }
                });
        Volley.newRequestQueue(this).add(stringRequest);
    }
    private void actualizarstock() {
        // Get data from EditTexts
        EditText etEan = findViewById(R.id.et_ean);
        EditText etDescripcion = findViewById(R.id.et_descripcion);
        EditText etPrecioCompra = findViewById(R.id.et_precio_compra);
        EditText etPrecioVenta = findViewById(R.id.et_precio_venta);
        EditText etStock = findViewById(R.id.et_stock);

        String ean = etEan.getText().toString();
        String descripcion = etDescripcion.getText().toString();
        String precioCompra = etPrecioCompra.getText().toString();
        String precioVenta = etPrecioVenta.getText().toString();
        String cantidadStock = etStock.getText().toString();

        // Validate that all fields are filled
        if (ean.isEmpty() || descripcion.isEmpty() || precioCompra.isEmpty() || precioVenta.isEmpty() || cantidadStock.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please fill all the fields", Toast.LENGTH_LONG).show();
            return;
        }

        // Build request body with parameters
        HashMap<String, String> params = new HashMap<>();
        params.put("codigo", ean);
        params.put("descripcion", descripcion);
        params.put("preciocompra", precioCompra);
        params.put("precioventa", precioVenta);
        params.put("cantidadstock", cantidadStock);
        params.put("usuario", username);

        // Send a POST request with Volley
        String url = "http://www.logicamente.com.ar/eanactualizarenstock.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Process response if received
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();

                // Clear EditTexts
                etEan.setText("");
                etDescripcion.setText("");
                etPrecioCompra.setText("");
                etPrecioVenta.setText("");
                etStock.setText("");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error sending data", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

}


