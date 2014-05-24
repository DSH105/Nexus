package com.dsh105.nexus.command.module.minecraft;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.exception.general.GenericUrlConnectionException;
import com.dsh105.nexus.util.JsonUtil;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.pircbotx.Colors;


@Command(command = "mcuser",
        aliases = {"minecraftuser"},
        needsChannel = false,
        help = "View information about a specific Minecraft username.",
        extendedHelp = {
                "{b}{p}{c}{/b} <usernane>- View information about the Minecraft username."
        })
public class MinecraftUserCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length == 1) {
            String username = event.getArgs()[0];
            if (!username.matches("^([A-Za-z]|[0-9]|_)+$")) {
                event.respondWithPing(Colors.RED + "Minecraft usernames can only contain letters, numbers and underscores!");
                return true;
            }

            String uuid = "N/A";
            boolean isValid;
            boolean hasPaid;
            try {
                UserProfile[] profiles = getProfiles(username);
                if (profiles.length > 0) {
                    uuid = profiles[0].id;
                }
                isValid = getPageContents("https://account.minecraft.net/buy/frame/checkName/" + username).equalsIgnoreCase("TAKEN");
                hasPaid = getPageContents("https://minecraft.net/haspaid.jsp?user=" + username).equalsIgnoreCase("TRUE");
            } catch (UnirestException e) {
                throw new GenericUrlConnectionException("Failed to fetch data on Minecraft username (" + username + ").", e);
            }

            event.respond("Minecraft Username " + Colors.BOLD + username + Colors.NORMAL + " (" + Colors.BOLD + uuid + Colors.NORMAL + "):");
            event.respond(Colors.BOLD + "Valid Username? " + (isValid ? Colors.BOLD + Colors.UNDERLINE + Colors.GREEN + "Yes" : Colors.BOLD + Colors.UNDERLINE + Colors.RED + "No") + Colors.NORMAL + " | "
                    + Colors.BOLD + "Paid Account? " + (hasPaid ? Colors.BOLD + Colors.UNDERLINE + Colors.GREEN + "Yes" : Colors.BOLD + Colors.UNDERLINE + Colors.RED + "No"));
            return true;
        }
        return false;
    }

    private String getPageContents(String urlString) throws UnirestException {
        return Unirest.get(urlString).asString().getBody();
    }

    private UserProfile[] getProfiles(String username) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.post("https://api.mojang.com/profiles/page/1").header("content-type", "application/json").body("[{\"name\":\"" + username + "\", \"agent\":\"Minecraft\"}]").asJson();
        return JsonUtil.read(response.getRawBody(), "profiles", UserProfile[].class);
    }

    private class UserProfile {
        private String id;
        private String name;
    }
}
