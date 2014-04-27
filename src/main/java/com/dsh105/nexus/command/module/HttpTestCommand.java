package com.dsh105.nexus.command.module;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

@Command(command = "httptest", needsChannel = false)
public class HttpTestCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        /**
         * This is a very simple example of how to use UniRest with a JSON
         * endpoint and extract some data. Obviously, if this were a real
         * command you wouldn't be hardcoding user agents or handling exceptions
         * like this. You'd also be doing this all async.
         */
        String url = "http://httpbin.org/user-agent";
        try {
            Unirest.setDefaultHeader("User-Agent", "Nexus/v1.0 (by DSH105)");
            HttpResponse<JsonNode> jsonResponse = Unirest.get(url)
                    .asJson();
            event.respond(jsonResponse.getBody().getObject().getString("user-agent"));
        } catch (UnirestException e) {
            e.printStackTrace();
            event.respond("Oh no! Something went wrong: " + e.getMessage());
        }
        return true;
    }

    @Override
    public String getHelp() { return "Tests a simple HTTP request."; }
}
