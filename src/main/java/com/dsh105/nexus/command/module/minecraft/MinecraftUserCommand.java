package com.dsh105.nexus.command.module.minecraft;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.util.JsonUtil;
import org.pircbotx.Colors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


@Command(command = "mcuser",
        aliases = {"minecraftuser"},
        needsChannel = false,
        help = "View information about a specific Minecraft username.",
        extendedHelp = {"{b}{p}{c}{/b} <usernane>- View information about the Minecraft username."})
public class MinecraftUserCommand extends CommandModule {

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length == 1) {
            String username = event.getArgs()[0];
            if (!username.matches("^([A-Za-z]|[0-9]|_)+$")) {
                event.respondWithPing(Colors.RED + "Minecraft usernames can only contain letters, numbers and underscores!");
                return true;
            }

            String uuid;
            boolean isValid;
            boolean hasPaid;
            try {
                JsonUtil jsonUtil = new JsonUtil();
                UserProfile[] profile = jsonUtil.read(getUUIDConnection(username), "profiles", UserProfile[].class);

                uuid = profile[0].id;
                isValid = getPageContents("https://account.minecraft.net/buy/frame/checkName/" + username).equalsIgnoreCase("TAKEN");
                hasPaid = getPageContents("https://minecraft.net/haspaid.jsp?user=" + username).equalsIgnoreCase("true");
            } catch (Exception e) {
                e.printStackTrace();
                event.respondWithPing(Colors.RED + "Error occured while fetching data! Try again later...");
                return true;
            }

            event.respond("Minecraft Username " + Colors.BOLD + username + Colors.NORMAL + " (" + Colors.BOLD + uuid + Colors.NORMAL + "):");
            event.respond(
                    Colors.BOLD + "Valid Username? " + (isValid ?
                            Colors.BOLD + Colors.UNDERLINE + Colors.GREEN + "Yes" :
                            Colors.BOLD + Colors.UNDERLINE + Colors.RED + "No") + Colors.NORMAL + " | "
                            + Colors.BOLD + "Paid Account? " + (hasPaid ?
                            Colors.BOLD + Colors.UNDERLINE + Colors.GREEN + "Yes" :
                            Colors.BOLD + Colors.UNDERLINE + Colors.RED + "No")
            );
            return true;
        }
        return false;
    }

    private String getPageContents(String urlString) throws IOException {
        URL url = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setUseCaches(false);

        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder buffer = new StringBuilder();
        String line;

        while ((line = input.readLine()) != null) {
            buffer.append(line);
        }

        String page = buffer.toString();

        input.close();

        return page;
    }

    private BufferedReader getUUIDConnection(String username) throws IOException {
        URL url = new URL("https://api.mojang.com/profiles/page/1");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        String JSON = String.format("[{\"name\":\"%s\", \"agent\":\"Minecraft\"}]", username);

        OutputStream stream = connection.getOutputStream();
        stream.write(JSON.getBytes());
        stream.flush();
        stream.close();

        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return input;
    }

    private class UserProfile {
        private String id;
        private String name;
    }
}
