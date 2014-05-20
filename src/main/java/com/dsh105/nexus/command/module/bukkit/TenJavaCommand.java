package com.dsh105.nexus.command.module.bukkit;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.bukkit.TenJavaDataLookupException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.ocpsoft.prettytime.PrettyTime;
import org.pircbotx.Colors;

import java.util.Date;

@Command(command = "tenjava", aliases = {"tj", "ten.java"}, needsChannel = false, help = "Get points donated to ten.java",
        extendedHelp = {"{b}{p}{c}{/b} - View the amount of points donated to ten.java"})

public class TenJavaCommand extends CommandModule {

    public static final String TEN_JAVA_URL = "http://tenjava.com/assets/data.json";

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

                event.respondWithPing("Current points donated to ten.java: {0} (${1} USD)" + Colors.NORMAL + ". Last updated: {2}", String.valueOf(points), String.valueOf(points * 0.05), pt.format(new Date(time)));
            } catch (Exception e) {
                throw new TenJavaDataLookupException("An error occurred in the lookup process", e);
            }

        return true;
    }
}

