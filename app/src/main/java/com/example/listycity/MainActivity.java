package com.example.listycity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AddCityFragment.AddCityDialogListener {

    private ArrayList<City> dataList;
    private ListView cityList;
    private CityArrayAdapter cityAdapter;

    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataList = new ArrayList<>();
        cityList = findViewById(R.id.city_list);
        cityAdapter = new CityArrayAdapter(this, dataList);
        cityList.setAdapter(cityAdapter);

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "Listener error", error);
                return;
            }
            if (value == null) return;

            dataList.clear();
            for (QueryDocumentSnapshot doc : value) {
                String name = doc.getString("name");
                String province = doc.getString("province");
                if (name != null && province != null) {
                    dataList.add(new City(doc.getId(), name, province));
                }
            }
            cityAdapter.notifyDataSetChanged();
        });


        Button addBtn = findViewById(R.id.button_add_city);
        addBtn.setOnClickListener(v ->
                AddCityFragment.newInstanceForAdd()
                        .show(getSupportFragmentManager(), "ADD_CITY")
        );

        cityList.setOnItemClickListener((parent, view, position, id) -> {
            City c = dataList.get(position);
            AddCityFragment.newInstanceForEdit(position, c.getName(), c.getProvince())
                    .show(getSupportFragmentManager(), "EDIT_CITY");
        });

        cityList.setOnItemLongClickListener((parent, view, position, id) -> {
            City c = dataList.get(position);

            new AlertDialog.Builder(this)
                    .setTitle("Delete city?")
                    .setMessage("Delete " + c.getName() + ", " + c.getProvince() + "?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (d, which) -> {
                        String docId = c.getId();
                        if (docId != null) {
                            citiesRef.document(docId).delete();
                        }
                    })
                    .show();

            return true;
        });
    }

    @Override
    public void onCitySaved(int position, City city) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", city.getName());
        data.put("province", city.getProvince());

        if (position == -1) {
            citiesRef.add(data);
        } else {
            City old = dataList.get(position);
            String docId = old.getId();
            if (docId != null) {
                citiesRef.document(docId).set(data);
            }
        }
    }
}
