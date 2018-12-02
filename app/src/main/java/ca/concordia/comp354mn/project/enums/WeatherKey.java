package ca.concordia.comp354mn.project.enums;

public enum WeatherKey {

    TIME("time"),
    APPARENT_TEMPERATURE("apparentTemperature"),
    HUMIDITY("humidity"),
    PRECIP_PROBABILITY("precipProbability"),
    SUMMARY("summary"),
    TEMPERATURE("temperature"),
    UV_INDEX("uvIndex"),
    WIND_SPEED("windSpeed");

    private String name;

    private WeatherKey(String name) {
        this.name = name;
    }

    public String getValue() {
        return name;
    }

}
