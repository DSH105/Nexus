package com.dsh105.nexus.command.module.bukkit;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.bukkit.TenJavaDataLookupException;
import com.dsh105.nexus.exception.general.TimeDataLookupException;
import com.dsh105.nexus.util.StringUtil;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.ocpsoft.prettytime.PrettyTime;
import org.pircbotx.Colors;

import java.text.DecimalFormat;
import java.util.Date;

@Command(command = "tenjava", aliases = {"tj"}, needsChannel = false, help = "Get tenjava points",
        extendedHelp = {"{b}{p}{c}{/b} - View tenjava donated points"})

public class TenJavaCommand extends CommandModule {

    public static final String TEN_JAVA_URL = "http://tenjava.com/assets/data.json";
    private final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    public boolean onCommand(CommandPerformEvent event) {

            try {
                HttpResponse<JsonNode> jsonResponse = Unirest.get(TEN_JAVA_URL)
                        .header("accept", "application/json")
                        .asJson();
                int points = jsonResponse.getBody().getObject().getInt("points");
                long time = jsonResponse.getBody().getObject().getLong("last_update");
                PrettyTime pt = new PrettyTime();
                time = time * 1000;

                event.respond("Current points donated: " + Colors.BOLD + points + " ($" + df.format(points * 0.05) + " USD)"+ Colors.NORMAL + ". Last updated: " + Colors.BOLD + pt.format(new Date(time)) + Colors.BOLD + " http://tenjava.com/points");



            } catch (Exception e) {
                throw new TenJavaDataLookupException("An error occurred in the lookup process", e);
            }

        return true;
    }
}

