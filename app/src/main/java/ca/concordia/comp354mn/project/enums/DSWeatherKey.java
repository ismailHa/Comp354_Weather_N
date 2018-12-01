package ca.concordia.comp354mn.project.enums;

import ca.concordia.comp354mn.project.interfaces.IWeatherKey;

import java.util.ArrayList;
import java.util.List;

public enum DSWeatherKey implements IWeatherKey {
            TIME {
                @Override
                public String toString() {
                    return "time";
                }
            },

            APPARENT_TEMPERATURE {
                @Override
                public String toString() {
                    return "apparentTemperature";
                }
            },

            HUMIDITY {
                @Override
                public String toString() {
                    return "humidity";
                }
            },

            PRECIP_PROBABILITY {
                @Override
                public String toString() {
                    return "precipProbability";
                }
            },

            SUMMARY {
                @Override
                public String toString() {
                    return "summary";
                }
            },

            TEMPERATURE {
                @Override
                public String toString() {
                    return "temperature";
                }
            },

            UV_INDEX {
                @Override
                public String toString() {
                    return "uvIndex";
                }
            };


//    public static List<String> getKeys() {
//        List<String> keys = new ArrayList<String>();
//        for(IWeatherKey key : DSWeatherKey.values()) {
//            keys.add(key.toString());
//        }
//        return keys;
//    }
    @Override
    public IWeatherKey[] getKeys() {
        return values();
    }
}
