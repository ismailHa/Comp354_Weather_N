package ca.concordia.comp354mn.project.parsing;

import ca.concordia.comp354mn.project.enums.DSWeatherKey;
import ca.concordia.comp354mn.project.interfaces.IDataParser;
import ca.concordia.comp354mn.project.interfaces.IWeatherKey;
import org.json.JSONObject;
import java.util.*;


public class JsonDataParser implements IDataParser {

    final String currentWeatherKey = "currently";

    HashMap<String, String> currentWeatherKV = new HashMap<String,String>();
    JSONObject jsonReader;
    JSONObject currentWeatherJson;

    public JsonDataParser(String apiResponse) {
        this.parse(apiResponse);
    }

    protected void parse(String apiResponse) {
        try {
            jsonReader = new JSONObject(apiResponse);
            currentWeatherJson = jsonReader.getJSONObject(currentWeatherKey);

            // TODO // I'd like to find a way to allow generically using IWeatherKey
            // TODO // but I'm coming up dry. Any ideas?

            for(IWeatherKey key : DSWeatherKey.values()) {
                String s_key = key.toString();
                currentWeatherKV.put(s_key, currentWeatherJson.getString(s_key));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public HashMap<String, String> retrieveHashMap() {
        return currentWeatherKV;
    }

    public String getTemperature() { return currentWeatherKV.get("temperature");}
    public String getSummary() { return currentWeatherKV.get("summary");}


}
