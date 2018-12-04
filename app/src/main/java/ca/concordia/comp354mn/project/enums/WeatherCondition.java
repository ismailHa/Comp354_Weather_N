package ca.concordia.comp354mn.project.enums;

public enum WeatherCondition {
    CLEAR("clear"),
    OVERCAST("overcast"),
    RAIN("rain"),
    SNOW("snow");

    private String name;
    WeatherCondition(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
