package com.dsh105.nexus.command.module.general;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.general.WeatherLookupException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONArray;
import org.json.JSONException;
import org.pircbotx.Colors;

@Command(command = "weather",
        aliases = {"wea", "sun"},
        needsChannel = false,
        help = "Get a location's weather",
        extendedHelp = {
                "{b}{p}{c}{/b} <location> - Allows you to get the weather of a location"
        })
public class WeatherCommand extends CommandModule {
    public static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?q=";

    public static double kToC(double k) {
        k = k - 273.15;
        return k;
    }

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length >= 1) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < event.getArgs().length; i++) {
                if (i != 0) {
                    b.append(" ");
                }
                b.append(event.getArgs()[i]);
            }
            try {
                HttpResponse<JsonNode> jsonResponse = Unirest.get(WEATHER_URL + b.toString())
                        .header("accept", "application/json")
                        .asJson();
                boolean working;
                try {
                    String message = jsonResponse.getBody().getObject().getString("message");
                    working = false;
                } catch (JSONException e) {
                    // if message field exists, error has occured
                    // if message field doesn't exist, all is grand throughout the land
                    working = true;
                }
                if (!working) {
                    event.errorWithPing("Location not found");
                } else {
                    double temp = jsonResponse.getBody().getObject().getJSONObject("main").getDouble("temp");
                    temp = kToC(temp);
                    temp = Math.round(temp * 10) / 10;
                    String name = jsonResponse.getBody().getObject().getString("name");
                    String location;
                    if (!name.equals("")) {
                        location = name + ", " + jsonResponse.getBody().getObject().getJSONObject("sys").getString("country");
                    } else {
                        location = jsonResponse.getBody().getObject().getJSONObject("sys").getString("country");
                    }
                    double humidity = jsonResponse.getBody().getObject().getJSONObject("main").getDouble("humidity");
                    double windspeed = jsonResponse.getBody().getObject().getJSONObject("wind").getDouble("speed");
                    JSONArray desc = jsonResponse.getBody().getObject().getJSONArray("weather");
                    String description = desc.getJSONObject(0).getString("description");
                    String colour;
                    if (temp < 20) {
                        colour = Colors.BLUE;
                    } else if (temp >= 20 && temp < 30) {
                        colour = Colors.OLIVE;
                    } else {
                        colour = Colors.RED;
                    }
                    description = description.substring(0, 1).toUpperCase() + description.substring(1);
                    event.respond("Weather in " + Colors.BOLD + location + ":");
                    event.respond("Temp: " + Colors.BOLD + colour + temp + "°C" + Colors.NORMAL + " - " + "Humidity: " + Colors.BOLD + humidity + Colors.NORMAL + " - " + "Wind speed: " + Colors.BOLD + windspeed + Colors.NORMAL + " - " + "Description: " + Colors.BOLD + description);

                    return true;
                }
            } catch (Exception e) {
                throw new WeatherLookupException("An error occurred in the lookup process", e);
            }
        } else {
            return false;
        }
        return true;
    }
}
