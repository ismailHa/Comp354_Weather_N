package ca.concordia.comp354mn.comp354mn_project;

import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class JsonDataParser implements IDataParser {

    HashMap<String, String> importedData;
    JSONObject jsonObj;

    public JsonDataParser() {

    }

    public void parse(String jsonString) {
        try {
            jsonObj = new JSONObject(jsonString);

        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}
