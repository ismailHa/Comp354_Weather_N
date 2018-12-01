package ca.concordia.comp354mn.project.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.preference.PreferenceManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import ca.concordia.comp354mn.project.network.DarkskyWeatherProvider;
import ca.concordia.comp354mn.project.interfaces.IDataStorage;
import ca.concordia.comp354mn.project.parsing.JsonDataParser;
import ca.concordia.comp354mn.project.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;

import java.text.DateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;
    SharedPreferences prefs;
    SharedPreferences.Editor prefsEditor;
    RetrieveWeatherTask retrieveWeatherTask;
    boolean setupComplete = false;
    Resources res;
    IDataStorage fileStorage;

    final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 1;
    final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;

    // Private internal class to query weather API data asynchronously
    private class RetrieveWeatherTask extends AsyncTask<Pair<Double,Double>,Void,DarkskyWeatherProvider> {
        private Exception exception;

        @Override
        @SafeVarargs
        protected final DarkskyWeatherProvider doInBackground(Pair<Double, Double>... pairs) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String apiKey = prefs.getString("pref_dark_sky_api","");

            DarkskyWeatherProvider ds = new DarkskyWeatherProvider(apiKey);
            try {
                ds.call(pairs[0].first,pairs[0].second);
            } catch(Exception e) {
                this.exception = e;
            }
            return ds;

        }

        protected void onPostExecute(DarkskyWeatherProvider ds) {

            JsonDataParser j = new JsonDataParser(ds.getAPIResponse());
            String currentTemp = j.getTemperature();
            String currentSummary = j.getSummary();

            TextView today_CurrentTemperature = (TextView) findViewById(R.id.card_Today_TV_Temperature);

            ProgressBar indeterminateProgressBar = (ProgressBar) findViewById(R.id.progressBar_indeterminate);


            String temperatureText = String.format(res.getString(R.string.card_today_TV_Temperature_Text), currentTemp, currentSummary);
//            View textViewId = findViewById(R.id.weather_info_text);
//            TextView tv1 = (TextView) textViewId;
//            JsonDataParser j = new JsonDataParser(ds.getAPIResponse());
//            String out = j.toString();
//            if(!out.isEmpty()) {
//                if(exception != null) {
//                    if(exception instanceof FileNotFoundException) {
//                        out = "Server refused. Check API key is correct.";
//                    } else {
//                        out = exception.toString();
//                    }
//                }
                today_CurrentTemperature.setText(temperatureText);

                indeterminateProgressBar.setVisibility(View.INVISIBLE);
                today_CurrentTemperature.setVisibility(View.VISIBLE);
//            }
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display layout
        setContentView(R.layout.activity_main);

        // Load preferences from persistent storage
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Load app resources
        res = getResources();

        setupLocation();

        // If API key is present, scrape for weather data
        if(prefs.contains("pref_dark_sky_api")) {
            if(retrieveWeatherTask == null) {
                retrieveWeatherTask = new RetrieveWeatherTask();
            }

            TextView today_CurrentTemperature = (TextView) findViewById(R.id.card_Today_TV_Temperature);
            today_CurrentTemperature.setVisibility(View.INVISIBLE);

            ProgressBar indeterminateProgressBar = (ProgressBar) findViewById(R.id.progressBar_indeterminate);
            indeterminateProgressBar.setVisibility(View.VISIBLE);

            Double latitude = Double.valueOf(prefs.getFloat("latitude",0.0f));
            Double longitude = Double.valueOf(prefs.getFloat("longitude",0.0f));
            Pair<Double,Double> position =  new Pair<Double,Double>(latitude,longitude);
            retrieveWeatherTask.execute(position);
        } else {
            Toast.makeText(this,"Missing Dark Sky API key.\n Please add it in Settings.",Toast.LENGTH_LONG).show();
        }

        populateGraph();


    }

    /**
     * Testing programmatically drawing to GraphView
     */
    void populateGraph() {
        GraphView graph = (GraphView) findViewById(R.id.card_Stats_GraphView);

        Calendar calendar = Calendar.getInstance();

        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        ArrayList<Date> lastThreeDays = new ArrayList<>(3);

        Date today = calendar.getTime();

        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterday = calendar.getTime();

        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date dayBeforeYesterday = calendar.getTime();

        lastThreeDays.add(today);
        lastThreeDays.add(yesterday);
        lastThreeDays.add(dayBeforeYesterday);

        ArrayList<Double> kmBiked = new ArrayList<Double>(
                Arrays.asList(
                        12.5,
                        5.9,
                        6.8
                ));

        BarGraphSeries<DataPoint> barGraph = new BarGraphSeries<>(new DataPoint[] {
            new DataPoint(lastThreeDays.get(0),kmBiked.get(0)),
            new DataPoint(lastThreeDays.get(1),kmBiked.get(1)),
            new DataPoint(lastThreeDays.get(2),kmBiked.get(2))
        });

        graph.addSeries(barGraph);

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this,df));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3);

        // set manual x bounds to have nice steps
        graph.getViewport().setMinX(dayBeforeYesterday.getTime());
        graph.getViewport().setMaxX(today.getTime());
        graph.getViewport().setXAxisBoundsManual(true);

        graph.getGridLabelRenderer().setHumanRounding(false);
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
            Log.i("COMP354","Location permission granted.");
        }

        // Tell LocationManager that our main activity is a listener for GPS updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 50, this);

    }

    // LocationListener Callbacks

    @Override
    public void onLocationChanged(Location location) {
        prefsEditor = prefs.edit();
        prefsEditor.putFloat("latitude", (float)location.getLatitude());
        prefsEditor.putFloat("longitude", (float) location.getLongitude());
        prefsEditor.apply();
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
    @SuppressWarnings("unchecked")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                Toast.makeText(this, "Refreshing weather data...", Toast.LENGTH_SHORT).show();
                retrieveWeatherTask = new RetrieveWeatherTask();
                Double latitude = Double.valueOf(prefs.getFloat("latitude",0.0f));
                Double longitude = Double.valueOf(prefs.getFloat("longitude",0.0f));
                retrieveWeatherTask.execute(new Pair<>(latitude,longitude));
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

}
