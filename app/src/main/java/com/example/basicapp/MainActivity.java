package com.example.basicapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.evrencoskun.tableview.TableView;
import com.example.basicapp.adapters.StockSummaryAdapter;
import com.example.basicapp.model.Cell;
import com.example.basicapp.model.ColumnHeader;
import com.example.basicapp.model.RowHeader;
import com.example.basicapp.model.StockSummary;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class MainActivity extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();
    private static final String STOCK_SUMMARY_URL = "https://flask-inventory-backend.herokuapp.com/stock_summary";

    private StockSummaryAdapter stockSummaryAdapter;

    private List<RowHeader> mRowHeaderList;
    private List<ColumnHeader> mColumnHeaderList;
    private List<List<Cell>> mCellList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mColumnHeaderList = Arrays.asList(StockSummary.FIELDS);
        mRowHeaderList = new ArrayList<>();
        mCellList = new ArrayList<>();

        TableView tableView = this.findViewById(R.id.stock_summary_container);
        stockSummaryAdapter = new StockSummaryAdapter(this);
        tableView.setAdapter(stockSummaryAdapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading data...");
        progressDialog.show();
        requestStockSummaryData();
    }


    private void requestStockSummaryData() {
        Request request = new Request.Builder()
                .url(STOCK_SUMMARY_URL)
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

                        String jsonStr = responseBody.string();
                        Log.d("JSON", jsonStr);
                        jsonObject = new JSONObject(jsonStr);
                        JSONArray array = jsonObject.getJSONArray("result");
                        for(int i = 0; i < array.length(); i++){
                            // Add row header
                            RowHeader rowHeader = new RowHeader(String.valueOf(i));
                            mRowHeaderList.add(rowHeader);
                            // Get Cells of the row
                            JSONObject obj = array.getJSONObject(i);
                            List<Cell> cells = new ArrayList<>();
                            cells.add(new Cell(String.valueOf(obj.getInt("product_id"))));
                            cells.add(new Cell(obj.getString("product_name")));
                            cells.add(new Cell(obj.getString("provider_id")));
                            cells.add(new Cell(obj.getString("provider_name")));
                            cells.add(new Cell(String.valueOf(obj.getInt("stock"))));
                            cells.add(new Cell(stringUtcToLocal(obj.getString("created_at"))));
                            cells.add(new Cell(stringUtcToLocal(obj.getString("updated_at"))));
                            mCellList.add(cells);
                        }
                        runOnUiThread(() -> {
                            stockSummaryAdapter.setAllItems(mColumnHeaderList, mRowHeaderList, mCellList);
                            progressDialog.hide();
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private String stringUtcToLocal(String strDate) {
        //Parse String as UTC
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZoneUTC();
        DateTime dt = formatter.parseDateTime(strDate);

        //Get local TimeZone and convert collected date to Local.
        DateTimeZone localTz = DateTimeZone.getDefault();
        DateTime local = new DateTime(dt,DateTimeZone.forID(localTz.getID()));
        DateTimeFormatter formatterLocal = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        return formatterLocal.print(local);
    }

    public void createEntry(View view) {
        Intent intent = new Intent(MainActivity.this, CreateEntry.class);
        startActivity(intent);
    }

    public void createOut(View view) {
        Intent intent = new Intent(MainActivity.this, CreateOut.class);
        startActivity(intent);
    }

    public void viewEntries(View view) {
        Intent intent = new Intent(MainActivity.this, ViewEntriesActivity.class);
        startActivity(intent);
    }

    public void viewOuts(View view) {
        Intent intent = new Intent(MainActivity.this, ViewOutsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            return false; //I have tried here true also
        }
        return super.onKeyDown(keyCode, event);
    }
}