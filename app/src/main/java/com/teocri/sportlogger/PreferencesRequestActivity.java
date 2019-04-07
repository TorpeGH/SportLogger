package com.teocri.sportlogger;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class PreferencesRequestActivity extends AppCompatActivity {

    RadioGroup radioTime;
    RadioGroup radioGap;
    RadioButton radioTimeButton;
    RadioButton radioGapButton;
    int oldTime;
    int oldGap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences_request);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        radioTime = (RadioGroup) findViewById(R.id.radioTime);
        radioGap = (RadioGroup) findViewById(R.id.radioGap);
    }

    @Override
    protected void onStart() {
        super.onStart();

        String actualTime = String.valueOf(MapsActivity.updateTime);
        switch(actualTime){
            case "300000":    //radio_2
                radioTimeButton = (RadioButton) findViewById(R.id.radioButton2);
                radioTimeButton.setChecked(true);
                break;
            case "600000":    //radio_3
                radioTimeButton = (RadioButton) findViewById(R.id.radioButton3);
                radioTimeButton.setChecked(true);
                break;
            case "900000":    //radio_4
                radioTimeButton = (RadioButton) findViewById(R.id.radioButton4);
                radioTimeButton.setChecked(true);
                break;
            case "60000":     //radio_1
            default:
                radioTimeButton = (RadioButton) findViewById(R.id.radioButton1);
                radioTimeButton.setChecked(true);
        }
        oldTime = radioTimeButton.getId();

        String actualGap = String.valueOf(MapsActivity.updateGap);
        switch(actualGap){
            case "100":    //radio_6
                radioGapButton = (RadioButton) findViewById(R.id.radioButton6);
                radioGapButton.setChecked(true);
                break;
            case "500":    //radio_7
                radioGapButton = (RadioButton) findViewById(R.id.radioButton7);
                radioGapButton.setChecked(true);
                break;
            case "1000":    //radio_8
                radioGapButton = (RadioButton) findViewById(R.id.radioButton8);
                radioGapButton.setChecked(true);
                break;
            case "50":     //radio_5
            default:
                radioGapButton = (RadioButton) findViewById(R.id.radioButton5);
                radioGapButton.setChecked(true);
        }
        oldGap = radioGapButton.getId();
    }


    public void SetChanges(View view) {
        int selectedTimeId = radioTime.getCheckedRadioButtonId();
        int selectedGapId = radioGap.getCheckedRadioButtonId();

        if (selectedTimeId == oldTime && selectedGapId == oldGap){
            Toast.makeText(getApplicationContext(), R.string.toast_no_changes, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTimeId != oldTime){
            radioTimeButton = (RadioButton) findViewById(selectedTimeId);
            long newTime = Integer.parseInt(radioTimeButton.getHint().toString());
            MapsActivity.updateTime = newTime;
        }

        if (selectedGapId != oldGap){
            radioGapButton = (RadioButton) findViewById(selectedGapId);
            long newGap = Integer.parseInt(radioGapButton.getHint().toString());
            MapsActivity.updateGap = newGap;
        }

        MapsActivity.newOptions = true;
        Toast.makeText(this, R.string.toast_changes, Toast.LENGTH_SHORT).show();
        onOptionsItemSelected(null);
    }

    @Override
    public void onBackPressed() { onOptionsItemSelected(null); }

    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }
}
