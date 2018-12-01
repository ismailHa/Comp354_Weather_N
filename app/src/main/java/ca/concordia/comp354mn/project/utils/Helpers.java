package ca.concordia.comp354mn.project.utils;


import ca.concordia.comp354mn.project.enums.Season;
import ca.concordia.comp354mn.project.enums.WeatherCondition;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Helpers {

    /**
     * Convert from Fahrenheit to Celsius. Only one method implements conversion,
     * the other two just call it.
     *
     * @param tempF temperature in degrees Fahrenheit
     * @return temperature in degrees Celsius
     */
    public static Double fToC(Double tempF) {
        return ((tempF - 32.0) * 5.0/9.0);
    }

    /**
     * See Double fToC(Double tempF).
     * @param tempF temperature in degrees Fahrenheit as Integer
     * @return temperature in degrees Celsius
     */
    public static Double fToC(Integer tempF) {
        return fToC(Double.valueOf(tempF));
    }

    /**
     * See Double fToC(Double tempF).
     * @param tempF temperature in degrees Fahrenheit as String
     * @return temperature in degrees Celsius
     */
    public static Double fToC(String tempF) {
        return fToC(Double.valueOf(tempF));
    }

    /**
     * Returns an enumerated type representing the season of the given date. If
     * called with no parameters, returns season of today's date.
     *
     * @param d a Date object
     * @return Season (SPRING,SUMMER,FALL,WINTER)
     */
    public static Season getSeason(Date d) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);

        int month = calendar.get(Calendar.MONTH);

        Season season;

        switch(month) {

            case Calendar.DECEMBER:
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
            case Calendar.MARCH:
                season = Season.WINTER;
                break;

            case Calendar.APRIL:
            case Calendar.MAY:
                season = Season.SPRING;
                break;

            case Calendar.JUNE:
            case Calendar.JULY:
            case Calendar.AUGUST:
                season = Season.SUMMER;
                break;

            case Calendar.SEPTEMBER:
            case Calendar.OCTOBER:
                season = Season.FALL;
                break;

            default:
                season = Season.WINTER;
                break;
        }
        return season;

    }

    public static Season getSeason() {
        return getSeason(new Date());
    }

    /**
     *
     * Returns an enumerated value to represent the weather conditions based on
     * a given summary string. Used to change image on main page primarily.
     *
     * @param description textual weather description as provided from API
     * @return WeatherCondition (SNOW,RAIN,OVERCAST,CLEAR)
     */
    public static WeatherCondition getWeatherCondition(String description) {
        WeatherCondition weatherCondition;

        description = description.toLowerCase();

        if(description.contains("snow")) {
            weatherCondition = WeatherCondition.SNOW;
        } else if( description.contains("rain")) {
            weatherCondition = WeatherCondition.RAIN;
        } else if(description.contains("cloud")) {
            weatherCondition = WeatherCondition.OVERCAST;
        } else {
            weatherCondition = WeatherCondition.CLEAR;
        }

        return weatherCondition;

    }

    /**
     * Helper method to average out a list of numerical values
     * @param values a list of numbers of some kinds
     * @return the average of those numbers
     */
    public static <T extends Number> Double averageValues(List<T> values) {
        Double out = 0.0;
        for (T value : values) {
            out += value.doubleValue();
        }
        out /= values.size();
        return out;
    }


}
