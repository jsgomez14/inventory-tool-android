package com.example.basicapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;


import com.evrencoskun.tableview.TableView;
import com.example.basicapp.adapters.EntryAdapter;
import com.example.basicapp.adapters.StockSummaryAdapter;
import com.example.basicapp.model.Cell;
import com.example.basicapp.model.ColumnHeader;
import com.example.basicapp.model.Entry;
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

public class ViewEntriesActivity extends AppCompatActivity {
    private final OkHttpClient client = new OkHttpClient();
    private static final String ENTRY_URL = "https://flask-inventory-backend.herokuapp.com/entry";

    private EntryAdapter entryAdapter;
    private List<RowHeader> mRowHeaderList;
    private List<ColumnHeader> mColumnHeaderList;
    private List<List<Cell>> mCellList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_entries);
        mColumnHeaderList = Arrays.asList(Entry.FIELDS);
        mRowHeaderList = new ArrayList<>();
        mCellList = new ArrayList<>();

        TableView tableView = this.findViewById(R.id.entry_container);
        entryAdapter = new EntryAdapter(this);
        tableView.setAdapter(entryAdapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading data...");
        progressDialog.show();
        requestEntryData();
    }

    private void requestEntryData() {
        Request request = new Request.Builder()
                .url(ENTRY_URL)
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
                            cells.add(new Cell(String.valueOf(obj.getInt("provider_id"))));
                            cells.add(new Cell(obj.getString("provider_name")));
                            cells.add(new Cell(String.valueOf(obj.getInt("quantity"))));
                            cells.add(new Cell(stringUtcToLocal(obj.getString("created_at"))));
                            mCellList.add(cells);
                        }
                        runOnUiThread(() -> {
                            entryAdapter.setAllItems(mColumnHeaderList, mRowHeaderList, mCellList);
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
}