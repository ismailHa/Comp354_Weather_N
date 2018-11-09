package ca.concordia.comp354mn.comp354mn_project;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DarkskyWeatherProvider implements IWeatherProvider {

    public final String ADDRESS = "https://api.darksky.net/forecast/";
    private String apiKey;


    public DarkskyWeatherProvider(String apiKey) {

        this.apiKey = apiKey;
    }

    public String call(double latitude, double longitude) throws IOException {
        return call(latitude, longitude, null);
    }

    public String call(double latitude, double longitude, Integer time) throws IOException {

        String link = ADDRESS + apiKey + "/" + String.valueOf(latitude) + "," + String.valueOf(longitude);
        if (time != null)
            link += "," + time.toString();

        URL url = new URL(link);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        String jsonString;

        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            for (String line = r.readLine(); line != null; line = r.readLine())
                sb.append(line);
            jsonString = sb.toString();
            in.close();
        } finally {
            urlConnection.disconnect();
        }

        return jsonString;
    }


}
