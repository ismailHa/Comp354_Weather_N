package ca.concordia.comp354mn.project.interfaces;

import ca.concordia.comp354mn.project.enums.WeatherKey;

import java.util.HashMap;

/**
 *  Interface to implement parsing of data of disparate types
 */
public interface IDataParser {

    /**
     * Returns a Java-navigable key-value store of the parsed data.
     * @return HashMap<String,String>
     */
    HashMap<WeatherKey, String> retrieveHashMap();

}
