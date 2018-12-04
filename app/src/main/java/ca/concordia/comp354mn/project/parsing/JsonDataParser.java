package ca.concordia.comp354mn.project.parsing;

import ca.concordia.comp354mn.project.enums.WeatherKey;
import ca.concordia.comp354mn.project.interfaces.IDataParser;
import org.json.JSONObject;
import java.util.*;


public class JsonDataParser implements IDataParser {

    private static final String TAG = "JsonDataParser";
    private final String currentWeatherKey = "currently";

    HashMap<WeatherKey, String> currentWeatherKV = new HashMap<>();
    JSONObject jsonReader;
    JSONObject currentWeatherJson;

    public JsonDataParser(String apiResponse) {
        this.parse(apiResponse);
    }

    protected void parse(String apiResponse) {
        try {
            jsonReader = new JSONObject(apiResponse);
            currentWeatherJson = jsonReader.getJSONObject(currentWeatherKey);

            for(WeatherKey key : WeatherKey.values()) {
                currentWeatherKV.put(key, currentWeatherJson.getString(key.getValue()));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public HashMap<WeatherKey, String> retrieveHashMap() {
        return currentWeatherKV;
    }

//    public String getTemperature() { return currentWeatherKV.get(WeatherKey.TEMPERATURE);}
//    public String getSummary() { return currentWeatherKV.get("summary");}


}
