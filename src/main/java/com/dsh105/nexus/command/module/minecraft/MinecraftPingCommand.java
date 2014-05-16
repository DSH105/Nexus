package com.dsh105.nexus.command.module.minecraft;

import com.dsh105.nexus.command.Command;
import com.dsh105.nexus.command.CommandModule;
import com.dsh105.nexus.command.CommandPerformEvent;
import com.dsh105.nexus.util.StringUtil;
import com.dsh105.nexus.util.minecraft.MinecraftPing;
import com.dsh105.nexus.util.minecraft.MinecraftPingOptions;
import com.dsh105.nexus.util.minecraft.MinecraftPingReply;
import org.pircbotx.Colors;

import java.util.regex.Pattern;

@Command(command = "mcping",
        aliases = {"minecraftping", "pingserver"},
        needsChannel = false,
        help = "Ping the specified Minecraft server.",
        extendedHelp = {"{b}{p}{c}{/b} <Hostname> - Ping the Hostname.",
                "{b}{p}{c}{/b} <Hostname> <Port> - Ping the Hostname on the port.",
                "{b}{p}{c}{/b} <Hostname> <Port> <Timeout> - Ping the Hostname on the port with the chosen timeout."})
public class MinecraftPingCommand extends CommandModule {

    private static final char COLOR_CHAR = '\u00A7';
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-OR]");

    @Override
    public boolean onCommand(CommandPerformEvent event) {
        if (event.getArgs().length > 0) {
            String hostname = null;
            int port = 25565;
            int timeout = 2000;

            if (event.getArgs().length >= 1) {
                hostname = event.getArgs()[0];
            }

            if (event.getArgs().length >= 2) {
                String inputPort = event.getArgs()[1];
                if (StringUtil.isInt(inputPort)) {
                    port = Integer.parseInt(inputPort);
                }
            }

            if (event.getArgs().length >= 3) {
                String inputTimeout = event.getArgs()[2];
                if (StringUtil.isInt(inputTimeout)) {
                    timeout = Integer.parseInt(inputTimeout);
                    if (timeout <= 0 || timeout > 30000) {
                        timeout = 2000;
                    }
                }
            }

            try {
                MinecraftPingOptions options = new MinecraftPingOptions();

                options.setHostname(hostname);
                options.setPort(port);
                options.setTimeout(timeout);

                event.respond("Pinging {0}:{1} with a timeout of {2}ms...", hostname, String.valueOf(port), String.valueOf(timeout));

                MinecraftPingReply pingReply = new MinecraftPing().getPing(options);

                event.respond("{0}:{1} responded to the ping!", hostname, String.valueOf(port));
                event.respond(Colors.BOLD + Colors.BLUE + "MOTD: " + Colors.NORMAL + "{0} " + Colors.BOLD + Colors.BLUE + "Players: " + Colors.NORMAL + "{1}/{2}", stripColor(pingReply.getDescription().trim().replaceAll("( )+", " ")), String.valueOf(pingReply.getPlayers().getOnline()), String.valueOf(pingReply.getPlayers().getMax()));
            } catch (Exception ex) {
                event.errorWithPing(Colors.RED + "Error contacting {0}" + Colors.RED + ":{1}" + Colors.RED + "! Is it online?", hostname, String.valueOf(port));
            }
            return true;
        }
        return false;
    }

    public String stripColor(final String input) {
        if (input == null) {
            return null;
        }

        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }
}
