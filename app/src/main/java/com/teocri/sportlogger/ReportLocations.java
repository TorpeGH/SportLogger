package com.teocri.sportlogger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import static com.teocri.sportlogger.MainActivity.db;

public class ReportLocations extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ArrayList<String> dates = db.getSingleDates();
    private String[][] locations = new String[dates.size()][2];
    ArrayList<HashMap<String, String>> list = new ArrayList<>();
    private SimpleAdapter adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_locations);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        HashMap<String, String> item;
        for (int i = 0; i < dates.size(); i++){
            locations[i][0] = dates.get(i);
            locations[i][1] = getString(R.string.entries_per_day) + db.getDateIstances(dates.get(i));
        }

        for (int i = 0; i < dates.size(); i++){
            item = new HashMap<>();
            item.put("line1", locations[i][0]);
            item.put("line2", locations[i][1]);
            list.add(item);
        }

        adapter = new SimpleAdapter(this, list, R.layout.list_item,
                new String[] {"line1", "line2"},
                new int[] {R.id.item, R.id.subItem});

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onBackPressed() { onOptionsItemSelected(null); }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String lat = db.getDateLatitude(dates.get(i));
        String lon = db.getDateLongitude(dates.get(i));
        MapsActivity.moveCamLatLon(Double.valueOf(lat), Double.valueOf(lon));
        onBackPressed();
    }

    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }
}

