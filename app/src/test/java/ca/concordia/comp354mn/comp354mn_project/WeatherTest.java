package ca.concordia.comp354mn.comp354mn_project;

import org.junit.Test;

import static org.junit.Assert.*;

public class WeatherTest {

    @Test
    public void stringOk() throws Exception {
        DarkskyWeatherProvider wp = new DarkskyWeatherProvider("ac2534b55814c01e69cbc494fe1a49b5");
        String s = wp.call(45.5020347, -73.7732866);
        System.out.println(s);
    }
}
