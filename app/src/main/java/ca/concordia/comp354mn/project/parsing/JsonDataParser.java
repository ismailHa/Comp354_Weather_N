package ca.concordia.comp354mn.project.parsing;

import ca.concordia.comp354mn.project.interfaces.IDataParser;
import org.json.JSONObject;
import java.util.*;
import java.text.*;

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
            for(String key : currentWeatherSubKeys) {
                currentWeatherKV.put(key, currentWeatherJson.getString(key));
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

//    public String toString() {
//        Iterator it = currentWeatherKV.entrySet().iterator();
//        StringBuilder sb = new StringBuilder();
//        sb.append("Hello!\n\n");
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
//            String key = (String)pair.getKey();
//            String value = (String)pair.getValue();
//
//            // dirty, dirty handling for getting epoch time in local TZ
//            // to fix, date output is wrong
//            if(key.equals("time")) {
//                Long epochTime = Long.parseLong((String)pair.getValue());
//                Date date = new Date(epochTime);
//                DateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//                dateFmt.setTimeZone(TimeZone.getTimeZone("America/New York"));
//                String formattedDate = dateFmt.format(date);
//                sb.append("The last scrape time was " + formattedDate + ".\n");
//                it.remove();
//                continue;
//            }
//
//
//            String keyCapitalized = StringUtils.capitalize(key);
//            sb.append("The " + keyCapitalized + " is " + value + ".\n");
//            it.remove();
//        }
//        sb.append("\n");
//        sb.append("What now?");
//
//        return sb.toString();
//
//    }

}