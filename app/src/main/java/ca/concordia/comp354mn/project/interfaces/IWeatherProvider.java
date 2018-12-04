package ca.concordia.comp354mn.project.interfaces;

import java.io.IOException;

public interface IWeatherProvider {


    /**
     * Request current data from Weather API provider at a given position
     * @param latitude   GPS-derived or user-specified latitude
     * @param longitude  GPS-derived or user-specified longitude
     *
     * @throws IOException If an error is encountered communicating with API (e.g. wrong API key, network down)
     */
    void call(double latitude, double longitude) throws IOException;

    /**
     * Request historical data from Weather API provider at a given position and time
     * @param latitude  GPS-derived or user-specified latitude
     * @param longitude GPS-derived or user-specified longitude
     * @param time      timestamp for requested weather info
     *
     * @throws IOException If an error is encountered communicating with API (e.g. wrong API key, network down)
     */
    public void call(double latitude, double longitude, Integer time) throws IOException;

    /**
     * Once data has been retrieved from the network & API, return as a
     * structured representation to be fed to a IDataParser implementor
     *
     * @return Stringified data response from HTTP/API request
     */
    public String getAPIResponse();
}
