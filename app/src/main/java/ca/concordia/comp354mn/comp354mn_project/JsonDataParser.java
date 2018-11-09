package ca.concordia.comp354mn.comp354mn_project;

import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;
import java.text.*;

import java.util.*;
import org.apache.commons.lang3.StringUtils;

public class JsonDataParser implements IDataParser {

    final String currentWeatherKey = "currently";
    // Should be replaced with an enum most likely
    final List<String> currentWeatherSubKeys = Arrays.asList(
            "time",
            "apparentTemperature",
            "humidity",
            "precipProbability",
            "summary",
            "temperature",
            "uvIndex"
    );

    HashMap<String, String> currentWeatherKV;
    JSONObject jsonReader;
    JSONObject currentWeatherJson;

    public JsonDataParser(String jsonString) {
        currentWeatherKV = new HashMap<String,String>();
        this.parse(jsonString);
    }

    protected void parse(String jsonString) {
        try {
            jsonReader = new JSONObject(jsonString);
            currentWeatherJson = jsonReader.getJSONObject(currentWeatherKey);
            for(String key : currentWeatherSubKeys) {
                currentWeatherKV.put(key, currentWeatherJson.getString(key));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    HashMap<String, String> retrieveCurrent() {
        return currentWeatherKV;
    }

    public String toString() {
        Iterator it = currentWeatherKV.entrySet().iterator();
        StringBuilder sb = new StringBuilder();
        sb.append("Hello!\n\n");
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            String key = (String)pair.getKey();
            String value = (String)pair.getValue();

            // dirty, dirty handling for getting epoch time in local TZ
            // to fix, date output is wrong
            if(key.equals("time")) {
                Long epochTime = Long.parseLong((String)pair.getValue());
                Date date = new Date(epochTime);
                DateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                dateFmt.setTimeZone(TimeZone.getTimeZone("America/New York"));
                String formattedDate = dateFmt.format(date);
                sb.append("The last scrape time was " + formattedDate + ".\n");
                it.remove();
                continue;
            }




            String keyCapitalized = StringUtils.capitalize(key);
            sb.append("The " + keyCapitalized + " is " + value + ".\n");
            it.remove();
        }
        sb.append("\n");
        sb.append("What now?");

        return sb.toString();

    }

}
