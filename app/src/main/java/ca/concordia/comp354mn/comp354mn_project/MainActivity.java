package ca.concordia.comp354mn.comp354mn_project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import android.os.AsyncTask;


import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;


public class MainActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;
    private boolean doPrintDebugLogcat = true;
    SharedPreferences prefs;
    Double longitude, latitude;
    RetrieveWeatherTask retrieveWeatherTask;


    final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 1;
    final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;

    private class RetrieveWeatherTask extends AsyncTask<Pair<Double,Double>,Void,DarkskyWeatherProvider> {
        final String apiKey = "ac2534b55814c01e69cbc494fe1a49b5";
        private Exception exception;

        @Override
        protected DarkskyWeatherProvider doInBackground(Pair<Double, Double>... pairs) {
            DarkskyWeatherProvider ds = new DarkskyWeatherProvider(apiKey);
            try {
                ds.call(pairs[0].first,pairs[0].second);
            } catch(Exception e) {
                this.exception = e;
                return null;
            }
            return ds;

        }

        protected void onPostExecute(DarkskyWeatherProvider ds) {

            View textViewId = findViewById(R.id.weather_info_text);
            TextView tv1 = (TextView) textViewId;
            JsonDataParser j = new JsonDataParser(ds.getJsonString());
            String out = j.toString();
            if(!out.isEmpty()) {
                if(exception != null) {
                    out = exception.toString();
                }
                tv1.setText(out);
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display layout
        setContentView(R.layout.activity_main);

        // Load preferences from persistent storage
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setupLocation();

        // Hard coding this stuff for demo purposes
        if(latitude == null) {
            Log.e("COMP354", "Lat/Long not set!");
            latitude = 45.5017;
            longitude = 73.5673;
        }

        if(retrieveWeatherTask == null) {
            retrieveWeatherTask = new RetrieveWeatherTask();
        }

        retrieveWeatherTask.execute(new Pair<>(latitude,longitude));

    }

    void setupLocation() {
        // Initialize LocationManager instance
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Check that we actually have permission to access location data
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            Toast.makeText(this, "No location permission, requesting...", Toast.LENGTH_SHORT).show();

            // Request coarse location availability
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, MY_PERMISSIONS_REQUEST_COARSE_LOCATION);
            // Request fine location availability
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            printDebugLogcat("Location permission granted.");
        }

        // Tell LocationManager that our main activity is a listener for GPS updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 50, this);
    }

    // LocationListener Callbacks

    @Override
    public void onLocationChanged(Location location) {
          latitude = location.getLatitude();
          longitude = location.getLongitude();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.e("COMP354", "Location provider disabled!");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i("COMP354", "Location provider enabled!");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //
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
                Toast.makeText(this, "Refreshing weather data...", Toast.LENGTH_SHORT).show();
                if(retrieveWeatherTask != null) {
                    retrieveWeatherTask.execute(new Pair<>(latitude,longitude));
                } else {
                    Toast.makeText(this, "Error retrieving updated weather data.", Toast.LENGTH_SHORT).show();
                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "Need your coarse location!", Toast.LENGTH_SHORT).show();
                }

                break;
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "Need your fine location!", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    private void printDebugLogcat(String log) {
        if(this.doPrintDebugLogcat) {
            System.out.println(log);
        }
    }

}
