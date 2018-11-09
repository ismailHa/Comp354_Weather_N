package ca.concordia.comp354mn.comp354mn_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        String s = "{\"currently\": {\"apparentTemperature\": 70.9,\"cloudCover\": 0,\"dewPoint\": 34.27,\"humidity\": 0.26,\"icon\": \"clear-day\",\"nearestStormBearing\": 58,\"nearestStormDistance\": 494,\"ozone\": 249.08,\"precipIntensity\": 0,\"precipProbability\": 0,\"pressure\": 1016.58,\"summary\": \"Clear\",\"temperature\": 70.9,\"time\": 1541625600,\"uvIndex\": 3,\"visibility\": 10,\"windBearing\": 325,\"windGust\": 7.67,\"windSpeed\": 1.72}}";
        JsonDataParser j = new JsonDataParser(s);

        String weatherInfo = j.toString();

        View textViewId = findViewById(R.id.weather_info_text);
        TextView tv1 = (TextView) textViewId;
        tv1.setText(weatherInfo);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // Move code from onCreate to here for updating weather content on demand
                Toast.makeText(this, "Refreshing weather data...", Toast.LENGTH_SHORT)
                        .show();
                break;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            default:
                break;
        }

        return true;
    }

}
