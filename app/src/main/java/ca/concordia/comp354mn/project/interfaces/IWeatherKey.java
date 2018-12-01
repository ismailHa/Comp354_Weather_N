package ca.concordia.comp354mn.project.interfaces;

import java.util.List;

/**
 *  Empty interface to be inherited by any enumerated type
 *  that lists keys for scraping from a given weather provider.
 *  Individual implementors override the toString() method of the
 *  required keys, so we can use a common (Facade) interface to multiple
 *  providers to access data that may be structured differently in the
 *  API response.
 *
 */
public interface IWeatherKey {
    /*
        Required keys:
        --------------
        TIME,
        APPARENT_TEMPERATURE
        HUMIDITY
        PRECIP_PROBABILITY
        SUMMARY
        TEMPERATURE
        UV_INDEX
     */

    String toString();
    IWeatherKey[] getKeys();

}
