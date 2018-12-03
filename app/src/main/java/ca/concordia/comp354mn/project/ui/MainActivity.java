package ca.concordia.comp354mn.project.ui;

// Java stdlib imports

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
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.*;
import com.jjoe64.graphview.helper.*;

// Project imports

import ca.concordia.comp354mn.project.enums.WeatherKey;
import ca.concordia.comp354mn.project.network.DarkskyWeatherProvider;
import ca.concordia.comp354mn.project.interfaces.IDataStorage;
import ca.concordia.comp354mn.project.parsing.JsonDataParser;
import ca.concordia.comp354mn.project.R;
import ca.concordia.comp354mn.project.utils.*;


public class MainActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;
    private ConnectivityManager connectivityManager;
    SharedPreferences prefs;
    SharedPreferences.Editor prefsEditor;
    RetrieveWeatherTask retrieveWeatherTask;
    boolean setupComplete = false;
    Resources res;
    IDataStorage fileStorage;
    GoogleSignInClient m_GoogleSignInClient;
    DriveClient m_DriveClient;
    DriveResourceClient m_DriveResourceClient;

    final int MY_PERMISSIONS_REQUEST_COARSE_LOCATION = 1;
    final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;

    // Private internal class to query weather API data asynchronously
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

                Double currentTempCelsius = Helpers.fToC(weatherData.get(WeatherKey.TEMPERATURE));
                String currentSummary = weatherData.get(WeatherKey.SUMMARY);

                TextView today_CurrentTemperature = (TextView) findViewById(R.id.card_Today_TV_Temperature);
                ProgressBar indeterminateProgressBar = (ProgressBar) findViewById(R.id.progressBar_indeterminate);
                ImageView today_CurrentWeatherImg = (ImageView)findViewById(R.id.card_Today_IV_CurrentWeather);


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

                TextView today_PrecipWarning = (TextView) findViewById(R.id.card_Today_TV_PrecipWarning);
                TextView today_WindWarning = (TextView) findViewById(R.id.card_Today_TV_WindWarning);

                if(precipPercentage > 25) {
                    warnPrecip = true;
                    String warnBanner = res.getString(R.string.warning);
                    String precipWarning = String.format(res.getString(R.string.card_Today_TV_WarnPrecip_Text), precipPercentage);
                    Spannable spannablePrecipWarning = new SpannableString(warnBanner + precipWarning);

                    spannablePrecipWarning.setSpan(new ForegroundColorSpan(Color.RED),
                            0,
                            (warnBanner + precipWarning).length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


                    today_PrecipWarning.setText(spannablePrecipWarning);

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

                    today_WindWarning.setText(spannableWindWarning);
                }

                // Finally, display everything

                today_CurrentTemperature.setText(temperatureText);
                today_CurrentWeatherImg.setImageResource(imgToDisplay);

                indeterminateProgressBar.setVisibility(View.INVISIBLE);
                today_CurrentTemperature.setVisibility(View.VISIBLE);
                today_CurrentWeatherImg.setVisibility(View.VISIBLE);

                if(warnPrecip) {
                    today_PrecipWarning.setVisibility(View.VISIBLE);
                }

                if(warnWind) {
                    today_WindWarning.setVisibility(View.VISIBLE);
                }

            } else {
                Toast.makeText(App.getAppContext(),"Error communicating with API.",Toast.LENGTH_LONG).show();
            }

        }
    }

    private GoogleSignInClient buildGoogleSignInClient() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        Log.e("COMP354-GDrive","Running buildGoogleSignInClient");
        return GoogleSignIn.getClient(this, signInOptions);
    }

    private void updateViewWithGoogleSignInAccountTask(Task<GoogleSignInAccount> task) {
        Log.i("COMP354-GDrive", "Update view with sign in account task");
        task.addOnSuccessListener(
                new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        Log.i("COMP354-GDrive", "Sign in success");
                        // Build a drive client.
                        m_DriveClient = Drive.getDriveClient(getApplicationContext(), googleSignInAccount);
                        // Build a drive resource client.
                        m_DriveResourceClient =
                                Drive.getDriveResourceClient(getApplicationContext(), googleSignInAccount);

                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("COMP354-GDrive", "Sign in failed", e);
                            }
                        });
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

//        GoogleSignInClient.getGoogleSignInAccountFromIntent();

        setupLocation();

        // Check if we have an active network connection.

        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isLinkUp = ((activeNetwork != null) && (activeNetwork.isConnectedOrConnecting()));


        // If API key is present, scrape for weather data
        if(isLinkUp) {
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
        } else {
            Toast.makeText(this,"No network connection.",Toast.LENGTH_LONG).show();
        }


        populateGraph();

        setupYouCard();

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
