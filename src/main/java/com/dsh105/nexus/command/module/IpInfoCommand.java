package com.dsh105.nexus.command.module;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Command(command = "ipinfo", aliases = {"ip"}, needsChannel = false, help = "Looks up IP information.",
        extendedHelp = {"Gives approximate location, country & ISP information."})
public class IpInfoCommand extends CommandModule {

    public static final String API_URL = "http://www.telize.com/geoip/";

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        String[] args = event.getArgs();
        if (args.length != 1 || (args[0].length() < 7)) {
            return false;
        }

        String url = null;

        try {
            url = API_URL + URLEncoder.encode(args[0], "UTF-8");
            System.out.print(url);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            HttpResponse<JsonNode> resp = Unirest.get(url).asJson();

            if (resp.getCode() != 200) {
                return false;
            }

            StringBuilder builder = new StringBuilder();
            final JSONObject object = resp.getBody().getObject();
            String maps = "No maps available.";

            if (object.has("latitude")) {
                maps = "https://maps.google.com/maps?q=" + object.get("latitude") + "," + object.get("longitude");
            }

            String info = "Info for " + args[0] + ": ";
            builder.append(info);

            if (object.has("country")) {
                builder.append("Country: ").append(object.getString("country")).append(" ");
            }

            if (object.has("isp")) {
                builder.append("ISP: ").append(object.getString("isp")).append(" ");
            }

            event.respond(builder.toString().trim());
            event.respond(maps);

        } catch (UnirestException e) {
            return false;
        }
        return true;
    }
}
