package ca.concordia.comp354mn.project.network;

import ca.concordia.comp354mn.project.interfaces.IWeatherProvider;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class DarkskyWeatherProvider implements IWeatherProvider {

    public final String ADDRESS = "https://api.darksky.net/forecast/";
    private String apiKey = "";
    private String jsonString = "";

    public DarkskyWeatherProvider(String apiKey) {

        this.apiKey = apiKey;
    }

    public void call(double latitude, double longitude) throws IOException {
        call(latitude, longitude, null);
    }

    public void call(double latitude, double longitude, Integer time) throws IOException {

        if(apiKey.isEmpty()) {
            throw new IOException("API key not set!");
        }

        String link = ADDRESS + apiKey + "/" + String.valueOf(latitude) + "," + String.valueOf(longitude);
        if (time != null)
            link += "," + time.toString();

        URL url = new URL(link);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {

            if(! (200 == urlConnection.getResponseCode())) {
                throw new IOException("Connection refused! Check API key is correct.");
            }


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

    }

    public String getAPIResponse() {
        return jsonString;
    }
}

