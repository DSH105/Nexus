package com.dsh105.nexus.command.module.general;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.general.TimeDataLookupException;
import com.dsh105.nexus.exception.general.WeatherLookupException;
import com.dsh105.nexus.util.StringUtil;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.pircbotx.Colors;

import java.text.SimpleDateFormat;

@Command(command = "time", aliases = {}, needsChannel = false, help = "Get a location's time",
        extendedHelp = {"{b}{p}{c}{/b} <location> - Allows you to get the time of a location"})

public class TimeCommand extends CommandModule {

    public static final String GOOGLE_COORDS_URL = "http://maps.googleapis.com/maps/api/geocode/json?sensor=false&address=";
    public static final String TIME_URL = "http://www.earthtools.org/timezone-1.1/";

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length > 0) {
            String args = StringUtil.combineSplit(0, event.getArgs(), " ");
            try {
                HttpResponse<JsonNode> jsonResponse = Unirest.get(GOOGLE_COORDS_URL + args)
                        .header("accept", "application/json")
                        .asJson();
                JSONArray response = jsonResponse.getBody().getObject().getJSONArray("results");
                if (!jsonResponse.getBody().getObject().getString("status").equalsIgnoreCase("OK")) {
                    event.errorWithPing("Invalid request");
                } else {
                    double lat = response.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                    double lng = response.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                    String loc = response.getJSONObject(0).getString("formatted_address");
                    Document doc = Jsoup.connect(TIME_URL + lat + "/" + lng).get();
                    Element timeEl = doc.select("localtime").first();
                    String time = timeEl.text();
                    event.respond("Time in " + Colors.BOLD + loc + ": " + time);
                    return true;
                }
            } catch (Exception e) {
                throw new TimeDataLookupException("An error occurred in the lookup process", e);
            }
        } else {
            return false;
        }
        return true;
    }
}

