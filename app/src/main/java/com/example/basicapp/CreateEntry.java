package com.example.basicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CreateEntry extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String PROVIDER_URL = "https://flask-inventory-backend.herokuapp.com/provider?id=";
    private static final String PRODUCT_URL = "https://flask-inventory-backend.herokuapp.com/product?id=";
    private static final String CREATE_ENTRY_URL = "https://flask-inventory-backend.herokuapp.com/entry";

    private EditText etProviderId,etProviderName,etProductId,etProductName,
    etProductDescription,etEntryDate,etProductMeasure,etQuantity;

    private Button btFindProvider,btFindProduct,btSubmitEntry;

    private AwesomeValidation providerValidation;
    private AwesomeValidation productValidation;
    private AwesomeValidation submitValidation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_entry);

        //Assign Variables
        etProviderId = findViewById(R.id.entry_provider_id);
        etProviderName = findViewById(R.id.entry_provider_name);
        etProductId =findViewById(R.id.entry_product_id);
        etProductName=findViewById(R.id.entry_product_name);
        etProductDescription=findViewById(R.id.entry_product_description);
        //set current Date
        etEntryDate=findViewById(R.id.entry_date);
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String strDate = dateFormat.format(date);
        etEntryDate.setText(strDate);
        //get other fields
        etProductMeasure=findViewById(R.id.entry_product_measure);
        etQuantity=findViewById(R.id.entry_quantity);
        btFindProvider =findViewById(R.id.entry_find_provider);
        btFindProduct =findViewById(R.id.entry_find_product);
        btSubmitEntry =findViewById(R.id.entry_submit);

        //Initialize Validation Style
        providerValidation = new AwesomeValidation(ValidationStyle.BASIC);
        productValidation = new AwesomeValidation(ValidationStyle.BASIC);
        submitValidation = new AwesomeValidation(ValidationStyle.BASIC);

        //Add Validation for provider fields
        providerValidation.addValidation(this, R.id.entry_provider_id,
                RegexTemplate.NOT_EMPTY, R.string.err_provider_id);
        submitValidation.addValidation(this, R.id.entry_provider_name,
                RegexTemplate.NOT_EMPTY, R.string.err_provider_name);
        //Add Validation for product fields
        productValidation.addValidation(this, R.id.entry_product_id,
                RegexTemplate.NOT_EMPTY, R.string.err_product_id);
        submitValidation.addValidation(this, R.id.entry_product_name,
                RegexTemplate.NOT_EMPTY, R.string.err_product_name);
        submitValidation.addValidation(this, R.id.entry_product_description,
                RegexTemplate.NOT_EMPTY, R.string.err_product_description);
        submitValidation.addValidation(this, R.id.entry_product_measure,
                RegexTemplate.NOT_EMPTY, R.string.err_product_measure);
        //Add Validation for entry fields
        submitValidation.addValidation(this, R.id.entry_quantity,
                RegexTemplate.NOT_EMPTY, R.string.err_entry_quantity);
        submitValidation.addValidation(this, R.id.entry_date,
                input -> {
                    try {
                        Calendar calendarIn = Calendar.getInstance();
                        calendarIn.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(input));
                        return true;
                    } catch (ParseException e) {
                        return false;
                    }
                }, R.string.err_entry_date);

        btFindProvider.setOnClickListener(v -> {
            //get provider data
            if(providerValidation.validate()){
                getProviderData(etProviderId.getText().toString());
            } else {
                Toast.makeText(getApplicationContext(), "ID Proveedor no válido", Toast.LENGTH_SHORT).show();
            }
        });
        btFindProduct.setOnClickListener(v -> {
            //get product data
            if(productValidation.validate()){
                getProductData(etProductId.getText().toString(),etProviderId.getText().toString());
            } else {
                Toast.makeText(getApplicationContext(), "ID Producto no válido", Toast.LENGTH_SHORT).show();
            }
        });
        btSubmitEntry.setOnClickListener(v -> {
            //Check Validation
            if(submitValidation.validate() && productValidation.validate() && providerValidation.validate()){
                createEntry(etProviderId.getText().toString(),
                        etProviderName.getText().toString(),
                        etProductId.getText().toString(),
                        etProductName.getText().toString(),
                        etProductDescription.getText().toString(),
                        etProductMeasure.getText().toString(),
                        etEntryDate.getText().toString(),
                        etQuantity.getText().toString());
            } else {
                Toast.makeText(getApplicationContext(), "Check invalid fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getProviderData(String id) {
        Request request = new Request.Builder()
                .url(PROVIDER_URL+id)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    JSONObject jsonObject = null;
                    try {
                        String providerName = null;
                        String jsonStr = responseBody.string();
                        Log.d("JSON", jsonStr);
                        jsonObject = new JSONObject(jsonStr);
                        JSONArray array = jsonObject.getJSONArray("result");
                        for(int i = 0; i < array.length(); i++){
                            JSONObject obj = array.getJSONObject(i);
                            providerName = obj.getString("name");
                        }
                        String finalProviderName = providerName;
                        runOnUiThread(() -> {
                            etProviderName.setText(finalProviderName);
                            Toast.makeText(getApplicationContext(), "Provider loaded succesfully", Toast.LENGTH_SHORT).show();
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void getProductData(String id, String providerId) {
        Request request = new Request.Builder()
                .url(PRODUCT_URL+id)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    JSONObject jsonObject = null;
                    try {
                        String productName = null;
                        String productDesc = null;
                        String productMeasure = null;
                        String providerId = null;

                        String jsonStr = responseBody.string();
                        Log.d("JSON", jsonStr);
                        jsonObject = new JSONObject(jsonStr);
                        JSONArray array = jsonObject.getJSONArray("result");
                        for(int i = 0; i < array.length(); i++){
                            JSONObject obj = array.getJSONObject(i);
                            productName = obj.getString("name");
                            productDesc = obj.getString("description");
                            productMeasure = obj.getString("measure");
                            providerId = String.valueOf(obj.getInt("provider_id"));
                        }
                        String finalProductName = productName;
                        String finalProductDesc = productDesc;
                        String finalProductMeasure = productMeasure;
                        String finalProviderId = String.valueOf(providerId);
                        runOnUiThread(() -> {
                            if (finalProviderId != null){
                                if (finalProviderId.equals(String.valueOf(etProviderId.getText()))) {
                                    etProductName.setText(finalProductName);
                                    etProductDescription.setText(finalProductDesc);
                                    etProductMeasure.setText(finalProductMeasure);
                                    Toast.makeText(getApplicationContext(), "Producto cargado correctamente.", Toast.LENGTH_SHORT).show();
                                } else
                                {
                                    Toast.makeText(getApplicationContext(), "Producto no existe para el proveedor seleccionado.", Toast.LENGTH_SHORT).show();
                                }
                            } else
                            {
                                Toast.makeText(getApplicationContext(), "Producto no existe en la base de datos.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Producto no existe en la base de datos.", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void createEntry(String providerId, String providerName,
                             String productId, String productName, String productDescription,
                             String productMeasure, String entryDate, String entryQuantity) {


        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        DateTime dt = formatter.parseDateTime(entryDate).toDateTime(DateTimeZone.UTC);

        String json = "{" +
                "\"provider_id\":"+Integer.valueOf(providerId)+"," +
                "\"provider_name\": \""+ providerName+"\"," +
                "\"product_id\":"+ Integer.valueOf(productId) +","+
                "\"product_name\": \"" + productName +"\","+
                "\"product_description\": \"" + productDescription +"\","+
                "\"measure\": \"" +productMeasure+"\","+
                "\"entry_date\": \"" + formatter.print(dt) + "\","+
                "\"quantity\":" +Integer.valueOf(entryQuantity) +"}";

        RequestBody formBody = RequestBody.create(JSON,json);
        Request request = new Request.Builder()
                .url(CREATE_ENTRY_URL)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) {
                try (ResponseBody responseBody = response.body()) {
                    runOnUiThread(() -> {
                        etProviderId.setText("");
                        etProviderName.setText("");
                        etProductId.setText("");
                        etProductName.setText("");
                        etProductDescription.setText("");
                        etProductMeasure.setText("");
                        etEntryDate.setText("");
                        etQuantity.setText("");
                        Toast.makeText(getApplicationContext(),"Entrada creada exitosamente.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CreateEntry.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    });
                }
            }
        });
    }
}