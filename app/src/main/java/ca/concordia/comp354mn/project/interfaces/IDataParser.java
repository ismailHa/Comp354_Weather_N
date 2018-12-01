package ca.concordia.comp354mn.project.interfaces;

import java.util.HashMap;

/**
 *  Interface to implement parsing of data of disparate types
 */
public interface IDataParser {

    /**
     * Returns a Java-navigable key-value store of the parsed data.
     * @return HashMap<String,String>
     */
    public HashMap<String, String> retrieveHashMap();

}
