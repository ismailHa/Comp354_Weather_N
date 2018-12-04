package ca.concordia.comp354mn.project.ui;

// Java stdlib imports

import java.io.File;
import java.text.DateFormat;
import java.util.*;

// Android OS imports
import android.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.*;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.*;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.*;
import android.location.*;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.*;
import android.view.*;
import android.os.*;
import android.widget.*;

// GraphView imports

import ca.concordia.comp354mn.project.enums.Season;
import ca.concordia.comp354mn.project.enums.WeatherCondition;
import ca.concordia.comp354mn.project.persistence.GDriveStorage;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.*;
import com.jjoe64.graphview.helper.*;

// Project imports

import ca.concordia.comp354mn.project.enums.WeatherKey;
import ca.concordia.comp354mn.project.network.DarkskyWeatherProvider;
import ca.concordia.comp354mn.project.parsing.JsonDataParser;
import ca.concordia.comp354mn.project.R;
import ca.concordia.comp354mn.project.utils.*;


public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "MainActivity";

    private LocationManager locationManager;
    private ConnectivityManager connectivityManager;
    private Resources res;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefsEditor;

    private GDriveStorage gdrive;

    // VIEWS
    GraphView graph;
    ProgressBar indeterminateProgressBar;
    ImageView iv_TodayCurrentWeather;
    TextView tv_TodayCurrentTemperature;
    TextView tv_TodayPrecipWarning;
    TextView tv_TodayWindWarning;

    final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 1;
    final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;

    /**
     * Private internal class to query weather API data asynchronously
     */
    private class RetrieveWeatherTask extends AsyncTask<Pair<Double,Double>,Void,DarkskyWeatherProvider> {
        private Exception exception;
        Boolean success = false;

        @Override
        @SafeVarargs
        protected final DarkskyWeatherProvider doInBackground(Pair<Double, Double>... pairs) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String apiKey = prefs.getString("pref_dark_sky_api","");

            DarkskyWeatherProvider ds = new DarkskyWeatherProvider(apiKey);
            try {
                ds.call(pairs[0].first,pairs[0].second);
                success = true;
            } catch(Exception e) {
                this.exception = e;
                success = false;
            }
            return ds;

        }

        protected void onPostExecute(DarkskyWeatherProvider ds) {

            if(success) {
                JsonDataParser j = new JsonDataParser(ds.getAPIResponse());
                HashMap<WeatherKey,String> weatherData = j.retrieveHashMap();
                updateWeatherViews(weatherData);
            } else {
                Toast.makeText(App.getAppContext(),"Error communicating with API.",Toast.LENGTH_LONG).show();
            }

        }
    }

    /**
     * Private internal class to get saved JSON files from Google Drive
     */
    private class RetrieveHistory extends AsyncTask<Void,Void,ArrayList<String>> {

        @Override
        @SafeVarargs
        protected final ArrayList<String> doInBackground(Void... v) {

            return gdrive.getFileList(".json");

        }

        @SuppressWarnings("unchecked")
        protected void onPostExecute(ArrayList<String> files) {

            if(files.size() != 0) {
                ProcessHistoricalJson p = new ProcessHistoricalJson();
                p.execute(files);
            }

        }
    }

    /**
     *  Private internal class to send the JSON files from drive to parser
     */
    private class ProcessHistoricalJson extends AsyncTask<ArrayList<String>,Void,ArrayList<HashMap<WeatherKey,String>>> {

        @Override
        @SafeVarargs
        protected final ArrayList<HashMap<WeatherKey,String>> doInBackground(ArrayList<String>... jsondata) {

            ArrayList<HashMap<WeatherKey,String>> results = new ArrayList<>();

                for(ArrayList<String> jsonContents : jsondata) {
                    if(jsonContents.size() != 0) {
                        results.add(new JsonDataParser(jsonContents.get(0)).retrieveHashMap());
                    }
                }

            return results;
        }

        @Override
        protected final void onPostExecute(ArrayList<HashMap<WeatherKey, String>> hashMaps) {
            Log.e(TAG,"hashmaps length: " + hashMaps.size());
            updateGraphCard(hashMaps);
        }
    }

    // TODO doesn't actually do anything but now has access to historical data
    // scraped from gdrive
    private void updateGraphCard(ArrayList<HashMap<WeatherKey,String>> hashMaps) {
        Log.e(TAG,"Running updateGraphCard");

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

        ArrayList<Double> kmBiked = new ArrayList<>(
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


    @SuppressWarnings("unchecked")
    public void updateWeather(Double latitude, Double longitude) {
//        retrieveWeatherTask = new RetrieveWeatherTask();
        Pair<Double,Double> position =  new Pair<Double,Double>(latitude,longitude);
        new RetrieveWeatherTask().execute(position);
    }

    public void updateWeatherViews(HashMap<WeatherKey,String> weatherData) {
        Double currentTempCelsius = Helpers.fToC(weatherData.get(WeatherKey.TEMPERATURE));
        String currentSummary = weatherData.get(WeatherKey.SUMMARY);

        //Update the weather description text
        String temperatureText = String.format(res.getString(R.string.card_today_TV_Temperature_Text), currentTempCelsius, currentSummary.toLowerCase());

        // Figure out what weather conditions we should display.
        Season s = Helpers.getSeason();
        WeatherCondition w = Helpers.getWeatherCondition(currentSummary);

        String imgName = Helpers.determineWeatherImage(s,w);
        Context appContext = App.getAppContext();
        int imgToDisplay = appContext.getResources().getIdentifier(imgName, "drawable", appContext.getPackageName());


        // Determine if there are any extra warnings we should emit.

        Boolean warnPrecip = false;
        Boolean warnWind = false;

        Double precipPercentage = Double.valueOf(weatherData.get(WeatherKey.PRECIP_PROBABILITY)) * 100;
        Double windSpeed = Helpers.milesToKm(weatherData.get(WeatherKey.WIND_SPEED));

        if(precipPercentage > 25) {
            warnPrecip = true;
            String warnBanner = res.getString(R.string.warning);
            String precipWarning = String.format(res.getString(R.string.card_Today_TV_WarnPrecip_Text), precipPercentage);
            Spannable spannablePrecipWarning = new SpannableString(warnBanner + precipWarning);

            spannablePrecipWarning.setSpan(new ForegroundColorSpan(Color.RED),
                    0,
                    (warnBanner + precipWarning).length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


            tv_TodayPrecipWarning.setText(spannablePrecipWarning);

        }
        if(windSpeed > 10) {
            warnWind = true;
            String warnBanner = res.getString(R.string.warning);
            String windWarning = String.format(res.getString(R.string.card_Today_TV_WarnWind_Text), windSpeed);
            Spannable spannableWindWarning = new SpannableString(warnBanner + windWarning);

            spannableWindWarning.setSpan(new ForegroundColorSpan(Color.RED),
                    0,
                    (warnBanner + windWarning).length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            tv_TodayWindWarning.setText(spannableWindWarning);
        }

        // Finally, display everything

        tv_TodayCurrentTemperature.setText(temperatureText);
        iv_TodayCurrentWeather.setImageResource(imgToDisplay);

        indeterminateProgressBar.setVisibility(View.INVISIBLE);
        tv_TodayCurrentTemperature.setVisibility(View.VISIBLE);
        iv_TodayCurrentWeather.setVisibility(View.VISIBLE);

        if(warnPrecip) {
            tv_TodayPrecipWarning.setVisibility(View.VISIBLE);
        }

        if(warnWind) {
            tv_TodayWindWarning.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if we have an active network connection.
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isLinkUp = ((activeNetwork != null) && (activeNetwork.isConnectedOrConnecting()));

        // If API key is present, scrape for weather data
        if (isLinkUp) {
            if (prefs.contains("pref_dark_sky_api")) {

                tv_TodayCurrentTemperature.setVisibility(View.INVISIBLE);
                indeterminateProgressBar.setVisibility(View.VISIBLE);

                Double latitude = Double.valueOf(prefs.getFloat("latitude", 0.0f));
                Double longitude = Double.valueOf(prefs.getFloat("longitude", 0.0f));
                updateWeather(latitude, longitude);

            } else {
                Toast.makeText(this, "Missing Dark Sky API key.\n Please add it in Settings.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No network connection.", Toast.LENGTH_LONG).show();
        }

        setupYouCard();

        RetrieveHistory r = new RetrieveHistory();
        r.execute();
    }

    /**
     * GDrive doesn't let you access files via the API unless you created them or
     * have previously edited them. So this is a hack to get around that, so we can
     * have the required 20 data points
     */
    void jankyUploadFiles() {

        String[] filenames = getFilesDir().list();

        for (String filename : filenames) {
            String st = "";
            try {
                Scanner sc = new Scanner(new File(getFilesDir(), filename));

                while (sc.hasNextLine())
                    st += sc.nextLine();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
//            Log.e(TAG, st);
        gdrive.write(filename,st);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display layout
        setContentView(R.layout.activity_main);

        // Load preferences from persistent storage
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Load app resources
        res = getResources();

        // Handles to various resources
        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Get handles to Views

        graph = (GraphView) findViewById(R.id.card_Stats_GraphView);
        tv_TodayCurrentTemperature = (TextView) findViewById(R.id.card_Today_TV_Temperature);
        indeterminateProgressBar = (ProgressBar) findViewById(R.id.progressBar_indeterminate);
        iv_TodayCurrentWeather = (ImageView)findViewById(R.id.card_Today_IV_CurrentWeather);
        tv_TodayPrecipWarning = (TextView) findViewById(R.id.card_Today_TV_PrecipWarning);
        tv_TodayWindWarning = (TextView) findViewById(R.id.card_Today_TV_WindWarning);

        setupLocation();
        gdrive = new GDriveStorage();

    }


    /**
     *  Testing display of summary statistics for a user
     */
    void setupYouCard() {
        List<ListItem> items = Arrays.asList(new ListItem("User","Chris"),
                                             new ListItem("Total km cycled","5"),
                                             new ListItem("Average km daily","2.3"));
          TwoLineArrayAdapter ar = new TwoLineArrayAdapter(this,items);
        ListView you_Items = (ListView) findViewById(R.id.card_You_ListView);
        you_Items.setAdapter(ar);
    }



    void setupLocation() {

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
        Log.e(TAG, "Location provider disabled!");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, "Location provider enabled!");
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
                Double latitude = Double.valueOf(prefs.getFloat("latitude",0.0f));
                Double longitude = Double.valueOf(prefs.getFloat("longitude",0.0f));
                updateWeather(latitude,longitude);
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
